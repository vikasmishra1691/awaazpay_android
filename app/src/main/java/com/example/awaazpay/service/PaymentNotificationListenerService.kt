package com.example.awaazpay.service

import android.content.Context
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.awaazpay.data.Payment
import com.example.awaazpay.data.PaymentDatabase
import com.example.awaazpay.parser.PaymentParser
import com.example.awaazpay.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * NotificationListenerService that filters UPI payment notifications.
 *
 * RESPONSIBILITIES (ONLY):
 * - Filter UPI app notifications
 * - Parse notification text
 * - Send payment data to PaymentAnnouncementService
 * - Save payment to DB asynchronously (non-blocking)
 *
 * DOES NOT:
 * - Initialize TTS
 * - Make announcements
 * - Block on DB writes
 * - Perform heavy operations on critical path
 */
class PaymentNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val prefs by lazy { getSharedPreferences("awaazpay_prefs", Context.MODE_PRIVATE) }

    // UPI apps to monitor
    private val upiApps = setOf(
        // Major UPI Apps
        "com.google.android.apps.nbu.paisa.user",  // Google Pay
        "com.phonepe.app",                          // PhonePe
        "net.one97.paytm",                          // Paytm
        "in.org.npci.upiapp",                       // BHIM UPI

        // E-commerce & Wallets
        "com.amazon.mShop.android.shopping",        // Amazon Pay
        "com.freecharge.android",                   // Freecharge
        "com.mobikwik_new",                         // MobiKwik

        // Social & Messaging
        "com.whatsapp",                             // WhatsApp Pay

        // Business & Merchant Apps
        "com.bharatpe.merchant.user",               // BharatPe Merchant
        "in.co.bharatpe",                           // BharatPe

        // Credit & Lending
        "com.dreamplug.androidapp",                 // CRED

        // Telecom Payment Apps
        "com.myairtelapp",                          // Airtel Thanks (Airtel Payments Bank)
        "com.airtel.money",                         // Airtel Money

        // Bank UPI Apps
        "com.csam.icici.bank.imobile",              // iMobile Pay (ICICI)
        "com.axis.mobile",                          // Axis Mobile
        "com.sbi.lotusintouch",                     // YONO SBI
        "com.sbi.SBIFreedomPlus",                   // YONO SBI Lite
        "com.snapwork.hdfc",                        // HDFC Bank MobileBanking
        "com.fedbank.fednxt",                       // FedMobile (Federal Bank)
        "com.fss.pnb.mbanking"                      // PNB One
    )

    override fun onListenerConnected() {
        super.onListenerConnected()

        // Mark listener as active
        prefs.edit().putBoolean("listener_active", true).apply()

        Logger.i("NotificationListener connected")

        // Ensure announcement service is running
        PaymentAnnouncementService.startService(this)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()

        // Mark listener as inactive
        prefs.edit().putBoolean("listener_active", false).apply()

        Logger.i("NotificationListener disconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return

        val packageName = sbn.packageName

        Logger.d("Notification received from: $packageName")

        // Quick filter: Only process UPI apps
        if (packageName !in upiApps) {
            Logger.d("Not a UPI app, ignoring")
            return
        }

        Logger.d("UPI app detected: $packageName")

        // Extract notification text
        val notification = sbn.notification
        val extras = notification?.extras
        val title = extras?.getCharSequence("android.title")?.toString()
        val text = extras?.getCharSequence("android.text")?.toString()
        val notificationText = "$title $text"

        Logger.d("Notification text: $notificationText")

        // Parse payment (fast, synchronous)
        val parsed = PaymentParser.parsePayment(notificationText, packageName)

        if (parsed == null) {
            Logger.d("Not a payment notification")
            return
        }

        Logger.i("Payment detected: ₹${parsed.amount} from ${parsed.senderName ?: "Unknown"}")

        // Check if announcements are enabled
        val announcementsEnabled = prefs.getBoolean("announcements_enabled", true)

        if (announcementsEnabled) {
            // CRITICAL PATH: Announce immediately (non-blocking)
            announcePaymentImmediately(parsed.amount, parsed.senderName)
        } else {
            Logger.d("Announcements disabled, skipping TTS")
        }

        // ASYNC PATH: Save to database (always save, regardless of announcement setting)
        savePaymentAsync(parsed, notificationText)
    }

    /**
     * CRITICAL: Send to announcement service immediately.
     * No blocking, no coroutine hops, no DB waits.
     */
    private fun announcePaymentImmediately(amount: String, senderName: String?) {
        val language = prefs.getString("language", "en") ?: "en"

        val intent = Intent(this, PaymentAnnouncementService::class.java).apply {
            action = PaymentAnnouncementService.ACTION_ANNOUNCE
            putExtra(PaymentAnnouncementService.EXTRA_AMOUNT, amount)
            putExtra(PaymentAnnouncementService.EXTRA_SENDER_NAME, senderName)
            putExtra(PaymentAnnouncementService.EXTRA_LANGUAGE, language)
        }

        // Start service with announcement data (service is already running, so this is fast)
        startService(intent)
    }

    /**
     * Save payment to database asynchronously.
     * Runs on IO dispatcher, does not block announcement.
     */
    private fun savePaymentAsync(parsed: com.example.awaazpay.parser.ParsedPayment, rawText: String) {
        Logger.d("Starting async DB save for ₹${parsed.amount}")

        serviceScope.launch(Dispatchers.IO) {
            try {
                val payment = Payment(
                    amount = parsed.amount,
                    senderName = parsed.senderName,
                    appName = parsed.appName,
                    timestamp = System.currentTimeMillis(),
                    rawNotificationText = rawText
                )

                val db = PaymentDatabase.getDatabase(applicationContext)
                val insertedId = db.paymentDao().insert(payment)

                Logger.i("Payment saved to DB with ID: $insertedId")

            } catch (e: Exception) {
                Logger.e("DB insert failed: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}

