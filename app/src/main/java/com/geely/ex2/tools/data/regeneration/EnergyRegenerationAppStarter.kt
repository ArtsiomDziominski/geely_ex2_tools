package com.geely.ex2.tools.data.regeneration

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

object EnergyRegenerationAppStarter {
    fun startRestoreService(context: Context, reason: String) {
        val appContext = context.applicationContext
        EnergyRegenerationController.restoreEnergyRegenerationIfNeeded(appContext, reason)

        val intent = Intent(appContext, EnergyRegenerationRestoreService::class.java).apply {
            putExtra(EnergyRegenerationRestoreService.EXTRA_REASON, reason)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            appContext.startForegroundService(intent)
        } else {
            appContext.startService(intent)
        }
        Log.i(EnergyRegenerationController.TAG, "Regen restore service start requested: $reason")
    }

    fun stopRestoreService(context: Context, reason: String) {
        Log.i(EnergyRegenerationController.TAG, "Regen restore service stop requested: $reason")
        val appContext = context.applicationContext
        appContext.stopService(Intent(appContext, EnergyRegenerationRestoreService::class.java))
    }
}
