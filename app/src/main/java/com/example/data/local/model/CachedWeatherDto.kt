package com.example.data.local.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CachedHourlyItem(
    val rawTime: String,
    val timeFormatted: String,
    val isCurrentHour: Boolean,
    val temperatureC: Double,
    val weatherCode: Int,
    val precipitationProbability: Int,
    val windSpeedKmh: Double
)

@JsonClass(generateAdapter = true)
data class CachedDailyItem(
    val rawDate: String,
    val dayOfWeek: String,
    val dateFormatted: String,
    val weatherCode: Int,
    val minTempC: Double,
    val maxTempC: Double,
    val precipitationProbMax: Int,
    val uvMax: Double
)
