package com.geely.ex2.tools.data.regeneration

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class EnergyRegenerationEventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: "null"
        Log.i(EnergyRegenerationController.TAG, "Receiver action: $action")

        if (
            action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == QUICKBOOT_POWERON
        ) {
            EnergyRegenerationController.restoreEnergyRegenerationIfNeeded(context, "receiver: $action")
            EnergyRegenerationAppStarter.startRestoreService(context, "receiver: $action")
        }
    }

    companion object {
        private const val QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON"
    }
}
