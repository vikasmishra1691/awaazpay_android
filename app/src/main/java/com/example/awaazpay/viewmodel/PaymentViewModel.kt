package com.example.awaazpay.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.awaazpay.BuildConfig
import com.example.awaazpay.data.Payment
import com.example.awaazpay.data.PaymentDatabase
import com.example.awaazpay.data.PaymentRepository
import com.example.awaazpay.util.ISTTimeHelper
import com.example.awaazpay.util.Logger
import com.example.awaazpay.util.PermissionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class PaymentUiState(
    val isListenerActive: Boolean = false,
    val announcementsEnabled: Boolean = true,
    val selectedLanguage: String = "en",
    val autoStartOnBoot: Boolean = false,
    val todayEarnings: Double = 0.0,
    val weekEarnings: Double = 0.0,
    val monthEarnings: Double = 0.0,
    val recentPayments: List<Payment> = emptyList(),
    val last50Payments: List<Payment> = emptyList(),
    val totalPayments: Int = 0,
    val debugInfo: DebugInfo? = null
)

data class DebugInfo(
    val lastNotificationPackage: String = "None",
    val lastNotificationTime: String = "Never",
    val totalNotifications: Int = 0
)

class PaymentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PaymentRepository
    private val prefs = application.getSharedPreferences("awaazpay_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    init {
        val database = PaymentDatabase.getDatabase(application)
        repository = PaymentRepository(database.paymentDao())

        Logger.d("ViewModel: initialized")
        loadSettings()
        observeEarnings()
        observePayments()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            // Check actual system permission state (more reliable than SharedPreferences on fresh install)
            val isActive = PermissionHelper.isNotificationListenerEnabled(getApplication())
            val announcementsEnabled = prefs.getBoolean("announcements_enabled", true)
            val language = prefs.getString("language", "en") ?: "en"
            val autoStart = prefs.getBoolean("auto_start_on_boot", false)

            _uiState.value = _uiState.value.copy(
                isListenerActive = isActive,
                announcementsEnabled = announcementsEnabled,
                selectedLanguage = language,
                autoStartOnBoot = autoStart
            )

            Logger.d("Settings loaded: active=$isActive, announcements=$announcementsEnabled")

            if (BuildConfig.DEBUG_MODE) {
                loadDebugInfo()
            }
        }
    }

    private fun observeEarnings() {
        viewModelScope.launch {
            val todayStart = ISTTimeHelper.getStartOfToday()
            val weekStart = ISTTimeHelper.getStartOfWeek()
            val monthStart = ISTTimeHelper.getStartOfMonth()

            combine(
                repository.getTotalEarningsFrom(todayStart),
                repository.getTotalEarningsFrom(weekStart),
                repository.getTotalEarningsFrom(monthStart)
            ) { today, week, month ->
                Triple(today ?: 0.0, week ?: 0.0, month ?: 0.0)
            }.collect { (today, week, month) ->
                _uiState.value = _uiState.value.copy(
                    todayEarnings = today,
                    weekEarnings = week,
                    monthEarnings = month
                )
                Logger.d("Earnings updated: today=$today, week=$week, month=$month")
            }
        }
    }

    private fun observePayments() {
        viewModelScope.launch {
            combine(
                repository.recentPayments,
                repository.last50Payments,
                repository.getTotalCount()
            ) { recent, last50, total ->
                Triple(recent, last50, total)
            }.collect { (recent, last50, total) ->
                _uiState.value = _uiState.value.copy(
                    recentPayments = recent,
                    last50Payments = last50,
                    totalPayments = total
                )
                Logger.d("UI recomposition trigger: ${recent.size} recent payments, $total total")
            }
        }
    }

    fun toggleAnnouncements(enabled: Boolean) {
        viewModelScope.launch {
            prefs.edit()
                .putBoolean("announcements_enabled", enabled)
                .apply()
            _uiState.value = _uiState.value.copy(announcementsEnabled = enabled)
            Logger.d("Announcements toggled: $enabled")
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            prefs.edit()
                .putString("language", language)
                .apply()
            _uiState.value = _uiState.value.copy(selectedLanguage = language)
            Logger.d("Language set: $language")
        }
    }

    fun toggleAutoStart(enabled: Boolean) {
        viewModelScope.launch {
            prefs.edit()
                .putBoolean("auto_start_on_boot", enabled)
                .apply()
            _uiState.value = _uiState.value.copy(autoStartOnBoot = enabled)
            Logger.d("Auto-start toggled: $enabled")
        }
    }


    fun refreshListenerState() {
        viewModelScope.launch {
            // Check actual system permission state (more reliable than SharedPreferences)
            val isActive = PermissionHelper.isNotificationListenerEnabled(getApplication())
            _uiState.value = _uiState.value.copy(isListenerActive = isActive)
            Logger.d("Listener state refreshed: $isActive")
        }
    }

    private fun loadDebugInfo() {
        viewModelScope.launch {
            val packageName = prefs.getString("last_notification_package", "None") ?: "None"
            val timestamp = prefs.getLong("last_notification_time", 0)
            val count = prefs.getInt("total_notifications", 0)

            val timeStr = if (timestamp > 0) {
                ISTTimeHelper.formatTimestamp(timestamp)
            } else {
                "Never"
            }

            _uiState.value = _uiState.value.copy(
                debugInfo = DebugInfo(packageName, timeStr, count)
            )
        }
    }

    fun refreshDebugInfo() {
        if (BuildConfig.DEBUG_MODE) {
            loadDebugInfo()
        }
    }
}

