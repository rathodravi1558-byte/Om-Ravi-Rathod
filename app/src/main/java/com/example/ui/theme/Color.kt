package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.model.ConditionType

// Default theme colors
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Core Theme Accents
val WeatherSkyBlue = Color(0xFF0077E6)
val WeatherDeepNavy = Color(0xFF0B192C)
val WeatherSunGold = Color(0xFFFFB300)
val WeatherRainBlue = Color(0xFF2962FF)
val WeatherCloudSlate = Color(0xFF475569)

// Surface & Card tints
val GlassCardBackgroundLight = Color(0xFFFFFFFF).copy(alpha = 0.88f)
val GlassCardBackgroundDark = Color(0xFF1E293B).copy(alpha = 0.85f)
val GlassCardBorder = Color(0xFFFFFFFF).copy(alpha = 0.25f)

// UV Risk Badge Colors
val UvLow = Color(0xFF2E7D32)
val UvModerate = Color(0xFFF57F17)
val UvHigh = Color(0xFFE65100)
val UvVeryHigh = Color(0xFFC2185B)
val UvExtreme = Color(0xFF6A1B9A)

object WeatherGradients {
    val ClearDay = listOf(
        Color(0xFF1E88E5), // Vibrant Azure
        Color(0xFF42A5F5), // Sky Blue
        Color(0xFF90CAF9)  // Soft Blue
    )

    val ClearNight = listOf(
        Color(0xFF0A1128), // Deepest Midnight
        Color(0xFF1C2541), // Starlit Navy
        Color(0xFF3A506B)  // Steel Blue
    )

    val PartlyCloudyDay = listOf(
        Color(0xFF2B5876),
        Color(0xFF4E6572),
        Color(0xFF78909C)
    )

    val PartlyCloudyNight = listOf(
        Color(0xFF0F2027),
        Color(0xFF203A43),
        Color(0xFF2C5364)
    )

    val OvercastCloudy = listOf(
        Color(0xFF37474F),
        Color(0xFF546E7A),
        Color(0xFF78909C)
    )

    val Rainy = listOf(
        Color(0xFF1A2A3A),
        Color(0xFF2C3E50),
        Color(0xFF3F5870)
    )

    val Thunderstorm = listOf(
        Color(0xFF121826),
        Color(0xFF1F293D),
        Color(0xFF312E4A)
    )

    val Snow = listOf(
        Color(0xFF3949AB),
        Color(0xFF5C6BC0),
        Color(0xFF9FA8DA)
    )

    val Fog = listOf(
        Color(0xFF455A64),
        Color(0xFF607D8B),
        Color(0xFF90A4AE)
    )

    fun getGradient(conditionType: ConditionType): List<Color> {
        return when (conditionType) {
            ConditionType.CLEAR_DAY -> ClearDay
            ConditionType.CLEAR_NIGHT -> ClearNight
            ConditionType.PARTLY_CLOUDY_DAY -> PartlyCloudyDay
            ConditionType.PARTLY_CLOUDY_NIGHT -> PartlyCloudyNight
            ConditionType.CLOUDY -> OvercastCloudy
            ConditionType.FOG -> Fog
            ConditionType.DRIZZLE, ConditionType.RAIN, ConditionType.HEAVY_RAIN -> Rainy
            ConditionType.SNOW -> Snow
            ConditionType.THUNDERSTORM -> Thunderstorm
        }
    }
}
