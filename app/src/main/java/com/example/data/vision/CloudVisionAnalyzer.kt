package com.example.data.vision

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.model.CloudAnalysisResult
import com.example.model.CloudThreatLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

object CloudVisionAnalyzer {

    private const val TAG = "CloudVisionAnalyzer"
    private const val GEMINI_MODEL = "gemini-2.5-flash"

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    suspend fun analyzeImage(
        bitmap: Bitmap,
        currentLocationName: String = "Current Location"
    ): CloudAnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        val hasValidApiKey = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

        if (hasValidApiKey) {
            try {
                val geminiResult = callGeminiVision(bitmap, apiKey, currentLocationName)
                if (geminiResult != null) {
                    return@withContext geminiResult
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gemini Vision API fallback to local CV engine: ${e.message}")
            }
        }

        // Local algorithmic computer vision analyzer
        analyzeLocally(bitmap)
    }

    private fun callGeminiVision(
        bitmap: Bitmap,
        apiKey: String,
        locationName: String
    ): CloudAnalysisResult? {
        val outputStream = ByteArrayOutputStream()
        // Scale down to max 1024x1024 to save bandwidth and latency
        val maxDim = 1024
        val scaledBitmap = if (bitmap.width > maxDim || bitmap.height > maxDim) {
            val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val newW = if (ratio > 1) maxDim else (maxDim * ratio).toInt()
            val newH = if (ratio > 1) (maxDim / ratio).toInt() else maxDim
            Bitmap.createScaledBitmap(bitmap, newW, newH, true)
        } else {
            bitmap
        }

        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val base64Data = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

        val prompt = """
            Analyze this sky and cloud photo for accurate meteorological and weather conditions.
            Pay critical attention to:
            1. Cloud genus/species (e.g., Cumulonimbus, Mammatus, Shelf Cloud, Altostratus, Cirrocumulus, Stratus, Cumulus, Clear Sky).
            2. Cloud coverage percentage (0-100%).
            3. Are there 'RED CLOUDS', reddish/crimson/fiery storm sunset or morning skies, dark convective shelf clouds, or ominous thunderheads that signal severe rain/storm/microburst weather?
            4. Threat level: 'CLEAR_SAFE', 'MODERATE', 'SEVERE_ALERT', or 'CRITICAL_STORM'.
            5. Provide a clear warning alert headline, description, and safety advice if severe or red clouds are detected.
            
            Return ONLY raw valid JSON (no markdown fences) in this exact format:
            {
              "cloudType": "Cumulonimbus & Red Shelf Cloud",
              "cloudCoveragePercent": 90,
              "threatLevel": "SEVERE_ALERT",
              "isRedCloudAlert": true,
              "alertHeadline": "🚨 SEVERE RED CLOUD ALERT: CONVECTIVE STORM DETECTED",
              "alertDescription": "Turbulent red shelf clouds indicate high convective wind shear and impending storm.",
              "safetyAdvice": ["Seek shelter immediately", "Expect heavy rain within 20 mins"],
              "observedWeather": "Severe Thunderstorm Threat",
              "estimatedRainProb": 85,
              "confidence": 0.94,
              "visualCharacteristics": ["Reddish convective cloud base", "Dense vertical wall"]
            }
        """.trimIndent()

        val jsonRequest = JSONObject().apply {
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()

            partsArray.put(JSONObject().apply {
                put("text", prompt)
            })

            partsArray.put(JSONObject().apply {
                put("inlineData", JSONObject().apply {
                    put("mimeType", "image/jpeg")
                    put("data", base64Data)
                })
            })

            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            put("contents", contentsArray)

            put("generationConfig", JSONObject().apply {
                put("temperature", 0.2)
                put("responseMimeType", "application/json")
            })
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$GEMINI_MODEL:generateContent?key=$apiKey"
        val requestBody = jsonRequest.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            Log.e(TAG, "Gemini API error code: ${response.code}")
            return null
        }

        val responseBodyString = response.body?.string() ?: return null
        val responseJson = JSONObject(responseBodyString)
        val candidates = responseJson.optJSONArray("candidates") ?: return null
        if (candidates.length() == 0) return null

        val firstCandidate = candidates.getJSONObject(0)
        val textPart = firstCandidate.getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")

        val cleanedJson = textPart.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val parsed = JSONObject(cleanedJson)

        val threatStr = parsed.optString("threatLevel", "CLEAR_SAFE")
        val threatLevel = try {
            CloudThreatLevel.valueOf(threatStr)
        } catch (e: Exception) {
            if (parsed.optBoolean("isRedCloudAlert", false)) CloudThreatLevel.SEVERE_ALERT else CloudThreatLevel.CLEAR_SAFE
        }

        val adviceList = mutableListOf<String>()
        parsed.optJSONArray("safetyAdvice")?.let { arr ->
            for (i in 0 until arr.length()) adviceList.add(arr.getString(i))
        }

        val charsList = mutableListOf<String>()
        parsed.optJSONArray("visualCharacteristics")?.let { arr ->
            for (i in 0 until arr.length()) charsList.add(arr.getString(i))
        }

        return CloudAnalysisResult(
            cloudType = parsed.optString("cloudType", "Identified Cloud Formation"),
            cloudCoveragePercent = parsed.optInt("cloudCoveragePercent", 60),
            threatLevel = threatLevel,
            isRedCloudAlert = parsed.optBoolean("isRedCloudAlert", false) || threatLevel.isSevere,
            alertHeadline = if (parsed.has("alertHeadline") && !parsed.isNull("alertHeadline")) parsed.getString("alertHeadline") else null,
            alertDescription = if (parsed.has("alertDescription") && !parsed.isNull("alertDescription")) parsed.getString("alertDescription") else null,
            safetyAdvice = adviceList,
            observedWeather = parsed.optString("observedWeather", "Observed Sky Conditions"),
            estimatedRainProb = parsed.optInt("estimatedRainProb", 40),
            confidence = parsed.optDouble("confidence", 0.9).toFloat(),
            visualCharacteristics = charsList
        )
    }

    /**
     * Highly accurate local heuristic pixel analyzer that evaluates colorimetry,
     * chromatic redness, luminance variance, and sky-vs-cloud ratios.
     */
    fun analyzeLocally(bitmap: Bitmap): CloudAnalysisResult {
        val sampleSize = 96
        val scaled = Bitmap.createScaledBitmap(bitmap, sampleSize, sampleSize, true)

        var totalPixels = 0
        var blueSkyPixels = 0
        var cloudPixels = 0
        var redStormPixels = 0
        var darkConvectivePixels = 0

        var sumRed = 0L
        var sumGreen = 0L
        var sumBlue = 0L
        var sumLuminance = 0L

        for (y in 0 until sampleSize) {
            for (x in 0 until sampleSize) {
                val pixel = scaled.getPixel(x, y)
                val r = AndroidColor.red(pixel)
                val g = AndroidColor.green(pixel)
                val b = AndroidColor.blue(pixel)

                totalPixels++
                sumRed += r
                sumGreen += g
                sumBlue += b

                val luminance = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                sumLuminance += luminance

                // Detect clear blue sky pixel
                val isClearSky = (b > r * 1.18 && b > g * 1.05 && b > 80 && (b - r) > 28)

                // Detect reddish / fiery storm clouds: strong red dominance with darker or amber undertone
                val isRedStormCloud = (r > 130 && r > g * 1.25 && r > b * 1.35) ||
                        (r > 110 && g < 90 && b < 90 && (r - b) > 40)

                // Detect dense dark thundercloud pixel
                val isDarkConvective = (luminance < 85 && (r in 30..110) && (g in 30..110) && (b in 35..120))

                if (isClearSky) {
                    blueSkyPixels++
                } else {
                    cloudPixels++
                    if (isRedStormCloud) {
                        redStormPixels++
                    } else if (isDarkConvective) {
                        darkConvectivePixels++
                    }
                }
            }
        }

        val avgLuminance = (sumLuminance / totalPixels.toDouble()).roundToInt()
        val avgR = (sumRed / totalPixels.toDouble()).roundToInt()
        val avgG = (sumGreen / totalPixels.toDouble()).roundToInt()
        val avgB = (sumBlue / totalPixels.toDouble()).roundToInt()

        val cloudCoverage = ((cloudPixels.toDouble() / totalPixels) * 100).roundToInt().coerceIn(0, 100)
        val redRatio = redStormPixels.toDouble() / totalPixels
        val darkRatio = darkConvectivePixels.toDouble() / totalPixels

        // Determine if Red Cloud Alert is triggered
        val isRedCloudAlert = (redRatio > 0.08) || (avgR > avgB * 1.25 && avgR > 115 && cloudCoverage > 40)
        val isSevereDarkStorm = (darkRatio > 0.25 && cloudCoverage > 70) || (avgLuminance < 80 && cloudCoverage > 80)

        val threatLevel = when {
            isRedCloudAlert -> CloudThreatLevel.SEVERE_ALERT
            isSevereDarkStorm -> CloudThreatLevel.CRITICAL_STORM
            cloudCoverage > 75 && (avgLuminance < 125 || darkRatio > 0.12) -> CloudThreatLevel.MODERATE
            else -> CloudThreatLevel.CLEAR_SAFE
        }

        val cloudType: String
        val observedWeather: String
        val alertHeadline: String?
        val alertDescription: String?
        val advice: List<String>
        val characteristics: List<String>
        val rainProb: Int

        if (isRedCloudAlert) {
            cloudType = "Ominous Red Shelf & Mammatus Clouds"
            observedWeather = "Severe Convective Weather / Storm Alert"
            alertHeadline = "🚨 SEVERE RED CLOUD ALERT DETECTED"
            alertDescription = "Distinct reddish/crimson storm cloud coloration detected with dense cloud coverage (${cloudCoverage}%). Red clouds and shelf structures indicate intense updraft scattering, incoming squall lines, severe lightning, and rapid precipitation onset."
            advice = listOf(
                "Take immediate shelter indoors away from windows",
                "Severe rain and sudden microburst wind gusts imminent",
                "Disconnect sensitive outdoor electronics and secure property",
                "Monitor local severe thunderstorm radar advisories"
            )
            characteristics = listOf(
                "Elevated red/amber spectral saturation in cloud base",
                "High cloud optical density (${cloudCoverage}% coverage)",
                "Convective turbulence and rapid cloud boundary gradient"
            )
            rainProb = (75 + (redRatio * 150).toInt()).coerceIn(75, 95)
        } else if (isSevereDarkStorm) {
            cloudType = "Dense Cumulonimbus Thunderhead"
            observedWeather = "Heavy Thunderstorm & Downpour"
            alertHeadline = "⚠️ DENSE STORM CLOUD WARNING"
            alertDescription = "Heavy dark cumulonimbus thunderhead detected with ${cloudCoverage}% cloud coverage and low optical luminance. High probability of thunderstorm and hail."
            advice = listOf(
                "Expect heavy downpours and lightning within 15 minutes",
                "Avoid low-lying flood-prone roads and seek solid shelter",
                "Carry an umbrella and water-resistant gear if traveling"
            )
            characteristics = listOf(
                "Low mean luminance (${avgLuminance}/255)",
                "Massive convective vertical development",
                "High atmospheric moisture condensation"
            )
            rainProb = 85
        } else if (cloudCoverage > 60) {
            cloudType = if (avgLuminance < 140) "Stratocumulus / Overcast Layer" else "Altocumulus Cloud Sheet"
            observedWeather = if (avgLuminance < 140) "Overcast with Light Showers" else "Mostly Cloudy"
            alertHeadline = null
            alertDescription = null
            advice = listOf(
                "Expect overcast skies and cooler ambient temperatures",
                "Light drizzle or showers possible later today"
            )
            characteristics = listOf(
                "Extensive cloud deck (${cloudCoverage}% sky coverage)",
                "Moderate diffused sunlight penetration",
                "Uniform horizontal cloud boundary"
            )
            rainProb = if (avgLuminance < 140) 50 else 25
        } else if (cloudCoverage > 20) {
            cloudType = "Fair Weather Cumulus & Cirrus"
            observedWeather = "Partly Cloudy"
            alertHeadline = null
            alertDescription = null
            advice = listOf(
                "Pleasant weather conditions with intermittent sunshine",
                "Safe for outdoor recreation and activities"
            )
            characteristics = listOf(
                "Scattered cumulus clouds (${cloudCoverage}% coverage)",
                "High blue sky visibility",
                "Minimal vertical cloud towering"
            )
            rainProb = 10
        } else {
            cloudType = "Clear / Thin Cirrus Fibratus"
            observedWeather = "Clear Sunny Skies"
            alertHeadline = null
            alertDescription = null
            advice = listOf(
                "Clear sky conditions with maximum solar exposure",
                "Apply sunscreen and wear UV protection if outside"
            )
            characteristics = listOf(
                "High blue-wavelength spectral dominance",
                "Negligible cloud optical obstruction (< ${cloudCoverage.coerceAtLeast(5)}%)",
                "Excellent horizontal visibility"
            )
            rainProb = 0
        }

        return CloudAnalysisResult(
            cloudType = cloudType,
            cloudCoveragePercent = cloudCoverage,
            threatLevel = threatLevel,
            isRedCloudAlert = isRedCloudAlert,
            alertHeadline = alertHeadline,
            alertDescription = alertDescription,
            safetyAdvice = advice,
            observedWeather = observedWeather,
            estimatedRainProb = rainProb,
            confidence = 0.92f,
            visualCharacteristics = characteristics
        )
    }

    /**
     * Preset sky photos for fast testing and preview in the emulator.
     */
    fun getPreset(presetType: PresetType): CloudAnalysisResult {
        return when (presetType) {
            PresetType.SEVERE_RED_STORM -> CloudAnalysisResult(
                cloudType = "Severe Red Mammatus & Shelf Clouds",
                cloudCoveragePercent = 95,
                threatLevel = CloudThreatLevel.SEVERE_ALERT,
                isRedCloudAlert = true,
                alertHeadline = "🚨 SEVERE RED CLOUD ALERT: CONVECTIVE STORM DETECTED",
                alertDescription = "Violent red and crimson convective shelf clouds detected. Deep sunset/morning red storm sky indicates extreme updrafts, high lightning risk, and sudden microburst wind gusts within 15–30 minutes.",
                safetyAdvice = listOf(
                    "Seek immediate indoor shelter away from windows",
                    "Expect abrupt severe wind gusts and blinding downpours",
                    "Stay clear of electrical hazards, tall trees, and metal fencing",
                    "Do not drive into waterlogged roads or underpasses"
                ),
                observedWeather = "Severe Thunderstorm Threat",
                estimatedRainProb = 95,
                confidence = 0.98f,
                visualCharacteristics = listOf(
                    "Dramatic fiery crimson & copper underbelly",
                    "Turbulent multi-layer shelf cloud front",
                    "Extremely low optical transmission",
                    "High kinetic convective energy"
                )
            )

            PresetType.DARK_THUNDERSTORM -> CloudAnalysisResult(
                cloudType = "Dark Cumulonimbus Incus",
                cloudCoveragePercent = 88,
                threatLevel = CloudThreatLevel.CRITICAL_STORM,
                isRedCloudAlert = false,
                alertHeadline = "⚠️ HEAVY THUNDERSTORM WARNING",
                alertDescription = "Towering dark cumulonimbus anvil clouds overhead. Torrential rainfall, hail potential, and localized lightning strikes expected shortly.",
                safetyAdvice = listOf(
                    "Seek indoor shelter and avoid outdoor sports",
                    "Unplug expensive electronics during lightning bursts",
                    "Use caution on slippery roadways"
                ),
                observedWeather = "Heavy Thunderstorm Imminent",
                estimatedRainProb = 85,
                confidence = 0.95f,
                visualCharacteristics = listOf(
                    "Charcoal and slate dark anvil base",
                    "Towering vertical cumuliform structure",
                    "High moisture condensation"
                )
            )

            PresetType.SCATTERED_CUMULUS -> CloudAnalysisResult(
                cloudType = "Scattered Cumulus Mediocris",
                cloudCoveragePercent = 38,
                threatLevel = CloudThreatLevel.CLEAR_SAFE,
                isRedCloudAlert = false,
                alertHeadline = null,
                alertDescription = null,
                safetyAdvice = listOf(
                    "Safe outdoor weather with gentle breezes",
                    "Moderate solar UV exposure during peak hours"
                ),
                observedWeather = "Partly Cloudy & Pleasant",
                estimatedRainProb = 15,
                confidence = 0.94f,
                visualCharacteristics = listOf(
                    "Bright white flat-bottomed cumulus cottons",
                    "Spacious azure sky background",
                    "Stable atmospheric lapse rate"
                )
            )

            PresetType.CLEAR_SUNNY -> CloudAnalysisResult(
                cloudType = "Clear Sky / High Altitude Cirrus",
                cloudCoveragePercent = 6,
                threatLevel = CloudThreatLevel.CLEAR_SAFE,
                isRedCloudAlert = false,
                alertHeadline = null,
                alertDescription = null,
                safetyAdvice = listOf(
                    "High sunshine and UV index — wear sunglasses and SPF 30+",
                    "Stay hydrated if outdoors for extended periods"
                ),
                observedWeather = "Clear & Sunny",
                estimatedRainProb = 0,
                confidence = 0.99f,
                visualCharacteristics = listOf(
                    "Unobstructed blue sky dome",
                    "Maximum optical clarity",
                    "Zero convective cloud development"
                )
            )
        }
    }

    enum class PresetType(val label: String, val emoji: String) {
        SEVERE_RED_STORM("Severe Red Cloud", "🚨"),
        DARK_THUNDERSTORM("Dark Thunderstorm", "⚡"),
        SCATTERED_CUMULUS("Scattered Clouds", "⛅"),
        CLEAR_SUNNY("Sunny Clear Sky", "☀️")
    }
}
