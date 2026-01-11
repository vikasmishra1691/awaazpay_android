package com.example.awaazpay

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.awaazpay.data.OnboardingManager
import com.example.awaazpay.ui.components.*
import com.example.awaazpay.ui.onboarding.OnboardingScreen
import com.example.awaazpay.ui.screens.SettingsScreen
import com.example.awaazpay.ui.theme.AwaazPayTheme
import com.example.awaazpay.util.Logger
import com.example.awaazpay.viewmodel.PaymentViewModel
import com.example.awaazpay.viewmodel.FeedbackViewModel
import com.example.awaazpay.viewmodel.FeedbackUiState
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Logger.d("Notification permission result: $isGranted")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Logger.d("MainActivity: onCreate")

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Start long-lived announcement service for instant announcements
        com.example.awaazpay.service.PaymentAnnouncementService.startService(this)

        setContent {
            AwaazPayTheme {
                AwaazPayApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Logger.d("MainActivity: onResume")
    }
}

@Composable
fun AwaazPayApp(viewModel: PaymentViewModel = viewModel()) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val onboardingManager = remember { OnboardingManager(context) }
    val isOnboardingCompleted by onboardingManager.isOnboardingCompleted.collectAsState(initial = null)
    val coroutineScope = rememberCoroutineScope()

    // Show loading while checking onboarding status
    if (isOnboardingCompleted == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Show onboarding if not completed
    if (isOnboardingCompleted == false) {
        OnboardingScreen(
            onComplete = {
                Logger.d("Onboarding completed, marking as done")
                coroutineScope.launch {
                    onboardingManager.completeOnboarding()
                }
            }
        )
        return
    }

    // Show main app after onboarding is completed
    HomeScreen(viewModel = viewModel)
}

@Composable
private fun HomeScreen(viewModel: PaymentViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // Feedback ViewModel
    val feedbackViewModel: FeedbackViewModel = viewModel()
    val feedbackState by feedbackViewModel.uiState.collectAsState()

    // Navigation state
    var showSettings by remember { mutableStateOf(false) }

    // Refresh listener state when app resumes
    LaunchedEffect(Unit) {
        viewModel.refreshListenerState()
        viewModel.refreshDebugInfo()
    }

    // Refresh on every lifecycle resume
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.refreshListenerState()
                viewModel.refreshDebugInfo()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Show success/error snackbar for feedback
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedbackState) {
        when (val state = feedbackState) {
            is FeedbackUiState.Success -> {
                snackbarHostState.showSnackbar(
                    message = "Thank you for your feedback 🙏",
                    duration = SnackbarDuration.Short
                )
                feedbackViewModel.resetState()
            }
            is FeedbackUiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Long
                )
                feedbackViewModel.resetState()
            }
            else -> {}
        }
    }

    if (showSettings) {
        // Show Settings Screen
        SettingsScreen(
            announcementsEnabled = uiState.announcementsEnabled,
            selectedLanguage = uiState.selectedLanguage,
            autoStartOnBoot = uiState.autoStartOnBoot,
            onToggleAnnouncements = viewModel::toggleAnnouncements,
            onLanguageSelected = viewModel::setLanguage,
            onToggleAutoStart = viewModel::toggleAutoStart,
            onBackClick = { showSettings = false }
        )
    } else {
        // Show Home Screen
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            HomeScreen(
                uiState = uiState,
                onToggleAnnouncements = viewModel::toggleAnnouncements,
                onLanguageSelected = viewModel::setLanguage,
                onToggleAutoStart = viewModel::toggleAutoStart,
                feedbackState = feedbackState,
                onSubmitFeedback = feedbackViewModel::submitFeedback,
                onSettingsClick = { showSettings = true },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun HomeScreen(
    uiState: com.example.awaazpay.viewmodel.PaymentUiState,
    onToggleAnnouncements: (Boolean) -> Unit,
    onLanguageSelected: (String) -> Unit,
    onToggleAutoStart: (Boolean) -> Unit,
    feedbackState: FeedbackUiState,
    onSubmitFeedback: (String) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showTransactionHistory by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Premium App Header with Settings Icon
        PremiumHeaderWithSettings(onSettingsClick = onSettingsClick)

        // Service Status Card
        ServiceStatusCard(isActive = uiState.isListenerActive)


        Spacer(modifier = Modifier.height(8.dp))

        // Earnings Section
        SectionTitle(text = "Earnings")

        // Today's Earnings (Large)
        EarningsCard(
            title = "Today's Earning",
            amount = uiState.todayEarnings,
            isLarge = true
        )

        // This Week's Earnings
        EarningsCard(
            title = "This Week's Earning",
            amount = uiState.weekEarnings
        )

        // This Month's Earnings
        EarningsCard(
            title = "This Month's Earning",
            amount = uiState.monthEarnings
        )

        // Transaction History Card
        TransactionHistoryCard(
            totalTransactions = uiState.totalPayments,
            onClick = { showTransactionHistory = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Give Feedback Button
        FeedbackButton(onClick = { showFeedbackDialog = true })

        Spacer(modifier = Modifier.height(16.dp))

        // Settings Navigation Card
        SettingsNavigationCard(onClick = onSettingsClick)

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Transaction History Dialog
    if (showTransactionHistory) {
        TransactionHistoryDialog(
            transactions = uiState.last50Payments,
            onDismiss = { showTransactionHistory = false }
        )
    }

    // Feedback Bottom Sheet
    if (showFeedbackDialog) {
        FeedbackBottomSheet(
            onDismiss = { showFeedbackDialog = false },
            onSubmit = { message ->
                onSubmitFeedback(message)
                showFeedbackDialog = false
            },
            isLoading = feedbackState is FeedbackUiState.Loading
        )
    }
}

