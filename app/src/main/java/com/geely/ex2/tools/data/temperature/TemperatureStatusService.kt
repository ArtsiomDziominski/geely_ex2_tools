package com.geely.ex2.tools.data.temperature

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log

class TemperatureStatusService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var reader: TemperatureReader? = null
    private var isRunning = false

    private val updateRunnable = object : Runnable {
        override fun run() {
            updateTemperature("periodic")
            if (isRunning && TemperatureSettings.isEnabled(this@TemperatureStatusService)) {
                handler.postDelayed(this, UPDATE_INTERVAL_MS)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        reader = TemperatureReader(this)
        isRunning = true
        startForeground(
            TemperatureStatusIconHelper.SERVICE_NOTIFICATION_ID,
            TemperatureStatusIconHelper.buildServiceNotification(this),
        )
        Log.i(TAG, "Temperature service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val reason = intent?.getStringExtra(EXTRA_REASON) ?: "service start"

        if (!TemperatureSettings.isEnabled(this)) {
            Log.i(TAG, "Temperature service stopping, disabled: $reason")
            TemperatureStatusIconHelper.cancelStatusIcon(this)
            stopSelf()
            return START_NOT_STICKY
        }

        updateTemperature(reason)
        handler.removeCallbacks(updateRunnable)
        handler.postDelayed(updateRunnable, UPDATE_INTERVAL_MS)

        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        handler.removeCallbacks(updateRunnable)
        TemperatureStatusIconHelper.cancelStatusIcon(this)
        reader?.close()
        reader = null
        Log.i(TAG, "Temperature service destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun updateTemperature(reason: String) {
        val temperatureReader = reader ?: TemperatureReader(this).also { reader = it }
        val result = temperatureReader.readTemperature()
        if (result.ok) {
            Log.i(TAG, "Outside temperature: ${result.value} C, source: ${result.source}")
        } else {
            Log.w(TAG, "Temperature read failed: ${result.source}, details: ${result.details}")
        }
        TemperatureStatusIconHelper.notifyTemperature(this, result, reason)
    }

    companion object {
        const val EXTRA_REASON = "reason"
        private const val UPDATE_INTERVAL_MS = 100_000L
        private const val TAG = "GeeKitTemperature"
    }
}
