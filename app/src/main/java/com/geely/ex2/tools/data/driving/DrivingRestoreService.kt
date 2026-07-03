package com.geely.ex2.tools.data.driving

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.geely.ex2.tools.R
import com.geely.ex2.tools.data.vhal.VhalConstants

class DrivingRestoreService : Service() {
    private val handler = Handler(Looper.getMainLooper())

    private val pollRunnable = object : Runnable {
        override fun run() {
            if (!DrivingSettings.isPersistEnabled(this@DrivingRestoreService)) {
                Log.i(TAG, "Poll stopped, persist disabled")
                stopSelf()
                return
            }
            DrivingModeController.restoreDrivingModeIfNeeded(this@DrivingRestoreService, "service poll")
            handler.postDelayed(this, POLL_INTERVAL_MS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        DrivingModeController.restoreDrivingModeIfNeeded(this, "service create")
        handler.post(pollRunnable)
        Log.i(TAG, "Driving restore service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val reason = intent?.getStringExtra(EXTRA_REASON) ?: "service start"
        DrivingModeController.restoreDrivingModeIfNeeded(this, reason)
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(pollRunnable)
        Log.i(TAG, "Driving restore service destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.driving_restore_channel),
            NotificationManager.IMPORTANCE_MIN,
        )
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
            .setSmallIcon(R.drawable.ic_notification_speed)
            .setContentTitle(getString(R.string.driving_restore_notification_title))
            .setContentText(getString(R.string.driving_restore_notification_text))
            .setOngoing(true)
            .build()
    }

    companion object {
        const val EXTRA_REASON = "reason"
        private const val TAG = "GeelyToolsDriving"
        private const val CHANNEL_ID = "driving_restore"
        private const val NOTIFICATION_ID = 4106
        private const val POLL_INTERVAL_MS = VhalConstants.POLL_INTERVAL_MS
    }
}
