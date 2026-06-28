package com.geely.ex2.tools.data.driving

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager

object DrivingWakeEvents {
    private const val QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON"

    fun isWakeAction(action: String?): Boolean {
        if (action == null) return false
        return action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == QUICKBOOT_POWERON ||
            action == WifiManager.WIFI_STATE_CHANGED_ACTION
    }

    fun onManifestWake(context: Context, reason: String) {
        val appContext = context.applicationContext
        if (!DrivingSettings.isPersistEnabled(appContext)) {
            DrivingAppStarter.stopRestoreService(appContext, reason)
            return
        }
        DrivingModeController.restoreDrivingModeIfNeeded(appContext, reason)
        DrivingAppStarter.startRestoreService(appContext, reason)
    }

    fun onServiceWake(context: Context, reason: String) {
        val appContext = context.applicationContext
        if (!DrivingSettings.isPersistEnabled(appContext)) {
            return
        }
        DrivingModeController.restoreDrivingModeIfNeeded(appContext, reason)
    }
}
