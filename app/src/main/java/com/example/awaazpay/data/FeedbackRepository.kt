package com.example.awaazpay.data

import android.content.Context
import android.os.Build
import com.example.awaazpay.BuildConfig
import com.example.awaazpay.util.Logger
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Repository for submitting user feedback to Firebase Firestore.
 *
 * Architecture:
 * - Uses Firebase Firestore for cloud storage
 * - All operations are non-blocking (Kotlin coroutines)
 * - Safe error handling (try/catch)
 * - Runs on IO dispatcher
 * - Does NOT log user message content (privacy)
 */
class FeedbackRepository(private val context: Context) {

    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    /**
     * Submit feedback to Firestore.
     *
     * @param message User feedback message (trimmed, max 500 chars)
     * @return Result<Unit> Success or failure
     */
    suspend fun submitFeedback(message: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val trimmedMessage = message.trim()

            if (trimmedMessage.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("Message cannot be empty"))
            }

            if (trimmedMessage.length > 500) {
                return@withContext Result.failure(IllegalArgumentException("Message too long (max 500 chars)"))
            }

            val feedbackData = createFeedbackData(trimmedMessage)

            Logger.i("Submitting feedback to Firestore")

            // Submit to Firestore
            firestore.collection("feedback")
                .add(feedbackData)
                .await()

            Logger.i("Feedback submitted successfully")
            Result.success(Unit)

        } catch (e: Exception) {
            Logger.e("Failed to submit feedback: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Create feedback document data with metadata.
     */
    private fun createFeedbackData(message: String): Map<String, Any> {
        return hashMapOf(
            "message" to message,
            "timestamp" to System.currentTimeMillis(),
            "appVersion" to BuildConfig.VERSION_NAME,
            "versionCode" to BuildConfig.VERSION_CODE,
            "deviceModel" to "${Build.MANUFACTURER} ${Build.MODEL}",
            "androidVersion" to Build.VERSION.SDK_INT,
            "language" to getAppLanguage()
        )
    }

    /**
     * Get current app language from shared preferences.
     */
    private fun getAppLanguage(): String {
        val prefs = context.getSharedPreferences("awaazpay_prefs", Context.MODE_PRIVATE)
        return prefs.getString("language", "en") ?: "en"
    }
}

