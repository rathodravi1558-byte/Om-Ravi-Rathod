package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class DevLanguageFilter {
    BOTH,
    ENGLISH,
    HINDI
}

const val DEVELOPER_SIGNATURE = "Om"
const val DEVELOPER_SIGNATURE_HI = "ओम"
const val DEVELOPER_NAME_EN = "Om Ravi Rathod"
const val DEVELOPER_NAME_HI = "ओम रवि राठौड़"
const val DEVELOPER_EMAIL = "rathodravi1558@gmail.com"
const val DEVELOPER_YOUTUBE_HANDLE = "@RathodRavi-27w1"
const val DEVELOPER_YOUTUBE_URL = "https://www.youtube.com/@RathodRavi-27w1"
const val DEVELOPER_APP_TAGLINE = "App created by Om Ravi Rathod with Google AI Studio"
const val DEVELOPER_APP_TAGLINE_HI = "Google AI Studio के साथ ओम रवि राठौड़ द्वारा निर्मित ऐप"

/**
 * Developer Contact Card displayed directly on the weather feed with bilingual (Hindi & English) information.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeveloperContactCard(
    modifier: Modifier = Modifier,
    onOpenFullProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedLanguage by remember { mutableStateOf(DevLanguageFilter.BOTH) }
    var copiedRecently by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val cardBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E293B).copy(alpha = 0.85f),
            Color(0xFF0F172A).copy(alpha = 0.92f)
        )
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("developer_contact_card"),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(cardBrush)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0xFF60A5FA).copy(alpha = 0.4f),
                            Color(0xFFF43F5E).copy(alpha = 0.2f),
                            Color.White.copy(alpha = 0.08f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                // Header Row: Avatar, Name & Language Filter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Developer Avatar Badge with initials OR
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF2563EB), Color(0xFF7C3AED))
                                    )
                                )
                                .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "OR",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = when (selectedLanguage) {
                                        DevLanguageFilter.HINDI -> DEVELOPER_NAME_HI
                                        DevLanguageFilter.ENGLISH -> DEVELOPER_NAME_EN
                                        DevLanguageFilter.BOTH -> "$DEVELOPER_NAME_EN • $DEVELOPER_NAME_HI"
                                    },
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Rounded.Verified,
                                    contentDescription = "Verified Creator",
                                    tint = Color(0xFF60A5FA),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = when (selectedLanguage) {
                                    DevLanguageFilter.HINDI -> "मुख्य ऐप डेवलपर (Lead App Developer)"
                                    DevLanguageFilter.ENGLISH -> "Lead Android App Developer"
                                    DevLanguageFilter.BOTH -> "Lead App Developer • मुख्य ऐप डेवलपर"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF93C5FD),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Developer Signature & Google AI Studio Tagline
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF6366F1).copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, Color(0xFF818CF8).copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("developer_signature_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
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
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color(0xFFFBBF24),
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.2.sp
                                    )
                                )
                                Text(
                                    text = " • $DEVELOPER_SIGNATURE_HI",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFFFDE68A),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF4338CA).copy(alpha = 0.6f))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Google AI Studio",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFFA5B4FC),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = when (selectedLanguage) {
                                DevLanguageFilter.HINDI -> DEVELOPER_APP_TAGLINE_HI
                                DevLanguageFilter.ENGLISH -> DEVELOPER_APP_TAGLINE
                                DevLanguageFilter.BOTH -> "$DEVELOPER_APP_TAGLINE\n$DEVELOPER_APP_TAGLINE_HI"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.92f),
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // YouTube Channel Card Button (@RathodRavi-27w1)
                Surface(
                    onClick = { openUrlIntent(context, DEVELOPER_YOUTUBE_URL) },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFDC2626).copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("developer_youtube_channel_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDC2626)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = "YouTube Channel",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = when (selectedLanguage) {
                                        DevLanguageFilter.HINDI -> "यूट्यूब चैनल (YouTube Channel)"
                                        DevLanguageFilter.ENGLISH -> "Official YouTube Channel"
                                        DevLanguageFilter.BOTH -> "YouTube Channel • यूट्यूब चैनल"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFFFCA5A5),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Text(
                                    text = DEVELOPER_YOUTUBE_HANDLE,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp
                                    )
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                            contentDescription = "Open YouTube Channel",
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Language Selector Pills (Hindi & English focus)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Language,
                        contentDescription = "Language Option",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )

                    Text(
                        text = "भाषा / Lang:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                    )

                    FilterChip(
                        selected = selectedLanguage == DevLanguageFilter.BOTH,
                        onClick = { selectedLanguage = DevLanguageFilter.BOTH },
                        label = { Text("दोनों (Both)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF2563EB),
                            selectedLabelColor = Color.White,
                            containerColor = Color.White.copy(alpha = 0.08f),
                            labelColor = Color.White.copy(alpha = 0.75f)
                        ),
                        modifier = Modifier.testTag("developer_lang_both")
                    )

                    FilterChip(
                        selected = selectedLanguage == DevLanguageFilter.ENGLISH,
                        onClick = { selectedLanguage = DevLanguageFilter.ENGLISH },
                        label = { Text("English", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF2563EB),
                            selectedLabelColor = Color.White,
                            containerColor = Color.White.copy(alpha = 0.08f),
                            labelColor = Color.White.copy(alpha = 0.75f)
                        ),
                        modifier = Modifier.testTag("developer_lang_en")
                    )

                    FilterChip(
                        selected = selectedLanguage == DevLanguageFilter.HINDI,
                        onClick = { selectedLanguage = DevLanguageFilter.HINDI },
                        label = { Text("हिंदी", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF2563EB),
                            selectedLabelColor = Color.White,
                            containerColor = Color.White.copy(alpha = 0.08f),
                            labelColor = Color.White.copy(alpha = 0.75f)
                        ),
                        modifier = Modifier.testTag("developer_lang_hi")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Email Contact Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.12f),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Email,
                                contentDescription = "Developer Email",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = when (selectedLanguage) {
                                        DevLanguageFilter.HINDI -> "ईमेल संपर्क (Email Contact)"
                                        DevLanguageFilter.ENGLISH -> "Official Email Contact"
                                        DevLanguageFilter.BOTH -> "Official Email / आधिकारिक ईमेल"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 11.sp
                                    )
                                )
                                Text(
                                    text = DEVELOPER_EMAIL,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                )
                            }
                        }

                        // Copy confirmation status
                        AnimatedVisibility(
                            visible = copiedRecently,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = "Copied",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (selectedLanguage == DevLanguageFilter.HINDI) "कॉपी हुआ!" else "Copied!",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF6EE7B7),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Developer Message / Description in selected languages
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF38BDF8).copy(alpha = 0.07f))
                        .padding(12.dp)
                ) {
                    if (selectedLanguage == DevLanguageFilter.BOTH || selectedLanguage == DevLanguageFilter.ENGLISH) {
                        Text(
                            text = "Created by Om Ravi Rathod. Feel free to contact for app feedback, queries, or collaboration.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.9f),
                                lineHeight = 18.sp
                            )
                        )
                    }

                    if (selectedLanguage == DevLanguageFilter.BOTH) {
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (selectedLanguage == DevLanguageFilter.BOTH || selectedLanguage == DevLanguageFilter.HINDI) {
                        Text(
                            text = "ओम रवि राठौड़ द्वारा निर्मित। ऐप संबंधी सुझाव, प्रश्न या सहयोग के लिए ईमेल पर संपर्क करें।",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFFED7AA),
                                lineHeight = 18.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons: Send Email and Copy Email
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Send Email Button
                    ElevatedButton(
                        onClick = {
                            sendEmailIntent(context, DEVELOPER_EMAIL)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("developer_send_email_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color(0xFF2563EB),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "Send Email",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (selectedLanguage) {
                                DevLanguageFilter.HINDI -> "ईमेल भेजें"
                                DevLanguageFilter.ENGLISH -> "Send Email"
                                DevLanguageFilter.BOTH -> "Send Email / ईमेल"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }

                    // Copy Email Button
                    OutlinedButton(
                        onClick = {
                            copyToClipboard(context, DEVELOPER_EMAIL)
                            copiedRecently = true
                            scope.launch {
                                delay(2200)
                                copiedRecently = false
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("developer_copy_email_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                listOf(Color(0xFF60A5FA), Color.White.copy(alpha = 0.3f))
                            )
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = "Copy Email",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF60A5FA)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (selectedLanguage) {
                                DevLanguageFilter.HINDI -> "कॉपी करें"
                                DevLanguageFilter.ENGLISH -> "Copy Email"
                                DevLanguageFilter.BOTH -> "Copy / कॉपी करें"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Detailed Developer Contact Bottom Sheet Modal.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperContactSheet(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    var selectedLanguage by remember { mutableStateOf(DevLanguageFilter.BOTH) }
    var copiedRecently by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F172A),
        contentColor = Color.White,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(48.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.3f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .testTag("developer_contact_sheet"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF2563EB), Color(0xFF9333EA), Color(0xFFF43F5E))
                        )
                    )
                    .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "OR",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bilingual Names
            Text(
                text = DEVELOPER_NAME_EN,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = DEVELOPER_NAME_HI,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color(0xFFF59E0B),
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "App Developer & Creator • ऐप डेवलपर एवं निर्माता",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF93C5FD)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Signature & Google AI Studio Tagline Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF6366F1).copy(alpha = 0.18f),
                border = BorderStroke(1.dp, Color(0xFF818CF8).copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("developer_sheet_signature_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
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
                                    fontSize = 12.sp
                                )
                            )
                            Text(
                                text = DEVELOPER_SIGNATURE,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color(0xFFFBBF24),
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.2.sp
                                )
                            )
                            Text(
                                text = " • $DEVELOPER_SIGNATURE_HI",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFFDE68A),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF4338CA).copy(alpha = 0.7f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Google AI Studio",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFA5B4FC),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = when (selectedLanguage) {
                            DevLanguageFilter.HINDI -> DEVELOPER_APP_TAGLINE_HI
                            DevLanguageFilter.ENGLISH -> DEVELOPER_APP_TAGLINE
                            DevLanguageFilter.BOTH -> "$DEVELOPER_APP_TAGLINE\n$DEVELOPER_APP_TAGLINE_HI"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // YouTube Channel Button (@RathodRavi-27w1)
            Surface(
                onClick = { openUrlIntent(context, DEVELOPER_YOUTUBE_URL) },
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFDC2626).copy(alpha = 0.16f),
                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("developer_sheet_youtube_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDC2626)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = "YouTube",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = if (selectedLanguage == DevLanguageFilter.HINDI) "आधिकारिक यूट्यूब चैनल" else "Official YouTube Channel",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFFCA5A5),
                                    fontSize = 11.sp
                                )
                            )
                            Text(
                                text = "$DEVELOPER_YOUTUBE_HANDLE • YouTube",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                        contentDescription = "Open Channel",
                        tint = Color(0xFFF87171),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Language Selector Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Language / भाषा:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                )

                FilterChip(
                    selected = selectedLanguage == DevLanguageFilter.BOTH,
                    onClick = { selectedLanguage = DevLanguageFilter.BOTH },
                    label = { Text("दोनों (Both)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2563EB),
                        selectedLabelColor = Color.White,
                        containerColor = Color.White.copy(alpha = 0.08f),
                        labelColor = Color.White.copy(alpha = 0.7f)
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                FilterChip(
                    selected = selectedLanguage == DevLanguageFilter.ENGLISH,
                    onClick = { selectedLanguage = DevLanguageFilter.ENGLISH },
                    label = { Text("English") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2563EB),
                        selectedLabelColor = Color.White,
                        containerColor = Color.White.copy(alpha = 0.08f),
                        labelColor = Color.White.copy(alpha = 0.7f)
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                FilterChip(
                    selected = selectedLanguage == DevLanguageFilter.HINDI,
                    onClick = { selectedLanguage = DevLanguageFilter.HINDI },
                    label = { Text("हिंदी") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2563EB),
                        selectedLabelColor = Color.White,
                        containerColor = Color.White.copy(alpha = 0.08f),
                        labelColor = Color.White.copy(alpha = 0.7f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Contact Info Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Email,
                                contentDescription = "Email",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (selectedLanguage == DevLanguageFilter.HINDI) "आधिकारिक ईमेल (Email)" else "Official Contact Email",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                )
                                Text(
                                    text = DEVELOPER_EMAIL,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ElevatedButton(
                            onClick = { sendEmailIntent(context, DEVELOPER_EMAIL) },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = Color(0xFF2563EB),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Send,
                                contentDescription = "Send",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (selectedLanguage == DevLanguageFilter.HINDI) "ईमेल भेजें" else "Send Email",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                copyToClipboard(context, DEVELOPER_EMAIL)
                                copiedRecently = true
                                scope.launch {
                                    delay(2000)
                                    copiedRecently = false
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Icon(
                                imageVector = if (copiedRecently) Icons.Rounded.CheckCircle else Icons.Rounded.ContentCopy,
                                contentDescription = "Copy",
                                tint = if (copiedRecently) Color(0xFF10B981) else Color(0xFF60A5FA),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (copiedRecently) {
                                    if (selectedLanguage == DevLanguageFilter.HINDI) "कॉपी हुआ!" else "Copied!"
                                } else {
                                    if (selectedLanguage == DevLanguageFilter.HINDI) "कॉपी करें" else "Copy Email"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Details and Bio in Hindi & English
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (selectedLanguage == DevLanguageFilter.BOTH || selectedLanguage == DevLanguageFilter.ENGLISH) {
                    Column {
                        Text(
                            text = "English",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF60A5FA),
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "This weather application was engineered and designed by Om Ravi Rathod. Featuring real-time Open-Meteo atmospheric forecasts, Gemini Cloud Vision storm analysis, and red cloud severe weather early warning systems. For inquiries, technical discussions, or suggestions, feel free to get in touch at rathodravi1558@gmail.com.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.85f),
                                lineHeight = 19.sp
                            )
                        )
                    }
                }

                if (selectedLanguage == DevLanguageFilter.BOTH) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.1f))
                    )
                }

                if (selectedLanguage == DevLanguageFilter.BOTH || selectedLanguage == DevLanguageFilter.HINDI) {
                    Column {
                        Text(
                            text = "हिंदी (Hindi)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFF59E0B),
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "यह मौसम एप्लिकेशन ओम रवि राठौड़ द्वारा विकसित और डिज़ाइन किया गया है। इसमें रीयल-टाइम मौसम पूर्वानुमान, जेमिनी क्लाउड विज़न तूफानी बादलों का विश्लेषण, और लाल बादलों (Red Cloud) की आपदा चेतावनी प्रणाली शामिल है। सुझावों, पूछताछ अथवा सहयोग के लिए rathodravi1558@gmail.com पर संपर्क कर सकते हैं।",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.85f),
                                lineHeight = 19.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

fun sendEmailIntent(context: Context, emailAddress: String) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$emailAddress")
            putExtra(Intent.EXTRA_SUBJECT, "Weather App - Inquiry / Suggestion for Om Ravi Rathod")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Send Email via / ईमेल भेजें"))
    } catch (e: Exception) {
        Toast.makeText(context, "Email client not available. Email copied: $emailAddress", Toast.LENGTH_LONG).show()
        copyToClipboard(context, emailAddress)
    }
}

fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val clip = ClipData.newPlainText("Developer Info", text)
    clipboard?.setPrimaryClip(clip)
    Toast.makeText(context, "Copied: $text", Toast.LENGTH_SHORT).show()
}

fun openUrlIntent(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot open link: $url", Toast.LENGTH_SHORT).show()
    }
}
