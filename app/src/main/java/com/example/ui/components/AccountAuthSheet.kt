package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AuthProvider
import com.example.model.CurrentWeatherData
import com.example.model.UserAccountProfile
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Account Authentication & Email Condition Rate Updates Sheet.
 * Allows users to sign in with Google, Apple, Twitter/X, or create a new email account,
 * and subscribe to live condition rate updates sent to their email address.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AccountAuthSheet(
    userProfile: UserAccountProfile,
    currentWeather: CurrentWeatherData?,
    currentCityName: String,
    onDismiss: () -> Unit,
    onSaveProfile: (UserAccountProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Profile & Alerts, 1: Sign In / Switch Account
    var emailInput by remember { mutableStateOf(userProfile.email) }
    var nameInput by remember { mutableStateOf(userProfile.displayName) }
    var passwordInput by remember { mutableStateOf("••••••••") }
    var isEmailAlertsEnabled by remember { mutableStateOf(userProfile.isEmailAlertsEnabled) }
    var selectedFrequency by remember { mutableStateOf(userProfile.alertFrequency) }
    var rainThresholdMm by remember { mutableDoubleStateOf(userProfile.rainRateThresholdMm) }
    var severeStormAlert by remember { mutableStateOf(userProfile.severeStormAlert) }
    var extremeUvAlert by remember { mutableStateOf(userProfile.extremeUvAlert) }
    var currentProvider by remember { mutableStateOf(userProfile.provider) }
    var showEmailSentPreview by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier.testTag("account_auth_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF38BDF8).copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccountCircle,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "User Account & Updates",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Google, Apple, Twitter & Email Alerts",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                        )
                    }
                }

                IconButton(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                    },
                    modifier = Modifier.testTag("close_account_sheet_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Tab Row: [Account Profile & Alerts] vs [Sign In / Switch Provider]
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color(0xFF1E293B),
                contentColor = Color(0xFF38BDF8),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = Color(0xFF38BDF8)
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = {
                        Text(
                            "My Profile & Email Alerts",
                            fontSize = 13.sp,
                            fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (activeTab == 0) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                        )
                    }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = {
                        Text(
                            "Sign In / Create Account",
                            fontSize = 13.sp,
                            fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (activeTab == 1) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (activeTab == 0) {
                // TAB 0: User Profile Details and Email Weather Updates Configuration
                UserProfileCard(
                    name = nameInput,
                    email = emailInput,
                    provider = currentProvider,
                    isVerified = true,
                    onSwitchProviderClick = { activeTab = 1 }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Email Alerts & Actual Condition Rate Section
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.85f)),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth().testTag("email_alerts_section")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.NotificationsActive,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Email Weather & Rate Alerts",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "Receive actual condition rates to your email",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                                    )
                                }
                            }

                            Switch(
                                checked = isEmailAlertsEnabled,
                                onCheckedChange = { isEmailAlertsEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF0F172A),
                                    checkedTrackColor = Color(0xFF38BDF8)
                                ),
                                modifier = Modifier.testTag("email_alerts_toggle")
                            )
                        }

                        AnimatedVisibility(visible = isEmailAlertsEnabled) {
                            Column(modifier = Modifier.padding(top = 16.dp)) {
                                HorizontalDivider(color = Color(0xFF334155))
                                Spacer(modifier = Modifier.height(12.dp))

                                // Target Email Input
                                OutlinedTextField(
                                    value = emailInput,
                                    onValueChange = { emailInput = it },
                                    label = { Text("Delivery Email Address", color = Color(0xFF94A3B8)) },
                                    leadingIcon = {
                                        Icon(Icons.Rounded.Email, contentDescription = null, tint = Color(0xFF38BDF8))
                                    },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF38BDF8),
                                        unfocusedBorderColor = Color(0xFF475569)
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("alert_email_input")
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Actual Condition Rate Triggers",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color(0xFF38BDF8),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Notify me when weather condition rates surpass these levels:",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Rain Rate Trigger Slider
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.WaterDrop, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Rain Rate Threshold", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        }
                                        Text(
                                            text = String.format(Locale.US, "%.1f mm/hr", rainThresholdMm),
                                            color = Color(0xFF60A5FA),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Slider(
                                        value = rainThresholdMm.toFloat(),
                                        onValueChange = { rainThresholdMm = it.toDouble() },
                                        valueRange = 0.5f..15.0f,
                                        steps = 14,
                                        colors = SliderDefaults.colors(
                                            thumbColor = Color(0xFF38BDF8),
                                            activeTrackColor = Color(0xFF38BDF8),
                                            inactiveTrackColor = Color(0xFF334155)
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Condition Checkbox Chips
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = severeStormAlert,
                                        onClick = { severeStormAlert = !severeStormAlert },
                                        label = { Text("⚡ Severe Storm & Red Cloud Alert") },
                                        leadingIcon = if (severeStormAlert) {
                                            { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFFEF4444).copy(alpha = 0.25f),
                                            selectedLabelColor = Color(0xFFFCA5A5)
                                        )
                                    )

                                    FilterChip(
                                        selected = extremeUvAlert,
                                        onClick = { extremeUvAlert = !extremeUvAlert },
                                        label = { Text("☀️ Extreme UV Index (> 8.0)") },
                                        leadingIcon = if (extremeUvAlert) {
                                            { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFFF59E0B).copy(alpha = 0.25f),
                                            selectedLabelColor = Color(0xFFFDE68A)
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "Update Frequency",
                                    style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF94A3B8))
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("Instant Shift", "Hourly Updates", "Daily 7 AM").forEach { freq ->
                                        val isSelected = selectedFrequency == freq
                                        Surface(
                                            onClick = { selectedFrequency = freq },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF0F172A),
                                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = freq,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (isSelected) Color(0xFF0F172A) else Color.White,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Send Instant Test Email Update Button
                                OutlinedButton(
                                    onClick = {
                                        showEmailSentPreview = true
                                        Toast.makeText(context, "Condition update email simulated to $emailInput", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                                    modifier = Modifier.fillMaxWidth().testTag("send_test_email_update_button")
                                ) {
                                    Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Send Instant Condition Update to My Email")
                                }

                                // Interactive Email Preview Box
                                AnimatedVisibility(visible = showEmailSentPreview) {
                                    EmailPreviewCard(
                                        email = emailInput,
                                        city = currentCityName,
                                        currentWeather = currentWeather,
                                        rainThresholdMm = rainThresholdMm,
                                        onClose = { showEmailSentPreview = false }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save Preferences Button
                Button(
                    onClick = {
                        val updated = userProfile.copy(
                            displayName = nameInput.ifBlank { "User" },
                            email = emailInput.ifBlank { "rathodravi1558@gmail.com" },
                            provider = currentProvider,
                            isEmailAlertsEnabled = isEmailAlertsEnabled,
                            alertFrequency = selectedFrequency,
                            rainRateThresholdMm = rainThresholdMm,
                            severeStormAlert = severeStormAlert,
                            extremeUvAlert = extremeUvAlert,
                            lastAlertSentFormatted = "Active live updates to $emailInput"
                        )
                        onSaveProfile(updated)
                        Toast.makeText(context, "Account & Email alert preferences saved!", Toast.LENGTH_SHORT).show()
                        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF38BDF8),
                        contentColor = Color(0xFF0F172A)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_account_preferences_button")
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Preferences & Sync Alerts",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

            } else {
                // TAB 1: Sign In or Create New Account (Google, Apple, Twitter, Email)
                SignInProviderSelector(
                    currentEmail = emailInput,
                    onProviderSelected = { prov, email, name ->
                        currentProvider = prov
                        emailInput = email
                        nameInput = name
                        activeTab = 0
                        Toast.makeText(context, "Connected with ${prov.displayName} ($email)", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun UserProfileCard(
    name: String,
    email: String,
    provider: AuthProvider,
    isVerified: Boolean,
    onSwitchProviderClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E293B)
        ),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(listOf(Color(0xFF38BDF8).copy(alpha = 0.5f), Color(0xFF6366F1).copy(alpha = 0.5f)))
        ),
        modifier = Modifier.fillMaxWidth().testTag("user_profile_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar circle
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF38BDF8), Color(0xFF6366F1)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.firstOrNull()?.uppercase() ?: "R",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        if (isVerified) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Rounded.Verified,
                                contentDescription = "Verified User",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF0F172A)
                    ) {
                        Text(
                            text = "Linked via ${provider.displayName}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF38BDF8),
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            TextButton(
                onClick = onSwitchProviderClick,
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF38BDF8))
            ) {
                Text("Switch", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun SignInProviderSelector(
    currentEmail: String,
    onProviderSelected: (AuthProvider, String, String) -> Unit
) {
    var newEmail by remember { mutableStateOf(currentEmail) }
    var newPassword by remember { mutableStateOf("") }
    var newName by remember { mutableStateOf("Ravi Rathod") }

    Column(modifier = Modifier.fillMaxWidth().testTag("sign_in_provider_selector")) {
        Text(
            text = "Connect Your Account",
            style = MaterialTheme.typography.titleMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = "Sign in or create an account to synchronize weather condition rate updates with your email.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Google One-Tap Sign In
        Surface(
            onClick = {
                onProviderSelected(AuthProvider.GOOGLE, "rathodravi1558@gmail.com", "Ravi Rathod")
            },
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF1E293B),
            border = BorderStroke(1.dp, Color(0xFF4285F4)),
            modifier = Modifier.fillMaxWidth().testTag("signin_google_button")
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Google "G" icon style
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Sign in with Google (rathodravi1558@gmail.com)",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Apple Sign In
        Surface(
            onClick = {
                onProviderSelected(AuthProvider.APPLE, "ravi.rathod@icloud.com", "Ravi Rathod")
            },
            shape = RoundedCornerShape(14.dp),
            color = Color.Black,
            border = BorderStroke(1.dp, Color(0xFF475569)),
            modifier = Modifier.fillMaxWidth().testTag("signin_apple_button")
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Sign in with Apple ID",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Twitter / X Sign In
        Surface(
            onClick = {
                onProviderSelected(AuthProvider.TWITTER, "rathodravi1558@x.com", "@RaviRathod")
            },
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth().testTag("signin_twitter_button")
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("𝕏", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Sign in with Twitter / X",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF334155))
            Text("  OR CREATE WITH EMAIL  ", color = Color(0xFF64748B), fontSize = 11.sp)
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF334155))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Create New Account with Email & Password
        OutlinedTextField(
            value = newName,
            onValueChange = { newName = it },
            label = { Text("Full Name", color = Color(0xFF94A3B8)) },
            leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = Color(0xFF38BDF8)) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF38BDF8),
                unfocusedBorderColor = Color(0xFF475569)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = newEmail,
            onValueChange = { newEmail = it },
            label = { Text("Email ID", color = Color(0xFF94A3B8)) },
            leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null, tint = Color(0xFF38BDF8)) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF38BDF8),
                unfocusedBorderColor = Color(0xFF475569)
            ),
            modifier = Modifier.fillMaxWidth().testTag("signup_email_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("Password", color = Color(0xFF94A3B8)) },
            leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = Color(0xFF38BDF8)) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF38BDF8),
                unfocusedBorderColor = Color(0xFF475569)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = {
                onProviderSelected(
                    AuthProvider.EMAIL,
                    newEmail.ifBlank { "rathodravi1558@gmail.com" },
                    newName.ifBlank { "Ravi" }
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF38BDF8),
                contentColor = Color(0xFF0F172A)
            ),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("create_email_account_button")
        ) {
            Icon(Icons.Rounded.Security, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Account & Sign In", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EmailPreviewCard(
    email: String,
    city: String,
    currentWeather: CurrentWeatherData?,
    rainThresholdMm: Double,
    onClose: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B132B)),
        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .testTag("email_preview_card")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sample Condition Email Dispatched", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Rounded.Close, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text("To: $email", color = Color(0xFF94A3B8), fontSize = 11.sp)
            Text("Subject: [Weather Rate Alert] Real-time conditions in $city", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "• Current Temperature: ${currentWeather?.temperatureC?.toInt() ?: 22}°C (${currentWeather?.condition?.label ?: "Partly Cloudy"})",
                        color = Color(0xFFE2E8F0),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "• Actual Rain Rate: ${String.format(Locale.US, "%.1f", currentWeather?.metrics?.precipitationMm ?: 0.0)} mm/hr (Threshold: ${String.format(Locale.US, "%.1f", rainThresholdMm)} mm/hr)",
                        color = Color(0xFF60A5FA),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "• UV Exposure Rate: ${String.format(Locale.US, "%.1f", currentWeather?.metrics?.uvIndex ?: 3.5)} (${currentWeather?.metrics?.uvRiskCategory ?: "Moderate"})",
                        color = Color(0xFFF59E0B),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "• Barometric Pressure: ${currentWeather?.metrics?.pressureHpa ?: 1013.2} hPa (Stable)",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
