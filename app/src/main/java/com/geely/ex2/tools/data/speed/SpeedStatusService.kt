package com.geely.ex2.tools.data.speed

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.geely.ex2.tools.data.vhal.VhalSpeedReader
import com.geely.ex2.tools.data.vhal.VhalSpeedReaderFactory

class SpeedStatusService : Service() {
    private var reader: VhalSpeedReader? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(
            SpeedStatusIconHelper.SERVICE_NOTIFICATION_ID,
            SpeedStatusIconHelper.buildServiceNotification(this),
        )
        Log.i(TAG, "Speed service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val reason = intent?.getStringExtra(EXTRA_REASON) ?: "service start"

        if (!SpeedSettings.isEnabled(this)) {
            Log.i(TAG, "Speed service stopping, disabled: $reason")
            stopReader()
            SpeedStatusIconHelper.cancelStatusIcon(this)
            stopSelf()
            return START_NOT_STICKY
        }

        startReader(reason)
        return START_STICKY
    }

    override fun onDestroy() {
        stopReader()
        SpeedStatusIconHelper.cancelStatusIcon(this)
        Log.i(TAG, "Speed service destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startReader(reason: String) {
        if (reader != null) return

        val speedReader = VhalSpeedReaderFactory.create(this).also { reader = it }
        speedReader.startListening(
            onUpdate = { sample ->
                if (!SpeedSettings.isEnabled(this)) {
                    Log.i(TAG, "Speed disabled during poll, stopping listener")
                    stopReader()
                    SpeedStatusIconHelper.cancelStatusIcon(this)
                    stopSelf()
                    return@startListening
                }

                if (sample.isAvailable) {
                    Log.d(TAG, "Vehicle speed: ${sample.speedKmh} km/h, source: ${sample.source}")
                } else {
                    Log.w(TAG, "Speed read failed: ${sample.source}")
                }
                SpeedStatusIconHelper.notifySpeed(this, sample, "listener")
            },
            shouldContinue = { SpeedSettings.isEnabled(this) },
        )
        Log.i(TAG, "Speed listener started: $reason")
    }

    private fun stopReader() {
        reader?.stopListening()
        reader?.close()
        reader = null
    }

    companion object {
        const val EXTRA_REASON = "reason"
        private const val TAG = "GeelyToolsSpeed"
    }
}
