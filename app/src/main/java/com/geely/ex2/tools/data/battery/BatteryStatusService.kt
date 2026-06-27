package com.geely.ex2.tools.data.battery

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.geely.ex2.tools.data.vhal.VhalBatteryReader
import com.geely.ex2.tools.data.vhal.VhalBatteryReaderFactory

class BatteryStatusService : Service() {
    private var reader: VhalBatteryReader? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(
            BatteryStatusIconHelper.SERVICE_NOTIFICATION_ID,
            BatteryStatusIconHelper.buildServiceNotification(this),
        )
        Log.i(TAG, "Battery service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val reason = intent?.getStringExtra(EXTRA_REASON) ?: "service start"

        if (!BatterySettings.isEnabled(this)) {
            Log.i(TAG, "Battery service stopping, disabled: $reason")
            stopReader()
            BatteryStatusIconHelper.cancelStatusIcon(this)
            stopSelf()
            return START_NOT_STICKY
        }

        startReader(reason)
        return START_STICKY
    }

    override fun onDestroy() {
        stopReader()
        BatteryStatusIconHelper.cancelStatusIcon(this)
        Log.i(TAG, "Battery service destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startReader(reason: String) {
        if (reader != null) return

        val batteryReader = VhalBatteryReaderFactory.create(this).also { reader = it }
        batteryReader.startListening(
            onUpdate = { sample ->
                if (!BatterySettings.isEnabled(this)) {
                    Log.i(TAG, "Battery disabled during poll, stopping listener")
                    stopReader()
                    BatteryStatusIconHelper.cancelStatusIcon(this)
                    stopSelf()
                    return@startListening
                }

                if (sample.isAvailable) {
                    Log.d(TAG, "Battery SOC: ${sample.socPercent}%, source: ${sample.source}")
                } else {
                    Log.w(TAG, "Battery read failed: ${sample.source}")
                }
                BatteryStatusIconHelper.notifyBattery(this, sample, "listener")
                BatteryAppWidgetHelper.updateAll(this, "listener")
            },
            shouldContinue = { BatterySettings.isEnabled(this) },
        )
        Log.i(TAG, "Battery listener started: $reason")
    }

    private fun stopReader() {
        reader?.stopListening()
        reader?.close()
        reader = null
    }

    companion object {
        const val EXTRA_REASON = "reason"
        private const val TAG = "GeelyToolsBattery"
    }
}
