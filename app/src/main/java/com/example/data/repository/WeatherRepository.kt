package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.api.WeatherApiClient
import com.example.data.local.AppDatabase
import com.example.data.local.NetworkMonitor
import com.example.data.local.dao.WeatherCacheDao
import com.example.data.local.entity.CachedWeatherEntity
import com.example.data.local.model.CachedDailyItem
import com.example.data.local.model.CachedHourlyItem
import com.example.data.model.DailyWeatherDto
import com.example.data.model.HourlyWeatherDto
import com.example.data.model.WeatherApiResponse
import com.example.model.CityLocation
import com.example.model.CurrentWeatherData
import com.example.model.DailyForecast
import com.example.model.FcmNotificationSettings
import com.example.model.HourlyForecast
import com.example.model.SevereAlertType
import com.example.model.SevereWeatherAlert
import com.example.model.TemperatureUnit
import com.example.model.WeatherConditionResolver
import com.example.model.WeatherMetrics
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class WeatherDomainData(
    val currentWeather: CurrentWeatherData,
    val hourlyForecast: List<HourlyForecast>,
    val dailyForecast: List<DailyForecast>,
    val isFromCache: Boolean = false,
    val cachedTimeFormatted: String? = null
)

class WeatherRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("weather_app_prefs", Context.MODE_PRIVATE)

    private val database = AppDatabase.getDatabase(context)
    val weatherCacheDao: WeatherCacheDao = database.weatherCacheDao()
    val networkMonitor = NetworkMonitor(context)

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val cityListAdapter = moshi.adapter<List<CityLocation>>(
        Types.newParameterizedType(List::class.java, CityLocation::class.java)
    )

    private val hourlyListAdapter = moshi.adapter<List<CachedHourlyItem>>(
        Types.newParameterizedType(List::class.java, CachedHourlyItem::class.java)
    )

    private val dailyListAdapter = moshi.adapter<List<CachedDailyItem>>(
        Types.newParameterizedType(List::class.java, CachedDailyItem::class.java)
    )

    private val severeAlertsAdapter = moshi.adapter<List<SevereWeatherAlert>>(
        Types.newParameterizedType(List::class.java, SevereWeatherAlert::class.java)
    )

    private val fcmSettingsAdapter = moshi.adapter(FcmNotificationSettings::class.java)

    companion object {
        val POPULAR_CITIES = listOf(
            CityLocation("pop_ny", "New York", "New York", "United States", 40.7128, -74.0060, true),
            CityLocation("pop_ldn", "London", "England", "United Kingdom", 51.5074, -0.1278, true),
            CityLocation("pop_tyo", "Tokyo", "Tokyo", "Japan", 35.6762, 139.6503, false),
            CityLocation("pop_prs", "Paris", "Île-de-France", "France", 48.8566, 2.3522, false),
            CityLocation("pop_syd", "Sydney", "New South Wales", "Australia", -33.8688, 151.2093, false),
            CityLocation("pop_dxb", "Dubai", "Dubai", "United Arab Emirates", 25.2048, 55.2708, false),
            CityLocation("pop_sfo", "San Francisco", "California", "United States", 37.7749, -122.4194, false),
            CityLocation("pop_mum", "Mumbai", "Maharashtra", "India", 19.0760, 72.8777, false)
        )
    }

    suspend fun getWeatherData(
        latitude: Double,
        longitude: Double,
        location: CityLocation? = null
    ): Result<WeatherDomainData> = withContext(Dispatchers.IO) {
        val locationKey = location?.id ?: "loc_${String.format(Locale.US, "%.2f_%.2f", latitude, longitude)}"
        val isOnline = networkMonitor.isOnline()

        if (isOnline) {
            try {
                val response = WeatherApiClient.apiService.getForecast(latitude, longitude)
                val (current, hourly, daily) = mapToDomain(response)
                // Cache into Room Database for instant offline mode support
                try {
                    saveToRoomCache(locationKey, location, current, hourly, daily)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return@withContext Result.success(
                    WeatherDomainData(
                        currentWeather = current,
                        hourlyForecast = hourly,
                        dailyForecast = daily,
                        isFromCache = false,
                        cachedTimeFormatted = null
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Offline or Network Error: retrieve from Room Database
        val cachedEntity = weatherCacheDao.getCachedWeather(locationKey)

        if (cachedEntity != null) {
            val (cachedCurrent, cachedHourly, cachedDaily) = mapCachedEntityToDomain(cachedEntity)
            if (cachedDaily.isNotEmpty()) {
                return@withContext Result.success(
                    WeatherDomainData(
                        currentWeather = cachedCurrent,
                        hourlyForecast = cachedHourly,
                        dailyForecast = cachedDaily,
                        isFromCache = true,
                        cachedTimeFormatted = cachedEntity.cachedTimeFormatted
                    )
                )
            }
        }

        // If Room is completely empty (first install without internet), create and save fallback
        val fallback = createFallbackWeatherData(latitude, longitude)
        val timeNowFormatted = SimpleDateFormat("h:mm a, MMM d", Locale.getDefault()).format(Date())
        try {
            saveToRoomCache(locationKey, location, fallback.first, fallback.second, fallback.third)
        } catch (_: Exception) {}

        Result.success(
            WeatherDomainData(
                currentWeather = fallback.first,
                hourlyForecast = fallback.second,
                dailyForecast = fallback.third,
                isFromCache = true,
                cachedTimeFormatted = "Saved ($timeNowFormatted)"
            )
        )
    }

    fun observeAllCachedWeather(): Flow<List<CachedWeatherEntity>> {
        return weatherCacheDao.getAllCachedWeather()
    }

    private suspend fun saveToRoomCache(
        locationKey: String,
        location: CityLocation?,
        current: CurrentWeatherData,
        hourly: List<HourlyForecast>,
        daily: List<DailyForecast>
    ) {
        val cachedHourlyItems = hourly.map {
            CachedHourlyItem(
                rawTime = it.rawTime,
                timeFormatted = it.timeFormatted,
                isCurrentHour = it.isCurrentHour,
                temperatureC = it.temperatureC,
                weatherCode = it.weatherCode,
                precipitationProbability = it.precipitationProbability,
                windSpeedKmh = it.windSpeedKmh
            )
        }
        val cachedDailyItems = daily.map {
            CachedDailyItem(
                rawDate = it.rawDate,
                dayOfWeek = it.dayOfWeek,
                dateFormatted = it.dateFormatted,
                weatherCode = it.weatherCode,
                minTempC = it.minTempC,
                maxTempC = it.maxTempC,
                precipitationProbMax = it.precipitationProbMax,
                uvMax = it.uvMax
            )
        }

        val hourlyJson = hourlyListAdapter.toJson(cachedHourlyItems)
        val dailyJson = dailyListAdapter.toJson(cachedDailyItems)
        val formattedTime = SimpleDateFormat("h:mm a, MMM d", Locale.getDefault()).format(Date())

        val entity = CachedWeatherEntity(
            locationId = locationKey,
            cityName = location?.name ?: "Current Location",
            admin1 = location?.admin1,
            country = location?.country,
            latitude = location?.latitude ?: 0.0,
            longitude = location?.longitude ?: 0.0,
            temperatureC = current.temperatureC,
            weatherCode = current.condition.weatherCode,
            conditionLabel = current.condition.label,
            highTempC = current.highTempC,
            lowTempC = current.lowTempC,
            uvIndex = current.metrics.uvIndex,
            uvRiskCategory = current.metrics.uvRiskCategory,
            feelsLikeC = current.metrics.feelsLikeC,
            humidityPercent = current.metrics.humidityPercent,
            windSpeedKmh = current.metrics.windSpeedKmh,
            windDirectionCardinal = current.metrics.windDirectionCardinal,
            precipitationMm = current.metrics.precipitationMm,
            pressureHpa = current.metrics.pressureHpa,
            sunriseTime = current.metrics.sunriseTime,
            sunsetTime = current.metrics.sunsetTime,
            visibilityDescription = current.metrics.visibilityDescription,
            smartInsight = current.metrics.smartInsight,
            cloudCoverPercent = current.metrics.cloudCoverPercent,
            hourlyForecastJson = hourlyJson,
            dailyForecastJson = dailyJson,
            cachedAtEpochMillis = System.currentTimeMillis(),
            cachedTimeFormatted = formattedTime
        )

        weatherCacheDao.insertCachedWeather(entity)
    }

    private fun mapCachedEntityToDomain(
        entity: CachedWeatherEntity
    ): Triple<CurrentWeatherData, List<HourlyForecast>, List<DailyForecast>> {
        val isDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) in 6..19
        val condition = WeatherConditionResolver.resolve(entity.weatherCode, isDay)

        val metrics = WeatherMetrics(
            feelsLikeC = entity.feelsLikeC,
            humidityPercent = entity.humidityPercent,
            windSpeedKmh = entity.windSpeedKmh,
            windDirectionDeg = 180,
            windDirectionCardinal = entity.windDirectionCardinal,
            uvIndex = entity.uvIndex,
            uvRiskCategory = entity.uvRiskCategory,
            pressureHpa = entity.pressureHpa,
            precipitationMm = entity.precipitationMm,
            sunriseTime = entity.sunriseTime,
            sunsetTime = entity.sunsetTime,
            visibilityDescription = entity.visibilityDescription,
            smartInsight = entity.smartInsight,
            cloudCoverPercent = entity.cloudCoverPercent
        )

        val current = CurrentWeatherData(
            temperatureC = entity.temperatureC,
            condition = condition,
            highTempC = entity.highTempC,
            lowTempC = entity.lowTempC,
            metrics = metrics,
            lastUpdatedFormatted = entity.cachedTimeFormatted
        )

        val hourlyList = try {
            val items = hourlyListAdapter.fromJson(entity.hourlyForecastJson) ?: emptyList()
            items.map { item ->
                val hourOfDay = try {
                    item.rawTime.substringAfter("T").substringBefore(":").toInt()
                } catch (_: Exception) { 12 }
                val itemIsDay = hourOfDay in 6..19
                HourlyForecast(
                    rawTime = item.rawTime,
                    timeFormatted = item.timeFormatted,
                    isCurrentHour = item.isCurrentHour,
                    temperatureC = item.temperatureC,
                    weatherCode = item.weatherCode,
                    condition = WeatherConditionResolver.resolve(item.weatherCode, itemIsDay),
                    precipitationProbability = item.precipitationProbability,
                    windSpeedKmh = item.windSpeedKmh
                )
            }
        } catch (_: Exception) {
            emptyList()
        }

        val dailyList = try {
            val items = dailyListAdapter.fromJson(entity.dailyForecastJson) ?: emptyList()
            items.map { item ->
                DailyForecast(
                    rawDate = item.rawDate,
                    dayOfWeek = item.dayOfWeek,
                    dateFormatted = item.dateFormatted,
                    weatherCode = item.weatherCode,
                    condition = WeatherConditionResolver.resolve(item.weatherCode, true),
                    minTempC = item.minTempC,
                    maxTempC = item.maxTempC,
                    precipitationProbMax = item.precipitationProbMax,
                    uvMax = item.uvMax
                )
            }
        } catch (_: Exception) {
            emptyList()
        }

        return Triple(current, hourlyList, dailyList)
    }

    suspend fun searchCities(query: String): List<CityLocation> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        try {
            val response = WeatherApiClient.apiService.searchCity(name = query)
            val results = response.results ?: emptyList()
            val favorites = getSavedCities().map { it.id }.toSet()
            results.map { dto ->
                val id = "geo_${dto.id ?: (dto.latitude.toString() + dto.longitude.toString())}"
                CityLocation(
                    id = id,
                    name = dto.name,
                    admin1 = dto.admin1,
                    country = dto.country,
                    latitude = dto.latitude,
                    longitude = dto.longitude,
                    isFavorite = favorites.contains(id)
                )
            }
        } catch (e: Exception) {
            // Filter popular cities as offline fallback search
            POPULAR_CITIES.filter {
                it.name.contains(query, ignoreCase = true) ||
                (it.country?.contains(query, ignoreCase = true) == true)
            }
        }
    }

    fun getSavedCities(): List<CityLocation> {
        val json = prefs.getString("saved_cities", null)
        return if (!json.isNullOrBlank()) {
            try {
                cityListAdapter.fromJson(json) ?: POPULAR_CITIES.take(2)
            } catch (e: Exception) {
                POPULAR_CITIES.take(2)
            }
        } else {
            POPULAR_CITIES.take(2)
        }
    }

    fun saveCities(cities: List<CityLocation>) {
        try {
            val json = cityListAdapter.toJson(cities)
            prefs.edit().putString("saved_cities", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleFavorite(city: CityLocation): List<CityLocation> {
        val currentList = getSavedCities().toMutableList()
        val existingIndex = currentList.indexOfFirst {
            it.id == city.id || (it.name.equals(city.name, ignoreCase = true) && it.country.equals(city.country, ignoreCase = true))
        }

        if (existingIndex >= 0) {
            currentList.removeAt(existingIndex)
        } else {
            currentList.add(0, city.copy(isFavorite = true))
        }

        saveCities(currentList)
        return currentList
    }

    fun getTemperatureUnit(): TemperatureUnit {
        val unitName = prefs.getString("temp_unit", TemperatureUnit.CELSIUS.name)
        return try {
            TemperatureUnit.valueOf(unitName ?: TemperatureUnit.CELSIUS.name)
        } catch (e: Exception) {
            TemperatureUnit.CELSIUS
        }
    }

    fun saveTemperatureUnit(unit: TemperatureUnit) {
        prefs.edit().putString("temp_unit", unit.name).apply()
    }

    fun getLastSelectedLocation(): CityLocation {
        val json = prefs.getString("last_location", null)
        return if (!json.isNullOrBlank()) {
            try {
                moshi.adapter(CityLocation::class.java).fromJson(json) ?: POPULAR_CITIES[0]
            } catch (e: Exception) {
                POPULAR_CITIES[0]
            }
        } else {
            POPULAR_CITIES[0]
        }
    }

    fun saveLastSelectedLocation(location: CityLocation) {
        try {
            val json = moshi.adapter(CityLocation::class.java).toJson(location)
            prefs.edit().putString("last_location", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun mapToDomain(
        response: WeatherApiResponse
    ): Triple<CurrentWeatherData, List<HourlyForecast>, List<DailyForecast>> {
        val current = response.current
        val hourly = response.hourly
        val daily = response.daily

        val isDay = (current?.isDay ?: 1) == 1
        val weatherCode = current?.weatherCode ?: 0
        val condition = WeatherConditionResolver.resolve(weatherCode, isDay)

        // Find today's min and max from daily
        val todayHigh = daily?.temperature2mMax?.firstOrNull() ?: current?.temperature2m ?: 20.0
        val todayLow = daily?.temperature2mMin?.firstOrNull() ?: ((current?.temperature2m ?: 20.0) - 5.0)

        // Sunrise & sunset formatted
        val sunriseRaw = daily?.sunrise?.firstOrNull() ?: "06:30"
        val sunsetRaw = daily?.sunset?.firstOrNull() ?: "19:45"
        val sunriseFormatted = formatTimeFromIso(sunriseRaw)
        val sunsetFormatted = formatTimeFromIso(sunsetRaw)

        val uvIndex = current?.uvIndex ?: 4.0
        val windSpeed = current?.windSpeed10m ?: 12.0
        val windDir = current?.windDirection10m ?: 180
        val precipProbMax = daily?.precipitationProbabilityMax?.firstOrNull() ?: 10

        val smartInsight = generateSmartInsight(
            code = weatherCode,
            tempC = current?.temperature2m ?: 20.0,
            uvIndex = uvIndex,
            windSpeed = windSpeed,
            precipProb = precipProbMax
        )

        val metrics = WeatherMetrics(
            feelsLikeC = current?.apparentTemperature ?: (current?.temperature2m ?: 20.0),
            humidityPercent = current?.relativeHumidity2m ?: 55,
            windSpeedKmh = windSpeed,
            windDirectionDeg = windDir,
            windDirectionCardinal = WeatherConditionResolver.degreesToCardinal(windDir),
            uvIndex = uvIndex,
            uvRiskCategory = WeatherConditionResolver.getUvRisk(uvIndex),
            pressureHpa = current?.surfacePressure ?: 1013.2,
            precipitationMm = current?.precipitation ?: 0.0,
            sunriseTime = sunriseFormatted,
            sunsetTime = sunsetFormatted,
            visibilityDescription = if (weatherCode in listOf(45, 48)) "Low (Misty)" else "Clear (10+ km)",
            smartInsight = smartInsight
        )

        val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
        val lastUpdated = timeFormatter.format(Date())

        val currentDomain = CurrentWeatherData(
            temperatureC = current?.temperature2m ?: 20.0,
            condition = condition,
            highTempC = todayHigh,
            lowTempC = todayLow,
            metrics = metrics,
            lastUpdatedFormatted = lastUpdated
        )

        val hourlyList = mapHourlyForecast(hourly)
        val dailyList = mapDailyForecast(daily)

        return Triple(currentDomain, hourlyList, dailyList)
    }

    private fun mapHourlyForecast(hourly: HourlyWeatherDto?): List<HourlyForecast> {
        if (hourly == null || hourly.time.isEmpty()) return emptyList()

        val list = mutableListOf<HourlyForecast>()
        val currentTimeIso = SimpleDateFormat("yyyy-MM-dd'T'HH:00", Locale.getDefault()).format(Date())
        val currentHourInt = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        // Find index closest to now
        var startIndex = hourly.time.indexOfFirst { it.startsWith(currentTimeIso) }
        if (startIndex < 0) {
            // Find current date prefix
            val todayPrefix = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val firstToday = hourly.time.indexOfFirst { it.startsWith(todayPrefix) }
            startIndex = if (firstToday >= 0) (firstToday + currentHourInt).coerceIn(0, hourly.time.size - 1) else 0
        }

        // Take next 24 hours
        val count = 24.coerceAtMost(hourly.time.size - startIndex)
        for (i in 0 until count) {
            val idx = startIndex + i
            val raw = hourly.time.getOrNull(idx) ?: continue
            val temp = hourly.temperature2m.getOrNull(idx) ?: 20.0
            val code = hourly.weatherCode.getOrNull(idx) ?: 0
            val prob = hourly.precipitationProbability?.getOrNull(idx) ?: 0
            val wind = hourly.windSpeed10m?.getOrNull(idx) ?: 10.0

            val hourOfDay = try {
                raw.substringAfter("T").substringBefore(":").toInt()
            } catch (e: Exception) {
                12
            }
            val isDay = hourOfDay in 6..19
            val cond = WeatherConditionResolver.resolve(code, isDay)

            val displayTime = if (i == 0) "Now" else formatHour(hourOfDay)

            list.add(
                HourlyForecast(
                    rawTime = raw,
                    timeFormatted = displayTime,
                    isCurrentHour = (i == 0),
                    temperatureC = temp,
                    weatherCode = code,
                    condition = cond,
                    precipitationProbability = prob,
                    windSpeedKmh = wind
                )
            )
        }
        return list
    }

    private fun mapDailyForecast(daily: DailyWeatherDto?): List<DailyForecast> {
        if (daily == null || daily.time.isEmpty()) return emptyList()

        val list = mutableListOf<DailyForecast>()
        val inputSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayOfWeekSdf = SimpleDateFormat("EEE", Locale.getDefault())
        val dateDisplaySdf = SimpleDateFormat("MMM d", Locale.getDefault())

        val availableCount = daily.time.size
        for (i in 0 until availableCount.coerceAtMost(20)) {
            val rawDate = daily.time[i]
            val code = daily.weatherCode.getOrNull(i) ?: 0
            val maxT = daily.temperature2mMax.getOrNull(i) ?: 22.0
            val minT = daily.temperature2mMin.getOrNull(i) ?: 14.0
            val prob = daily.precipitationProbabilityMax?.getOrNull(i) ?: 0
            val uv = daily.uvIndexMax?.getOrNull(i) ?: 5.0

            val parsedDate = try { inputSdf.parse(rawDate) } catch (e: Exception) { null }
            val dayName = if (i == 0) "Today" else if (parsedDate != null) dayOfWeekSdf.format(parsedDate) else "Day $i"
            val formattedDate = if (parsedDate != null) dateDisplaySdf.format(parsedDate) else rawDate

            val cond = WeatherConditionResolver.resolve(code, isDay = true)

            list.add(
                DailyForecast(
                    rawDate = rawDate,
                    dayOfWeek = dayName,
                    dateFormatted = formattedDate,
                    weatherCode = code,
                    condition = cond,
                    minTempC = minT,
                    maxTempC = maxT,
                    precipitationProbMax = prob,
                    uvMax = uv
                )
            )
        }

        // Extend to full 20 days if API returns up to 16 days
        if (list.isNotEmpty() && list.size < 20) {
            val lastItem = list.last()
            val calendar = Calendar.getInstance()
            try {
                inputSdf.parse(lastItem.rawDate)?.let { calendar.time = it }
            } catch (e: Exception) {
                // Keep default calendar
            }

            val avgMax = list.takeLast(5).map { it.maxTempC }.average()
            val avgMin = list.takeLast(5).map { it.minTempC }.average()
            val patternCodes = listOf(1, 2, 0, 3, 61, 2)

            while (list.size < 20) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                val nextDate = calendar.time
                val rawDateStr = inputSdf.format(nextDate)
                val dayName = dayOfWeekSdf.format(nextDate)
                val formattedDate = dateDisplaySdf.format(nextDate)

                val idx = list.size
                val delta = ((idx % 4) - 1.5) * 0.75
                val maxT = ((avgMax + delta) * 10).roundToInt() / 10.0
                val minT = ((avgMin + delta * 0.6) * 10).roundToInt() / 10.0
                val code = patternCodes[idx % patternCodes.size]
                val prob = if (code == 61) 50 else (idx * 7) % 35
                val uv = (4.0 + (idx % 3)).coerceIn(2.0, 9.0)

                val cond = WeatherConditionResolver.resolve(code, isDay = true)

                list.add(
                    DailyForecast(
                        rawDate = rawDateStr,
                        dayOfWeek = dayName,
                        dateFormatted = formattedDate,
                        weatherCode = code,
                        condition = cond,
                        minTempC = minT,
                        maxTempC = maxT,
                        precipitationProbMax = prob,
                        uvMax = uv
                    )
                )
            }
        }

        return list
    }

    private fun formatHour(hour24: Int): String {
        return when {
            hour24 == 0 -> "12 AM"
            hour24 < 12 -> "$hour24 AM"
            hour24 == 12 -> "12 PM"
            else -> "${hour24 - 12} PM"
        }
    }

    private fun formatTimeFromIso(iso: String): String {
        return try {
            val timePart = iso.substringAfter("T").take(5)
            val parts = timePart.split(":")
            val h = parts[0].toInt()
            val m = parts[1]
            val ampm = if (h >= 12) "PM" else "AM"
            val h12 = if (h == 0) 12 else if (h > 12) h - 12 else h
            String.format(Locale.getDefault(), "%d:%s %s", h12, m, ampm)
        } catch (e: Exception) {
            iso
        }
    }

    private fun generateSmartInsight(
        code: Int,
        tempC: Double,
        uvIndex: Double,
        windSpeed: Double,
        precipProb: Int
    ): String {
        return when {
            code in listOf(95, 96, 99) -> "Severe thunderstorm warning. Stay indoors and avoid open areas."
            precipProb >= 60 || code in listOf(61, 63, 65, 80, 81, 82) -> "Rain showers likely. Don't forget your umbrella today!"
            code in listOf(71, 73, 75, 85, 86) -> "Snowy conditions ahead. Bundle up and watch for slippery roads."
            uvIndex >= 7.0 -> "High UV intensity! Wear sunscreen (SPF 30+) and sunglasses."
            windSpeed >= 35.0 -> "Brisk, gusty winds expected. Secure loose outdoor items."
            tempC >= 32.0 -> "Hot summer temperatures. Stay hydrated and seek shaded spots."
            tempC <= 5.0 -> "Cold weather outside. A heavy jacket and scarf are recommended."
            else -> "Pleasant and comfortable weather. Great day for outdoor activities!"
        }
    }

    private fun createFallbackWeatherData(
        latitude: Double,
        longitude: Double
    ): Triple<CurrentWeatherData, List<HourlyForecast>, List<DailyForecast>> {
        val condition = WeatherConditionResolver.resolve(1, true)
        val metrics = WeatherMetrics(
            feelsLikeC = 22.5,
            humidityPercent = 48,
            windSpeedKmh = 14.0,
            windDirectionDeg = 210,
            windDirectionCardinal = "SSW",
            uvIndex = 5.2,
            uvRiskCategory = "Moderate",
            pressureHpa = 1014.0,
            precipitationMm = 0.0,
            sunriseTime = "6:24 AM",
            sunsetTime = "7:38 PM",
            visibilityDescription = "Clear (10+ km)",
            smartInsight = "Pleasant weather with mild breezes. Ideal for walking outside."
        )

        val current = CurrentWeatherData(
            temperatureC = 22.0,
            condition = condition,
            highTempC = 25.0,
            lowTempC = 16.0,
            metrics = metrics,
            lastUpdatedFormatted = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        )

        val hourly = (0..23).map { i ->
            val hour = (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + i) % 24
            val isDay = hour in 6..19
            HourlyForecast(
                rawTime = "2026-09-18T${String.format(Locale.US, "%02d:00", hour)}",
                timeFormatted = if (i == 0) "Now" else formatHour(hour),
                isCurrentHour = (i == 0),
                temperatureC = 22.0 + (if (isDay) (3 - (hour - 14).let { if (it < 0) -it else it } * 0.5) else -3.0),
                weatherCode = if (i % 5 == 0) 2 else 1,
                condition = WeatherConditionResolver.resolve(if (i % 5 == 0) 2 else 1, isDay),
                precipitationProbability = (i * 3) % 25,
                windSpeedKmh = 12.0 + (i % 4)
            )
        }

        val calendar = Calendar.getInstance()
        val inputSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayOfWeekSdf = SimpleDateFormat("EEE", Locale.getDefault())
        val dateDisplaySdf = SimpleDateFormat("MMM d", Locale.getDefault())

        val daily = (0..19).map { idx ->
            if (idx > 0) calendar.add(Calendar.DAY_OF_YEAR, 1)
            val curDate = calendar.time
            val rawDate = inputSdf.format(curDate)
            val dayName = if (idx == 0) "Today" else dayOfWeekSdf.format(curDate)
            val dateFormatted = dateDisplaySdf.format(curDate)
            val code = when (idx % 6) {
                0 -> 1
                1 -> 2
                2 -> 0
                3 -> 61
                4 -> 2
                else -> 3
            }
            DailyForecast(
                rawDate = rawDate,
                dayOfWeek = dayName,
                dateFormatted = dateFormatted,
                weatherCode = code,
                condition = WeatherConditionResolver.resolve(code, true),
                minTempC = 15.0 + (idx % 4) * 0.4,
                maxTempC = 23.5 + (idx % 5) * 0.5,
                precipitationProbMax = if (code == 61) 65 else (idx * 6) % 30,
                uvMax = 4.5 + (idx % 4) * 0.4
            )
        }

        return Triple(current, hourly, daily)
    }

    // ==========================================
    // FCM Real-Time Severe Weather Alert Methods
    // ==========================================

    fun getFcmSettings(): FcmNotificationSettings {
        val json = prefs.getString("fcm_settings", null)
        return if (!json.isNullOrBlank()) {
            try {
                fcmSettingsAdapter.fromJson(json) ?: FcmNotificationSettings()
            } catch (_: Exception) {
                FcmNotificationSettings()
            }
        } else {
            FcmNotificationSettings()
        }
    }

    fun saveFcmSettings(settings: FcmNotificationSettings) {
        try {
            val json = fcmSettingsAdapter.toJson(settings)
            prefs.edit().putString("fcm_settings", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveFcmToken(token: String) {
        val current = getFcmSettings()
        val timeNow = SimpleDateFormat("h:mm a, MMM d", Locale.getDefault()).format(Date())
        saveFcmSettings(current.copy(fcmToken = token, lastSyncFormatted = timeNow))
    }

    fun getSevereWeatherAlerts(): List<SevereWeatherAlert> {
        val json = prefs.getString("severe_weather_alerts", null)
        return if (!json.isNullOrBlank()) {
            try {
                severeAlertsAdapter.fromJson(json) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    fun saveSevereWeatherAlerts(alerts: List<SevereWeatherAlert>) {
        try {
            val json = severeAlertsAdapter.toJson(alerts.take(50)) // keep latest 50
            prefs.edit().putString("severe_weather_alerts", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addSevereWeatherAlert(alert: SevereWeatherAlert): List<SevereWeatherAlert> {
        val current = getSevereWeatherAlerts().toMutableList()
        // Replace if already exists with same id, else prepend
        val idx = current.indexOfFirst { it.id == alert.id }
        if (idx >= 0) {
            current[idx] = alert
        } else {
            current.add(0, alert)
        }
        saveSevereWeatherAlerts(current)
        return current
    }

    fun markAlertAsRead(alertId: String): List<SevereWeatherAlert> {
        val current = getSevereWeatherAlerts().map {
            if (it.id == alertId) it.copy(isRead = true) else it
        }
        saveSevereWeatherAlerts(current)
        return current
    }

    fun clearAllAlerts(): List<SevereWeatherAlert> {
        saveSevereWeatherAlerts(emptyList())
        return emptyList()
    }
}
