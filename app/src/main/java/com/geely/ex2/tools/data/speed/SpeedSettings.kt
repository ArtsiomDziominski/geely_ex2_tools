package com.geely.ex2.tools.data.speed

import android.content.Context
import android.content.SharedPreferences
import android.os.Build

object SpeedSettings {
    private const val PREFS = "geelytools_speed"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_STATUS_ICON_RANK = "status_icon_rank"

    const val ICON_SIZE_PERCENT = 130

    fun isEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_ENABLED, true)

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).commit()
    }

    fun getStatusIconRank(context: Context): Int {
        return SpeedWidgetRank.clamp(
            prefs(context).getInt(KEY_STATUS_ICON_RANK, SpeedWidgetRank.DEFAULT),
        )
    }

    fun setStatusIconRank(context: Context, rank: Int) {
        prefs(context).edit()
            .putInt(KEY_STATUS_ICON_RANK, SpeedWidgetRank.clamp(rank))
            .commit()
    }

    fun stepStatusIconRank(context: Context, delta: Int): Int {
        val newRank = SpeedWidgetRank.clamp(getStatusIconRank(context) + delta)
        setStatusIconRank(context, newRank)
        return newRank
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
