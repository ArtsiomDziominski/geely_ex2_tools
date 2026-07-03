package com.geely.ex2.tools.data.driving

import android.content.Context
import android.util.Log

object DrivingWifiWake {
    @Volatile
    private var wasConnected = false

    fun onWifiStatusUpdated(context: Context, isConnected: Boolean, reason: String) {
        val reconnected = isConnected && !wasConnected
        wasConnected = isConnected

        if (!reconnected) {
            return
        }

        val appContext = context.applicationContext
        if (!DrivingSettings.isPersistEnabled(appContext)) {
            return
        }

        Log.i(DrivingModeController.TAG, "Wi-Fi reconnected, restore driving mode: $reason")
        DrivingModeController.restoreDrivingModeIfNeeded(appContext, "Wi-Fi reconnected: $reason")
    }
}
