package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WeatherMetrics
import com.example.ui.theme.UvExtreme
import com.example.ui.theme.UvHigh
import com.example.ui.theme.UvLow
import com.example.ui.theme.UvModerate
import com.example.ui.theme.UvVeryHigh

@Composable
fun WeatherMetricsGrid(
    metrics: WeatherMetrics,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("weather_metrics_grid"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1: UV Index & Wind
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // UV Card
            val uvColor = when (metrics.uvRiskCategory) {
                "Low" -> UvLow
                "Moderate" -> UvModerate
                "High" -> UvHigh
                "Very High" -> UvVeryHigh
                else -> UvExtreme
            }

            MetricCard(
                title = "UV INDEX",
                icon = Icons.Rounded.WbSunny,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = String.format("%.1f", metrics.uvIndex),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = uvColor
                ) {
                    Text(
                        text = metrics.uvRiskCategory,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (metrics.uvIndex < 3.0) "Minimal sun protection needed" else "Sun protection recommended",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp
                    )
                )
            }

            // Wind Card
            MetricCard(
                title = "WIND",
                icon = Icons.Rounded.Air,
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${metrics.windSpeedKmh.toInt()}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "km/h",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Navigation,
                        contentDescription = "Wind direction",
                        tint = Color(0xFF80D8FF),
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(metrics.windDirectionDeg.toFloat())
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${metrics.windDirectionCardinal} (${metrics.windDirectionDeg}°)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (metrics.windSpeedKmh < 20) "Gentle breeze" else "Brisk winds",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp
                    )
                )
            }
        }

        // Row 2: Humidity & Pressure
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Humidity Card
            MetricCard(
                title = "HUMIDITY",
                icon = Icons.Rounded.WaterDrop,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${metrics.humidityPercent}%",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when {
                        metrics.humidityPercent < 35 -> "Dry conditions"
                        metrics.humidityPercent < 65 -> "Comfortable balance"
                        else -> "Humid and muggy"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Visibility: ${metrics.visibilityDescription}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.85f)
                    )
                )
            }

            // Pressure Card
            MetricCard(
                title = "PRESSURE",
                icon = Icons.Rounded.Compress,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${metrics.pressureHpa.toInt()}",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "hPa (Steady)",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (metrics.pressureHpa >= 1013) "High pressure: Stable" else "Low pressure: Variable",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp
                    )
                )
            }
        }

        // Row 3: Sun Cycle & Precipitation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Sun Cycle Card
            MetricCard(
                title = "SUN CYCLE",
                icon = Icons.Rounded.WbTwilight,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Sunrise",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        )
                        Text(
                            text = metrics.sunriseTime,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Column {
                        Text(
                            text = "Sunset",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        )
                        Text(
                            text = metrics.sunsetTime,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Natural daylight cycle",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp
                    )
                )
            }

            // Rain / Precip Card
            MetricCard(
                title = "PRECIPITATION",
                icon = Icons.Rounded.WaterDrop,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${String.format("%.1f", metrics.precipitationMm)} mm",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (metrics.precipitationMm > 0.0) "Accumulation today" else "No rainfall detected",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Expected dry conditions",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.height(148.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.22f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.75f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column {
                content()
            }
        }
    }
}
