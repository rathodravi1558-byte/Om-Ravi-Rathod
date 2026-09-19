package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ConditionType
import com.example.ui.components.AccountAuthSheet
import com.example.ui.components.AtmosphericBackground
import com.example.ui.components.CitySearchBar
import com.example.ui.components.CitySearchSheet
import com.example.ui.components.CloudIndicatorOverviewCard
import com.example.ui.components.CurrentWeatherHero
import com.example.ui.components.DailyForecastCard
import com.example.ui.components.DeveloperAlertBanner
import com.example.ui.components.DeveloperContactCard
import com.example.ui.components.DeveloperContactSheet
import com.example.ui.components.DeveloperDiagnosticsSheet
import com.example.ui.components.HourlyForecastCard
import com.example.ui.components.InteractiveWeatherMapCard
import com.example.ui.components.OfflineModeBanner
import com.example.ui.components.RedCloudAlertBanner
import com.example.ui.components.SevereWeatherAlertsSheet
import com.example.ui.components.SkyPhotoAnalyzerSheet
import com.example.ui.components.SmartInsightCard
import com.example.ui.components.UvIndexSafetyCard
import com.example.ui.components.VoiceWeatherCommandSheet
import com.example.ui.components.WeatherMetricsGrid
import com.example.ui.components.WeatherTopBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val searchSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSearchSheet by remember { mutableStateOf(false) }
    var showDeveloperSheet by remember { mutableStateOf(false) }

    val conditionType = uiState.currentWeather?.condition?.conditionType ?: ConditionType.CLEAR_DAY

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissError()
        }
    }

    AtmosphericBackground(
        conditionType = conditionType,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            if (uiState.isLoading && uiState.currentWeather == null) {
                // Initial Loading State
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Fetching weather forecast...",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            } else {
                // Main Scrollable Weather Content
                val listState = rememberLazyListState()

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("weather_content_list")
                ) {
                    // Top App Bar
                    item {
                        WeatherTopBar(
                            location = uiState.currentLocation,
                            unit = uiState.temperatureUnit,
                            isRefreshing = uiState.isRefreshing,
                            onSearchClick = { showSearchSheet = true },
                            onFavoriteClick = { viewModel.toggleFavorite() },
                            onUnitToggle = { viewModel.toggleTemperatureUnit() },
                            onRefreshClick = { viewModel.refresh() },
                            onVoiceClick = { viewModel.openVoiceSearchSheet() },
                            onCameraClick = { viewModel.openSkyPhotoSheet() },
                            onAccountClick = { viewModel.openAccountAuthSheet() },
                            onDiagnosticsClick = { viewModel.openDeveloperDiagnosticsSheet() },
                            onDeveloperClick = { showDeveloperSheet = true },
                            onSevereAlertsClick = { viewModel.openSevereAlertsSheet() },
                            activeAlertCount = uiState.severeWeatherAlerts.count { !it.isRead },
                            isOffline = uiState.isOffline || uiState.isUsingCachedData
                        )
                    }

                    // Developer Alert Banner (Diagnostic Issues, Telemetry & Auto-Fix Trigger)
                    uiState.activeDeveloperAlert?.let { alert ->
                        item {
                            DeveloperAlertBanner(
                                alert = alert,
                                onAutoFix = { viewModel.autoFixAllIssues() },
                                onDismiss = { viewModel.dismissDeveloperAlert() }
                            )
                        }
                    }

                    // Offline Mode Banner (Shows when offline or displaying Room cached forecast)
                    item {
                        OfflineModeBanner(
                            isOffline = uiState.isOffline,
                            isUsingCachedData = uiState.isUsingCachedData,
                            cachedTimestamp = uiState.cachedTimestampFormatted,
                            isRefreshing = uiState.isRefreshing,
                            onRetry = { viewModel.retryConnection() }
                        )
                    }

                    // Persistent Red Cloud Severe Alert Banner (if alert active)
                    uiState.cloudAnalysis?.let { analysis ->
                        if (analysis.isRedCloudAlert) {
                            item {
                                RedCloudAlertBanner(
                                    analysis = analysis,
                                    onDismiss = { viewModel.dismissRedCloudAlert() }
                                )
                            }
                        }
                    }

                    // Direct Search Bar UI Component for Inputting City Names
                    item {
                        CitySearchBar(
                            query = uiState.searchQuery,
                            searchResults = uiState.searchResults,
                            isSearching = uiState.isSearching,
                            onQueryChange = { viewModel.onSearchQueryChanged(it) },
                            onCitySelected = { city ->
                                viewModel.selectCity(city)
                            },
                            onVoiceSearchClick = { viewModel.openVoiceSearchSheet() }
                        )
                    }

                    // Cloud Formation & Threat Level Indicator Card
                    item {
                        CloudIndicatorOverviewCard(
                            cloudAnalysis = uiState.cloudAnalysis,
                            cloudCoverPercent = uiState.currentWeather?.metrics?.cloudCoverPercent ?: 42,
                            onOpenSkyScanner = { viewModel.openSkyPhotoSheet() }
                        )
                    }

                    // Current Weather Hero Section
                    uiState.currentWeather?.let { weather ->
                        item {
                            CurrentWeatherHero(
                                weather = weather,
                                unit = uiState.temperatureUnit
                            )
                        }

                        // UV Index & Sun Safety Card
                        item {
                            val todayMaxUv = uiState.dailyForecast.firstOrNull()?.uvMax ?: weather.metrics.uvIndex
                            UvIndexSafetyCard(
                                uvIndex = weather.metrics.uvIndex,
                                maxTodayUv = todayMaxUv
                            )
                        }

                        // Smart Insights Recommendation
                        item {
                            SmartInsightCard(
                                insightText = weather.metrics.smartInsight
                            )
                        }
                    }

                    // Interactive Weather Map (Precipitation Radar & Temperature Heatmap)
                    item {
                        InteractiveWeatherMapCard(
                            location = uiState.currentLocation,
                            currentWeather = uiState.currentWeather,
                            unit = uiState.temperatureUnit,
                            activeOverlay = uiState.activeMapOverlay,
                            basemapStyle = uiState.mapBasemapStyle,
                            isSatelliteOnly = uiState.isSatelliteRadarOnly,
                            onToggleSatelliteOnly = { viewModel.toggleSatelliteRadarOnly() },
                            onOverlayChange = { viewModel.setMapOverlayType(it) },
                            onBasemapStyleChange = { viewModel.setMapBasemapStyle(it) },
                            onLocationFound = { lat, lon, name ->
                                viewModel.updateWithGpsCoordinates(lat, lon, name)
                            }
                        )
                    }

                    // Hourly Forecast Carousel
                    if (uiState.hourlyForecast.isNotEmpty()) {
                        item {
                            HourlyForecastCard(
                                hourlyList = uiState.hourlyForecast,
                                unit = uiState.temperatureUnit
                            )
                        }
                    }

                    // 7-Day Forecast
                    if (uiState.dailyForecast.isNotEmpty()) {
                        item {
                            DailyForecastCard(
                                dailyList = uiState.dailyForecast,
                                unit = uiState.temperatureUnit
                            )
                        }
                    }

                    // Detailed 6-card Metrics Grid
                    uiState.currentWeather?.let { weather ->
                        item {
                            WeatherMetricsGrid(
                                metrics = weather.metrics
                            )
                        }
                    }

                    // App Developer Profile & Contact (Om Ravi Rathod - Hindi & English)
                    item {
                        DeveloperContactCard(
                            onOpenFullProfile = { showDeveloperSheet = true }
                        )
                    }

                    // Footer / Data Source attribution
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Updated: ${uiState.currentWeather?.lastUpdatedFormatted ?: "Recently"}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Weather data provided by Open-Meteo",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }

            // Snackbar Host for Error Messages
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )

            // City Search Bottom Sheet
            if (showSearchSheet) {
                CitySearchSheet(
                    sheetState = searchSheetState,
                    searchQuery = uiState.searchQuery,
                    searchResults = uiState.searchResults,
                    isSearching = uiState.isSearching,
                    savedCities = uiState.savedCities,
                    onQueryChange = { viewModel.onSearchQueryChanged(it) },
                    onCitySelected = { city ->
                        viewModel.selectCity(city)
                        scope.launch { searchSheetState.hide() }.invokeOnCompletion {
                            showSearchSheet = false
                        }
                    },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onDismiss = {
                        viewModel.clearSearch()
                        showSearchSheet = false
                    },
                    onVoiceSearchClick = {
                        showSearchSheet = false
                        viewModel.openVoiceSearchSheet()
                    }
                )
            }

            // Voice Weather Command Bottom Sheet
            if (uiState.showVoiceSearchSheet) {
                VoiceWeatherCommandSheet(
                    onDismiss = {
                        viewModel.closeVoiceSearchSheet()
                    },
                    onVoiceLocationCaptured = { spokenPhrase ->
                        viewModel.processVoiceSpokenLocation(spokenPhrase)
                    },
                    feedbackMessage = uiState.voiceFeedbackMessage
                )
            }

            // Sky Photo Weather Analyzer Bottom Sheet
            if (uiState.showSkyPhotoSheet) {
                SkyPhotoAnalyzerSheet(
                    onDismiss = {
                        viewModel.closeSkyPhotoSheet()
                    },
                    onApplyToForecast = { analysis ->
                        viewModel.applyCloudAnalysis(analysis)
                    }
                )
            }

            // Developer Contact Bottom Sheet (Om Ravi Rathod - rathodravi1558@gmail.com)
            if (showDeveloperSheet) {
                DeveloperContactSheet(
                    onDismiss = {
                        showDeveloperSheet = false
                    }
                )
            }

            // User Account, Sign-up / Login & Condition Rate Email Alerts Sheet
            if (uiState.showAccountAuthSheet) {
                AccountAuthSheet(
                    userProfile = uiState.userProfile,
                    currentWeather = uiState.currentWeather,
                    currentCityName = uiState.currentLocation.name,
                    onDismiss = { viewModel.closeAccountAuthSheet() },
                    onSaveProfile = { updatedProfile ->
                        viewModel.updateUserProfile(updatedProfile)
                    }
                )
            }

            // Developer Diagnostics Hub, System Self-Test & Auto-Fix Sheet
            if (uiState.showDeveloperDiagnosticsSheet) {
                DeveloperDiagnosticsSheet(
                    onDismiss = { viewModel.closeDeveloperDiagnosticsSheet() },
                    onTriggerDeveloperAlert = { alert ->
                        viewModel.triggerDeveloperAlert(alert)
                    },
                    onAutoFixIssues = { viewModel.autoFixAllIssues() }
                )
            }

            // Real-Time FCM Severe Weather Alerts Bottom Sheet
            if (uiState.showSevereAlertsSheet) {
                SevereWeatherAlertsSheet(
                    viewModel = viewModel,
                    alerts = uiState.severeWeatherAlerts,
                    fcmSettings = uiState.fcmSettings,
                    savedCities = uiState.savedCities,
                    currentLocation = uiState.currentLocation,
                    onDismiss = { viewModel.closeSevereAlertsSheet() }
                )
            }
        }
    }
}
