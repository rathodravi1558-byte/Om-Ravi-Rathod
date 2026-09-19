package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing cached meteorological data for a specific location.
 * Used to provide offline mode functionality when network connectivity is lost.
 */
@Entity(tableName = "cached_weather")
data class CachedWeatherEntity(
    @PrimaryKey
    val locationId: String,
    val cityName: String,
    val admin1: String?,
    val country: String?,
    val latitude: Double,
    val longitude: Double,
    val temperatureC: Double,
    val weatherCode: Int,
    val conditionLabel: String,
    val highTempC: Double,
    val lowTempC: Double,
    val uvIndex: Double,
    val uvRiskCategory: String,
    val feelsLikeC: Double,
    val humidityPercent: Int,
    val windSpeedKmh: Double,
    val windDirectionCardinal: String,
    val precipitationMm: Double,
    val pressureHpa: Double,
    val sunriseTime: String,
    val sunsetTime: String,
    val visibilityDescription: String,
    val smartInsight: String,
    val cloudCoverPercent: Int,
    val hourlyForecastJson: String,
    val dailyForecastJson: String,
    val cachedAtEpochMillis: Long = System.currentTimeMillis(),
    val cachedTimeFormatted: String
)
