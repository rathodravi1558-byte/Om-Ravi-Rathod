package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.BeachAccess
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.DailyForecast
import com.example.model.WeatherConditionResolver
import com.example.ui.theme.UvExtreme
import com.example.ui.theme.UvHigh
import com.example.ui.theme.UvLow
import com.example.ui.theme.UvModerate
import com.example.ui.theme.UvVeryHigh
import java.util.Locale

@Composable
fun UvIndexSafetyCard(
    uvIndex: Double,
    maxTodayUv: Double? = null,
    modifier: Modifier = Modifier
) {
    val safetyInfo = remember(uvIndex) {
        WeatherConditionResolver.getUvSafetyRecommendation(uvIndex)
    }

    val riskColor = when {
        uvIndex < 3.0 -> UvLow
        uvIndex < 6.0 -> UvModerate
        uvIndex < 8.0 -> UvHigh
        uvIndex < 11.0 -> UvVeryHigh
        else -> UvExtreme
    }

    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("uv_index_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f)
        ),
        border = BorderStroke(1.dp, riskColor.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Icon, Title & Risk Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(riskColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WbSunny,
                            contentDescription = "UV Index",
                            tint = riskColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.uv_index_card_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.uv_index_current),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Risk Level Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = riskColor.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, riskColor.copy(alpha = 0.40f)),
                    modifier = Modifier.testTag("uv_risk_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(riskColor)
                        )
                        Text(
                            text = safetyInfo.riskLevel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = riskColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary UV Metric Display Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%.1f", uvIndex),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("uv_index_value")
                    )
                    Text(
                        text = "/ 12",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                if (maxTodayUv != null && maxTodayUv > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = "Peak Today: ${String.format(Locale.US, "%.1f", maxTodayUv)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Continuous Visual UV Spectrum Gauge Bar
            UvSpectrumBar(
                currentUv = uvIndex,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Recommendation Box
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = riskColor.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, riskColor.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("uv_safety_recommendation")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = "Protection Recommendation",
                        tint = riskColor,
                        modifier = Modifier
                            .size(22.dp)
                            .padding(top = 1.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.uv_safety_recommendation_label),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = riskColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = safetyInfo.recommendation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Protective Gear Checklist
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("uv_gear_checklist"),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SunGearPill(
                    icon = Icons.Rounded.WaterDrop,
                    label = "Sunscreen",
                    subtext = if (safetyInfo.needsSunscreen) "SPF 30+" else "Optional",
                    isRecommended = safetyInfo.needsSunscreen,
                    activeColor = riskColor,
                    modifier = Modifier.weight(1f)
                )
                SunGearPill(
                    icon = Icons.Rounded.Visibility,
                    label = "Eyewear",
                    subtext = if (safetyInfo.needsSunglasses) "UV400" else "Standard",
                    isRecommended = safetyInfo.needsSunglasses,
                    activeColor = riskColor,
                    modifier = Modifier.weight(1f)
                )
                SunGearPill(
                    icon = Icons.Rounded.BeachAccess,
                    label = "Cover / Shade",
                    subtext = if (safetyInfo.needsShade) "Required" else "Suggested",
                    isRecommended = safetyInfo.needsShade,
                    activeColor = riskColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Expandable Sun Safety Details & Peak Exposure Warning
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = "Peak UV Hours",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = safetyInfo.peakHoursWarning,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse advice" else "Expand advice",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Detailed Sun Exposure Advice",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = safetyInfo.detailedAdvice,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun UvSpectrumBar(
    currentUv: Double,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (currentUv.toFloat() / 12f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "uv_needle_progress"
    )

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
        ) {
            // Gradient spectrum track
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .align(Alignment.Center)
            ) {
                val corner = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                val brush = Brush.horizontalGradient(
                    colors = listOf(
                        UvLow,
                        UvModerate,
                        UvHigh,
                        UvVeryHigh,
                        UvExtreme
                    )
                )
                drawRoundRect(
                    brush = brush,
                    size = Size(size.width, size.height),
                    cornerRadius = corner
                )

                // Draw needle marker
                val markerX = (size.width * animatedProgress).coerceIn(4.dp.toPx(), size.width - 4.dp.toPx())
                drawCircle(
                    color = Color.White,
                    radius = 6.dp.toPx(),
                    center = Offset(markerX, size.height / 2f)
                )
                drawCircle(
                    color = Color(0xFF1E293B),
                    radius = 3.dp.toPx(),
                    center = Offset(markerX, size.height / 2f)
                )
            }
        }

        // Scale Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0 Low", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = UvLow)
            Text("3 Mod", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = UvModerate)
            Text("6 High", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = UvHigh)
            Text("8 Very High", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = UvVeryHigh)
            Text("11+ Ext", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = UvExtreme)
        }
    }
}

@Composable
private fun SunGearPill(
    icon: ImageVector,
    label: String,
    subtext: String,
    isRecommended: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isRecommended) activeColor.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
    val tintColor = if (isRecommended) activeColor else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(
            1.dp,
            if (isRecommended) activeColor.copy(alpha = 0.35f) else Color.Transparent
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tintColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = tintColor,
                fontSize = 11.sp,
                maxLines = 1
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp,
                maxLines = 1
            )
        }
    }
}
