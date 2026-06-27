package com.geely.ex2.tools.data.vhal

import android.content.Context
import com.geely.ex2.tools.data.ambient.FlymeAmbientLightApi
import java.util.Locale

class CarPropertyAmbientLightReader(private val context: Context) {
    private val bindings = CarVhalBindings(context)

    fun readAmbientLight(): AmbientLightSample {
        val debug = StringBuilder()

        val flymeEnabled = FlymeAmbientLightApi.readEnabled(context)
        if (flymeEnabled != null) {
            val supported = FlymeAmbientLightApi.isSupported(context)
            debug.append("Flyme mValue: ").append(flymeEnabled)
            if (supported != null) {
                debug.append(", mSupported: ").append(supported)
            }
            return AmbientLightSample(
                isOn = flymeEnabled,
                rawValue = FlymeAmbientLightApi.enabledToRawValue(flymeEnabled),
                isAvailable = supported != false,
                source = "Flyme BooleanFuncLiveData",
                details = debug.toString(),
            )
        }
        debug.append("Flyme read: unavailable")

        if (!bindings.ensureConnected(debug)) {
            return AmbientLightSample(
                isOn = null,
                rawValue = 0,
                isAvailable = false,
                source = "Car init error",
                details = debug.toString(),
            )
        }

        val probe = bindings.readIntProperty(VhalConstants.PROP_BCM_FUNC_LIGHT_ATMOSPHERE_LAMPS)
        debug.append('\n').append(probe.line("BCM_FUNC_LIGHT_ATMOSPHERE_LAMPS"))

        if (!probe.ok) {
            return AmbientLightSample(
                isOn = null,
                rawValue = 0,
                isAvailable = false,
                source = probe.error ?: "ambient light unreadable",
                details = debug.toString(),
            )
        }

        return AmbientLightSample(
            isOn = decodeOnOff(probe.value),
            rawValue = probe.value,
            isAvailable = true,
            source = "BCM_FUNC_LIGHT_ATMOSPHERE_LAMPS 0x21051000",
            details = debug.toString(),
        )
    }

    fun writeAmbientLight(enabled: Boolean): AmbientLightWriteResult {
        val debug = StringBuilder()

        if (FlymeAmbientLightApi.writeEnabled(context, enabled)) {
            debug.append("Flyme updateFuncValueForce: OK")
            val readBack = FlymeAmbientLightApi.readEnabled(context)
            if (readBack != null) {
                debug.append('\n').append("Flyme read-back: ").append(readBack)
            }
            return AmbientLightWriteResult(
                ok = true,
                requestedOn = enabled,
                error = null,
            )
        }
        debug.append("Flyme write: failed")

        val value = if (enabled) {
            VhalConstants.COMMON_VALUE_ON
        } else {
            VhalConstants.COMMON_VALUE_OFF
        }

        if (!bindings.ensureConnected(debug)) {
            return AmbientLightWriteResult(
                ok = false,
                requestedOn = enabled,
                error = debug.toString().ifEmpty { "Car init error" },
            )
        }

        val writeProbe = bindings.writeIntProperty(
            VhalConstants.PROP_BCM_FUNC_LIGHT_ATMOSPHERE_LAMPS,
            value,
        )
        if (!writeProbe.ok) {
            return AmbientLightWriteResult(
                ok = false,
                requestedOn = enabled,
                error = writeProbe.error ?: "write failed",
            )
        }

        debug.append('\n').append("VHAL setIntProperty: OK")
        return AmbientLightWriteResult(
            ok = true,
            requestedOn = enabled,
            error = null,
        )
    }

    fun close() {
        bindings.close()
    }

    private fun decodeOnOff(rawValue: Int): Boolean? = when (rawValue) {
        VhalConstants.COMMON_VALUE_ON -> true
        VhalConstants.COMMON_VALUE_OFF -> false
        else -> null
    }

    private fun CarVhalBindings.IntProbe.line(name: String): String {
        return if (ok) {
            String.format(Locale.US, "%s 0x%08X: int 0x%08X (%d)", name, propertyId, value, value)
        } else {
            String.format(Locale.US, "%s 0x%08X: ERROR %s", name, propertyId, error ?: "")
        }
    }
}
