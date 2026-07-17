package com.geely.ex2.tools.data.wifi

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.geely.ex2.tools.data.kv.AppKv
import com.tencent.mmkv.MMKV

object WifiAutoEnableController {
    const val TAG = "WifiStatusAutoEnable"

    private const val PREFS = "wifi_status_auto_enable_prefs"
    private const val KEY_AUTO_ENABLE_WIFI = "auto_enable_wifi"

    fun isAutoEnableEnabled(context: Context): Boolean {
        return kv(context).decodeBool(KEY_AUTO_ENABLE_WIFI, true)
    }

    fun setAutoEnableEnabled(context: Context, enabled: Boolean) {
        kv(context).encode(KEY_AUTO_ENABLE_WIFI, enabled)
        Log.i(TAG, "auto_enable_wifi set to $enabled")
        if (enabled) {
            enableWifiIfNeeded(context, "auto-enable enabled from UI")
        }
    }

    fun getWifiState(context: Context): Int {
        val wifiManager = getWifiManager(context) ?: return WifiManager.WIFI_STATE_UNKNOWN
        return wifiManager.wifiState
    }

    fun isWifiEnabledOrEnabling(context: Context): Boolean {
        val state = getWifiState(context)
        return state == WifiManager.WIFI_STATE_ENABLED || state == WifiManager.WIFI_STATE_ENABLING
    }

    fun enableWifiIfNeeded(context: Context, reason: String) {
        val appContext = context.applicationContext

        if (!isAutoEnableEnabled(appContext)) {
            Log.i(TAG, "Skip Wi-Fi enable, auto-enable is disabled, reason: $reason")
            return
        }

        val wifiManager = getWifiManager(appContext)
        if (wifiManager == null) {
            Log.e(TAG, "WifiManager is null, reason: $reason")
            return
        }

        val state = wifiManager.wifiState
        Log.i(TAG, "Wi-Fi state before enable: $state, reason: $reason")

        if (state == WifiManager.WIFI_STATE_DISABLING) {
            Log.i(TAG, "Wi-Fi disabling in progress, skip enable")
            return
        }

        if (state == WifiManager.WIFI_STATE_ENABLED || state == WifiManager.WIFI_STATE_ENABLING) {
            Log.i(TAG, "Wi-Fi already enabled or enabling")
            return
        }

        @Suppress("DEPRECATION")
        val result = wifiManager.setWifiEnabled(true)
        Log.i(TAG, "setWifiEnabled(true) result: $result")
    }

    fun enableWifiFromUi(context: Context) {
        val appContext = context.applicationContext
        setAutoEnableEnabled(appContext, true)
        enableWifiIfNeeded(appContext, "UI Wi-Fi enable button")
    }

    fun disableWifiFromUi(context: Context) {
        val appContext = context.applicationContext
        setAutoEnableEnabled(appContext, false)

        val wifiManager = getWifiManager(appContext)
        if (wifiManager == null) {
            Log.e(TAG, "WifiManager is null while disabling Wi-Fi")
            return
        }

        @Suppress("DEPRECATION")
        val result = wifiManager.setWifiEnabled(false)
        Log.i(TAG, "setWifiEnabled(false) result: $result")
    }

    private fun kv(context: Context): MMKV {
        AppKv.init(context)
        return AppKv.of(PREFS)
    }

    private fun getWifiManager(context: Context): WifiManager? {
        return context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    }
}
