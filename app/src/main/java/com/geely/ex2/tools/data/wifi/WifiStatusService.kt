package com.geely.ex2.tools.data.wifi

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.IBinder
import android.util.Log

class WifiStatusService : Service() {
    private var receiverRegistered = false

    private val wifiReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            val action = intent?.action ?: "null"
            WifiAutoEnableController.enableWifiIfNeeded(context, "service Wi-Fi event: $action")
            updateIcon("service Wi-Fi event: $action")
        }
    }

    override fun onCreate() {
        super.onCreate()
        registerWifiReceiver()
        startForeground(
            WifiStatusIconHelper.SERVICE_NOTIFICATION_ID,
            WifiStatusIconHelper.buildServiceNotification(this),
        )
        Log.i(WifiAutoEnableController.TAG, "Wi-Fi foreground service notification started: service create")
        WifiAutoEnableController.enableWifiIfNeeded(this, "service create")
        updateIcon("service create")
        Log.i(WifiAutoEnableController.TAG, "Wi-Fi status service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val reason = intent?.getStringExtra(EXTRA_START_REASON) ?: "service start"
        WifiAutoEnableController.enableWifiIfNeeded(this, reason)
        updateIcon(reason)
        return START_STICKY
    }

    override fun onDestroy() {
        unregisterWifiReceiver()
        Log.i(WifiAutoEnableController.TAG, "Wi-Fi status service destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun registerWifiReceiver() {
        if (receiverRegistered) {
            return
        }

        val filter = IntentFilter().apply {
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
            addAction(WifiManager.RSSI_CHANGED_ACTION)
            addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        }

        registerReceiver(wifiReceiver, filter)
        receiverRegistered = true
        Log.i(WifiAutoEnableController.TAG, "Wi-Fi receiver registered in service")
    }

    private fun unregisterWifiReceiver() {
        if (!receiverRegistered) {
            return
        }

        try {
            unregisterReceiver(wifiReceiver)
        } catch (_: IllegalArgumentException) {
        }

        receiverRegistered = false
    }

    private fun updateIcon(reason: String) {
        WifiStatusIconHelper.notifyStatusIcon(this, reason)
    }

    companion object {
        const val EXTRA_START_REASON = "start_reason"
    }
}
