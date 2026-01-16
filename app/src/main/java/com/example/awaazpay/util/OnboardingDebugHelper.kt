package com.example.awaazpay.util

import android.content.Context
import com.example.awaazpay.BuildConfig
import com.example.awaazpay.data.OnboardingManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Developer utility for testing onboarding flow.
 * ⚠️ WARNING: USE ONLY IN DEBUG/DEVELOPMENT MODE - NEVER IN PRODUCTION ⚠️
 *
 * This helper is automatically disabled in release builds.
 */
object OnboardingDebugHelper {

    /**
     * Reset onboarding to test the flow again.
     * This will show onboarding on next app launch.
     *
     * ⚠️ DEBUG ONLY - Automatically disabled in release builds
     *
     * Usage in debug mode:
     * ```
     * OnboardingDebugHelper.resetOnboarding(context)
     * ```
     */
    fun resetOnboarding(context: Context) {
        // Safety check: Only allow in debug builds
        if (!BuildConfig.DEBUG_MODE) {
            Logger.e("OnboardingDebugHelper cannot be used in production builds!")
            return
        }

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
     *
     * ⚠️ DEBUG ONLY - Automatically disabled in release builds
     */
    suspend fun isOnboardingCompleted(context: Context): Boolean {
        // Safety check: Only allow in debug builds
        if (!BuildConfig.DEBUG_MODE) {
            Logger.e("OnboardingDebugHelper cannot be used in production builds!")
            return true
        }

        val manager = OnboardingManager(context)
        var isCompleted = false
        manager.isOnboardingCompleted.collect { completed ->
            isCompleted = completed
        }
        return isCompleted
    }
}

