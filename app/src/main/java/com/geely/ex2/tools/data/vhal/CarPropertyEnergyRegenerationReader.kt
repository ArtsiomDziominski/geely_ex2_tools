package com.geely.ex2.tools.data.vhal

import android.content.Context
import com.geely.ex2.tools.data.driving.EcarxEnergyRegenerationApi
import com.geely.ex2.tools.data.driving.FlymeEnergyRegenerationApi
import java.util.Locale

class CarPropertyEnergyRegenerationReader(private val context: Context) {
    private val bindings = CarVhalBindings(context)

    fun readEnergyRegeneration(): EnergyRegenerationSample {
        val debug = StringBuilder()

        if (FlymeEnergyRegenerationApi.isAvailable(context)) {
            val flymeValue = FlymeEnergyRegenerationApi.readLevelValue(context)
            if (flymeValue != null) {
                debug.append("Flyme mValue: 0x").append(flymeValue.toString(16))
                return EnergyRegenerationSample(
                    levelValue = flymeValue,
                    isAvailable = true,
                    source = "Flyme EnumFuncLiveData",
                    details = debug.toString(),
                )
            }
            debug.append("Flyme read: unavailable")
        } else {
            debug.append("Flyme read: skipped")
        }

        if (EcarxEnergyRegenerationApi.isAvailable(context)) {
            val ecarxValue = EcarxEnergyRegenerationApi.readLevelValue(context)
            if (ecarxValue != null) {
                debug.append('\n').append("eCarX read: 0x").append(ecarxValue.toString(16))
                return EnergyRegenerationSample(
                    levelValue = ecarxValue,
                    isAvailable = true,
                    source = "eCarX ICarFunction",
                    details = debug.toString(),
                )
            }
            debug.append('\n').append("eCarX read: unavailable")
        } else {
            debug.append('\n').append("eCarX read: skipped")
        }

        if (!bindings.ensureConnected(debug)) {
            return EnergyRegenerationSample(
                levelValue = 0,
                isAvailable = false,
                source = "Car init error",
                details = debug.toString(),
            )
        }

        val probe = bindings.readIntProperty(VhalConstants.PROP_SETTING_FUNC_ENERGY_REGENERATION)
        debug.append('\n').append(probe.line("SETTING_FUNC_ENERGY_REGENERATION"))

        if (probe.ok) {
            return EnergyRegenerationSample(
                levelValue = probe.value,
                isAvailable = true,
                source = "SETTING_FUNC_ENERGY_REGENERATION 0x22020500",
                details = debug.toString(),
            )
        }

        return EnergyRegenerationSample(
            levelValue = 0,
            isAvailable = false,
            source = probe.error ?: "energy regeneration unreadable",
            details = debug.toString(),
        )
    }

    fun writeEnergyRegeneration(levelValue: Int): EnergyRegenerationWriteResult {
        val debug = StringBuilder()

        if (FlymeEnergyRegenerationApi.writeLevelValue(context, levelValue)) {
            debug.append("Flyme write: OK")
            val readBack = FlymeEnergyRegenerationApi.readLevelValue(context)
            if (readBack != null) {
                debug.append('\n').append(
                    String.format(Locale.US, "Flyme read-back: 0x%08X", readBack),
                )
            }
            return EnergyRegenerationWriteResult(
                ok = true,
                requestedValue = levelValue,
                error = null,
            )
        }
        debug.append("Flyme write: failed")

        if (EcarxEnergyRegenerationApi.writeLevelValue(context, levelValue)) {
            debug.append('\n').append("eCarX write: OK")
            val readBack = EcarxEnergyRegenerationApi.readLevelValue(context)
            if (readBack != null) {
                debug.append('\n').append(
                    String.format(Locale.US, "eCarX read-back: 0x%08X", readBack),
                )
            }
            return EnergyRegenerationWriteResult(
                ok = true,
                requestedValue = levelValue,
                error = null,
            )
        }
        debug.append('\n').append("eCarX write: failed")

        if (!bindings.ensureConnected(debug)) {
            return EnergyRegenerationWriteResult(
                ok = false,
                requestedValue = levelValue,
                error = debug.toString().ifEmpty { "Car init error" },
            )
        }

        val writeProbe = bindings.writeIntProperty(
            VhalConstants.PROP_SETTING_FUNC_ENERGY_REGENERATION,
            levelValue,
        )
        if (!writeProbe.ok) {
            return EnergyRegenerationWriteResult(
                ok = false,
                requestedValue = levelValue,
                error = writeProbe.error ?: "write failed",
            )
        }

        debug.append('\n').append("VHAL setIntProperty: OK")
        return EnergyRegenerationWriteResult(
            ok = true,
            requestedValue = levelValue,
            error = null,
        )
    }

    fun close() {
        bindings.close()
    }

    private fun CarVhalBindings.IntProbe.line(name: String): String {
        return if (ok) {
            String.format(Locale.US, "%s 0x%08X: int 0x%08X (%d)", name, propertyId, value, value)
        } else {
            String.format(Locale.US, "%s 0x%08X: ERROR %s", name, propertyId, error ?: "")
        }
    }
}
