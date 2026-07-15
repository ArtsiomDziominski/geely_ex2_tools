package com.geely.ex2.tools.data.driving

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.geely.ex2.tools.data.vhal.VhalConstants

object DrivingSettings {
    private const val PREFS = "geelytools_driving"
    private const val KEY_PERSIST_ENABLED = "persist_enabled"
    private const val KEY_SAVED_MODE_VALUE = "saved_mode_value"
    private const val KEY_REGEN_PERSIST_ENABLED = "regen_persist_enabled"
    private const val KEY_SAVED_REGEN_VALUE = "saved_regen_value"

    fun isPersistEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_PERSIST_ENABLED, true)

    fun setPersistEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_PERSIST_ENABLED, enabled).apply()
    }

    fun getSavedModeValue(context: Context): Int =
        prefs(context).getInt(KEY_SAVED_MODE_VALUE, VhalConstants.DRIVE_MODE_COMFORT)

    fun setSavedModeValue(context: Context, modeValue: Int) {
        prefs(context).edit().putInt(KEY_SAVED_MODE_VALUE, modeValue).apply()
    }

    fun hasSavedMode(context: Context): Boolean =
        prefs(context).contains(KEY_SAVED_MODE_VALUE)

    fun isRegenPersistEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_REGEN_PERSIST_ENABLED, false)

    fun setRegenPersistEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_REGEN_PERSIST_ENABLED, enabled).apply()
    }

    fun getSavedRegenValue(context: Context): Int {
        val raw = prefs(context).getInt(
            KEY_SAVED_REGEN_VALUE,
            VhalConstants.ENERGY_REGENERATION_LEVEL_MID,
        )
        val normalized = EnergyRegenerationValues.normalizeStoredValue(raw)
        if (normalized != raw) {
            setSavedRegenValue(context, normalized)
        }
        return normalized
    }

    fun setSavedRegenValue(context: Context, levelValue: Int) {
        prefs(context).edit()
            .putInt(KEY_SAVED_REGEN_VALUE, EnergyRegenerationValues.normalizeStoredValue(levelValue))
            .apply()
    }

    fun hasAnyPersistEnabled(context: Context): Boolean =
        isPersistEnabled(context) || isRegenPersistEnabled(context)

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
