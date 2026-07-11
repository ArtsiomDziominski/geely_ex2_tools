package com.geely.ex2.tools.data.avas

import android.content.Context
import android.util.Log
import com.geely.ex2.tools.data.vhal.CarPropertyIo

class AvasRepository(context: Context) {
    private val appContext = context.applicationContext
    private val applier = AvasMuteApplier(appContext)

    fun isMutedSaved(): Boolean = AvasSettings.isMutedSaved(appContext)

    fun getLastActiveMode(): Int = AvasSettings.getLastActiveMode(appContext)

    fun readAvas(): AvasSample = CarPropertyIo.call {
        val sample = applier.read()
        if (sample.isAvailable && sample.mode > AvasConstants.MODE_MUTED) {
            AvasSettings.setLastActiveMode(appContext, sample.mode)
        }
        sample
    }

    fun setMuted(muted: Boolean): AvasWriteResult = CarPropertyIo.call {
        val lastActive = AvasSettings.getLastActiveMode(appContext)
        val result = applier.setMuted(muted, lastActive)
        if (result.ok) {
            AvasSettings.setMutedSaved(appContext, muted)
            Log.i(TAG, "AVAS mute=$muted ok; ${result.details.take(200)}")
        } else {
            Log.w(TAG, "AVAS mute=$muted failed: ${result.error}; ${result.details.take(200)}")
        }
        result
    }

    fun restoreMuteIfNeeded(reason: String) {
        AvasController.restoreMuteIfNeeded(appContext, reason)
    }

    fun startRestoreService(reason: String) {
        AvasAppStarter.startRestoreServiceIfEnabled(appContext, reason)
    }

    fun stopRestoreService(reason: String) {
        AvasAppStarter.stopRestoreService(appContext, reason)
    }

    fun close() {
        // Stateless applier; nothing to close.
    }

    companion object {
        private const val TAG = "GeelyToolsAvas"
    }
}
