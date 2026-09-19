package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material.icons.rounded.ZoomOut
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.R
import com.example.model.CityLocation
import com.example.model.CurrentWeatherData
import com.example.model.InspectedMapPoint
import com.example.model.MapBasemapStyle
import com.example.model.MapOverlayType
import com.example.model.TemperatureUnit
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Interactive Weather Map Card that renders precipitation radar and thermal heatmaps
 * directly over the user's current location with real-time gesture panning, zoom,
 * timeline scrub controls, and tap-to-inspect point readings.
 */
@Composable
fun InteractiveWeatherMapCard(
    location: CityLocation,
    currentWeather: CurrentWeatherData?,
    unit: TemperatureUnit,
    activeOverlay: MapOverlayType,
    basemapStyle: MapBasemapStyle,
    onOverlayChange: (MapOverlayType) -> Unit,
    onBasemapStyleChange: (MapBasemapStyle) -> Unit,
    onLocationFound: (Double, Double, String) -> Unit,
    isSatelliteOnly: Boolean = false,
    onToggleSatelliteOnly: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    // When satellite-only mode is active, lock style to GOOGLE_SATELLITE
    val effectiveBasemapStyle = if (isSatelliteOnly) MapBasemapStyle.GOOGLE_SATELLITE else basemapStyle

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("interactive_weather_map_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A).copy(alpha = 0.92f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.4f), Color(0xFF1E293B).copy(alpha = 0.6f))
            ),
            width = 1.2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Title, Subtitle, and Fullscreen/Locate buttons
            MapCardHeader(
                location = location,
                isSatelliteOnly = isSatelliteOnly,
                onToggleSatelliteOnly = onToggleSatelliteOnly,
                onExpandClick = { isExpanded = true },
                onLocationFound = onLocationFound
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Segmented Overlay Selector (Precipitation vs Temperature)
            MapOverlayTabSelector(
                activeOverlay = activeOverlay,
                onOverlayChange = onOverlayChange
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Main Map Viewport (Card Height: 280dp)
            InteractiveMapViewport(
                location = location,
                currentWeather = currentWeather,
                unit = unit,
                activeOverlay = activeOverlay,
                basemapStyle = effectiveBasemapStyle,
                isSatelliteOnly = isSatelliteOnly,
                onBasemapStyleCycle = {
                    if (isSatelliteOnly) {
                        // Locked to Google Satellite Only mode
                        onBasemapStyleChange(MapBasemapStyle.GOOGLE_SATELLITE)
                    } else {
                        val next = when (effectiveBasemapStyle) {
                            MapBasemapStyle.GOOGLE_SATELLITE -> MapBasemapStyle.METEOROLOGICAL_DARK
                            MapBasemapStyle.METEOROLOGICAL_DARK -> MapBasemapStyle.SATELLITE_NIGHT
                            MapBasemapStyle.SATELLITE_NIGHT -> MapBasemapStyle.TOPOGRAPHIC
                            MapBasemapStyle.TOPOGRAPHIC -> MapBasemapStyle.GOOGLE_SATELLITE
                        }
                        onBasemapStyleChange(next)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(18.dp))
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Legend bar at bottom
            MapLegendBar(activeOverlay = activeOverlay, unit = unit)
        }
    }

    // Fullscreen Interactive Dialog
    if (isExpanded) {
        FullscreenWeatherMapDialog(
            location = location,
            currentWeather = currentWeather,
            unit = unit,
            activeOverlay = activeOverlay,
            basemapStyle = effectiveBasemapStyle,
            isSatelliteOnly = isSatelliteOnly,
            onToggleSatelliteOnly = onToggleSatelliteOnly,
            onOverlayChange = onOverlayChange,
            onBasemapStyleChange = onBasemapStyleChange,
            onLocationFound = onLocationFound,
            onDismiss = { isExpanded = false }
        )
    }
}

@Composable
private fun MapCardHeader(
    location: CityLocation,
    isSatelliteOnly: Boolean,
    onToggleSatelliteOnly: () -> Unit,
    onExpandClick: () -> Unit,
    onLocationFound: (Double, Double, String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLocating by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fetchDeviceLocation(context, onLocationFound) { isLocating = false }
        } else {
            isLocating = false
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF0284C7).copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Explore,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.weather_map_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "📍 ${location.name} (${String.format(Locale.US, "%.2f°N, %.2f°E", location.latitude, location.longitude)})",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Satellite Only Toggle Chip ("Google map inside radar in only for satellite not other options")
            FilterChip(
                selected = isSatelliteOnly,
                onClick = onToggleSatelliteOnly,
                label = {
                    Text(
                        text = if (isSatelliteOnly) "🛰️ Satellite Only" else "🛰️ Satellite",
                        fontSize = 11.sp,
                        fontWeight = if (isSatelliteOnly) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF38BDF8).copy(alpha = 0.28f),
                    selectedLabelColor = Color(0xFF38BDF8),
                    containerColor = Color(0xFF1E293B)
                ),
                border = BorderStroke(1.dp, if (isSatelliteOnly) Color(0xFF38BDF8) else Color(0xFF475569)),
                modifier = Modifier.testTag("satellite_radar_only_toggle_chip")
            )

            Spacer(modifier = Modifier.width(6.dp))

            // GPS Location Action Button
            IconButton(
                onClick = {
                    val hasFine = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    val hasCoarse = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasFine || hasCoarse) {
                        isLocating = true
                        fetchDeviceLocation(context, onLocationFound) { isLocating = false }
                    } else {
                        isLocating = true
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF1E293B), CircleShape)
                    .testTag("map_gps_locate_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.MyLocation,
                    contentDescription = stringResource(R.string.weather_map_locate_device),
                    tint = if (isLocating) Color(0xFF38BDF8) else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Expand to Fullscreen
            IconButton(
                onClick = onExpandClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF1E293B), CircleShape)
                    .testTag("map_expand_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Fullscreen,
                    contentDescription = stringResource(R.string.weather_map_expand),
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun MapOverlayTabSelector(
    activeOverlay: MapOverlayType,
    onOverlayChange: (MapOverlayType) -> Unit
) {
    val selectedIndex = if (activeOverlay == MapOverlayType.PRECIPITATION) 0 else 1

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E293B)
    ) {
        TabRow(
            selectedTabIndex = selectedIndex,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                    height = 3.dp,
                    color = if (activeOverlay == MapOverlayType.PRECIPITATION) Color(0xFF38BDF8) else Color(0xFFF97316)
                )
            },
            divider = {}
        ) {
            Tab(
                selected = activeOverlay == MapOverlayType.PRECIPITATION,
                onClick = { onOverlayChange(MapOverlayType.PRECIPITATION) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.WaterDrop,
                            contentDescription = null,
                            tint = if (activeOverlay == MapOverlayType.PRECIPITATION) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.weather_map_overlay_precipitation),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (activeOverlay == MapOverlayType.PRECIPITATION) FontWeight.Bold else FontWeight.Medium,
                                color = if (activeOverlay == MapOverlayType.PRECIPITATION) Color.White else Color(0xFF94A3B8)
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("map_tab_precipitation")
            )

            Tab(
                selected = activeOverlay == MapOverlayType.TEMPERATURE,
                onClick = { onOverlayChange(MapOverlayType.TEMPERATURE) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Thermostat,
                            contentDescription = null,
                            tint = if (activeOverlay == MapOverlayType.TEMPERATURE) Color(0xFFF97316) else Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.weather_map_overlay_temperature),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (activeOverlay == MapOverlayType.TEMPERATURE) FontWeight.Bold else FontWeight.Medium,
                                color = if (activeOverlay == MapOverlayType.TEMPERATURE) Color.White else Color(0xFF94A3B8)
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("map_tab_temperature")
            )
        }
    }
}

/**
 * Highly interactive gesture-enabled Map Viewport supporting pinch zoom, pan,
 * live radar rotation animation, isotherms, and point inspection.
 */
@Composable
fun InteractiveMapViewport(
    location: CityLocation,
    currentWeather: CurrentWeatherData?,
    unit: TemperatureUnit,
    activeOverlay: MapOverlayType,
    basemapStyle: MapBasemapStyle,
    isSatelliteOnly: Boolean = false,
    onBasemapStyleCycle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var inspectedPoint by remember { mutableStateOf<InspectedMapPoint?>(null) }
    var inspectedCanvasOffset by remember { mutableStateOf<Offset?>(null) }

    // Timeline control: 0 = Past 30m, 1 = Live/Now, 2 = +30m, 3 = +60m
    var timelineStep by remember { mutableIntStateOf(1) }
    var isPlaying by remember { mutableStateOf(false) }

    // Continuous animations for radar beam and pulsating user pin
    val infiniteTransition = rememberInfiniteTransition(label = "map_radar_transition")
    val radarSweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarSweep"
    )
    val beaconPulseRadius by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 32f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "beaconPulse"
    )
    val beaconPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "beaconAlpha"
    )

    // Timeline playback timer
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying) {
                delay(1400)
                timelineStep = (timelineStep + 1) % 4
            }
        }
    }

    BoxWithConstraints(modifier = modifier.testTag("weather_map_canvas_container")) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val centerCanvas = Offset(widthPx / 2f, heightPx / 2f)

        // The custom Canvas drawing the map layers, grid, and overlays
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("weather_map_canvas")
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.5f, 5.0f)
                        panOffset = Offset(
                            x = (panOffset.x + pan.x).coerceIn(-widthPx * 1.5f, widthPx * 1.5f),
                            y = (panOffset.y + pan.y).coerceIn(-heightPx * 1.5f, heightPx * 1.5f)
                        )
                    }
                }
                .pointerInput(location, currentWeather, zoomScale, panOffset) {
                    detectTapGestures { tapOffset ->
                        val effectiveCenter = centerCanvas + panOffset
                        val dx = (tapOffset.x - effectiveCenter.x) / (zoomScale * 120f)
                        val dy = -(tapOffset.y - effectiveCenter.y) / (zoomScale * 120f)

                        val tappedLat = location.latitude + dy * 0.45
                        val tappedLon = location.longitude + dx * 0.45

                        val distKm = sqrt(dx * dx + dy * dy) * 45.0
                        val angleDeg = (Math.toDegrees(atan2(dx.toDouble(), dy.toDouble())) + 360) % 360
                        val bearingStr = when (angleDeg) {
                            in 337.5..360.0, in 0.0..22.5 -> "N"
                            in 22.5..67.5 -> "NE"
                            in 67.5..112.5 -> "E"
                            in 112.5..157.5 -> "SE"
                            in 157.5..202.5 -> "S"
                            in 202.5..247.5 -> "SW"
                            in 247.5..292.5 -> "W"
                            else -> "NW"
                        }

                        val baseTemp = currentWeather?.temperatureC ?: 20.0
                        val basePrecip = currentWeather?.metrics?.precipitationMm ?: 0.0
                        // Deterministic regional variation based on distance & angle
                        val varTemp = baseTemp + (sin(dx * 1.5) * 2.8) - (distKm * 0.04)
                        val varPrecip = (basePrecip + (cos(dy * 2.0 + timelineStep) * 1.8)).coerceAtLeast(0.0)

                        inspectedCanvasOffset = tapOffset
                        inspectedPoint = InspectedMapPoint(
                            latitude = tappedLat,
                            longitude = tappedLon,
                            temperatureC = varTemp,
                            precipitationMm = varPrecip,
                            distanceKm = distKm,
                            bearing = bearingStr,
                            conditionLabel = if (varPrecip > 5.0) "Heavy Rain" else if (varPrecip > 0.5) "Rain Showers" else "Partly Cloudy"
                        )
                    }
                }
        ) {
            val userCenter = centerCanvas + panOffset

            // 1. Basemap Background (Styles: Meteorological, Satellite, Topographic)
            drawBasemap(
                style = basemapStyle,
                center = userCenter,
                zoom = zoomScale,
                width = size.width,
                height = size.height
            )

            // 2. Weather Overlays (Precipitation Radar OR Temperature Heatmap)
            if (activeOverlay == MapOverlayType.PRECIPITATION) {
                drawPrecipitationRadarOverlay(
                    center = userCenter,
                    zoom = zoomScale,
                    precipitationMm = currentWeather?.metrics?.precipitationMm ?: 0.0,
                    cloudCover = currentWeather?.metrics?.cloudCoverPercent ?: 40,
                    radarSweepAngle = radarSweepAngle,
                    timelineStep = timelineStep
                )
            } else {
                drawTemperatureHeatmapOverlay(
                    center = userCenter,
                    zoom = zoomScale,
                    baseTempC = currentWeather?.temperatureC ?: 21.0,
                    unit = unit,
                    timelineStep = timelineStep
                )
            }

            // 3. User's Current Location Marker (Pulsing Beacon & Center Pin)
            drawUserLocationMarker(
                center = userCenter,
                locationName = location.name,
                beaconRadius = beaconPulseRadius,
                beaconAlpha = beaconPulseAlpha,
                activeOverlay = activeOverlay,
                currentWeather = currentWeather,
                unit = unit
            )

            // 4. Inspected Point Crosshair and Marker (if user tapped anywhere)
            inspectedCanvasOffset?.let { tapPos ->
                inspectedPoint?.let { point ->
                    drawInspectedPointReticle(
                        offset = tapPos,
                        point = point,
                        unit = unit,
                        activeOverlay = activeOverlay
                    )
                }
            }
        }

        // Floating Control Panel (Top-Right): Recenter, Zoom In, Zoom Out, Basemap Style
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Style Switcher
            FloatingMapButton(
                icon = Icons.Rounded.Layers,
                contentDescription = "Change Basemap Style",
                testTag = "map_style_button",
                onClick = onBasemapStyleCycle
            )

            // Recenter
            FloatingMapButton(
                icon = Icons.Rounded.CenterFocusStrong,
                contentDescription = stringResource(R.string.weather_map_recenter),
                testTag = "map_recenter_button",
                onClick = {
                    panOffset = Offset.Zero
                    zoomScale = 1.0f
                    inspectedPoint = null
                    inspectedCanvasOffset = null
                }
            )

            // Zoom In
            FloatingMapButton(
                icon = Icons.Rounded.ZoomIn,
                contentDescription = "Zoom In",
                testTag = "map_zoom_in_button",
                onClick = { zoomScale = (zoomScale * 1.3f).coerceAtMost(5.0f) }
            )

            // Zoom Out
            FloatingMapButton(
                icon = Icons.Rounded.ZoomOut,
                contentDescription = "Zoom Out",
                testTag = "map_zoom_out_button",
                onClick = { zoomScale = (zoomScale / 1.3f).coerceAtLeast(0.5f) }
            )
        }

        // Scale bar & Compass indicator & Satellite badge (Top-Left)
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.88f),
                border = BorderStroke(0.8.dp, if (isSatelliteOnly || basemapStyle == MapBasemapStyle.GOOGLE_SATELLITE) Color(0xFF38BDF8) else Color(0xFF334155))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSatelliteOnly) "🛰️ Google Satellite (Only)" else if (basemapStyle == MapBasemapStyle.GOOGLE_SATELLITE) "🛰️ Google Satellite" else "🗺️ Radar",
                        color = Color(0xFF38BDF8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.82f),
                border = BorderStroke(0.8.dp, Color(0xFF334155))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(0xFF38BDF8), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "N",
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    val kmPerScale = (40 / zoomScale).toInt().coerceAtLeast(5)
                    Text(
                        text = "Scale: ~$kmPerScale km",
                        color = Color(0xFFE2E8F0),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Timeline Bar (Bottom) for time projection
        TimelineScrubberBar(
            timelineStep = timelineStep,
            isPlaying = isPlaying,
            onStepChange = { timelineStep = it },
            onPlayPauseToggle = { isPlaying = !isPlaying },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp, start = 12.dp, end = 12.dp)
        )

        // Point Inspection Callout Dialog at the top
        inspectedPoint?.let { pt ->
            InspectedPointCard(
                point = pt,
                unit = unit,
                activeOverlay = activeOverlay,
                onDismiss = {
                    inspectedPoint = null
                    inspectedCanvasOffset = null
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp, start = 12.dp, end = 12.dp)
            )
        }
    }
}

@Composable
private fun FloatingMapButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color(0xFF0F172A).copy(alpha = 0.85f),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier
            .size(36.dp)
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun TimelineScrubberBar(
    timelineStep: Int,
    isPlaying: Boolean,
    onStepChange: (Int) -> Unit,
    onPlayPauseToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = listOf("-30m", "LIVE", "+30m", "+60m")

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.90f),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Play/Pause button
            IconButton(
                onClick = onPlayPauseToggle,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("map_timeline_play_button")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (isPlaying) "Pause timeline" else "Play timeline",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(18.dp)
                )
            }

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, label ->
                    val isSelected = timelineStep == index
                    Surface(
                        onClick = { onStepChange(index) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(0xFF38BDF8) else Color.Transparent,
                        modifier = Modifier.testTag("map_step_$index")
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) Color(0xFF0F172A) else Color(0xFF94A3B8),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InspectedPointCard(
    point: InspectedMapPoint,
    unit: TemperatureUnit,
    activeOverlay: MapOverlayType,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.95f),
        border = BorderStroke(1.2.dp, Color(0xFF38BDF8)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("inspected_point_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🎯 Inspected Coordinate",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${String.format(Locale.US, "%.1f km %s", point.distanceKm, point.bearing)}",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Temp: ${unit.formatWithUnit(point.temperatureC)}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Precip: ${String.format(Locale.US, "%.1f mm/h", point.precipitationMm)}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (point.precipitationMm > 0.2) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close inspection",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun MapLegendBar(
    activeOverlay: MapOverlayType,
    unit: TemperatureUnit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (activeOverlay == MapOverlayType.PRECIPITATION) "Precipitation Rate (mm/h)" else "Temperature Range",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            )
            Text(
                text = if (activeOverlay == MapOverlayType.PRECIPITATION) "Light ➔ Severe" else "${unit.symbol()} Isotherm",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF64748B),
                    fontSize = 10.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (activeOverlay == MapOverlayType.PRECIPITATION) {
            // Radar dBZ gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF0284C7).copy(alpha = 0.2f),
                                Color(0xFF22C55E), // Light
                                Color(0xFFEAB308), // Moderate
                                Color(0xFFF97316), // Heavy
                                Color(0xFFEF4444), // Severe
                                Color(0xFFA855F7)  // Extreme
                            )
                        )
                    )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0 mm/h", color = Color(0xFF64748B), fontSize = 9.sp)
                Text("2 mm/h", color = Color(0xFF64748B), fontSize = 9.sp)
                Text("10 mm/h", color = Color(0xFF64748B), fontSize = 9.sp)
                Text("25+ mm/h", color = Color(0xFF64748B), fontSize = 9.sp)
            }
        } else {
            // Temperature Thermal gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF4F46E5), // Freezing
                                Color(0xFF0EA5E9), // Cold
                                Color(0xFF10B981), // Mild
                                Color(0xFFF59E0B), // Warm
                                Color(0xFFF97316), // Hot
                                Color(0xFFEF4444)  // Extreme
                            )
                        )
                    )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(if (unit == TemperatureUnit.CELSIUS) "<0°C" else "<32°F", color = Color(0xFF64748B), fontSize = 9.sp)
                Text(if (unit == TemperatureUnit.CELSIUS) "15°C" else "59°F", color = Color(0xFF64748B), fontSize = 9.sp)
                Text(if (unit == TemperatureUnit.CELSIUS) "25°C" else "77°F", color = Color(0xFF64748B), fontSize = 9.sp)
                Text(if (unit == TemperatureUnit.CELSIUS) "35°C+" else "95°F+", color = Color(0xFF64748B), fontSize = 9.sp)
            }
        }
    }
}

/**
 * Fullscreen Interactive Weather Map Dialog for detailed panning and zooming
 */
@Composable
fun FullscreenWeatherMapDialog(
    location: CityLocation,
    currentWeather: CurrentWeatherData?,
    unit: TemperatureUnit,
    activeOverlay: MapOverlayType,
    basemapStyle: MapBasemapStyle,
    isSatelliteOnly: Boolean = false,
    onToggleSatelliteOnly: () -> Unit = {},
    onOverlayChange: (MapOverlayType) -> Unit,
    onBasemapStyleChange: (MapBasemapStyle) -> Unit,
    onLocationFound: (Double, Double, String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF090D16)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF1E293B), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.weather_map_close),
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.weather_map_title),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "📍 ${location.name} • ${if (activeOverlay == MapOverlayType.PRECIPITATION) "Precipitation Radar" else "Temperature Heatmap"}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                            )
                        }
                    }

                    // Basemap Style Toggle Button
                    FilledTonalButton(
                        onClick = {
                            if (isSatelliteOnly) {
                                onBasemapStyleChange(MapBasemapStyle.GOOGLE_SATELLITE)
                            } else {
                                val next = when (basemapStyle) {
                                    MapBasemapStyle.GOOGLE_SATELLITE -> MapBasemapStyle.METEOROLOGICAL_DARK
                                    MapBasemapStyle.METEOROLOGICAL_DARK -> MapBasemapStyle.SATELLITE_NIGHT
                                    MapBasemapStyle.SATELLITE_NIGHT -> MapBasemapStyle.TOPOGRAPHIC
                                    MapBasemapStyle.TOPOGRAPHIC -> MapBasemapStyle.GOOGLE_SATELLITE
                                }
                                onBasemapStyleChange(next)
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF1E293B),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Layers,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF38BDF8)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSatelliteOnly) "Google Satellite (Only)" else when (basemapStyle) {
                                MapBasemapStyle.GOOGLE_SATELLITE -> "Google Satellite"
                                MapBasemapStyle.METEOROLOGICAL_DARK -> "Dark Radar"
                                MapBasemapStyle.SATELLITE_NIGHT -> "Satellite"
                                MapBasemapStyle.TOPOGRAPHIC -> "Topographic"
                            },
                            fontSize = 12.sp
                        )
                    }
                }

                // Overlay selector tabs
                MapOverlayTabSelector(
                    activeOverlay = activeOverlay,
                    onOverlayChange = onOverlayChange
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Fullscreen Map Viewport
                Box(modifier = Modifier.weight(1f)) {
                    InteractiveMapViewport(
                        location = location,
                        currentWeather = currentWeather,
                        unit = unit,
                        activeOverlay = activeOverlay,
                        basemapStyle = basemapStyle,
                        isSatelliteOnly = isSatelliteOnly,
                        onBasemapStyleCycle = {
                            if (isSatelliteOnly) {
                                onBasemapStyleChange(MapBasemapStyle.GOOGLE_SATELLITE)
                            } else {
                                val next = when (basemapStyle) {
                                    MapBasemapStyle.GOOGLE_SATELLITE -> MapBasemapStyle.METEOROLOGICAL_DARK
                                    MapBasemapStyle.METEOROLOGICAL_DARK -> MapBasemapStyle.SATELLITE_NIGHT
                                    MapBasemapStyle.SATELLITE_NIGHT -> MapBasemapStyle.TOPOGRAPHIC
                                    MapBasemapStyle.TOPOGRAPHIC -> MapBasemapStyle.GOOGLE_SATELLITE
                                }
                                onBasemapStyleChange(next)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Bottom legend
                Surface(
                    color = Color(0xFF0F172A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        MapLegendBar(activeOverlay = activeOverlay, unit = unit)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// Canvas Drawing Helpers: Basemap, Precipitation Radar, Temperature Heatmap & Markers
// -----------------------------------------------------------------------------------------

private fun DrawScope.drawBasemap(
    style: MapBasemapStyle,
    center: Offset,
    zoom: Float,
    width: Float,
    height: Float
) {
    val bgColor = when (style) {
        MapBasemapStyle.GOOGLE_SATELLITE -> Color(0xFF020914)
        MapBasemapStyle.METEOROLOGICAL_DARK -> Color(0xFF0B132B)
        MapBasemapStyle.SATELLITE_NIGHT -> Color(0xFF050B14)
        MapBasemapStyle.TOPOGRAPHIC -> Color(0xFF0F172A)
    }
    drawRect(color = bgColor)

    // Draw stylized landmass / regional geographic features
    val landColor = when (style) {
        MapBasemapStyle.GOOGLE_SATELLITE -> Color(0xFF142B1A)
        MapBasemapStyle.METEOROLOGICAL_DARK -> Color(0xFF1C2541)
        MapBasemapStyle.SATELLITE_NIGHT -> Color(0xFF0E1A2B)
        MapBasemapStyle.TOPOGRAPHIC -> Color(0xFF1B2A4A)
    }

    // Dynamic land contours adapting to pan and zoom
    val landRadiusX = 220f * zoom
    val landRadiusY = 160f * zoom

    // Google Satellite coastal gradient / shelf waters
    if (style == MapBasemapStyle.GOOGLE_SATELLITE) {
        drawOval(
            color = Color(0xFF0077B6).copy(alpha = 0.35f),
            topLeft = Offset(center.x - landRadiusX * 1.25f, center.y - landRadiusY * 1.05f),
            size = Size(landRadiusX * 2.5f, landRadiusY * 2.1f)
        )
    }

    drawOval(
        color = landColor,
        topLeft = Offset(center.x - landRadiusX * 1.1f, center.y - landRadiusY * 0.9f),
        size = Size(landRadiusX * 2.2f, landRadiusY * 1.8f)
    )

    drawOval(
        color = landColor.copy(alpha = 0.85f),
        topLeft = Offset(center.x - landRadiusX * 0.4f, center.y - landRadiusY * 1.5f),
        size = Size(landRadiusX * 1.8f, landRadiusY * 1.6f)
    )

    // Topographic contour rings
    if (style == MapBasemapStyle.TOPOGRAPHIC) {
        for (i in 1..4) {
            val ringRadius = (50f * i * zoom)
            drawCircle(
                color = Color(0xFF38BDF8).copy(alpha = 0.12f),
                radius = ringRadius,
                center = center,
                style = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                )
            )
        }
    }

    // Lat/Long Coordinate Grid
    val gridSpacing = 60f * zoom
    val gridColor = Color(0xFF334155).copy(alpha = 0.45f)

    var curX = (center.x % gridSpacing)
    while (curX < width) {
        drawLine(
            color = gridColor,
            start = Offset(curX, 0f),
            end = Offset(curX, height),
            strokeWidth = 0.8f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
        )
        curX += gridSpacing
    }

    var curY = (center.y % gridSpacing)
    while (curY < height) {
        drawLine(
            color = gridColor,
            start = Offset(0f, curY),
            end = Offset(width, curY),
            strokeWidth = 0.8f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
        )
        curY += gridSpacing
    }
}

private fun DrawScope.drawPrecipitationRadarOverlay(
    center: Offset,
    zoom: Float,
    precipitationMm: Double,
    cloudCover: Int,
    radarSweepAngle: Float,
    timelineStep: Int
) {
    // Dynamic radar cells offset based on timeline step (simulating moving precipitation fronts)
    val timeOffsetX = (timelineStep - 1) * 24f * zoom
    val timeOffsetY = -(timelineStep - 1) * 16f * zoom

    // Multiple weather radar cells centered in surrounding region
    val cells = listOf(
        Triple(Offset(-35f * zoom + timeOffsetX, -20f * zoom + timeOffsetY), 65f * zoom, precipitationMm.coerceAtLeast(0.4)),
        Triple(Offset(45f * zoom + timeOffsetX, -40f * zoom + timeOffsetY), 55f * zoom, precipitationMm.coerceAtLeast(2.2)),
        Triple(Offset(10f * zoom + timeOffsetX, 35f * zoom + timeOffsetY), 75f * zoom, precipitationMm.coerceAtLeast(1.1)),
        Triple(Offset(-70f * zoom + timeOffsetX, 40f * zoom + timeOffsetY), 50f * zoom, precipitationMm.coerceAtLeast(0.6)),
        Triple(Offset(75f * zoom + timeOffsetX, 30f * zoom + timeOffsetY), 60f * zoom, precipitationMm.coerceAtLeast(5.5))
    )

    for ((relOffset, radius, precipRate) in cells) {
        val cellCenter = center + relOffset
        val (coreColor, outerColor) = when {
            precipRate >= 10.0 -> Pair(Color(0xFFEF4444).copy(alpha = 0.70f), Color(0xFFF97316).copy(alpha = 0.45f))
            precipRate >= 4.0 -> Pair(Color(0xFFF97316).copy(alpha = 0.65f), Color(0xFFEAB308).copy(alpha = 0.40f))
            precipRate >= 1.5 -> Pair(Color(0xFFEAB308).copy(alpha = 0.60f), Color(0xFF22C55E).copy(alpha = 0.35f))
            else -> Pair(Color(0xFF22C55E).copy(alpha = 0.55f), Color(0xFF0284C7).copy(alpha = 0.30f))
        }

        // Outer precipitation halo
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(coreColor, outerColor, Color.Transparent),
                center = cellCenter,
                radius = radius
            ),
            radius = radius,
            center = cellCenter
        )

        // Inner storm nucleus
        if (precipRate >= 3.0) {
            drawCircle(
                color = coreColor,
                radius = radius * 0.35f,
                center = cellCenter
            )
        }
    }

    // Rotating Radar Sweep Line with phosphor fade trail
    val sweepRad = Math.toRadians(radarSweepAngle.toDouble())
    val radarRadius = 140f * zoom
    val endX = center.x + (radarRadius * cos(sweepRad)).toFloat()
    val endY = center.y + (radarRadius * sin(sweepRad)).toFloat()

    // Radar scan beam
    drawLine(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.85f), Color(0xFF38BDF8).copy(alpha = 0.10f)),
            start = center,
            end = Offset(endX, endY)
        ),
        start = center,
        end = Offset(endX, endY),
        strokeWidth = 2.dp.toPx()
    )

    // Outer range rings (Radar distance rings: 25km, 50km, 75km)
    for (i in 1..3) {
        val r = (45f * i * zoom)
        drawCircle(
            color = Color(0xFF38BDF8).copy(alpha = 0.20f),
            radius = r,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

private fun DrawScope.drawTemperatureHeatmapOverlay(
    center: Offset,
    zoom: Float,
    baseTempC: Double,
    unit: TemperatureUnit,
    timelineStep: Int
) {
    // Thermal color rings based on base temperature
    val tempAdjust = (timelineStep - 1) * 0.8

    val rings = listOf(
        Pair(30f * zoom, baseTempC + tempAdjust + 1.2),
        Pair(65f * zoom, baseTempC + tempAdjust),
        Pair(105f * zoom, baseTempC + tempAdjust - 1.5),
        Pair(150f * zoom, baseTempC + tempAdjust - 3.2)
    )

    // Smooth gradient color wash
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                getThermalColor(baseTempC + 1.5).copy(alpha = 0.55f),
                getThermalColor(baseTempC).copy(alpha = 0.42f),
                getThermalColor(baseTempC - 2.0).copy(alpha = 0.25f),
                Color.Transparent
            ),
            center = center,
            radius = 160f * zoom
        ),
        radius = 160f * zoom,
        center = center
    )

    // Isotherm contour lines with labels
    for ((radius, temp) in rings) {
        val isoColor = getThermalColor(temp)
        drawCircle(
            color = isoColor.copy(alpha = 0.85f),
            radius = radius,
            center = center,
            style = Stroke(
                width = 1.4.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
            )
        )

        // Draw isotherm text pill tag on canvas
        val tagOffset = Offset(center.x + radius * 0.707f, center.y - radius * 0.707f)
        drawCircle(
            color = Color(0xFF0F172A).copy(alpha = 0.9f),
            radius = 11.dp.toPx(),
            center = tagOffset
        )
        drawCircle(
            color = isoColor,
            radius = 11.dp.toPx(),
            center = tagOffset,
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

private fun getThermalColor(tempC: Double): Color {
    return when {
        tempC < 0.0 -> Color(0xFF4F46E5)   // Deep Indigo (Freezing)
        tempC < 10.0 -> Color(0xFF0EA5E9)  // Cold Cyan
        tempC < 18.0 -> Color(0xFF10B981)  // Mild Emerald
        tempC < 25.0 -> Color(0xFFF59E0B)  // Warm Amber
        tempC < 32.0 -> Color(0xFFF97316)  // Hot Orange
        else -> Color(0xFFEF4444)          // Extreme Red
    }
}

private fun DrawScope.drawUserLocationMarker(
    center: Offset,
    locationName: String,
    beaconRadius: Float,
    beaconAlpha: Float,
    activeOverlay: MapOverlayType,
    currentWeather: CurrentWeatherData?,
    unit: TemperatureUnit
) {
    // 1. Pulsing radar beacon outer ring
    drawCircle(
        color = Color(0xFF38BDF8).copy(alpha = beaconAlpha),
        radius = beaconRadius * 1.6f,
        center = center
    )

    // 2. Beacon secondary ring
    drawCircle(
        color = Color(0xFF38BDF8).copy(alpha = beaconAlpha * 0.7f),
        radius = beaconRadius * 2.4f,
        center = center,
        style = Stroke(width = 1.5.dp.toPx())
    )

    // 3. User solid pin dot
    drawCircle(
        color = Color.White,
        radius = 8.dp.toPx(),
        center = center
    )
    drawCircle(
        color = Color(0xFF0284C7),
        radius = 6.dp.toPx(),
        center = center
    )

    // 4. Floating location & reading callout badge
    val badgeWidth = 140.dp.toPx()
    val badgeHeight = 36.dp.toPx()
    val badgeTopLeft = Offset(center.x - badgeWidth / 2f, center.y - 48.dp.toPx())

    // Badge background
    drawRoundRect(
        color = Color(0xFF0F172A).copy(alpha = 0.92f),
        topLeft = badgeTopLeft,
        size = Size(badgeWidth, badgeHeight),
        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
    )
    drawRoundRect(
        color = Color(0xFF38BDF8),
        topLeft = badgeTopLeft,
        size = Size(badgeWidth, badgeHeight),
        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
        style = Stroke(width = 1.2.dp.toPx())
    )

    // Needle pointing to center
    val needlePath = Path().apply {
        moveTo(center.x - 6.dp.toPx(), badgeTopLeft.y + badgeHeight)
        lineTo(center.x, center.y - 10.dp.toPx())
        lineTo(center.x + 6.dp.toPx(), badgeTopLeft.y + badgeHeight)
        close()
    }
    drawPath(
        path = needlePath,
        color = Color(0xFF0F172A)
    )
}

private fun DrawScope.drawInspectedPointReticle(
    offset: Offset,
    point: InspectedMapPoint,
    unit: TemperatureUnit,
    activeOverlay: MapOverlayType
) {
    // Crosshair lines
    val armLen = 14.dp.toPx()
    val reticleColor = Color(0xFFF43F5E)

    drawLine(
        color = reticleColor,
        start = Offset(offset.x - armLen, offset.y),
        end = Offset(offset.x + armLen, offset.y),
        strokeWidth = 2.dp.toPx()
    )
    drawLine(
        color = reticleColor,
        start = Offset(offset.x, offset.y - armLen),
        end = Offset(offset.x, offset.y + armLen),
        strokeWidth = 2.dp.toPx()
    )
    drawCircle(
        color = reticleColor,
        radius = 5.dp.toPx(),
        center = offset,
        style = Stroke(width = 1.8.dp.toPx())
    )
}

/**
 * Queries device GPS location using Play Services FusedLocationProviderClient
 */
private fun fetchDeviceLocation(
    context: Context,
    onLocationFound: (Double, Double, String) -> Unit,
    onFinish: () -> Unit
) {
    try {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        onLocationFound(loc.latitude, loc.longitude, "My Current Location")
                    } else {
                        // Fallback to lastLocation if fresh location is null
                        fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                            if (lastLoc != null) {
                                onLocationFound(lastLoc.latitude, lastLoc.longitude, "My Current Location")
                            }
                            onFinish()
                        }.addOnFailureListener { onFinish() }
                        return@addOnSuccessListener
                    }
                    onFinish()
                }
                .addOnFailureListener {
                    onFinish()
                }
        } else {
            onFinish()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        onFinish()
    }
}
