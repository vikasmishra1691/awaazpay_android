package com.example.awaazpay.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.awaazpay.data.FeedbackRepository
import com.example.awaazpay.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for user feedback feature.
 *
 * States:
 * - Idle: Initial state
 * - Loading: Submitting feedback
 * - Success: Feedback submitted successfully
 * - Error: Failed to submit feedback
 */
class FeedbackViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FeedbackRepository(application)

    private val _uiState = MutableStateFlow<FeedbackUiState>(FeedbackUiState.Idle)
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    /**
     * Submit user feedback.
     *
     * @param message User feedback message
     */
    fun submitFeedback(message: String) {
        val trimmedMessage = message.trim()

        // Validation
        if (trimmedMessage.isEmpty()) {
            _uiState.value = FeedbackUiState.Error("Please enter your feedback")
            return
        }

        if (trimmedMessage.length > 500) {
            _uiState.value = FeedbackUiState.Error("Feedback is too long (max 500 characters)")
            return
        }

        // Submit
        _uiState.value = FeedbackUiState.Loading

        viewModelScope.launch {
            repository.submitFeedback(trimmedMessage)
                .onSuccess {
                    Logger.i("Feedback submission successful")
                    _uiState.value = FeedbackUiState.Success
                }
                .onFailure { error ->
                    Logger.e("Feedback submission failed: ${error.message}")
                    val errorMessage = when (error) {
                        is IllegalArgumentException -> error.message ?: "Invalid feedback"
                        else -> "Failed to submit feedback. Please check your internet connection."
                    }
                    _uiState.value = FeedbackUiState.Error(errorMessage)
                }
        }
    }

    /**
     * Reset state to Idle (call after showing success/error message).
     */
    fun resetState() {
        _uiState.value = FeedbackUiState.Idle
    }
}

/**
 * UI state for feedback feature.
 */
sealed class FeedbackUiState {
    object Idle : FeedbackUiState()
    object Loading : FeedbackUiState()
    object Success : FeedbackUiState()
    data class Error(val message: String) : FeedbackUiState()
}

