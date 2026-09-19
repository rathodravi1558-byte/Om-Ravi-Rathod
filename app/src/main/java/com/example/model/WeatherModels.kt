package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbCloudy
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.Locale
import kotlin.math.roundToInt

enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT;

    fun convert(tempC: Double): Double {
        return when (this) {
            CELSIUS -> tempC
            FAHRENHEIT -> (tempC * 9.0 / 5.0) + 32.0
        }
    }

    fun format(tempC: Double): String {
        val converted = convert(tempC).roundToInt()
        return "$converted°"
    }

    fun formatWithUnit(tempC: Double): String {
        val converted = convert(tempC).roundToInt()
        val unitLabel = if (this == CELSIUS) "°C" else "°F"
        return "$converted$unitLabel"
    }

    fun symbol(): String = if (this == CELSIUS) "°C" else "°F"
}

enum class ConditionType {
    CLEAR_DAY,
    CLEAR_NIGHT,
    PARTLY_CLOUDY_DAY,
    PARTLY_CLOUDY_NIGHT,
    CLOUDY,
    FOG,
    DRIZZLE,
    RAIN,
    HEAVY_RAIN,
    SNOW,
    THUNDERSTORM
}

data class WeatherConditionInfo(
    val weatherCode: Int,
    val isDay: Boolean,
    val label: String,
    val description: String,
    val icon: ImageVector,
    val conditionType: ConditionType
)

data class CityLocation(
    val id: String,
    val name: String,
    val admin1: String? = null,
    val country: String? = null,
    val latitude: Double,
    val longitude: Double,
    val isFavorite: Boolean = false
) {
    val subtitle: String
        get() = listOfNotNull(admin1, country).joinToString(", ")
}

data class HourlyForecast(
    val rawTime: String,
    val timeFormatted: String,
    val isCurrentHour: Boolean,
    val temperatureC: Double,
    val weatherCode: Int,
    val condition: WeatherConditionInfo,
    val precipitationProbability: Int,
    val windSpeedKmh: Double
)

data class DailyForecast(
    val rawDate: String,
    val dayOfWeek: String,
    val dateFormatted: String,
    val weatherCode: Int,
    val condition: WeatherConditionInfo,
    val minTempC: Double,
    val maxTempC: Double,
    val precipitationProbMax: Int,
    val uvMax: Double
)

data class WeatherMetrics(
    val feelsLikeC: Double,
    val humidityPercent: Int,
    val windSpeedKmh: Double,
    val windDirectionDeg: Int,
    val windDirectionCardinal: String,
    val uvIndex: Double,
    val uvRiskCategory: String,
    val pressureHpa: Double,
    val precipitationMm: Double,
    val sunriseTime: String,
    val sunsetTime: String,
    val visibilityDescription: String,
    val smartInsight: String,
    val cloudCoverPercent: Int = 45
)

data class CurrentWeatherData(
    val temperatureC: Double,
    val condition: WeatherConditionInfo,
    val highTempC: Double,
    val lowTempC: Double,
    val metrics: WeatherMetrics,
    val lastUpdatedFormatted: String
)

enum class MapOverlayType {
    PRECIPITATION,
    TEMPERATURE
}

enum class MapBasemapStyle {
    GOOGLE_SATELLITE,
    METEOROLOGICAL_DARK,
    SATELLITE_NIGHT,
    TOPOGRAPHIC
}

enum class AuthProvider(val displayName: String) {
    GOOGLE("Google Account"),
    APPLE("Apple ID"),
    TWITTER("Twitter / X"),
    EMAIL("Email & Password")
}

data class UserAccountProfile(
    val id: String = "user_default",
    val displayName: String = "Ravi Rathod",
    val email: String = "rathodravi1558@gmail.com",
    val provider: AuthProvider = AuthProvider.GOOGLE,
    val avatarUrl: String? = null,
    val isVerified: Boolean = true,
    val isEmailAlertsEnabled: Boolean = true,
    val alertFrequency: String = "Every Condition Shift",
    val rainRateThresholdMm: Double = 1.5,
    val severeStormAlert: Boolean = true,
    val extremeUvAlert: Boolean = true,
    val lastAlertSentFormatted: String? = "Live updates synchronized"
)

enum class ObservationIntervalPreset(
    val label: String,
    val durationSeconds: Int,
    val description: String
) {
    SEC_30("30s", 30, "Rapid cloud drift check"),
    MIN_1("1m", 60, "1 minute dynamic scan"),
    MIN_2("2m", 120, "2 minutes convective evolution"),
    MIN_10("10m", 600, "10 minutes front tracking"),
    MIN_12("12m", 720, "12 minutes Doppler sync"),
    MIN_20("20m", 1200, "20 minutes microburst monitor"),
    MIN_50("50m", 3000, "50 minutes storm cell track"),
    HOUR_1("1h", 3600, "1 hour macro observation"),
    HOUR_2("2h", 7200, "2 hours atmospheric timelapse")
}

data class VideoObservationResult(
    val interval: ObservationIntervalPreset,
    val framesCaptured: Int,
    val cloudSpeedKmh: Double,
    val cloudHeadingCardinal: String,
    val barometricTrend: String,
    val rainRatePredictionMmPerHour: Double,
    val conditionShiftProbabilityPercent: Int,
    val summaryReport: String,
    val recordedAtFormatted: String
)

enum class DiagnosticStatus {
    HEALTHY,
    WARNING,
    RESOLVED,
    FIXING
}

data class DiagnosticItem(
    val id: String,
    val title: String,
    val status: DiagnosticStatus,
    val latencyMs: Long,
    val detail: String
)

data class DeveloperAlert(
    val id: String,
    val title: String,
    val message: String,
    val timestampFormatted: String,
    val severity: String = "INFO",
    val isAutoFixAvailable: Boolean = true
)

data class InspectedMapPoint(
    val latitude: Double,
    val longitude: Double,
    val temperatureC: Double,
    val precipitationMm: Double,
    val distanceKm: Double,
    val bearing: String,
    val conditionLabel: String
)

data class WeatherUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val currentLocation: CityLocation = CityLocation(
        id = "default_ny",
        name = "New York",
        admin1 = "New York",
        country = "United States",
        latitude = 40.7128,
        longitude = -74.0060,
        isFavorite = true
    ),
    val currentWeather: CurrentWeatherData? = null,
    val hourlyForecast: List<HourlyForecast> = emptyList(),
    val dailyForecast: List<DailyForecast> = emptyList(),
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val savedCities: List<CityLocation> = emptyList(),
    val searchResults: List<CityLocation> = emptyList(),
    val isSearching: Boolean = false,
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val cloudAnalysis: CloudAnalysisResult? = null,
    val showSkyPhotoSheet: Boolean = false,
    val showVoiceSearchSheet: Boolean = false,
    val voiceFeedbackMessage: String? = null,
    val activeMapOverlay: MapOverlayType = MapOverlayType.PRECIPITATION,
    val mapBasemapStyle: MapBasemapStyle = MapBasemapStyle.METEOROLOGICAL_DARK,
    val showExpandedMapModal: Boolean = false,
    val inspectedMapPoint: InspectedMapPoint? = null,
    val isOffline: Boolean = false,
    val isUsingCachedData: Boolean = false,
    val cachedTimestampFormatted: String? = null,
    val isSatelliteRadarOnly: Boolean = false,
    val userProfile: UserAccountProfile = UserAccountProfile(),
    val showAccountAuthSheet: Boolean = false,
    val showDeveloperDiagnosticsSheet: Boolean = false,
    val activeDeveloperAlert: DeveloperAlert? = null,
    val selectedObservationInterval: ObservationIntervalPreset = ObservationIntervalPreset.MIN_1,
    val lastVideoObservation: VideoObservationResult? = null,
    val severeWeatherAlerts: List<SevereWeatherAlert> = emptyList(),
    val fcmSettings: FcmNotificationSettings = FcmNotificationSettings(),
    val showSevereAlertsSheet: Boolean = false,
    val selectedSevereAlert: SevereWeatherAlert? = null
)

object WeatherConditionResolver {
    fun resolve(code: Int, isDay: Boolean = true): WeatherConditionInfo {
        return when (code) {
            0 -> if (isDay) {
                WeatherConditionInfo(
                    weatherCode = code,
                    isDay = true,
                    label = "Clear Sky",
                    description = "Completely clear and sunny",
                    icon = Icons.Rounded.WbSunny,
                    conditionType = ConditionType.CLEAR_DAY
                )
            } else {
                WeatherConditionInfo(
                    weatherCode = code,
                    isDay = false,
                    label = "Clear Sky",
                    description = "Starlit clear night",
                    icon = Icons.Rounded.NightsStay,
                    conditionType = ConditionType.CLEAR_NIGHT
                )
            }
            1 -> if (isDay) {
                WeatherConditionInfo(
                    weatherCode = code,
                    isDay = true,
                    label = "Mainly Clear",
                    description = "Mostly sunny with slight clouds",
                    icon = Icons.Rounded.WbSunny,
                    conditionType = ConditionType.CLEAR_DAY
                )
            } else {
                WeatherConditionInfo(
                    weatherCode = code,
                    isDay = false,
                    label = "Mainly Clear",
                    description = "Fair night skies",
                    icon = Icons.Rounded.NightsStay,
                    conditionType = ConditionType.CLEAR_NIGHT
                )
            }
            2 -> if (isDay) {
                WeatherConditionInfo(
                    weatherCode = code,
                    isDay = true,
                    label = "Partly Cloudy",
                    description = "Scattered clouds with sunshine",
                    icon = Icons.Rounded.CloudQueue,
                    conditionType = ConditionType.PARTLY_CLOUDY_DAY
                )
            } else {
                WeatherConditionInfo(
                    weatherCode = code,
                    isDay = false,
                    label = "Partly Cloudy",
                    description = "Clouds passing through",
                    icon = Icons.Rounded.NightsStay,
                    conditionType = ConditionType.PARTLY_CLOUDY_NIGHT
                )
            }
            3 -> WeatherConditionInfo(
                weatherCode = code,
                isDay = isDay,
                label = "Overcast",
                description = "Dense cloud cover",
                icon = Icons.Rounded.WbCloudy,
                conditionType = ConditionType.CLOUDY
            )
            45, 48 -> WeatherConditionInfo(
                weatherCode = code,
                isDay = isDay,
                label = "Foggy",
                description = "Misty haze and reduced visibility",
                icon = Icons.Rounded.Air,
                conditionType = ConditionType.FOG
            )
            51, 53, 55 -> WeatherConditionInfo(
                weatherCode = code,
                isDay = isDay,
                label = "Drizzle",
                description = "Light gentle misting rain",
                icon = Icons.Rounded.WaterDrop,
                conditionType = ConditionType.DRIZZLE
            )
            56, 57 -> WeatherConditionInfo(
                weatherCode = code,
                isDay = isDay,
                label = "Freezing Drizzle",
                description = "Icy drizzle conditions",
                icon = Icons.Rounded.AcUnit,
                conditionType = ConditionType.DRIZZLE
            )
            61, 63 -> WeatherConditionInfo(
                weatherCode = code,
                isDay = isDay,
                label = "Rain",
                description = "Light to moderate showers",
                icon = Icons.Rounded.Grain,
                conditionType = ConditionType.RAIN
            )
            65 -> WeatherConditionInfo(
                weatherCode = code,
                isDay = isDay,
                label = "Heavy Rain",
                description = "Steady downpours expected",
                icon = Icons.Rounded.WaterDrop,
                conditionType = ConditionType.HEAVY_RAIN
            )
            66, 67 -> WeatherConditionInfo(
                weatherCode = code,
                isDay = isDay,
                label = "Freezing Rain",
                description = "Cold rain freezing on contact",
                icon = Icons.Rounded.AcUnit,
                conditionType = ConditionType.RAIN
            )
            71, 73, 75, 77 -> WeatherConditionInfo(
                weatherCode = code,
                isDay = isDay,
                label = "Snow",
                description = "Snowfall and flurries",
                icon = Icons.Rounded.AcUnit,
                conditionType = ConditionType.SNOW
            )
            80, 81, 82 -> WeatherConditionInfo(
                weatherCode = code,
                isDay = isDay,
                label = "Rain Showers",
                description = "Intermittent rain showers",
                icon = Icons.Rounded.Grain,
                conditionType = ConditionType.RAIN
            )
            85, 86 -> WeatherConditionInfo(
                weatherCode = code,
                isDay = isDay,
                label = "Snow Showers",
                description = "Brisk snow showers",
                icon = Icons.Rounded.AcUnit,
                conditionType = ConditionType.SNOW
            )
            95, 96, 99 -> WeatherConditionInfo(
                weatherCode = code,
                isDay = isDay,
                label = "Thunderstorm",
                description = "Lightning, thunder and gusts",
                icon = Icons.Rounded.Thunderstorm,
                conditionType = ConditionType.THUNDERSTORM
            )
            else -> WeatherConditionInfo(
                weatherCode = code,
                isDay = isDay,
                label = "Partly Cloudy",
                description = "Variable conditions",
                icon = Icons.Rounded.Cloud,
                conditionType = ConditionType.CLOUDY
            )
        }
    }

    fun getUvRisk(uvIndex: Double): String {
        return when {
            uvIndex < 3.0 -> "Low"
            uvIndex < 6.0 -> "Moderate"
            uvIndex < 8.0 -> "High"
            uvIndex < 11.0 -> "Very High"
            else -> "Extreme"
        }
    }

    fun degreesToCardinal(degrees: Int): String {
        val directions = arrayOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
        val index = (((degrees % 360) + 360) % 360 / 22.5 + 0.5).toInt() % 16
        return directions[index]
    }

    fun getUvSafetyRecommendation(uvIndex: Double): UvSafetyRecommendation {
        return when {
            uvIndex < 3.0 -> UvSafetyRecommendation(
                riskLevel = "Low",
                recommendation = "Minimal sun danger. Safe to enjoy outdoor activities without special precautions.",
                detailedAdvice = "Wear sunglasses on bright days. Sun protection is generally not required for most skin types.",
                peakHoursWarning = "Safe during all daylight hours",
                needsSunscreen = false,
                needsSunglasses = true,
                needsHat = false,
                needsShade = false
            )
            uvIndex < 6.0 -> UvSafetyRecommendation(
                riskLevel = "Moderate",
                recommendation = "Take precautions: apply broad-spectrum SPF 30+ sunscreen and wear a hat.",
                detailedAdvice = "Cover up with light clothing, wear UV-blocking sunglasses, and seek shade during peak midday sun.",
                peakHoursWarning = "Seek shade between 10:00 AM – 4:00 PM",
                needsSunscreen = true,
                needsSunglasses = true,
                needsHat = true,
                needsShade = false
            )
            uvIndex < 8.0 -> UvSafetyRecommendation(
                riskLevel = "High",
                recommendation = "Protection essential: reduce sun exposure and apply SPF 30+ sunscreen generously.",
                detailedAdvice = "Unprotected skin can burn quickly. Wear a wide-brimmed hat, UV400 sunglasses, and reapply sunscreen every 2 hours.",
                peakHoursWarning = "Limit midday sun exposure (10:00 AM – 4:00 PM)",
                needsSunscreen = true,
                needsSunglasses = true,
                needsHat = true,
                needsShade = true
            )
            uvIndex < 11.0 -> UvSafetyRecommendation(
                riskLevel = "Very High",
                recommendation = "Extra protection required: skin burns rapidly without shade and sunscreen.",
                detailedAdvice = "Avoid direct sun exposure around midday. Wear protective clothing, SPF 50+ sunscreen, and sunglasses.",
                peakHoursWarning = "Stay in shade during peak hours (10:00 AM – 4:00 PM)",
                needsSunscreen = true,
                needsSunglasses = true,
                needsHat = true,
                needsShade = true
            )
            else -> UvSafetyRecommendation(
                riskLevel = "Extreme",
                recommendation = "Take all precautions: unprotected skin can burn in minutes. Avoid direct sun.",
                detailedAdvice = "White sand, water, and reflective surfaces intensify exposure. Stay indoors or under deep shade during peak hours.",
                peakHoursWarning = "Avoid outdoor sun exposure (10:00 AM – 4:00 PM)",
                needsSunscreen = true,
                needsSunglasses = true,
                needsHat = true,
                needsShade = true
            )
        }
    }
}

data class UvSafetyRecommendation(
    val riskLevel: String,
    val recommendation: String,
    val detailedAdvice: String,
    val peakHoursWarning: String,
    val needsSunscreen: Boolean,
    val needsSunglasses: Boolean,
    val needsHat: Boolean,
    val needsShade: Boolean
)
