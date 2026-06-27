package com.geely.ex2.tools.data.regeneration

import android.content.Context
import com.geely.ex2.tools.data.vhal.CarPropertyEnergyRegenerationReader
import com.geely.ex2.tools.data.vhal.EnergyRegenerationSample
import com.geely.ex2.tools.data.vhal.EnergyRegenerationWriteResult

class EnergyRegenerationRepository(private val context: Context) {
    private val appContext = context.applicationContext

    fun isPersistEnabled(): Boolean = EnergyRegenerationSettings.isPersistEnabled(appContext)

    fun setPersistEnabled(enabled: Boolean) {
        EnergyRegenerationSettings.setPersistEnabled(appContext, enabled)
    }

    fun getSavedLevelValue(): Int = EnergyRegenerationSettings.getSavedLevelValue(appContext)

    fun saveSelectedLevel(levelValue: Int) {
        EnergyRegenerationSettings.setSavedLevelValue(appContext, levelValue)
    }

    fun startRestoreService(reason: String) {
        EnergyRegenerationAppStarter.startRestoreService(appContext, reason)
    }

    fun stopRestoreService(reason: String) {
        EnergyRegenerationAppStarter.stopRestoreService(appContext, reason)
    }

    fun restoreSavedLevelIfNeeded(reason: String) {
        EnergyRegenerationController.restoreEnergyRegenerationIfNeeded(appContext, reason)
    }

    fun readEnergyRegeneration(): EnergyRegenerationSample {
        val reader = CarPropertyEnergyRegenerationReader(context)
        return try {
            reader.readEnergyRegeneration()
        } finally {
            reader.close()
        }
    }

    fun setEnergyRegeneration(levelValue: Int): EnergyRegenerationWriteResult {
        val reader = CarPropertyEnergyRegenerationReader(context)
        return try {
            reader.writeEnergyRegeneration(levelValue)
        } finally {
            reader.close()
        }
    }
}
