package com.example.awaazpay.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.awaazpay.util.Logger
import kotlinx.coroutines.launch

/**
 * Onboarding screen shown only on first app launch.
 * 3-screen flow explaining permissions and features.
 */
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    var currentPage by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    Logger.d("OnboardingScreen: currentPage=$currentPage")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Page indicator
            PageIndicator(
                totalPages = 3,
                currentPage = currentPage,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Content area with slide animation
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentPage,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(300)
                            ) + fadeIn() togetherWith
                                    slideOutHorizontally(
                                        targetOffsetX = { -it },
                                        animationSpec = tween(300)
                                    ) + fadeOut()
                        } else {
                            slideInHorizontally(
                                initialOffsetX = { -it },
                                animationSpec = tween(300)
                            ) + fadeIn() togetherWith
                                    slideOutHorizontally(
                                        targetOffsetX = { it },
                                        animationSpec = tween(300)
                                    ) + fadeOut()
                        }
                    },
                    label = "page_transition"
                ) { page ->
                    when (page) {
                        0 -> OnboardingPage1()
                        1 -> OnboardingPage2()
                        2 -> OnboardingPage3()
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip button (only on first two pages)
                if (currentPage < 2) {
                    TextButton(
                        onClick = {
                            Logger.d("Onboarding: Skip tapped")
                            coroutineScope.launch {
                                onComplete()
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.White.copy(alpha = 0.6f)
                        )
                    ) {
                        Text("Skip", fontSize = 16.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.width(80.dp))
                }

                // Next/Get Started button
                Button(
                    onClick = {
                        if (currentPage < 2) {
                            Logger.d("Onboarding: Next tapped, page=$currentPage")
                            currentPage++
                        } else {
                            Logger.d("Onboarding: Get Started tapped")
                            coroutineScope.launch {
                                onComplete()
                            }
                        }
                    },
                    modifier = Modifier
                        .height(56.dp)
                        .widthIn(min = 160.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00D9FF),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(28.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp
                    )
                ) {
                    Text(
                        text = if (currentPage < 2) "Continue" else "Get Started",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OnboardingPage1() {
    OnboardingPageContent(
        icon = Icons.Default.Notifications,
        iconColor = Color(0xFF00D9FF),
        title = "Welcome to AwaazPay",
        description = "Never miss a payment again. Get instant voice announcements when you receive money through UPI.",
        features = listOf(
            "🎯 Instant announcements in English or Hindi",
            "💰 Know who paid you and how much",
            "🔔 Works with all major UPI apps"
        )
    )
}

@Composable
private fun OnboardingPage2() {
    OnboardingPageContent(
        icon = Icons.Default.Lock,
        iconColor = Color(0xFF4CAF50),
        title = "Permission Required",
        description = "To work seamlessly, AwaazPay needs:",
        features = listOf(
            "🔔 Notification Access: Read UPI payment notifications to announce them",
            "⚡ Foreground Service: Stay active for reliable, instant announcements",
            "🔒 Your payment data stays on your device"
        )
    )
}

@Composable
private fun OnboardingPage3() {
    OnboardingPageContent(
        icon = Icons.Default.Favorite,
        iconColor = Color(0xFFFF6B9D),
        title = "Your Privacy Matters",
        description = "We're committed to keeping your data safe:",
        features = listOf(
            "✅ All payment data stored locally on your phone",
            "💬 Feedback is optional and helps us improve",
            "🔒 If you share feedback, it's securely stored"
        ),
        footer = "Ready to start? Let's set up AwaazPay!"
    )
}

@Composable
private fun OnboardingPageContent(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    features: List<String>,
    footer: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = iconColor
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Title
        Text(
            text = title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = description,
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Features list
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                features.forEach { feature ->
                    Text(
                        text = feature,
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 22.sp
                    )
                }
            }
        }

        if (footer != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = footer,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF00D9FF),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PageIndicator(
    totalPages: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(totalPages) { index ->
            Box(
                modifier = Modifier
                    .size(
                        width = if (index == currentPage) 24.dp else 8.dp,
                        height = 8.dp
                    )
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (index == currentPage)
                            Color(0xFF00D9FF)
                        else
                            Color.White.copy(alpha = 0.3f)
                    )
                    .animateContentSize()
            )
        }
    }
}

