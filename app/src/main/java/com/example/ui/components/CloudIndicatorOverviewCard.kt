package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CloudAnalysisResult
import com.example.model.CloudThreatLevel

@Composable
fun CloudIndicatorOverviewCard(
    cloudAnalysis: CloudAnalysisResult?,
    cloudCoverPercent: Int,
    onOpenSkyScanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRedAlert = cloudAnalysis?.isRedCloudAlert == true
    val activeThreat = cloudAnalysis?.threatLevel ?: if (cloudCoverPercent > 80) CloudThreatLevel.MODERATE else CloudThreatLevel.CLEAR_SAFE
    val displayCoverage = cloudAnalysis?.cloudCoveragePercent ?: cloudCoverPercent
    val displayCloudType = cloudAnalysis?.cloudType ?: if (cloudCoverPercent > 70) "Dense Stratus Deck" else if (cloudCoverPercent > 30) "Scattered Cumulus" else "Clear Skies"

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRedAlert) Color(0xFF5A0B0B).copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.22f)
        ),
        border = BorderStroke(
            1.dp,
            if (isRedAlert) Color(0xFFFF3B30).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.12f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("cloud_indicator_overview_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isRedAlert) Icons.Rounded.Warning else Icons.Rounded.Cloud,
                        contentDescription = null,
                        tint = if (isRedAlert) Color(0xFFFF5252) else Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CLOUD INDICATOR & THREAT LEVEL",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (isRedAlert) Color(0xFFFF8A80) else Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }

                // Threat Status Pill
                val badgeColor = when {
                    isRedAlert -> Color(0xFFFF1744)
                    activeThreat == CloudThreatLevel.MODERATE -> Color(0xFFFFB300)
                    else -> Color(0xFF66BB6A)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = if (isRedAlert) "RED ALERT" else activeThreat.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cloud Formation Label
            Text(
                text = displayCloudType,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Cloud Coverage Horizontal Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Cloud Coverage Density",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.7f)
                    )
                )
                Text(
                    text = "$displayCoverage%",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isRedAlert) Color(0xFFFF8A80) else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(displayCoverage / 100f)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                if (isRedAlert) {
                                    listOf(Color(0xFFFF5252), Color(0xFFFF1744))
                                } else {
                                    listOf(Color(0xFF42A5F5), Color(0xFF1E88E5))
                                }
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Button to Snap / Analyze Sky Photo
            Button(
                onClick = onOpenSkyScanner,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRedAlert) Color(0xFFD32F2F) else Color(0xFF2563EB)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("snap_sky_camera_btn")
            ) {
                Icon(
                    imageVector = Icons.Rounded.CameraAlt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "📸 Take / Submit Sky Photo for Accurate Analysis",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }
}
