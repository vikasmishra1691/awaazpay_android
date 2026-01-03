package com.example.awaazpay.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.app.NotificationCompat
import com.example.awaazpay.MainActivity
import com.example.awaazpay.R
import com.example.awaazpay.util.Logger
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Long-lived foreground service for payment announcements.
 * Keeps TTS initialized and ready for instant announcements (<1s latency).
 *
 * Architecture:
 * - Starts once when notification access is granted
 * - Stays alive as foreground service indefinitely
 * - Reuses same TTS instance for all announcements
 * - Handles audio focus properly
 * - Never auto-stops (runs until system kills it)
 */
class PaymentAnnouncementService : Service(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var ttsInitialized = false
    private var ttsWarmedUp = false
    private val pendingAnnouncements = ConcurrentLinkedQueue<AnnouncementData>()

    private val audioManager by lazy { getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    private var audioFocusRequest: AudioFocusRequest? = null
    private var hasAudioFocus = false


    private val mainHandler = Handler(Looper.getMainLooper())

    private val binder = LocalBinder()

    private data class AnnouncementData(
        val amount: String,
        val senderName: String?,
        val language: String
    )

    inner class LocalBinder : Binder() {
        fun getService(): PaymentAnnouncementService = this@PaymentAnnouncementService
    }

    companion object {
        private const val CHANNEL_ID = "payment_announcement_channel"
        private const val NOTIFICATION_ID = 1001

        const val ACTION_ANNOUNCE = "com.example.awaazpay.ANNOUNCE"
        const val EXTRA_AMOUNT = "extra_amount"
        const val EXTRA_SENDER_NAME = "extra_sender_name"
        const val EXTRA_LANGUAGE = "extra_language"

        @Volatile
        private var isServiceRunning = false

        fun isRunning(): Boolean = isServiceRunning

        fun startService(context: Context) {
            if (!isServiceRunning) {
                val intent = Intent(context, PaymentAnnouncementService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        isServiceRunning = true

        // Start as foreground service immediately
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createForegroundNotification())

        // Initialize TTS once (persistent)
        tts = TextToSpeech(applicationContext, this)

        Logger.i("PaymentAnnouncementService created - TTS initializing")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Ensure we're foreground
        startForeground(NOTIFICATION_ID, createForegroundNotification())


        // Process announcement if provided
        intent?.let { processIntent(it) }

        // START_STICKY: System will recreate service if killed
        return START_STICKY
    }

    private fun processIntent(intent: Intent) {
        Logger.d("Processing intent: action=${intent.action}")

        if (intent.action == ACTION_ANNOUNCE || intent.hasExtra(EXTRA_AMOUNT)) {
            val amount = intent.getStringExtra(EXTRA_AMOUNT)
            val senderName = intent.getStringExtra(EXTRA_SENDER_NAME)
            val language = intent.getStringExtra(EXTRA_LANGUAGE) ?: "en"

            Logger.d("Intent data: amount=$amount, sender=$senderName, language=$language")

            if (amount != null) {
                val announcement = AnnouncementData(amount, senderName, language)

                if (ttsInitialized) {
                    // Fast path: TTS ready, announce immediately
                    Logger.d("TTS ready, announcing now")
                    announcePayment(announcement)
                } else {
                    // TTS still initializing, queue announcement
                    Logger.d("TTS not ready, queuing announcement")
                    pendingAnnouncements.offer(announcement)
                }
            } else {
                Logger.e("Amount is null in intent!")
            }
        } else {
            Logger.d("Intent has no announcement action")
        }
    }

    override fun onInit(status: Int) {
        Logger.d("TTS onInit called with status: $status")

        if (status == TextToSpeech.SUCCESS) {
            ttsInitialized = true

            // Configure TTS for optimal performance
            tts?.apply {
                setSpeechRate(0.8f)
                setPitch(1.1f)

                setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        Logger.d("TTS started speaking: $utteranceId")
                    }

                    override fun onDone(utteranceId: String?) {
                        Logger.d("TTS finished speaking: $utteranceId")
                        releaseAudioFocus()
                    }

                    override fun onError(utteranceId: String?) {
                        Logger.e("TTS error for utterance: $utteranceId")
                        releaseAudioFocus()
                    }
                })
            }

            Logger.i("TTS initialized successfully - ready for announcements")

            // Perform one-time silent warm-up to pre-load TTS engine and audio pipeline
            performTtsWarmup()

            // Process all pending announcements
            processPendingAnnouncements()
        } else {
            Logger.e("TTS initialization failed with status: $status")
        }
    }

    private fun processPendingAnnouncements() {
        while (pendingAnnouncements.isNotEmpty()) {
            pendingAnnouncements.poll()?.let { announcePayment(it) }
        }
    }

    /**
     * One-time silent warm-up to pre-load TTS engine and audio pipeline.
     * This reduces latency for the first real payment announcement.
     * The warm-up produces no audible sound.
     */
    private fun performTtsWarmup() {
        if (ttsWarmedUp) {
            Logger.d("TTS already warmed up, skipping")
            return
        }

        tts?.let { ttsEngine ->
            Logger.i("Performing TTS warm-up (silent)")

            // Set minimal volume for warm-up (silent)
            val originalStreamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)

            mainHandler.post {
                // Speak silent character to pre-load TTS engine
                val params = android.os.Bundle().apply {
                    putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
                }

                // Use space character for silent warm-up
                val result = ttsEngine.speak(" ", TextToSpeech.QUEUE_FLUSH, params, "tts_warmup")

                Logger.d("TTS warm-up initiated, result: $result")

                // Restore original volume after a short delay
                mainHandler.postDelayed({
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalStreamVolume, 0)
                    Logger.d("TTS warm-up complete, volume restored")
                }, 500) // 500ms should be enough for warm-up

                ttsWarmedUp = true
            }
        } ?: Logger.e("Cannot warm-up TTS: engine is null")
    }

    /**
     * CRITICAL: Optimized for <1s latency
     * - Runs on Main thread (TTS requirement)
     * - Requests audio focus
     * - No blocking operations
     */
    private fun announcePayment(data: AnnouncementData) {
        Logger.d("announcePayment called: amount=${data.amount}")

        if (!ttsInitialized || tts == null) {
            Logger.e("TTS not initialized! Queuing announcement")
            pendingAnnouncements.offer(data)
            return
        }

        // Request audio focus before speaking
        requestAudioFocus()


        mainHandler.post {
            Logger.d("Main handler executing TTS speak")

            tts?.let { ttsEngine ->
                // Set language (cached, fast)
                val locale = if (data.language == "hi") {
                    Locale.Builder().setLanguage("hi").setRegion("IN").build()
                } else {
                    Locale.ENGLISH
                }
                ttsEngine.setLanguage(locale)

                // Format message
                val message = formatMessage(data.amount, data.senderName, data.language)

                Logger.i("Speaking: $message")

                // Speak with audio stream priority
                val params = android.os.Bundle().apply {
                    putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
                }

                // QUEUE_FLUSH ensures immediate playback
                val result = ttsEngine.speak(message, TextToSpeech.QUEUE_FLUSH, params, "payment_${System.currentTimeMillis()}")

                Logger.d("TTS speak result: $result")
            } ?: Logger.e("TTS engine is null!")
        }
    }

    private fun formatMessage(amount: String, senderName: String?, language: String): String {
        val firstName = senderName?.trim()?.split(" ")?.firstOrNull()

        return if (language == "hi") {
            if (firstName != null) {
                "₹$amount $firstName से प्राप्त हुए"
            } else {
                "₹$amount प्राप्त हुए"
            }
        } else {
            if (firstName != null) {
                "Payment received of ₹$amount from $firstName"
            } else {
                "Payment received of ₹$amount"
            }
        }
    }

    private fun requestAudioFocus() {
        if (hasAudioFocus) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(audioAttributes)
                .setWillPauseWhenDucked(false)
                .build()

            val result = audioManager.requestAudioFocus(audioFocusRequest!!)
            hasAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            val result = audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
            hasAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun releaseAudioFocus() {
        if (!hasAudioFocus) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
        hasAudioFocus = false
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Payment Listener",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps SoundPay listening for UPI payments"
                setSound(null, null)
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createForegroundNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AwaazPay Active")
            .setContentText("Monitoring incoming UPI payments")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        isServiceRunning = false

        // Cleanup
        releaseAudioFocus()

        // Shutdown TTS
        tts?.stop()
        tts?.shutdown()
        tts = null
        ttsInitialized = false

        Logger.i("PaymentAnnouncementService destroyed")
        super.onDestroy()
    }
}

