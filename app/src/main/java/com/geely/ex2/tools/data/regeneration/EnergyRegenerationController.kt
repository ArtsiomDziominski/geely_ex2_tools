package com.geely.ex2.tools.data.regeneration

import android.content.Context
import android.util.Log
import com.geely.ex2.tools.data.vhal.EnergyRegeneration

object EnergyRegenerationController {
    const val TAG = "GeelyToolsRegen"

    fun restoreEnergyRegenerationIfNeeded(context: Context, reason: String) {
        val appContext = context.applicationContext
        if (!EnergyRegenerationSettings.isPersistEnabled(appContext)) {
            Log.i(TAG, "Skip regen restore, persist disabled: $reason")
            return
        }

        val savedLevel = EnergyRegenerationSettings.getSavedLevelValue(appContext)
        if (!EnergyRegeneration.isSelectableValue(savedLevel)) {
            Log.w(TAG, "Skip regen restore, invalid saved level: 0x${savedLevel.toString(16)} ($reason)")
            return
        }

        val repository = EnergyRegenerationRepository(appContext)
        val current = repository.readEnergyRegeneration()
        if (!current.isAvailable) {
            Log.w(TAG, "Regen read unavailable ($reason): ${current.source}")
            return
        }

        if (current.levelValue == savedLevel) {
            Log.d(TAG, "Regen already saved value 0x${savedLevel.toString(16)} ($reason)")
            return
        }

        Log.i(
            TAG,
            "Regen mismatch: current=0x${current.levelValue.toString(16)} " +
                "saved=0x${savedLevel.toString(16)}, restoring ($reason)",
        )

        repeat(WRITE_ATTEMPTS) { attempt ->
            val writeResult = repository.setEnergyRegeneration(savedLevel)
            if (!writeResult.ok) {
                Log.w(
                    TAG,
                    "Regen write failed attempt ${attempt + 1}/$WRITE_ATTEMPTS ($reason): ${writeResult.error}",
                )
                return@repeat
            }

            val verify = repository.readEnergyRegeneration()
            if (verify.isAvailable && verify.levelValue == savedLevel) {
                Log.i(TAG, "Regen restored to 0x${savedLevel.toString(16)} ($reason)")
                return
            }
        }

        Log.w(TAG, "Regen restore incomplete ($reason)")
    }

    private const val WRITE_ATTEMPTS = 3
}
