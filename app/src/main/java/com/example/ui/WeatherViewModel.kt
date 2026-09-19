package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.WeatherRepository
import com.example.model.CityLocation
import com.example.model.CloudAnalysisResult
import com.example.model.DeveloperAlert
import com.example.model.FcmNotificationSettings
import com.example.model.InspectedMapPoint
import com.example.model.MapBasemapStyle
import com.example.model.MapOverlayType
import com.example.model.ObservationIntervalPreset
import com.example.model.SevereAlertType
import com.example.model.SevereWeatherAlert
import com.example.model.TemperatureUnit
import com.example.model.UserAccountProfile
import com.example.model.VideoObservationResult
import com.example.model.WeatherUiState
import com.example.service.FcmSubscriptionManager
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WeatherRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        val savedUnit = repository.getTemperatureUnit()
        val savedCities = repository.getSavedCities()
        val initialLocation = repository.getLastSelectedLocation()
        val fcmSettings = repository.getFcmSettings()
        val initialAlerts = repository.getSevereWeatherAlerts()

        _uiState.update {
            it.copy(
                temperatureUnit = savedUnit,
                savedCities = savedCities,
                isOffline = !repository.networkMonitor.isOnline(),
                fcmSettings = fcmSettings,
                severeWeatherAlerts = initialAlerts,
                currentLocation = initialLocation.copy(
                    isFavorite = savedCities.any { fav -> fav.name.equals(initialLocation.name, ignoreCase = true) }
                )
            )
        }

        // Initialize and sync FCM tokens and subscriptions
        viewModelScope.launch {
            try {
                val token = FcmSubscriptionManager.fetchFcmToken()
                if (!token.isNullOrBlank()) {
                    repository.saveFcmToken(token)
                }
                val activeTopics = FcmSubscriptionManager.syncTopicSubscriptions(
                    application.applicationContext,
                    savedCities
                )
                _uiState.update { current ->
                    current.copy(
                        fcmSettings = repository.getFcmSettings().copy(
                            subscribedTopics = activeTopics
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Monitor network connectivity changes for offline mode
        viewModelScope.launch {
            repository.networkMonitor.isOnlineFlow.collect { online ->
                val wasOffline = _uiState.value.isOffline
                _uiState.update { it.copy(isOffline = !online) }
                if (online && wasOffline) {
                    // Automatically refresh when reconnected
                    loadWeatherForLocation(_uiState.value.currentLocation, isRefresh = true)
                }
            }
        }

        loadWeatherForLocation(initialLocation)
    }

    fun refresh() {
        val location = _uiState.value.currentLocation
        loadWeatherForLocation(location, isRefresh = true)
    }

    fun retryConnection() {
        refresh()
    }

    fun selectCity(location: CityLocation) {
        val saved = _uiState.value.savedCities
        val isFav = saved.any { it.name.equals(location.name, ignoreCase = true) }
        val updatedLoc = location.copy(isFavorite = isFav)

        repository.saveLastSelectedLocation(updatedLoc)
        _uiState.update {
            it.copy(
                currentLocation = updatedLoc,
                searchQuery = "",
                searchResults = emptyList(),
                isSearching = false
            )
        }
        loadWeatherForLocation(updatedLoc)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()

        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            delay(350) // Debounce user typing
            val results = repository.searchCities(query.trim())
            _uiState.update {
                it.copy(
                    searchResults = results,
                    isSearching = false
                )
            }
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _uiState.update {
            it.copy(
                searchQuery = "",
                searchResults = emptyList(),
                isSearching = false
            )
        }
    }

    fun toggleFavorite(location: CityLocation = _uiState.value.currentLocation) {
        val updatedFavorites = repository.toggleFavorite(location)
        val isCurrentFav = updatedFavorites.any { it.name.equals(location.name, ignoreCase = true) }

        _uiState.update {
            it.copy(
                savedCities = updatedFavorites,
                currentLocation = if (it.currentLocation.name.equals(location.name, ignoreCase = true)) {
                    it.currentLocation.copy(isFavorite = isCurrentFav)
                } else {
                    it.currentLocation
                }
            )
        }

        // Resync FCM topic subscriptions with the updated saved locations
        viewModelScope.launch {
            try {
                val activeTopics = FcmSubscriptionManager.syncTopicSubscriptions(
                    getApplication<Application>().applicationContext,
                    updatedFavorites
                )
                _uiState.update {
                    it.copy(
                        fcmSettings = it.fcmSettings.copy(subscribedTopics = activeTopics)
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleTemperatureUnit() {
        val newUnit = if (_uiState.value.temperatureUnit == TemperatureUnit.CELSIUS) {
            TemperatureUnit.FAHRENHEIT
        } else {
            TemperatureUnit.CELSIUS
        }
        repository.saveTemperatureUnit(newUnit)
        _uiState.update { it.copy(temperatureUnit = newUnit) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun openVoiceSearchSheet() {
        _uiState.update { it.copy(showVoiceSearchSheet = true, voiceFeedbackMessage = null) }
    }

    fun closeVoiceSearchSheet() {
        _uiState.update { it.copy(showVoiceSearchSheet = false, voiceFeedbackMessage = null) }
    }

    /**
     * Parses spoken text (e.g. "What's the weather in Tokyo", "Delhi weather", "Mumbai", "मौसम मुंबई में")
     * into a clean location query, searches matching locations, and automatically updates the weather.
     */
    fun processVoiceSpokenLocation(spokenText: String) {
        val cleanLocation = extractLocationFromSpeech(spokenText)
        if (cleanLocation.isBlank()) {
            _uiState.update {
                it.copy(voiceFeedbackMessage = "Couldn't detect location name from '$spokenText'. Please try saying a city name.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    voiceFeedbackMessage = "Searching weather for '$cleanLocation'…"
                )
            }

            val results = repository.searchCities(cleanLocation)
            if (results.isNotEmpty()) {
                val matchedCity = results.first()
                selectCity(matchedCity)
                _uiState.update {
                    it.copy(
                        showVoiceSearchSheet = false,
                        voiceFeedbackMessage = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        voiceFeedbackMessage = "No location found matching '$cleanLocation'. Please try another city."
                    )
                }
            }
        }
    }

    private fun extractLocationFromSpeech(raw: String): String {
        var query = raw.trim()
        
        // Remove punctuation
        query = query.replace("?", "").replace(".", "").replace("!", "").replace(",", "")

        // Common English & Hindi prefixes/suffixes for weather inquiries
        val patterns = listOf(
            Regex("(?i)^(what is the weather in|weather in|how is the weather in|what's the weather in|check weather in|weather for|tell me weather of|weather at|weather of|temperature in)\\s+"),
            Regex("(?i)\\s+(weather|forecast|temperature|climate)$"),
            Regex("(?i)^(का मौसम|मौसम)\\s+"),
            Regex("(?i)\\s+(का मौसम|में मौसम|का तापमान|का वेदर|मौसम)$")
        )

        for (pat in patterns) {
            query = query.replace(pat, "").trim()
        }

        return query.ifBlank { raw.trim() }
    }

    fun openSkyPhotoSheet() {
        _uiState.update { it.copy(showSkyPhotoSheet = true) }
    }

    fun closeSkyPhotoSheet() {
        _uiState.update { it.copy(showSkyPhotoSheet = false) }
    }

    fun applyCloudAnalysis(result: CloudAnalysisResult) {
        _uiState.update {
            it.copy(
                cloudAnalysis = result,
                showSkyPhotoSheet = false
            )
        }
    }

    fun dismissRedCloudAlert() {
        _uiState.update {
            val cur = it.cloudAnalysis ?: return@update it
            it.copy(
                cloudAnalysis = cur.copy(isRedCloudAlert = false)
            )
        }
    }

    private fun loadWeatherForLocation(location: CityLocation, isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                if (isRefresh) it.copy(isRefreshing = true)
                else it.copy(isLoading = true, errorMessage = null)
            }

            val result = repository.getWeatherData(location.latitude, location.longitude, location)
            result.onSuccess { domainData ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        currentWeather = domainData.currentWeather,
                        hourlyForecast = domainData.hourlyForecast,
                        dailyForecast = domainData.dailyForecast,
                        isUsingCachedData = domainData.isFromCache,
                        cachedTimestampFormatted = domainData.cachedTimeFormatted,
                        isOffline = !repository.networkMonitor.isOnline(),
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isOffline = !repository.networkMonitor.isOnline(),
                        errorMessage = "Couldn't update live weather: ${error.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun setMapOverlayType(type: MapOverlayType) {
        _uiState.update { it.copy(activeMapOverlay = type) }
    }

    fun setMapBasemapStyle(style: MapBasemapStyle) {
        _uiState.update {
            if (it.isSatelliteRadarOnly) {
                it.copy(mapBasemapStyle = MapBasemapStyle.GOOGLE_SATELLITE)
            } else {
                it.copy(mapBasemapStyle = style)
            }
        }
    }

    fun openExpandedMap() {
        _uiState.update { it.copy(showExpandedMapModal = true) }
    }

    fun closeExpandedMap() {
        _uiState.update { it.copy(showExpandedMapModal = false) }
    }

    fun inspectMapPoint(point: InspectedMapPoint?) {
        _uiState.update { it.copy(inspectedMapPoint = point) }
    }

    fun clearInspectedPoint() {
        _uiState.update { it.copy(inspectedMapPoint = null) }
    }

    fun updateWithGpsCoordinates(lat: Double, lon: Double, locationName: String? = null) {
        val loc = CityLocation(
            id = "gps_${lat.roundToInt()}_${lon.roundToInt()}",
            name = locationName ?: "My Current Location",
            admin1 = "Device GPS",
            country = String.format(Locale.US, "%.3f°, %.3f°", lat, lon),
            latitude = lat,
            longitude = lon,
            isFavorite = false
        )
        selectCity(loc)
    }

    fun toggleSatelliteRadarOnly() {
        _uiState.update {
            val next = !it.isSatelliteRadarOnly
            it.copy(
                isSatelliteRadarOnly = next,
                mapBasemapStyle = if (next) MapBasemapStyle.GOOGLE_SATELLITE else it.mapBasemapStyle
            )
        }
    }

    fun setSatelliteRadarOnly(enabled: Boolean) {
        _uiState.update {
            it.copy(
                isSatelliteRadarOnly = enabled,
                mapBasemapStyle = if (enabled) MapBasemapStyle.GOOGLE_SATELLITE else it.mapBasemapStyle
            )
        }
    }

    fun openAccountAuthSheet() {
        _uiState.update { it.copy(showAccountAuthSheet = true) }
    }

    fun closeAccountAuthSheet() {
        _uiState.update { it.copy(showAccountAuthSheet = false) }
    }

    fun updateUserProfile(profile: UserAccountProfile) {
        _uiState.update { it.copy(userProfile = profile) }
    }

    fun openDeveloperDiagnosticsSheet() {
        _uiState.update { it.copy(showDeveloperDiagnosticsSheet = true) }
    }

    fun closeDeveloperDiagnosticsSheet() {
        _uiState.update { it.copy(showDeveloperDiagnosticsSheet = false) }
    }

    fun triggerDeveloperAlert(alert: DeveloperAlert) {
        _uiState.update { it.copy(activeDeveloperAlert = alert) }
    }

    fun dismissDeveloperAlert() {
        _uiState.update { it.copy(activeDeveloperAlert = null) }
    }

    fun autoFixAllIssues() {
        _uiState.update {
            it.copy(
                activeDeveloperAlert = null,
                errorMessage = null
            )
        }
        refresh()
    }

    fun setObservationInterval(interval: ObservationIntervalPreset) {
        _uiState.update { it.copy(selectedObservationInterval = interval) }
    }

    fun recordVideoObservation(result: VideoObservationResult) {
        _uiState.update { it.copy(lastVideoObservation = result) }
    }

    // ==========================================
    // Real-Time FCM Severe Weather Alert Actions
    // ==========================================

    fun openSevereAlertsSheet() {
        _uiState.update { it.copy(showSevereAlertsSheet = true) }
    }

    fun closeSevereAlertsSheet() {
        _uiState.update { it.copy(showSevereAlertsSheet = false, selectedSevereAlert = null) }
    }

    fun selectSevereAlert(alert: SevereWeatherAlert) {
        _uiState.update { it.copy(selectedSevereAlert = alert) }
        markSevereAlertRead(alert.id)
    }

    fun markSevereAlertRead(alertId: String) {
        val updated = repository.markAlertAsRead(alertId)
        _uiState.update { it.copy(severeWeatherAlerts = updated) }
    }

    fun clearAllSevereAlerts() {
        val updated = repository.clearAllAlerts()
        _uiState.update { it.copy(severeWeatherAlerts = updated, selectedSevereAlert = null) }
    }

    fun updateFcmSettings(newSettings: FcmNotificationSettings) {
        repository.saveFcmSettings(newSettings)
        _uiState.update { it.copy(fcmSettings = newSettings) }
    }

    fun toggleFcmNotifications(enabled: Boolean) {
        val updated = _uiState.value.fcmSettings.copy(isFcmEnabled = enabled)
        updateFcmSettings(updated)
    }

    fun toggleSavedLocationsOnly(savedOnly: Boolean) {
        val updated = _uiState.value.fcmSettings.copy(notifySavedLocationsOnly = savedOnly)
        updateFcmSettings(updated)
    }

    /**
     * Dispatches an interactive simulation of an incoming real-time severe weather alert
     * for one of the user's saved locations (e.g. Flash Flood, Severe Thunderstorm, Tornado Warning).
     */
    fun simulateIncomingSevereAlert(
        targetLocation: CityLocation = _uiState.value.savedCities.firstOrNull() ?: _uiState.value.currentLocation,
        type: SevereAlertType = SevereAlertType.SEVERE_THUNDERSTORM
    ) {
        val timeNow = java.text.SimpleDateFormat("h:mm a, MMM d", Locale.getDefault()).format(java.util.Date())
        val alert = SevereWeatherAlert(
            id = "alert_fcm_${System.currentTimeMillis()}",
            locationId = targetLocation.id,
            locationName = targetLocation.name,
            alertType = type,
            headline = "${type.label} Issued",
            description = "Meteorological Radar Network detected severe cell activity affecting ${targetLocation.name} with rapid barometric drop and high precipitation.",
            severity = type.defaultSeverity,
            instruction = when (type) {
                SevereAlertType.TORNADO_WARNING -> "Seek immediate underground shelter or an interior room away from exterior windows."
                SevereAlertType.FLASH_FLOOD -> "Move to higher ground immediately. Do not attempt to drive through flooded roads."
                SevereAlertType.SEVERE_THUNDERSTORM -> "Remain indoors. Unplug sensitive electronics and avoid tall open areas."
                else -> "Monitor live radar updates and stay tuned to emergency meteorological broadcasts."
            },
            issuedAtEpochMillis = System.currentTimeMillis(),
            issuedAtFormatted = timeNow,
            expiresAtFormatted = "In 2 hours",
            isRead = false
        )
        val updated = repository.addSevereWeatherAlert(alert)
        _uiState.update { it.copy(severeWeatherAlerts = updated, selectedSevereAlert = alert) }
    }
}
