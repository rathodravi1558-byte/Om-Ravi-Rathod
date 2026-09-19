package com.example.data.vision

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader

object CloudBitmapGenerator {

    fun generatePresetBitmap(type: CloudVisionAnalyzer.PresetType, width: Int = 720, height: Int = 480): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (type) {
            CloudVisionAnalyzer.PresetType.SEVERE_RED_STORM -> {
                bitmap.eraseColor(Color.rgb(185, 36, 28))
                // Fiery crimson/red storm sunset shelf clouds
                val skyShader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    intArrayOf(
                        Color.rgb(35, 12, 18),
                        Color.rgb(180, 32, 28),
                        Color.rgb(220, 68, 38),
                        Color.rgb(55, 18, 22)
                    ),
                    floatArrayOf(0f, 0.35f, 0.65f, 1f),
                    Shader.TileMode.CLAMP
                )
                paint.shader = skyShader
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                // Turbulent red shelf cloud layers
                paint.shader = null
                val cloudPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(190, 140, 24, 24)
                }
                canvas.drawOval(0f, height * 0.2f, width.toFloat() * 0.7f, height * 0.75f, cloudPaint)

                cloudPaint.color = Color.argb(220, 70, 15, 20)
                canvas.drawOval(width * 0.25f, height * 0.35f, width.toFloat() * 1.1f, height * 0.9f, cloudPaint)

                // Dramatic glowing rim
                val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = RadialGradient(
                        width * 0.5f, height * 0.5f, width * 0.4f,
                        Color.argb(240, 255, 85, 45),
                        Color.TRANSPARENT,
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawCircle(width * 0.5f, height * 0.5f, width * 0.4f, glowPaint)
            }

            CloudVisionAnalyzer.PresetType.DARK_THUNDERSTORM -> {
                bitmap.eraseColor(Color.rgb(35, 45, 55))
                // Ominous charcoal and slate cumulonimbus
                val skyShader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    intArrayOf(
                        Color.rgb(22, 28, 38),
                        Color.rgb(42, 52, 68),
                        Color.rgb(30, 38, 50),
                        Color.rgb(18, 22, 30)
                    ),
                    floatArrayOf(0f, 0.4f, 0.8f, 1f),
                    Shader.TileMode.CLAMP
                )
                paint.shader = skyShader
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                // Thundercloud mass
                val cloudPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(200, 28, 34, 46)
                }
                canvas.drawOval(width * 0.1f, height * 0.15f, width * 0.9f, height * 0.7f, cloudPaint)
                cloudPaint.color = Color.argb(230, 15, 20, 28)
                canvas.drawOval(0f, height * 0.4f, width.toFloat(), height.toFloat(), cloudPaint)
            }

            CloudVisionAnalyzer.PresetType.SCATTERED_CUMULUS -> {
                bitmap.eraseColor(Color.rgb(65, 150, 240))
                // Vibrant blue sky with puffy white clouds
                val skyShader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    intArrayOf(
                        Color.rgb(33, 150, 243),
                        Color.rgb(100, 181, 246),
                        Color.rgb(187, 222, 251)
                    ),
                    floatArrayOf(0f, 0.6f, 1f),
                    Shader.TileMode.CLAMP
                )
                paint.shader = skyShader
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                // Fluffy white cumulus puffs
                val cloudPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(225, 255, 255, 255)
                }
                canvas.drawCircle(width * 0.3f, height * 0.45f, 85f, cloudPaint)
                canvas.drawCircle(width * 0.42f, height * 0.42f, 110f, cloudPaint)
                canvas.drawCircle(width * 0.55f, height * 0.46f, 90f, cloudPaint)
                canvas.drawRoundRect(width * 0.22f, height * 0.45f, width * 0.65f, height * 0.58f, 35f, 35f, cloudPaint)

                // Smaller cloud in distance
                cloudPaint.color = Color.argb(180, 255, 255, 255)
                canvas.drawCircle(width * 0.78f, height * 0.28f, 50f, cloudPaint)
                canvas.drawCircle(width * 0.85f, height * 0.26f, 65f, cloudPaint)
            }

            CloudVisionAnalyzer.PresetType.CLEAR_SUNNY -> {
                bitmap.eraseColor(Color.rgb(35, 140, 245))
                // Deep azure sunny sky with radiant sun
                val skyShader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    intArrayOf(
                        Color.rgb(13, 120, 225),
                        Color.rgb(66, 165, 245),
                        Color.rgb(144, 202, 249)
                    ),
                    floatArrayOf(0f, 0.55f, 1f),
                    Shader.TileMode.CLAMP
                )
                paint.shader = skyShader
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                // Radiant sun flare
                val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = RadialGradient(
                        width * 0.82f, height * 0.25f, 160f,
                        Color.argb(250, 255, 250, 200),
                        Color.TRANSPARENT,
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawCircle(width * 0.82f, height * 0.25f, 160f, sunPaint)
            }
        }

        return bitmap
    }
}
