package com.geely.ex2.tools.data.regeneration

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.geely.ex2.tools.R
import com.geely.ex2.tools.data.vhal.VhalConstants

class EnergyRegenerationRestoreService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var isReceiverRegistered = false

    private val pollRunnable = object : Runnable {
        override fun run() {
            if (!EnergyRegenerationSettings.isPersistEnabled(this@EnergyRegenerationRestoreService)) {
                Log.i(TAG, "Poll stopped, persist disabled")
                stopSelf()
                return
            }
            EnergyRegenerationController.restoreEnergyRegenerationIfNeeded(
                this@EnergyRegenerationRestoreService,
                "service poll",
            )
            handler.postDelayed(this, POLL_INTERVAL_MS)
        }
    }

    private val eventReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            val action = intent?.action ?: "null"
            Log.i(TAG, "Event receiver: $action")
            EnergyRegenerationController.restoreEnergyRegenerationIfNeeded(
                context,
                "service event: $action",
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        registerEventReceiver()
        EnergyRegenerationController.restoreEnergyRegenerationIfNeeded(this, "service create")
        handler.post(pollRunnable)
        Log.i(TAG, "Regen restore service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val reason = intent?.getStringExtra(EXTRA_REASON) ?: "service start"
        EnergyRegenerationController.restoreEnergyRegenerationIfNeeded(this, reason)
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(pollRunnable)
        unregisterEventReceiver()
        Log.i(TAG, "Regen restore service destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun registerEventReceiver() {
        if (isReceiverRegistered) return

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(ACTION_QUICKBOOT_POWERON)
        }
        registerReceiver(eventReceiver, filter)
        isReceiverRegistered = true
        Log.i(TAG, "Regen event receiver registered")
    }

    private fun unregisterEventReceiver() {
        if (!isReceiverRegistered) return
        try {
            unregisterReceiver(eventReceiver)
        } catch (_: IllegalArgumentException) {
        }
        isReceiverRegistered = false
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.regen_restore_channel),
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
            .setContentTitle(getString(R.string.regen_restore_notification_title))
            .setContentText(getString(R.string.regen_restore_notification_text))
            .setOngoing(true)
            .build()
    }

    companion object {
        const val EXTRA_REASON = "reason"
        private const val TAG = EnergyRegenerationController.TAG
        private const val CHANNEL_ID = "regen_restore"
        private const val NOTIFICATION_ID = 4107
        private const val POLL_INTERVAL_MS = VhalConstants.POLL_INTERVAL_MS
        private const val ACTION_QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON"
    }
}
