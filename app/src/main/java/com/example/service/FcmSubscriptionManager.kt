package com.example.service

import android.content.Context
import android.util.Log
import com.example.model.CityLocation
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import java.util.Locale

object FcmSubscriptionManager {
    private const val TAG = "FcmSubscription"

    /**
     * Converts a city name and country into a safe, valid FCM topic name:
     * Letters, numbers, hyphens, and underscores only. Length <= 900.
     */
    fun sanitizeTopicName(city: CityLocation): String {
        val raw = "${city.name}_${city.country ?: ""}".lowercase(Locale.ROOT)
        val clean = raw.replace(Regex("[^a-z0-9_-]"), "_").take(80).trimEnd('_')
        return "weather_alert_$clean"
    }

    /**
     * Synchronizes Firebase Cloud Messaging topic subscriptions
     * according to the user's active saved locations.
     */
    suspend fun syncTopicSubscriptions(
        context: Context,
        savedCities: List<CityLocation>
    ): List<String> {
        val activeTopics = mutableListOf<String>()
        try {
            val fcm = FirebaseMessaging.getInstance()

            // Always subscribe to general severe alerts topic
            val globalTopic = "weather_severe_global"
            try {
                fcm.subscribeToTopic(globalTopic).await()
                activeTopics.add(globalTopic)
                Log.d(TAG, "Subscribed to FCM topic: $globalTopic")
            } catch (e: Exception) {
                Log.w(TAG, "Failed subscribing to global topic: ${e.message}")
            }

            // Subscribe to each saved city's designated topic
            for (city in savedCities) {
                val topic = sanitizeTopicName(city)
                try {
                    fcm.subscribeToTopic(topic).await()
                    activeTopics.add(topic)
                    Log.d(TAG, "Subscribed to FCM topic for ${city.name}: $topic")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed subscribing to topic $topic: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "FCM Subscription error: ${e.message}")
        }
        return activeTopics
    }

    /**
     * Unsubscribes from a specific removed city topic
     */
    suspend fun unsubscribeCity(city: CityLocation) {
        try {
            val topic = sanitizeTopicName(city)
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).await()
            Log.d(TAG, "Unsubscribed from FCM topic: $topic")
        } catch (e: Exception) {
            Log.w(TAG, "Failed unsubscribing from topic: ${e.message}")
        }
    }

    /**
     * Retrieves current FCM Registration Token asynchronously
     */
    suspend fun fetchFcmToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed fetching FCM token: ${e.message}")
            null
        }
    }
}
