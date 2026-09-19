package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DailyForecast
import com.example.model.TemperatureUnit

@Composable
fun DailyForecastCard(
    dailyList: List<DailyForecast>,
    unit: TemperatureUnit,
    modifier: Modifier = Modifier
) {
    if (dailyList.isEmpty()) return

    // Selected display range: 20, 14, or 7 days
    var selectedDaysCount by remember { mutableIntStateOf(20) }
    val displayList = dailyList.take(selectedDaysCount)

    val minOverall = displayList.minOfOrNull { it.minTempC } ?: 0.0
    val maxOverall = displayList.maxOfOrNull { it.maxTempC } ?: 30.0
    val rangeTotal = (maxOverall - minOverall).coerceAtLeast(1.0)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("daily_forecast_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.22f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize()
        ) {
            // Header with range toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarMonth,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "20-DAY EXTENDED FORECAST",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Day range selector pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(20, 14, 7).forEach { days ->
                    val isSelected = selectedDaysCount == days
                    Surface(
                        onClick = { selectedDaysCount = days },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color.White.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.08f),
                        border = if (isSelected) BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)) else null,
                        modifier = Modifier.testTag("forecast_tab_$days")
                    ) {
                        Text(
                            text = "$days Days",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.65f),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            displayList.forEachIndexed { index, dayItem ->
                DailyForecastRow(
                    item = dayItem,
                    unit = unit,
                    weekMin = minOverall,
                    weekRange = rangeTotal,
                    index = index
                )
                if (index < displayList.size - 1) {
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.08f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyForecastRow(
    item: DailyForecast,
    unit: TemperatureUnit,
    weekMin: Double,
    weekRange: Double,
    index: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("forecast_day_$index"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Day name & Date
        Column(
            modifier = Modifier.width(68.dp)
        ) {
            Text(
                text = item.dayOfWeek,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    fontWeight = if (item.dayOfWeek == "Today") FontWeight.Bold else FontWeight.SemiBold
                ),
                maxLines = 1
            )
            Text(
                text = item.dateFormatted,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 11.sp
                ),
                maxLines = 1
            )
        }

        // Weather Icon
        Icon(
            imageVector = item.condition.icon,
            contentDescription = item.condition.label,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )

        // Rain Probability (if > 0)
        Box(
            modifier = Modifier.width(42.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (item.precipitationProbMax > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WaterDrop,
                        contentDescription = null,
                        tint = Color(0xFF80D8FF),
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = "${item.precipitationProbMax}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF80D8FF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Low Temp
        Text(
            text = unit.format(item.minTempC),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White.copy(alpha = 0.72f),
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.End
            ),
            modifier = Modifier.width(34.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Relative Temperature Range Bar
        val startFrac = ((item.minTempC - weekMin) / weekRange).toFloat().coerceIn(0f, 0.85f)
        val endFrac = ((item.maxTempC - weekMin) / weekRange).toFloat().coerceIn(startFrac + 0.1f, 1f)

        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(endFrac)
                    .padding(start = (startFrac * 50).dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF64B5F6), Color(0xFFFFB74D))
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // High Temp
        Text(
            text = unit.format(item.maxTempC),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End
            ),
            modifier = Modifier.width(34.dp)
        )
    }
}
