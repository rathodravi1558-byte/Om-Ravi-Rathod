package com.example.model

enum class CloudThreatLevel(val label: String, val levelNumber: Int) {
    CLEAR_SAFE("Safe / Low Threat", 1),
    MODERATE("Moderate / Watch", 2),
    SEVERE_ALERT("Severe Cloud Alert", 3),
    CRITICAL_STORM("Critical Storm Warning", 4);

    val isSevere: Boolean
        get() = this == SEVERE_ALERT || this == CRITICAL_STORM
}

data class CloudAnalysisResult(
    val cloudType: String,
    val cloudCoveragePercent: Int,
    val threatLevel: CloudThreatLevel,
    val isRedCloudAlert: Boolean,
    val alertHeadline: String?,
    val alertDescription: String?,
    val safetyAdvice: List<String>,
    val observedWeather: String,
    val estimatedRainProb: Int,
    val confidence: Float,
    val visualCharacteristics: List<String>,
    val estimatedTempC: Double? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun defaultSample(): CloudAnalysisResult {
            return CloudAnalysisResult(
                cloudType = "Cumulonimbus Incus & Red Shelf Clouds",
                cloudCoveragePercent = 92,
                threatLevel = CloudThreatLevel.SEVERE_ALERT,
                isRedCloudAlert = true,
                alertHeadline = "🚨 SEVERE RED CLOUD ALERT: CONVECTIVE STORM DETECTED",
                alertDescription = "Ominous red and charcoal shelf clouds detected with dense vertical anvil structure. Imminent lightning risk, abrupt microburst wind gusts, and intense rainfall expected within 15–30 minutes.",
                safetyAdvice = listOf(
                    "Seek indoor shelter immediately and avoid open areas",
                    "Stay clear of metal objects, tall trees, and electrical devices",
                    "Secure outdoor furniture and loose exterior belongings",
                    "Be prepared for rapid temperature drops and zero-visibility downpours"
                ),
                observedWeather = "Severe Thunderstorm Imminent",
                estimatedRainProb = 90,
                confidence = 0.96f,
                visualCharacteristics = listOf(
                    "Dramatic crimson & ochre storm underbelly",
                    "Turbulent multi-tier shelf cloud wall",
                    "Dense overcast with high optical thickness",
                    "Active convective updraft indicators"
                )
            )
        }
    }
}
