package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HourlyForecast
import com.example.model.TemperatureUnit

@Composable
fun HourlyForecastCard(
    hourlyList: List<HourlyForecast>,
    unit: TemperatureUnit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("hourly_forecast_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.22f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Schedule,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "HOURLY FORECAST",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Carousel
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(hourlyList) { hourItem ->
                    HourlyItemView(item = hourItem, unit = unit)
                }
            }
        }
    }
}

@Composable
private fun HourlyItemView(
    item: HourlyForecast,
    unit: TemperatureUnit
) {
    val bgColor = if (item.isCurrentHour) {
        Color.White.copy(alpha = 0.25f)
    } else {
        Color.White.copy(alpha = 0.08f)
    }
    val border = if (item.isCurrentHour) {
        BorderStroke(1.dp, Color.White.copy(alpha = 0.45f))
    } else null

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = border,
        modifier = Modifier.width(66.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.timeFormatted,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (item.isCurrentHour) Color.White else Color.White.copy(alpha = 0.85f),
                    fontWeight = if (item.isCurrentHour) FontWeight.Bold else FontWeight.Normal
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Icon(
                imageVector = item.condition.icon,
                contentDescription = item.condition.label,
                tint = if (item.isCurrentHour) Color(0xFFFFD54F) else Color.White,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Rain probability indicator
            if (item.precipitationProbability > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WaterDrop,
                        contentDescription = "Rain chance",
                        tint = Color(0xFF80D8FF),
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = "${item.precipitationProbability}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF80D8FF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(14.dp))
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = unit.format(item.temperatureC),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}
