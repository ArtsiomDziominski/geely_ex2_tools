package com.geely.ex2.tools.data.regeneration

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.geely.ex2.tools.data.vhal.VhalConstants

object EnergyRegenerationSettings {
    private const val PREFS = "geelytools_regen"
    private const val KEY_PERSIST_ENABLED = "persist_enabled"
    private const val KEY_SAVED_LEVEL_VALUE = "saved_level_value"

    fun isPersistEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_PERSIST_ENABLED, true)

    fun setPersistEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_PERSIST_ENABLED, enabled).commit()
    }

    fun getSavedLevelValue(context: Context): Int =
        prefs(context).getInt(KEY_SAVED_LEVEL_VALUE, VhalConstants.REGEN_LEVEL_MID)

    fun setSavedLevelValue(context: Context, levelValue: Int) {
        prefs(context).edit().putInt(KEY_SAVED_LEVEL_VALUE, levelValue).commit()
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
