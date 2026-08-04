package com.example.util

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics

object FirebaseManager {
    private const val TAG = "FirebaseManager"

    fun logEvent(context: Context, eventName: String, params: Map<String, String> = emptyMap()) {
        try {
            val analytics = FirebaseAnalytics.getInstance(context)
            val bundle = Bundle()
            params.forEach { (key, value) ->
                bundle.putString(key, value)
            }
            analytics.logEvent(eventName, bundle)
            Log.d(TAG, "Firebase Event Logged: $eventName $params")
        } catch (e: Exception) {
            Log.d(TAG, "Analytics event fallback ($eventName): ${e.message}")
        }
    }

    fun logPlantCare(context: Context, careType: String, plantName: String) {
        logEvent(context, "plant_care_logged", mapOf(
            "care_type" to careType,
            "plant_name" to plantName
        ))
    }

    fun logThemeChanged(context: Context, themeMode: String) {
        logEvent(context, "theme_changed", mapOf("mode" to themeMode))
    }

    fun logAppRated(context: Context, rating: Int) {
        logEvent(context, "app_rated", mapOf("stars" to rating.toString()))
    }

    fun logAppShared(context: Context) {
        logEvent(context, "app_shared")
    }
}
