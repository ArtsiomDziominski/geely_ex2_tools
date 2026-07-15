package com.geely.ex2.tools.data.avas

import android.content.Context
import android.content.SharedPreferences
import android.os.Build

object AvasSettings {
    private const val PREFS = "geelytools_avas"
    private const val KEY_MUTED = "avas_muted_saved"
    private const val KEY_LAST_ACTIVE_MODE = "avas_last_active_mode"

    fun isMutedSaved(context: Context): Boolean =
        prefs(context).getBoolean(KEY_MUTED, false)

    fun setMutedSaved(context: Context, muted: Boolean) {
        prefs(context).edit().putBoolean(KEY_MUTED, muted).apply()
    }

    fun getLastActiveMode(context: Context): Int {
        val mode = prefs(context).getInt(KEY_LAST_ACTIVE_MODE, AvasConstants.MODE_DEFAULT_ACTIVE)
        return if (mode > AvasConstants.MODE_MUTED) mode else AvasConstants.MODE_DEFAULT_ACTIVE
    }

    fun setLastActiveMode(context: Context, mode: Int) {
        if (mode <= AvasConstants.MODE_MUTED) return
        prefs(context).edit().putInt(KEY_LAST_ACTIVE_MODE, mode).apply()
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
