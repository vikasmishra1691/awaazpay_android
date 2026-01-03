package com.example.awaazpay.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages onboarding completion state using SharedPreferences.
 * Ensures onboarding is only shown once after first installation.
 *
 * Note: Using SharedPreferences for now. Can be migrated to DataStore later.
 */
class OnboardingManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "onboarding_prefs"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _isOnboardingCompleted = MutableStateFlow(prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false))

    /**
     * Flow that emits true if onboarding has been completed, false otherwise.
     */
    val isOnboardingCompleted: Flow<Boolean> = _isOnboardingCompleted.asStateFlow()

    /**
     * Mark onboarding as completed.
     * This should be called when user completes the onboarding flow.
     */
    suspend fun completeOnboarding() {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, true).apply()
        _isOnboardingCompleted.value = true
    }

    /**
     * Reset onboarding state (for testing purposes).
     */
    suspend fun resetOnboarding() {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, false).apply()
        _isOnboardingCompleted.value = false
    }
}

