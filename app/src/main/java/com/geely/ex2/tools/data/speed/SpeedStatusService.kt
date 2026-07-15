package com.geely.ex2.tools.data.speed

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.geely.ex2.tools.data.vhal.CarPropertyIo
import com.geely.ex2.tools.data.vhal.VhalConstants
import com.geely.ex2.tools.data.vhal.VhalSpeedReader
import com.geely.ex2.tools.data.vhal.VhalSpeedReaderFactory

class SpeedStatusService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var reader: VhalSpeedReader? = null

    @Volatile
    private var isRunning = false

    private val updateRunnable = Runnable {
        if (!isRunning || !SpeedSettings.isEnabled(this)) {
            return@Runnable
        }
        updateSpeed("periodic")
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
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
            isRunning = false
            handler.removeCallbacks(updateRunnable)
            SpeedStatusIconHelper.cancelStatusIcon(this)
            closeReaderAsync()
            stopSelf()
            return START_NOT_STICKY
        }

        isRunning = true
        handler.removeCallbacks(updateRunnable)
        updateSpeed(reason)

        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        handler.removeCallbacks(updateRunnable)
        SpeedStatusIconHelper.cancelStatusIcon(this)
        closeReaderAsync()
        Log.i(TAG, "Speed service destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun updateSpeed(reason: String) {
        CarPropertyIo.execute {
            if (!isRunning || !SpeedSettings.isEnabled(this@SpeedStatusService)) {
                return@execute
            }

            val speedReader = reader ?: VhalSpeedReaderFactory.create(this@SpeedStatusService).also {
                reader = it
            }
            val sample = speedReader.readSpeed()
            if (!isRunning || !SpeedSettings.isEnabled(this@SpeedStatusService)) {
                return@execute
            }

            if (sample.isAvailable) {
                Log.d(TAG, "Vehicle speed: ${sample.speedKmh} km/h, source: ${sample.source}")
            } else {
                Log.w(TAG, "Speed read failed: ${sample.source}")
            }
            SpeedSampleStore.publish(sample)
            SpeedStatusIconHelper.notifySpeed(
                this@SpeedStatusService,
                sample,
                reason,
                force = reason != "periodic",
            )
            scheduleNextPoll()
        }
    }

    private fun scheduleNextPoll() {
        if (!isRunning || !SpeedSettings.isEnabled(this)) {
            return
        }
        handler.removeCallbacks(updateRunnable)
        handler.postDelayed(updateRunnable, VhalConstants.SPEED_POLL_INTERVAL_MS)
    }

    private fun closeReaderAsync() {
        CarPropertyIo.execute {
            reader?.close()
            reader = null
        }
    }

    companion object {
        const val EXTRA_REASON = "reason"
        private const val TAG = "GeelyToolsSpeed"
    }
}
