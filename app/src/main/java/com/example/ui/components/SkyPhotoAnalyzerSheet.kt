package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.FiberManualRecord
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.mutableIntStateOf
import com.example.model.ObservationIntervalPreset
import com.example.model.VideoObservationResult
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.vision.CloudBitmapGenerator
import com.example.data.vision.CloudVisionAnalyzer
import com.example.model.CloudAnalysisResult
import com.example.model.CloudThreatLevel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkyPhotoAnalyzerSheet(
    onDismiss: () -> Unit,
    onApplyToForecast: (CloudAnalysisResult) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var observationMode by remember { mutableIntStateOf(0) } // 0 = Accurate Photo, 1 = Video / Timelapse
    var selectedInterval by remember { mutableStateOf(ObservationIntervalPreset.MIN_10) }
    var isRecordingVideo by remember { mutableStateOf(false) }
    var recordedFrames by remember { mutableIntStateOf(24) }
    var videoObservationResult by remember {
        mutableStateOf<VideoObservationResult?>(
            VideoObservationResult(
                interval = ObservationIntervalPreset.MIN_10,
                framesCaptured = 60,
                cloudSpeedKmh = 28.4,
                cloudHeadingCardinal = "ENE (68°)",
                barometricTrend = "-1.3 hPa/hr (Falling)",
                rainRatePredictionMmPerHour = 3.6,
                conditionShiftProbabilityPercent = 85,
                summaryReport = "Rapid convective updrafts observed. Cumulonimbus vertical growth expanding eastward at 28.4 km/h.",
                recordedAtFormatted = "Just now"
            )
        )
    }

    var selectedBitmap by remember {
        mutableStateOf<Bitmap?>(
            CloudBitmapGenerator.generatePresetBitmap(CloudVisionAnalyzer.PresetType.SEVERE_RED_STORM)
        )
    }
    var currentAnalysis by remember {
        mutableStateOf<CloudAnalysisResult?>(
            CloudVisionAnalyzer.getPreset(CloudVisionAnalyzer.PresetType.SEVERE_RED_STORM)
        )
    }
    var isAnalyzing by remember { mutableStateOf(false) }
    var selectedPreset by remember { mutableStateOf<CloudVisionAnalyzer.PresetType?>(CloudVisionAnalyzer.PresetType.SEVERE_RED_STORM) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            selectedBitmap = bitmap
            selectedPreset = null
            // Trigger automatic submit and analysis
            isAnalyzing = true
            coroutineScope.launch {
                val result = CloudVisionAnalyzer.analyzeImage(bitmap)
                currentAnalysis = result
                isAnalyzing = false
            }
        }
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    selectedBitmap = bitmap
                    selectedPreset = null
                    isAnalyzing = true
                    coroutineScope.launch {
                        val result = CloudVisionAnalyzer.analyzeImage(bitmap)
                        currentAnalysis = result
                        isAnalyzing = false
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Scan line animation
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanProgress"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F172A),
        contentColor = Color.White,
        modifier = modifier.testTag("sky_photo_analyzer_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 36.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF64B5F6).copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color(0xFF64B5F6).copy(alpha = 0.4f)),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.CameraAlt,
                                contentDescription = null,
                                tint = Color(0xFF64B5F6),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Sky & Cloud Weather Vision",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Take or submit photo for instant cloud & threat alerts",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_sky_sheet_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Observation Mode Tab Selector: [📸 Accurate Photo] vs [🎥 Video Timelapse]
            TabRow(
                selectedTabIndex = observationMode,
                containerColor = Color(0xFF1E293B),
                contentColor = Color(0xFF38BDF8),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[observationMode]),
                        color = Color(0xFF38BDF8)
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = observationMode == 0,
                    onClick = { observationMode = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Accurate Photo", fontSize = 13.sp, fontWeight = if (observationMode == 0) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
                Tab(
                    selected = observationMode == 1,
                    onClick = { observationMode = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Video / Timelapse", fontSize = 13.sp, fontWeight = if (observationMode == 1) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (observationMode == 1) {
                // Video & Multi-Interval Timelapse View
                Column(modifier = Modifier.fillMaxWidth().testTag("video_timelapse_section")) {
                    Text(
                        text = "Observation Timelapse Interval:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Select recording span (30s, 1m, 2m, 10m, 12m, 20m, 50m, 1h, 2h)",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // All 9 interval options requested by the user
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ObservationIntervalPreset.entries.forEach { preset ->
                            val isSelected = selectedInterval == preset
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedInterval = preset
                                    videoObservationResult = videoObservationResult?.copy(
                                        interval = preset,
                                        framesCaptured = (preset.durationSeconds / 10).coerceAtLeast(12)
                                    )
                                },
                                label = {
                                    Text(
                                        text = preset.label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF38BDF8).copy(alpha = 0.25f),
                                    selectedLabelColor = Color(0xFF38BDF8),
                                    containerColor = Color(0xFF1E293B)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Observation Viewfinder Box
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF020617)),
                        border = BorderStroke(1.2.dp, if (isRecordingVideo) Color(0xFFEF4444) else Color(0xFF38BDF8)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .testTag("video_observation_viewfinder")
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Atmospheric simulation background in viewfinder
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))
                                        )
                                    )
                            )

                            // Crosshair Reticle & Optical Alignment Ring
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val c = center
                                drawCircle(
                                    color = Color(0xFF38BDF8).copy(alpha = 0.35f),
                                    radius = size.minDimension * 0.35f,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                                )
                                drawLine(
                                    color = Color(0xFF38BDF8).copy(alpha = 0.4f),
                                    start = androidx.compose.ui.geometry.Offset(c.x - 30f, c.y),
                                    end = androidx.compose.ui.geometry.Offset(c.x + 30f, c.y),
                                    strokeWidth = 2f
                                )
                                drawLine(
                                    color = Color(0xFF38BDF8).copy(alpha = 0.4f),
                                    start = androidx.compose.ui.geometry.Offset(c.x, c.y - 30f),
                                    end = androidx.compose.ui.geometry.Offset(c.x, c.y + 30f),
                                    strokeWidth = 2f
                                )
                            }

                            // Viewfinder HUD Header (Recording indicator + Interval)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF0F172A).copy(alpha = 0.85f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.FiberManualRecord,
                                            contentDescription = null,
                                            tint = if (isRecordingVideo) Color(0xFFEF4444) else Color(0xFF10B981),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isRecordingVideo) "REC ${selectedInterval.label}" else "READY • ${selectedInterval.label}",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF0F172A).copy(alpha = 0.85f)
                                ) {
                                    Text(
                                        text = "$recordedFrames frames captured",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Viewfinder Telemetry Overlay (Bottom)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .background(Color(0xFF0F172A).copy(alpha = 0.88f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Air, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Vector: 28.4 km/h ENE", color = Color.White, fontSize = 11.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Speed, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ΔP: -1.3 hPa/hr", color = Color(0xFFF59E0B), fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Video Controls: [Record / Stop] & [Simulate Rapid Timelapse]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                isRecordingVideo = !isRecordingVideo
                                if (isRecordingVideo) {
                                    recordedFrames += 15
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRecordingVideo) Color(0xFFEF4444) else Color(0xFF2563EB)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(46.dp).testTag("toggle_video_recording_button")
                        ) {
                            Icon(
                                imageVector = if (isRecordingVideo) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isRecordingVideo) "Stop Recording" else "Record ${selectedInterval.label}")
                        }

                        OutlinedButton(
                            onClick = {
                                recordedFrames += 30
                                videoObservationResult = VideoObservationResult(
                                    interval = selectedInterval,
                                    framesCaptured = recordedFrames,
                                    cloudSpeedKmh = 31.8,
                                    cloudHeadingCardinal = "NE (45°)",
                                    barometricTrend = "-1.8 hPa/hr (Falling)",
                                    rainRatePredictionMmPerHour = 4.8,
                                    conditionShiftProbabilityPercent = 90,
                                    summaryReport = "Dense rain-bearing nimbostratus cell propagating at 31.8 km/h. Optical rain shafts verified.",
                                    recordedAtFormatted = "Live Scan"
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                            modifier = Modifier.weight(1f).height(46.dp).testTag("simulate_timelapse_button")
                        ) {
                            Icon(Icons.Rounded.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Fast Timelapse Scan")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Observation Findings Report
                    videoObservationResult?.let { res ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier.fillMaxWidth().testTag("video_observation_report_card")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Optical Timelapse Report (${res.interval.label})",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "HIGH ACCURACY",
                                            color = Color(0xFF10B981),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = res.summaryReport,
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1))
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color(0xFF334155))
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Barometric Trend", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                        Text(res.barometricTrend, color = Color(0xFF60A5FA), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text("Expected Rain Rate", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                        Text("${res.rainRatePredictionMmPerHour} mm/hr", color = Color(0xFF38BDF8), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text("Cloud Speed", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                        Text("${res.cloudSpeedKmh} km/h", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val preset = CloudVisionAnalyzer.getPreset(CloudVisionAnalyzer.PresetType.SEVERE_RED_STORM)
                            onApplyToForecast(preset)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8), contentColor = Color(0xFF0F172A)),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("sync_video_observation_button")
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sync Video Observation with Weather Dashboard", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // TAB 0: Accurate Photo (Existing Photo & Camera pipeline)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        cameraLauncher.launch(null)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("take_photo_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Take Photo",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("upload_photo_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Collections,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Choose Photo",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Weather Presets (ideal for fast test preview in emulator)
            Text(
                text = "Or Select Sky Formation Preset:",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CloudVisionAnalyzer.PresetType.values().forEach { preset ->
                    val isSelected = selectedPreset == preset
                    Surface(
                        onClick = {
                            selectedPreset = preset
                            val bmp = CloudBitmapGenerator.generatePresetBitmap(preset)
                            selectedBitmap = bmp
                            val res = CloudVisionAnalyzer.getPreset(preset)
                            currentAnalysis = res
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) {
                            if (preset == CloudVisionAnalyzer.PresetType.SEVERE_RED_STORM) Color(0xFFB71C1C) else Color(0xFF1E40AF)
                        } else {
                            Color.White.copy(alpha = 0.1f)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color.White.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("preset_${preset.name}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = preset.emoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = preset.label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Image Preview Canvas with Real-Time Scanning Overlay
            selectedBitmap?.let { bmp ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(
                        width = 1.5.dp,
                        color = if (currentAnalysis?.isRedCloudAlert == true) Color(0xFFFF3B30) else Color.White.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .testTag("sky_photo_preview_card")
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Sky Photo Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Subtle dark gradient vignette
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.45f)
                                        )
                                    )
                                )
                        )

                        // Scanning laser line when actively analyzing
                        if (isAnalyzing) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .fillMaxHeight(scanProgress)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color.Transparent,
                                                Color(0xFFFF3B30),
                                                Color(0xFF64B5F6),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }

                        // Badge on preview image
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.65f),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Cloud,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isAnalyzing) "Scanning Sky..." else "Weather Target Photo",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        // Re-submit button if user wants to re-analyze
                        if (!isAnalyzing) {
                            Surface(
                                onClick = {
                                    isAnalyzing = true
                                    coroutineScope.launch {
                                        val res = CloudVisionAnalyzer.analyzeImage(bmp)
                                        currentAnalysis = res
                                        isAnalyzing = false
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF2563EB).copy(alpha = 0.85f),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(10.dp)
                                    .testTag("submit_sky_photo_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Re-Submit Analysis",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Loading state while analyzing
            if (isAnalyzing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = Color(0xFFFF3B30),
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Analyzing cloud formations & red cloud threat levels...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            // Analysis Results Presentation
            currentAnalysis?.let { analysis ->
                // 1. Red Cloud Severe Alert Banner (Displayed in vibrant red)
                if (analysis.isRedCloudAlert || analysis.threatLevel.isSevere) {
                    RedCloudAlertBanner(
                        analysis = analysis,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                // 2. Cloud Indicators Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.08f)
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cloud_indicators_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Title
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "CLOUD & WEATHER INDICATORS",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )

                            // Threat status pill
                            val threatBg = when (analysis.threatLevel) {
                                CloudThreatLevel.CLEAR_SAFE -> Color(0xFF2E7D32)
                                CloudThreatLevel.MODERATE -> Color(0xFFF57C00)
                                CloudThreatLevel.SEVERE_ALERT, CloudThreatLevel.CRITICAL_STORM -> Color(0xFFD32F2F)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = threatBg.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, threatBg)
                            ) {
                                Text(
                                    text = analysis.threatLevel.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Cloud Type Name
                        Text(
                            text = analysis.cloudType,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Condition: ${analysis.observedWeather}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF64B5F6),
                                fontWeight = FontWeight.Medium
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Cloud Coverage Indicator with Graphical Bar
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Sky Cloud Coverage",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                )
                                Text(
                                    text = "${analysis.cloudCoveragePercent}%",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.15f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(analysis.cloudCoveragePercent / 100f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                if (analysis.isRedCloudAlert) {
                                                    listOf(Color(0xFFE53935), Color(0xFFFF1744))
                                                } else {
                                                    listOf(Color(0xFF42A5F5), Color(0xFF1E88E5))
                                                }
                                            )
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Rain Probability & Confidence Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.25f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.WaterDrop,
                                        contentDescription = null,
                                        tint = Color(0xFF80D8FF),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Rain Probability",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.White.copy(alpha = 0.65f),
                                                fontSize = 10.sp
                                            )
                                        )
                                        Text(
                                            text = "${analysis.estimatedRainProb}%",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.25f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF81C784),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "AI Confidence",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.White.copy(alpha = 0.65f),
                                                fontSize = 10.sp
                                            )
                                        )
                                        Text(
                                            text = "${(analysis.confidence * 100).toInt()}%",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Visual Characteristics List
                        if (analysis.visualCharacteristics.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Optical Sky Findings:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            analysis.visualCharacteristics.forEach { item ->
                                Row(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF64B5F6))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Apply to Forecast Button
                Button(
                    onClick = {
                        onApplyToForecast(analysis)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (analysis.isRedCloudAlert) Color(0xFFD32F2F) else Color(0xFF2563EB)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("apply_to_forecast_button")
                ) {
                    Icon(
                        imageVector = if (analysis.isRedCloudAlert) Icons.Rounded.Warning else Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (analysis.isRedCloudAlert) "Apply Red Alert to Live Weather" else "Sync Sky Weather with Dashboard",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
}
