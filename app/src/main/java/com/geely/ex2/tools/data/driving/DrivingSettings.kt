package com.geely.ex2.tools.data.driving

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.geely.ex2.tools.data.vhal.VhalConstants

object DrivingSettings {
    private const val PREFS = "geelytools_driving"
    private const val KEY_PERSIST_ENABLED = "persist_enabled"
    private const val KEY_SAVED_MODE_VALUE = "saved_mode_value"

    fun isPersistEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_PERSIST_ENABLED, true)

    fun setPersistEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_PERSIST_ENABLED, enabled).commit()
    }

    fun getSavedModeValue(context: Context): Int =
        prefs(context).getInt(KEY_SAVED_MODE_VALUE, VhalConstants.DRIVE_MODE_COMFORT)

    fun setSavedModeValue(context: Context, modeValue: Int) {
        prefs(context).edit().putInt(KEY_SAVED_MODE_VALUE, modeValue).commit()
    }

    fun hasSavedMode(context: Context): Boolean =
        prefs(context).contains(KEY_SAVED_MODE_VALUE)

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
