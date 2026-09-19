package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CityLocation
import com.example.model.TemperatureUnit

@Composable
fun WeatherTopBar(
    location: CityLocation,
    unit: TemperatureUnit,
    isRefreshing: Boolean,
    onSearchClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onUnitToggle: () -> Unit,
    onRefreshClick: () -> Unit,
    onVoiceClick: () -> Unit = {},
    onCameraClick: () -> Unit = {},
    onAccountClick: () -> Unit = {},
    onDiagnosticsClick: () -> Unit = {},
    onDeveloperClick: () -> Unit = {},
    onSevereAlertsClick: () -> Unit = {},
    activeAlertCount: Int = 0,
    isOffline: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showOverflowMenu by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "refreshSpin"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Location Info (Clickable to open search as well)
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onSearchClick)
                .padding(vertical = 4.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = "Current Location",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = location.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isOffline) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = "OFFLINE",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFFD54F),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                if (location.subtitle.isNotBlank()) {
                    Text(
                        text = location.subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.82f)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Action Icons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Unit switch pill
            Surface(
                onClick = onUnitToggle,
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.2f),
                modifier = Modifier
                    .testTag("unit_toggle_button")
            ) {
                Text(
                    text = unit.symbol(),
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            // Favorite Bookmark
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("favorite_button")
            ) {
                Icon(
                    imageVector = if (location.isFavorite) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = if (location.isFavorite) "Remove from saved cities" else "Save to favorites",
                    tint = if (location.isFavorite) Color(0xFFFFD54F) else Color.White
                )
            }

            // Sky Photo Weather Camera
            IconButton(
                onClick = onCameraClick,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("top_bar_sky_camera_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.CameraAlt,
                    contentDescription = "Scan sky and clouds with camera",
                    tint = Color.White
                )
            }

            // Refresh
            IconButton(
                onClick = onRefreshClick,
                enabled = !isRefreshing,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("refresh_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Refresh weather",
                    tint = Color.White,
                    modifier = if (isRefreshing) Modifier.rotate(rotation) else Modifier
                )
            }

            // Search
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("search_city_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search locations",
                    tint = Color.White
                )
            }

            // Voice Weather Command Microphone
            IconButton(
                onClick = onVoiceClick,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("top_bar_voice_command_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Mic,
                    contentDescription = "Speak location for weather command",
                    tint = Color(0xFF60A5FA)
                )
            }

            // Account & Email Alerts Trigger Button
            IconButton(
                onClick = onAccountClick,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("top_bar_account_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.AccountCircle,
                    contentDescription = "User Account, Login & Email Condition Alerts",
                    tint = Color(0xFF38BDF8)
                )
            }

            // Real-Time FCM Severe Weather Alerts Button
            Box {
                IconButton(
                    onClick = onSevereAlertsClick,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("top_bar_severe_alerts_button")
                ) {
                    Icon(
                        imageVector = if (activeAlertCount > 0) Icons.Rounded.NotificationsActive else Icons.Rounded.Notifications,
                        contentDescription = "Real-time FCM Severe Weather Alerts for saved locations ($activeAlertCount active)",
                        tint = if (activeAlertCount > 0) Color(0xFFFB7185) else Color.White
                    )
                }
                if (activeAlertCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 4.dp, end = 4.dp)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE11D48)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (activeAlertCount > 9) "9+" else activeAlertCount.toString(),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Developer Info (Om Ravi Rathod - rathodravi1558@gmail.com)
            IconButton(
                onClick = onDeveloperClick,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("top_bar_developer_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = "App Developer Om Ravi Rathod - rathodravi1558@gmail.com",
                    tint = Color(0xFF93C5FD)
                )
            }

            // Three Dots Overflow Menu (More Options)
            Box {
                IconButton(
                    onClick = { showOverflowMenu = !showOverflowMenu },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("top_bar_more_options_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "More options: YouTube channel @RathodRavi-27w1, Signature Om",
                        tint = Color.White
                    )
                }

                DropdownMenu(
                    expanded = showOverflowMenu,
                    onDismissRequest = { showOverflowMenu = false },
                    modifier = Modifier
                        .width(280.dp)
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                        .testTag("top_bar_overflow_menu")
                ) {
                    // Header: Developer Signature & Google AI Studio
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.45f)),
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Signature: ",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 11.sp
                                        )
                                    )
                                    Text(
                                        text = DEVELOPER_SIGNATURE,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = Color(0xFFFBBF24),
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                    Text(
                                        text = " ($DEVELOPER_SIGNATURE_HI)",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFFFDE68A),
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF4338CA).copy(alpha = 0.7f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "AI Studio",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFFA5B4FC),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = DEVELOPER_APP_TAGLINE,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.95f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 15.sp
                                )
                            )
                        }
                    }

                    // Real-Time FCM Severe Weather Alerts
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = "Severe Weather Alerts",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Real-time FCM warnings for saved cities",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFFFB7185)
                                    )
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsActive,
                                contentDescription = null,
                                tint = Color(0xFFFB7185),
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        onClick = {
                            showOverflowMenu = false
                            onSevereAlertsClick()
                        },
                        modifier = Modifier.testTag("menu_severe_alerts_item")
                    )

                    // Account & Email Rate Alerts
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = "Account & Email Alerts",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Sign in & live condition rate alerts",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF38BDF8)
                                    )
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.AccountCircle,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        onClick = {
                            showOverflowMenu = false
                            onAccountClick()
                        },
                        modifier = Modifier.testTag("menu_account_alerts_item")
                    )

                    // Developer Diagnostics & Auto-Fix
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = "Developer Diagnostics & Fix",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Check everything & auto-fix issues",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF10B981)
                                    )
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Build,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        onClick = {
                            showOverflowMenu = false
                            onDiagnosticsClick()
                        },
                        modifier = Modifier.testTag("menu_developer_diagnostics_item")
                    )

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.12f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // YouTube Channel Option (@RathodRavi-27w1)
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = "YouTube Channel",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "$DEVELOPER_YOUTUBE_HANDLE • Website",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFFF87171),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDC2626)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = "YouTube Channel",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                contentDescription = null,
                                tint = Color(0xFFFCA5A5),
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        onClick = {
                            showOverflowMenu = false
                            openUrlIntent(context, DEVELOPER_YOUTUBE_URL)
                        },
                        modifier = Modifier.testTag("menu_youtube_channel_item")
                    )

                    // Developer Profile Option
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = DEVELOPER_NAME_EN,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Text(
                                    text = "Developer Contact & Info",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF93C5FD)
                                    )
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = Color(0xFF60A5FA),
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        onClick = {
                            showOverflowMenu = false
                            onDeveloperClick()
                        },
                        modifier = Modifier.testTag("menu_developer_profile_item")
                    )

                    // Email Developer Option
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = "Email Developer",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = DEVELOPER_EMAIL,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFFFBBF24),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Email,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        onClick = {
                            showOverflowMenu = false
                            sendEmailIntent(context, DEVELOPER_EMAIL)
                        },
                        modifier = Modifier.testTag("menu_email_developer_item")
                    )

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.12f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // Temperature Unit Toggle
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Unit: ${unit.symbol()} (Switch to ${if (unit == TemperatureUnit.CELSIUS) "°F" else "°C"})",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                            )
                        },
                        leadingIcon = {
                            Text(
                                text = unit.symbol(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color(0xFF60A5FA),
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        },
                        onClick = {
                            showOverflowMenu = false
                            onUnitToggle()
                        }
                    )

                    // Refresh Weather
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Refresh Weather",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        onClick = {
                            showOverflowMenu = false
                            onRefreshClick()
                        }
                    )
                }
            }
        }
    }
}
