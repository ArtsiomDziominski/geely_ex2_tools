package com.geely.ex2.tools.data.speed

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class SpeedEventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: "null"
        Log.i(TAG, "Receiver action: $action")

        if (
            action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == QUICKBOOT_POWERON
        ) {
            SpeedAppStarter.startServiceIfEnabled(context, "receiver: $action")
            SpeedAppStarter.notifyStatusIconIfEnabled(context, "receiver: $action")
        }
    }

    companion object {
        private const val TAG = "GeeKitSpeed"
        private const val QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON"
    }
}
