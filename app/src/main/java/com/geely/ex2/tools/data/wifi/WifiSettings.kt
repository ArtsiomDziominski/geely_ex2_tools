package com.geely.ex2.tools.data.wifi

import android.content.Context
import android.content.SharedPreferences
import android.os.Build

object WifiSettings {
    private const val PREFS = "wifi_status_auto_enable_prefs"
    private const val KEY_STATUS_ICON_RANK = "status_icon_rank"

    fun getStatusIconRank(context: Context): Int {
        return WifiWidgetRank.clamp(
            prefs(context).getInt(KEY_STATUS_ICON_RANK, WifiWidgetRank.DEFAULT),
        )
    }

    fun setStatusIconRank(context: Context, rank: Int) {
        prefs(context).edit()
            .putInt(KEY_STATUS_ICON_RANK, WifiWidgetRank.clamp(rank))
            .commit()
    }

    fun stepStatusIconRank(context: Context, delta: Int): Int {
        val newRank = WifiWidgetRank.clamp(getStatusIconRank(context) + delta)
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
