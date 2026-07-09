package com.geely.ex2.tools.data.battery

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.geely.ex2.tools.data.vhal.CarPropertyIo
import com.geely.ex2.tools.data.vhal.VhalBatteryReaderFactory

object BatteryAppStarter {
    fun startServiceIfEnabled(context: Context, reason: String) {
        val appContext = context.applicationContext
        if (!BatterySettings.isEnabled(appContext)) {
            Log.i(TAG, "Battery service not started, disabled: $reason")
            stopService(appContext, reason)
            BatteryStatusIconHelper.cancelStatusIcon(appContext)
            return
        }
        startService(appContext, reason)
    }

    fun notifyStatusIconIfEnabled(context: Context, reason: String, rank: Int? = null) {
        val appContext = context.applicationContext
        if (!BatterySettings.isEnabled(appContext)) {
            BatteryStatusIconHelper.cancelStatusIcon(appContext)
            return
        }

        val iconRank = rank ?: BatterySettings.getStatusIconRank(appContext)
        CarPropertyIo.execute {
            val reader = VhalBatteryReaderFactory.create(appContext)
            val sample = try {
                reader.readBatterySoc()
            } finally {
                reader.close()
            }
            BatteryStatusIconHelper.notifyBattery(
                context = appContext,
                sample = sample,
                reason = reason,
                rank = iconRank,
            )
            BatteryAppWidgetHelper.updateAll(appContext, reason)
        }
    }

    fun stopService(context: Context, reason: String) {
        Log.i(TAG, "Battery service stop requested: $reason")
        val appContext = context.applicationContext
        val intent = Intent(appContext, BatteryStatusService::class.java).apply {
            putExtra(BatteryStatusService.EXTRA_REASON, reason)
        }
        appContext.startService(intent)
        appContext.stopService(intent)
    }

    private fun startService(context: Context, reason: String) {
        val intent = Intent(context, BatteryStatusService::class.java).apply {
            putExtra(BatteryStatusService.EXTRA_REASON, reason)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        Log.i(TAG, "Battery service start requested: $reason")
    }

    private const val TAG = "GeelyToolsBattery"
}
