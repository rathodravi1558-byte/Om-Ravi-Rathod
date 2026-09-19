package com.example.model

enum class SevereAlertType(val label: String, val defaultSeverity: String) {
    TORNADO_WARNING("Tornado Warning", "EXTREME"),
    HURRICANE_WARNING("Hurricane / Cyclone Warning", "EXTREME"),
    SEVERE_THUNDERSTORM("Severe Thunderstorm Warning", "SEVERE"),
    FLASH_FLOOD("Flash Flood Warning", "SEVERE"),
    BLIZZARD_WARNING("Blizzard / Heavy Snow Warning", "SEVERE"),
    EXTREME_HEAT("Extreme Heat Advisory", "MODERATE"),
    DENSE_FOG("Dense Fog Advisory", "MODERATE"),
    HIGH_WIND("High Wind Warning", "MODERATE"),
    TEST_ALERT("Meteorological Test Alert", "INFO")
}

data class SevereWeatherAlert(
    val id: String,
    val locationId: String,
    val locationName: String,
    val alertType: SevereAlertType,
    val headline: String,
    val description: String,
    val severity: String, // EXTREME, SEVERE, MODERATE, INFO
    val instruction: String,
    val issuedAtEpochMillis: Long = System.currentTimeMillis(),
    val issuedAtFormatted: String,
    val expiresAtFormatted: String,
    val isRead: Boolean = false
)

data class FcmNotificationSettings(
    val isFcmEnabled: Boolean = true,
    val fcmToken: String? = null,
    val notifySavedLocationsOnly: Boolean = true,
    val notifyCurrentLocation: Boolean = true,
    val alertSeverityThreshold: String = "ALL", // ALL, SEVERE_AND_EXTREME, EXTREME_ONLY
    val soundAndVibrate: Boolean = true,
    val subscribedTopics: List<String> = emptyList(),
    val lastSyncFormatted: String? = null
)
