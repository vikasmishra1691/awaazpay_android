package com.example.awaazpay.util

import android.content.Context
import com.example.awaazpay.data.OnboardingManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Developer utility for testing onboarding flow.
 * USE ONLY IN DEBUG/DEVELOPMENT MODE.
 */
object OnboardingDebugHelper {

    /**
     * Reset onboarding to test the flow again.
     * This will show onboarding on next app launch.
     *
     * Usage in debug mode:
     * ```
     * OnboardingDebugHelper.resetOnboarding(context)
     * ```
     */
    fun resetOnboarding(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                OnboardingManager(context).resetOnboarding()
                Logger.d("Onboarding reset successfully - will show on next launch")
            } catch (e: Exception) {
                Logger.e("Failed to reset onboarding: ${e.message}")
            }
        }
    }

    /**
     * Check current onboarding status.
     */
    suspend fun isOnboardingCompleted(context: Context): Boolean {
        val manager = OnboardingManager(context)
        var isCompleted = false
        manager.isOnboardingCompleted.collect { completed ->
            isCompleted = completed
        }
        return isCompleted
    }
}

