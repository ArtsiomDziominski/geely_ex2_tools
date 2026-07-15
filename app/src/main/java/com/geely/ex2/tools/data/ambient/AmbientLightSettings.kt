package com.geely.ex2.tools.data.ambient

import android.content.Context
import android.content.SharedPreferences
import android.os.Build

object AmbientLightSettings {
    private const val PREFS = "geelytools_ambient_light"
    private const val KEY_AUTO_ENABLED = "auto_enabled"
    private const val KEY_START_HOUR = "start_hour"
    private const val KEY_START_MINUTE = "start_minute"
    private const val KEY_END_HOUR = "end_hour"
    private const val KEY_END_MINUTE = "end_minute"

    private const val DEFAULT_START_HOUR = 22
    private const val DEFAULT_START_MINUTE = 0
    private const val DEFAULT_END_HOUR = 7
    private const val DEFAULT_END_MINUTE = 0

    @Volatile
    private var sessionMode: AmbientLightControlMode? = null

    fun isAutoModeSaved(context: Context): Boolean {
        clearLegacyControlMode(context)
        return prefs(context).getBoolean(KEY_AUTO_ENABLED, true)
    }

    fun getControlMode(context: Context): AmbientLightControlMode {
        if (isAutoModeSaved(context)) {
            return AmbientLightControlMode.AUTO
        }
        return sessionMode ?: AmbientLightControlMode.OFF
    }

    fun setControlMode(context: Context, mode: AmbientLightControlMode) {
        clearLegacyControlMode(context)
        when (mode) {
            AmbientLightControlMode.AUTO -> {
                prefs(context).edit().putBoolean(KEY_AUTO_ENABLED, true).apply()
                sessionMode = null
            }
            AmbientLightControlMode.OFF,
            AmbientLightControlMode.ON,
            -> {
                prefs(context).edit().putBoolean(KEY_AUTO_ENABLED, false).apply()
                sessionMode = mode
            }
        }
    }

    fun syncSessionModeFromVehicle(mode: AmbientLightControlMode) {
        if (mode == AmbientLightControlMode.AUTO) {
            return
        }
        sessionMode = mode
    }

    fun isScheduleEnabled(context: Context): Boolean =
        isAutoModeSaved(context)

    fun shouldRestoreOnWake(context: Context): Boolean = isScheduleEnabled(context)

    fun getStartHour(context: Context): Int =
        prefs(context).getInt(KEY_START_HOUR, DEFAULT_START_HOUR)

    fun getStartMinute(context: Context): Int =
        prefs(context).getInt(KEY_START_MINUTE, DEFAULT_START_MINUTE)

    fun getEndHour(context: Context): Int =
        prefs(context).getInt(KEY_END_HOUR, DEFAULT_END_HOUR)

    fun getEndMinute(context: Context): Int =
        prefs(context).getInt(KEY_END_MINUTE, DEFAULT_END_MINUTE)

    fun setStartTime(context: Context, hour: Int, minute: Int) {
        prefs(context).edit()
            .putInt(KEY_START_HOUR, hour.coerceIn(0, 23))
            .putInt(KEY_START_MINUTE, minute.coerceIn(0, 59))
            .apply()
    }

    fun setEndTime(context: Context, hour: Int, minute: Int) {
        prefs(context).edit()
            .putInt(KEY_END_HOUR, hour.coerceIn(0, 23))
            .putInt(KEY_END_MINUTE, minute.coerceIn(0, 59))
            .apply()
    }

    private fun clearLegacyControlMode(context: Context) {
        val preferences = prefs(context)
        if (!preferences.contains("control_mode")) {
            return
        }
        val legacyMode = preferences.getString("control_mode", null)
        preferences.edit().remove("control_mode").apply()
        if (legacyMode != null && legacyMode != AmbientLightControlMode.AUTO.name) {
            preferences.edit().putBoolean(KEY_AUTO_ENABLED, false).apply()
        }
    }

    private fun prefs(context: Context): SharedPreferences {
        val appContext = context.applicationContext
        val storageContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            appContext.createDeviceProtectedStorageContext()
        } else {
            appContext
        }
        return storageContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }
}
