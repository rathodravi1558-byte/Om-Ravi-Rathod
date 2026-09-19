package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.repository.WeatherRepository
import com.example.model.SevereAlertType
import com.example.model.SevereWeatherAlert
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherFcmMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Registration Token: $token")
        val repo = WeatherRepository(applicationContext)
        repo.saveFcmToken(token)
        
        // Resubscribe to saved location topics
        CoroutineScope(Dispatchers.IO).launch {
            FcmSubscriptionManager.syncTopicSubscriptions(applicationContext, repo.getSavedCities())
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM Message received from: ${remoteMessage.from}")

        val repo = WeatherRepository(applicationContext)
        val fcmSettings = repo.getFcmSettings()
        if (!fcmSettings.isFcmEnabled) {
            Log.d(TAG, "FCM notifications are disabled by user preference.")
            return
        }

        val data = remoteMessage.data
        val title = remoteMessage.notification?.title 
            ?: data["title"] 
            ?: "Severe Weather Alert"
        val body = remoteMessage.notification?.body 
            ?: data["body"] 
            ?: "Severe conditions detected for your saved location."

        val locationName = data["location_name"] ?: data["city"] ?: "Saved Location"
        val locationId = data["location_id"] ?: ""
        val severity = (data["severity"] ?: "SEVERE").uppercase(Locale.getDefault())
        val alertTypeStr = data["alert_type"] ?: "SEVERE_THUNDERSTORM"
        val instruction = data["instruction"] ?: "Stay indoors and monitor local radar."

        // Check if user filtered by severity
        if (fcmSettings.alertSeverityThreshold == "EXTREME_ONLY" && severity != "EXTREME") {
            Log.d(TAG, "Alert filtered out by user severity threshold ($severity < EXTREME).")
            return
        }

        // Verify if alert is relevant to user's saved locations (or current location)
        val savedCities = repo.getSavedCities()
        val isSavedLocation = savedCities.any { 
            it.name.equals(locationName, ignoreCase = true) ||
            it.id == locationId ||
            locationName.contains(it.name, ignoreCase = true)
        }
        val currentLocation = repo.getLastSelectedLocation()
        val isCurrentLocation = currentLocation.name.equals(locationName, ignoreCase = true)

        if (fcmSettings.notifySavedLocationsOnly && !isSavedLocation && !isCurrentLocation) {
            Log.d(TAG, "Alert for '$locationName' ignored because it is not in saved locations.")
            return
        }

        val alertType = try {
            SevereAlertType.valueOf(alertTypeStr)
        } catch (_: Exception) {
            SevereAlertType.SEVERE_THUNDERSTORM
        }

        val timeFormatted = SimpleDateFormat("h:mm a, MMM d", Locale.getDefault()).format(Date())
        val alert = SevereWeatherAlert(
            id = data["alert_id"] ?: "alert_${System.currentTimeMillis()}",
            locationId = locationId,
            locationName = locationName,
            alertType = alertType,
            headline = title,
            description = body,
            severity = severity,
            instruction = instruction,
            issuedAtEpochMillis = System.currentTimeMillis(),
            issuedAtFormatted = timeFormatted,
            expiresAtFormatted = data["expires"] ?: "In 3 hours"
        )

        // Store into alerts repository
        repo.addSevereWeatherAlert(alert)

        // Display high-priority system notification with LED, sound, and vibration
        showSystemNotification(alert)
    }

    private fun showSystemNotification(alert: SevereWeatherAlert) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureNotificationChannel(notificationManager)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_ALERT_ID", alert.id)
            putExtra("EXTRA_LOCATION_NAME", alert.locationName)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            alert.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val priorityColor = when (alert.severity) {
            "EXTREME" -> Color.RED
            "SEVERE" -> Color.parseColor("#E11D48") // Deep rose red
            "MODERATE" -> Color.parseColor("#D97706") // Amber
            else -> Color.parseColor("#0284C7") // Sky blue
        }

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID_SEVERE_ALERTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⚠️ ${alert.headline}")
            .setContentText("${alert.locationName}: ${alert.description}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle("⚠️ ${alert.headline} • ${alert.locationName}")
                    .bigText("${alert.description}\n\nActions Recommended:\n${alert.instruction}")
            )
            .setColor(priorityColor)
            .setColorized(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setLights(priorityColor, 1000, 500)
            .setVibrate(longArrayOf(0, 400, 200, 400, 200, 600))

        notificationManager.notify(alert.id.hashCode(), notificationBuilder.build())
    }

    companion object {
        private const val TAG = "WeatherFCM"
        const val CHANNEL_ID_SEVERE_ALERTS = "weather_severe_alerts_channel"

        fun ensureNotificationChannel(notificationManager: NotificationManager) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID_SEVERE_ALERTS)
                if (existingChannel == null) {
                    val name = "Severe Weather Emergency Alerts"
                    val descriptionText = "High-priority real-time warning alerts for thunderstorms, flash floods, hurricanes and tornadoes across saved cities."
                    val importance = NotificationManager.IMPORTANCE_HIGH
                    val channel = NotificationChannel(CHANNEL_ID_SEVERE_ALERTS, name, importance).apply {
                        description = descriptionText
                        enableLights(true)
                        lightColor = Color.RED
                        enableVibration(true)
                        vibrationPattern = longArrayOf(0, 400, 200, 400, 200, 600)
                        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                        val audioAttributes = AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                            .build()
                        setSound(soundUri, audioAttributes)
                    }
                    notificationManager.createNotificationChannel(channel)
                }
            }
        }
    }
}
