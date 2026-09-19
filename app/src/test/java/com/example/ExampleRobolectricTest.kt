package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.WeatherRepository
import com.example.data.vision.CloudBitmapGenerator
import com.example.data.vision.CloudVisionAnalyzer
import com.example.model.CloudThreatLevel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Weather", appName)
  }

  @Test
  fun `verify 20-day weather forecast generation`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = WeatherRepository(context)
    val result = repository.getWeatherData(40.7128, -74.0060)
    assertTrue(result.isSuccess)
    val domainData = result.getOrThrow()
    val dailyForecast = domainData.dailyForecast
    assertEquals(20, dailyForecast.size)
  }

  @Test
  fun `verify red cloud alert preset triggers high alert and warnings`() {
    val result = CloudVisionAnalyzer.getPreset(CloudVisionAnalyzer.PresetType.SEVERE_RED_STORM)
    assertTrue("Red cloud alert must be true", result.isRedCloudAlert)
    assertEquals(CloudThreatLevel.SEVERE_ALERT, result.threatLevel)
    assertTrue("Coverage must be high", result.cloudCoveragePercent >= 90)
    assertNotNull("Alert headline must be present", result.alertHeadline)
    assertTrue("Safety advice must be provided", result.safetyAdvice.isNotEmpty())
  }

  @Test
  fun `verify local vision analyzer detects red cloud storm formation from bitmap`() {
    val redStormBmp = CloudBitmapGenerator.generatePresetBitmap(CloudVisionAnalyzer.PresetType.SEVERE_RED_STORM, 160, 100)
    val analysis = CloudVisionAnalyzer.analyzeLocally(redStormBmp)
    assertTrue("Should trigger red cloud alert for crimson storm sky", analysis.isRedCloudAlert)
    assertTrue("Threat level should be severe", analysis.threatLevel.isSevere)
    assertTrue("Cloud coverage should be substantial", analysis.cloudCoveragePercent > 40)
  }

  @Test
  fun `verify sunny clear sky preset indicates safe conditions`() {
    val sunnyBmp = CloudBitmapGenerator.generatePresetBitmap(CloudVisionAnalyzer.PresetType.CLEAR_SUNNY, 160, 100)
    val analysis = CloudVisionAnalyzer.analyzeLocally(sunnyBmp)
    assertEquals(CloudThreatLevel.CLEAR_SAFE, analysis.threatLevel)
    assertTrue("Low rain probability", analysis.estimatedRainProb <= 20)
  }

  @Test
  fun `verify developer information for Om Ravi Rathod with email in English and Hindi`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    
    // Verify string resources
    val devName = context.getString(R.string.developer_name)
    val devEmail = context.getString(R.string.developer_email)
    val devNameHindi = context.getString(R.string.developer_name_hindi)
    
    assertEquals("Om Ravi Rathod", devName)
    assertEquals("rathodravi1558@gmail.com", devEmail)
    assertEquals("ओम रवि राठौड़", devNameHindi)
    
    // Verify developer constants in UI component
    assertEquals("Om Ravi Rathod", com.example.ui.components.DEVELOPER_NAME_EN)
    assertEquals("ओम रवि राठौड़", com.example.ui.components.DEVELOPER_NAME_HI)
    assertEquals("rathodravi1558@gmail.com", com.example.ui.components.DEVELOPER_EMAIL)
  }

  @Test
  fun `verify voice command parses spoken location queries properly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    
    // Verify voice string resources
    val voiceTitle = context.getString(R.string.voice_search_title)
    assertTrue("Voice title should be present", voiceTitle.isNotBlank())

    // Test extraction logic with WeatherViewModel
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.WeatherViewModel(app)
    
    viewModel.openVoiceSearchSheet()
    assertTrue("Voice sheet should be open", viewModel.uiState.value.showVoiceSearchSheet)
    
    // Test processing spoken query "Weather in Tokyo"
    viewModel.processVoiceSpokenLocation("Weather in Tokyo")
    
    // Test closing voice sheet
    viewModel.closeVoiceSearchSheet()
    assertFalse("Voice sheet should be closed", viewModel.uiState.value.showVoiceSearchSheet)
  }

  @Test
  fun `verify developer signature, app creation tagline, and YouTube channel handle`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    
    // Verify string resources
    val signature = context.getString(R.string.developer_signature)
    val tagline = context.getString(R.string.developer_created_tagline)
    val ytHandle = context.getString(R.string.developer_youtube_handle)
    val ytUrl = context.getString(R.string.developer_youtube_url)
    
    assertEquals("Om", signature)
    assertEquals("App created by Om Ravi Rathod with Google AI Studio", tagline)
    assertEquals("@RathodRavi-27w1", ytHandle)
    assertTrue("YouTube URL should link to user's handle", ytUrl.contains("@RathodRavi-27w1"))

    // Verify UI constants
    assertEquals("Om", com.example.ui.components.DEVELOPER_SIGNATURE)
    assertEquals("ओम", com.example.ui.components.DEVELOPER_SIGNATURE_HI)
    assertEquals("@RathodRavi-27w1", com.example.ui.components.DEVELOPER_YOUTUBE_HANDLE)
    assertEquals("https://www.youtube.com/@RathodRavi-27w1", com.example.ui.components.DEVELOPER_YOUTUBE_URL)
    assertEquals("App created by Om Ravi Rathod with Google AI Studio", com.example.ui.components.DEVELOPER_APP_TAGLINE)
  }

  @Test
  fun `verify interactive map component state, overlays, and string resources`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.WeatherViewModel(app)

    // Verify map string resources
    val mapTitle = context.getString(R.string.weather_map_title)
    val precipTitle = context.getString(R.string.weather_map_overlay_precipitation)
    val tempTitle = context.getString(R.string.weather_map_overlay_temperature)
    val recenterLabel = context.getString(R.string.weather_map_recenter)

    assertTrue("Map title should be present", mapTitle.isNotBlank())
    assertTrue("Precipitation overlay title should be present", precipTitle.isNotBlank())
    assertTrue("Temperature overlay title should be present", tempTitle.isNotBlank())
    assertTrue("Recenter label should be present", recenterLabel.isNotBlank())

    // Verify default map state
    assertEquals(com.example.model.MapOverlayType.PRECIPITATION, viewModel.uiState.value.activeMapOverlay)
    assertEquals(com.example.model.MapBasemapStyle.METEOROLOGICAL_DARK, viewModel.uiState.value.mapBasemapStyle)

    // Switch overlay to Temperature
    viewModel.setMapOverlayType(com.example.model.MapOverlayType.TEMPERATURE)
    assertEquals(com.example.model.MapOverlayType.TEMPERATURE, viewModel.uiState.value.activeMapOverlay)

    // Switch overlay back to Precipitation
    viewModel.setMapOverlayType(com.example.model.MapOverlayType.PRECIPITATION)
    assertEquals(com.example.model.MapOverlayType.PRECIPITATION, viewModel.uiState.value.activeMapOverlay)

    // Cycle basemap style
    viewModel.setMapBasemapStyle(com.example.model.MapBasemapStyle.TOPOGRAPHIC)
    assertEquals(com.example.model.MapBasemapStyle.TOPOGRAPHIC, viewModel.uiState.value.mapBasemapStyle)

    // Verify GPS update
    viewModel.updateWithGpsCoordinates(37.7749, -122.4194, "San Francisco GPS")
    assertEquals(37.7749, viewModel.uiState.value.currentLocation.latitude, 0.001)
    assertEquals(-122.4194, viewModel.uiState.value.currentLocation.longitude, 0.001)
    assertEquals("San Francisco GPS", viewModel.uiState.value.currentLocation.name)

    // Test inspected map point
    val samplePoint = com.example.model.InspectedMapPoint(
      latitude = 37.8,
      longitude = -122.4,
      temperatureC = 18.5,
      precipitationMm = 1.2,
      distanceKm = 4.2,
      bearing = "NE",
      conditionLabel = "Light Rain"
    )
    viewModel.inspectMapPoint(samplePoint)
    assertEquals(samplePoint, viewModel.uiState.value.inspectedMapPoint)

    viewModel.clearInspectedPoint()
    assertNull("Inspected point should be cleared", viewModel.uiState.value.inspectedMapPoint)
  }

  @Test
  fun `verify UV index risk categories and sun safety recommendations`() {
    val lowRec = com.example.model.WeatherConditionResolver.getUvSafetyRecommendation(2.0)
    assertEquals("Low", lowRec.riskLevel)
    assertFalse("Low UV doesn't require mandatory sunscreen", lowRec.needsSunscreen)

    val modRec = com.example.model.WeatherConditionResolver.getUvSafetyRecommendation(4.8)
    assertEquals("Moderate", modRec.riskLevel)
    assertTrue("Moderate UV requires sunscreen", modRec.needsSunscreen)
    assertTrue("Moderate UV requires sunglasses", modRec.needsSunglasses)

    val highRec = com.example.model.WeatherConditionResolver.getUvSafetyRecommendation(7.2)
    assertEquals("High", highRec.riskLevel)
    assertTrue("High UV requires shade", highRec.needsShade)

    val veryHighRec = com.example.model.WeatherConditionResolver.getUvSafetyRecommendation(9.5)
    assertEquals("Very High", veryHighRec.riskLevel)
    assertTrue("Very High UV requires full protection", veryHighRec.needsSunscreen && veryHighRec.needsShade)

    val extremeRec = com.example.model.WeatherConditionResolver.getUvSafetyRecommendation(12.5)
    assertEquals("Extreme", extremeRec.riskLevel)
    assertTrue("Extreme UV requires avoid exposure", extremeRec.recommendation.contains("precautions", ignoreCase = true))
  }

  @Test
  fun `verify Room database caching and offline retrieval`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = com.example.data.local.AppDatabase.getDatabase(context)
    val dao = database.weatherCacheDao()

    val testEntity = com.example.data.local.entity.CachedWeatherEntity(
      locationId = "test_loc_delhi",
      cityName = "New Delhi",
      admin1 = "Delhi",
      country = "India",
      latitude = 28.6139,
      longitude = 77.2090,
      temperatureC = 31.5,
      weatherCode = 0,
      conditionLabel = "Clear Sky",
      highTempC = 35.0,
      lowTempC = 24.0,
      uvIndex = 8.5,
      uvRiskCategory = "Very High",
      feelsLikeC = 33.0,
      humidityPercent = 50,
      windSpeedKmh = 11.0,
      windDirectionCardinal = "NW",
      precipitationMm = 0.0,
      pressureHpa = 1012.0,
      sunriseTime = "06:10 AM",
      sunsetTime = "06:45 PM",
      visibilityDescription = "Clear (10+ km)",
      smartInsight = "High UV levels today. Use sunscreen.",
      cloudCoverPercent = 10,
      hourlyForecastJson = "[]",
      dailyForecastJson = "[]",
      cachedAtEpochMillis = System.currentTimeMillis(),
      cachedTimeFormatted = "10:30 AM, Sep 19"
    )

    dao.insertCachedWeather(testEntity)

    val retrieved = dao.getCachedWeather("test_loc_delhi")
    assertNotNull("Retrieved cached weather entity should not be null", retrieved)
    assertEquals("New Delhi", retrieved?.cityName)
    assertEquals(8.5, retrieved?.uvIndex ?: 0.0, 0.01)
    assertEquals("Very High", retrieved?.uvRiskCategory)
    assertEquals(31.5, retrieved?.temperatureC ?: 0.0, 0.01)
  }

  @Test
  fun `verify UV index and offline mode string resources`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val uvTitle = context.getString(R.string.uv_index_card_title)
    val uvCurrent = context.getString(R.string.uv_index_current)
    val uvSafetyLabel = context.getString(R.string.uv_safety_recommendation_label)
    val offlineTitle = context.getString(R.string.offline_mode_title)
    val offlineDesc = context.getString(R.string.offline_mode_desc)
    val offlineRetry = context.getString(R.string.offline_retry_button)

    assertTrue("UV card title must be valid", uvTitle.isNotBlank())
    assertTrue("UV current label must be valid", uvCurrent.isNotBlank())
    assertTrue("UV safety label must be valid", uvSafetyLabel.isNotBlank())
    assertTrue("Offline title must be valid", offlineTitle.isNotBlank())
    assertTrue("Offline description must be valid", offlineDesc.isNotBlank())
    assertTrue("Offline retry button must be valid", offlineRetry.isNotBlank())
  }

  @Test
  fun `verify satellite-only radar mode locks basemap style to Google Satellite`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.WeatherViewModel(app)

    // Initially false
    assertFalse(viewModel.uiState.value.isSatelliteRadarOnly)

    // Toggle on satellite only
    viewModel.toggleSatelliteRadarOnly()
    assertTrue(viewModel.uiState.value.isSatelliteRadarOnly)
    assertEquals(com.example.model.MapBasemapStyle.GOOGLE_SATELLITE, viewModel.uiState.value.mapBasemapStyle)

    // Attempting to change to METEOROLOGICAL_DARK while locked must stay GOOGLE_SATELLITE
    viewModel.setMapBasemapStyle(com.example.model.MapBasemapStyle.METEOROLOGICAL_DARK)
    assertEquals(com.example.model.MapBasemapStyle.GOOGLE_SATELLITE, viewModel.uiState.value.mapBasemapStyle)

    // Toggle off allows other basemap styles
    viewModel.toggleSatelliteRadarOnly()
    assertFalse(viewModel.uiState.value.isSatelliteRadarOnly)
    viewModel.setMapBasemapStyle(com.example.model.MapBasemapStyle.TOPOGRAPHIC)
    assertEquals(com.example.model.MapBasemapStyle.TOPOGRAPHIC, viewModel.uiState.value.mapBasemapStyle)
  }

  @Test
  fun `verify user authentication profile supports Google Apple Twitter Email and live condition rate alerts`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.WeatherViewModel(app)

    val testProfile = com.example.model.UserAccountProfile(
      id = "user_test",
      displayName = "Om Ravi Rathod",
      email = "rathodravi1558@gmail.com",
      provider = com.example.model.AuthProvider.GOOGLE,
      isVerified = true,
      isEmailAlertsEnabled = true,
      alertFrequency = "Every 10 minutes",
      rainRateThresholdMm = 2.5,
      severeStormAlert = true,
      extremeUvAlert = true,
      lastAlertSentFormatted = "Just now"
    )

    viewModel.updateUserProfile(testProfile)
    val stateProfile = viewModel.uiState.value.userProfile
    assertTrue(stateProfile.isVerified)
    assertTrue(stateProfile.isEmailAlertsEnabled)
    assertEquals(com.example.model.AuthProvider.GOOGLE, stateProfile.provider)
    assertEquals("rathodravi1558@gmail.com", stateProfile.email)
    assertEquals("Every 10 minutes", stateProfile.alertFrequency)
    assertEquals(2.5, stateProfile.rainRateThresholdMm, 0.01)
  }

  @Test
  fun `verify all 9 video observation interval presets are defined with accurate durations`() {
    val intervals = com.example.model.ObservationIntervalPreset.entries
    assertEquals(9, intervals.size)

    assertEquals("30s", com.example.model.ObservationIntervalPreset.SEC_30.label)
    assertEquals(30, com.example.model.ObservationIntervalPreset.SEC_30.durationSeconds)

    assertEquals("1m", com.example.model.ObservationIntervalPreset.MIN_1.label)
    assertEquals(60, com.example.model.ObservationIntervalPreset.MIN_1.durationSeconds)

    assertEquals("2m", com.example.model.ObservationIntervalPreset.MIN_2.label)
    assertEquals(120, com.example.model.ObservationIntervalPreset.MIN_2.durationSeconds)

    assertEquals("10m", com.example.model.ObservationIntervalPreset.MIN_10.label)
    assertEquals(600, com.example.model.ObservationIntervalPreset.MIN_10.durationSeconds)

    assertEquals("12m", com.example.model.ObservationIntervalPreset.MIN_12.label)
    assertEquals(720, com.example.model.ObservationIntervalPreset.MIN_12.durationSeconds)

    assertEquals("20m", com.example.model.ObservationIntervalPreset.MIN_20.label)
    assertEquals(1200, com.example.model.ObservationIntervalPreset.MIN_20.durationSeconds)

    assertEquals("50m", com.example.model.ObservationIntervalPreset.MIN_50.label)
    assertEquals(3000, com.example.model.ObservationIntervalPreset.MIN_50.durationSeconds)

    assertEquals("1h", com.example.model.ObservationIntervalPreset.HOUR_1.label)
    assertEquals(3600, com.example.model.ObservationIntervalPreset.HOUR_1.durationSeconds)

    assertEquals("2h", com.example.model.ObservationIntervalPreset.HOUR_2.label)
    assertEquals(7200, com.example.model.ObservationIntervalPreset.HOUR_2.durationSeconds)
  }

  @Test
  fun `verify developer diagnostics alert and auto-fix flow`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.WeatherViewModel(app)

    assertNull(viewModel.uiState.value.activeDeveloperAlert)

    // Trigger developer alert
    val testAlert = com.example.model.DeveloperAlert(
      id = "alert_test",
      title = "Doppler Radar Frame Dropped",
      message = "Satellite frame packet latency exceeded 450ms.",
      severity = "Warning",
      isAutoFixAvailable = true,
      timestampFormatted = "Just now"
    )
    viewModel.triggerDeveloperAlert(testAlert)
    assertEquals(testAlert, viewModel.uiState.value.activeDeveloperAlert)

    // Auto-fix all issues
    viewModel.autoFixAllIssues()
    assertNull("Active alert should be dismissed after auto-fix", viewModel.uiState.value.activeDeveloperAlert)
  }

  @Test
  fun `verify FCM topic name sanitization handles special characters and formatting`() {
    val city1 = com.example.model.CityLocation("c1", "New York", "NY", "United States", 40.71, -74.00, true)
    val topic1 = com.example.service.FcmSubscriptionManager.sanitizeTopicName(city1)
    assertEquals("weather_alert_new_york_united_states", topic1)

    val citySpecial = com.example.model.CityLocation("c2", "São Paulo / Centro!", "SP", "Brazil", -23.55, -46.63, true)
    val topic2 = com.example.service.FcmSubscriptionManager.sanitizeTopicName(citySpecial)
    assertTrue("Topic must start with weather_alert_", topic2.startsWith("weather_alert_"))
    assertTrue("Topic must not contain slashes or exclamation", !topic2.contains("/") && !topic2.contains("!"))
  }

  @Test
  fun `verify FCM settings persistence and alert additions in repository`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = WeatherRepository(context)

    val settings = repository.getFcmSettings()
    assertTrue("FCM should be enabled by default", settings.isFcmEnabled)
    assertTrue("Saved locations only should be true by default", settings.notifySavedLocationsOnly)

    // Save custom FCM token
    repository.saveFcmToken("fcm_token_sample_12345")
    val updatedSettings = repository.getFcmSettings()
    assertEquals("fcm_token_sample_12345", updatedSettings.fcmToken)

    // Add severe weather alert
    val testAlert = com.example.model.SevereWeatherAlert(
      id = "alert_flood_001",
      locationId = "pop_ny",
      locationName = "New York",
      alertType = com.example.model.SevereAlertType.FLASH_FLOOD,
      headline = "Flash Flood Warning Issued",
      description = "Torrential rains producing life-threatening flash flooding.",
      severity = "SEVERE",
      instruction = "Move to higher ground immediately.",
      issuedAtFormatted = "2:30 PM, Oct 12",
      expiresAtFormatted = "In 2 hours",
      isRead = false
    )
    repository.addSevereWeatherAlert(testAlert)

    val alerts = repository.getSevereWeatherAlerts()
    assertTrue("Alerts list should contain test alert", alerts.any { it.id == "alert_flood_001" })

    // Mark as read
    repository.markAlertAsRead("alert_flood_001")
    val alertsAfterRead = repository.getSevereWeatherAlerts()
    val marked = alertsAfterRead.firstOrNull { it.id == "alert_flood_001" }
    assertNotNull(marked)
    assertTrue("Alert should be marked as read", marked!!.isRead)

    // Clear all
    repository.clearAllAlerts()
    assertEquals(0, repository.getSevereWeatherAlerts().size)
  }

  @Test
  fun `verify ViewModel simulates severe weather alert for saved location`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.WeatherViewModel(app)

    val savedCount = viewModel.uiState.value.savedCities.size
    assertTrue("Saved cities must be non-empty", savedCount > 0)

    val targetCity = viewModel.uiState.value.savedCities.first()
    viewModel.simulateIncomingSevereAlert(
      targetLocation = targetCity,
      type = com.example.model.SevereAlertType.TORNADO_WARNING
    )

    val alerts = viewModel.uiState.value.severeWeatherAlerts
    assertTrue("Severe alert list should have at least 1 alert", alerts.isNotEmpty())
    val latest = alerts.first()
    assertEquals(targetCity.name, latest.locationName)
    assertEquals(com.example.model.SevereAlertType.TORNADO_WARNING, latest.alertType)
    assertEquals("EXTREME", latest.severity)
  }
}
