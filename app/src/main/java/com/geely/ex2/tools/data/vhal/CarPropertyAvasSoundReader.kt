package com.geely.ex2.tools.data.vhal

import android.content.Context
import com.geely.ex2.tools.data.avas.FlymeAvasSoundApi
import java.util.Locale

class CarPropertyAvasSoundReader(private val context: Context) {
    private val bindings = CarVhalBindings(context)

    fun readAvasSound(): AvasSoundSample {
        val debug = StringBuilder()

        val flymeEnabled = FlymeAvasSoundApi.readSwitchEnabled(context)
        val flymeType = FlymeAvasSoundApi.readSoundType(context)
        if (flymeEnabled != null || flymeType != null) {
            val supported = FlymeAvasSoundApi.isSupported(context)
            debug.append("Flyme switch: ").append(flymeEnabled)
            debug.append(", type: ").append(flymeType?.let { String.format(Locale.US, "0x%08X", it) } ?: "null")
            if (supported != null) {
                debug.append(", mSupported: ").append(supported)
            }
            return AvasSoundSample(
                isEnabled = flymeEnabled,
                soundType = flymeType,
                rawSwitchValue = flymeEnabled?.let { FlymeAvasSoundApi.enabledToRawValue(it) } ?: 0,
                rawTypeValue = flymeType ?: 0,
                isAvailable = supported != false,
                source = "Flyme BooleanFuncLiveData / EnumFuncLiveData",
                details = debug.toString(),
            )
        }
        debug.append("Flyme read: unavailable")

        if (!bindings.ensureConnected(debug)) {
            return AvasSoundSample(
                isEnabled = null,
                soundType = null,
                rawSwitchValue = 0,
                rawTypeValue = 0,
                isAvailable = false,
                source = "Car init error",
                details = debug.toString(),
            )
        }

        val switchProbe = bindings.readIntProperty(VhalConstants.PROP_SETTING_FUNC_AVAS_SOUND_SWITCH)
        debug.append('\n').append(switchProbe.line("SETTING_FUNC_AVAS_SOUND_SWITCH"))

        val typeProbe = bindings.readIntProperty(VhalConstants.PROP_SETTING_FUNC_AVAS_SOUND_TYPE)
        debug.append('\n').append(typeProbe.line("SETTING_FUNC_AVAS_SOUND_TYPE"))

        if (!switchProbe.ok && !typeProbe.ok) {
            val avasProbe = bindings.readIntProperty(VhalConstants.PROP_AVAS_SWITCH)
            debug.append('\n').append(avasProbe.line("AVASSwitch"))
            if (!avasProbe.ok) {
                return AvasSoundSample(
                    isEnabled = null,
                    soundType = null,
                    rawSwitchValue = 0,
                    rawTypeValue = 0,
                    isAvailable = false,
                    source = switchProbe.error ?: "AVAS unreadable",
                    details = debug.toString(),
                )
            }
            return AvasSoundSample(
                isEnabled = decodeOnOff(avasProbe.value),
                soundType = null,
                rawSwitchValue = avasProbe.value,
                rawTypeValue = 0,
                isAvailable = true,
                source = "AVASSwitch 0x2140A148",
                details = debug.toString(),
            )
        }

        return AvasSoundSample(
            isEnabled = if (switchProbe.ok) decodeOnOff(switchProbe.value) else null,
            soundType = if (typeProbe.ok) typeProbe.value else null,
            rawSwitchValue = if (switchProbe.ok) switchProbe.value else 0,
            rawTypeValue = if (typeProbe.ok) typeProbe.value else 0,
            isAvailable = true,
            source = "SETTING_FUNC_AVAS_SOUND_SWITCH 0x2030B600",
            details = debug.toString(),
        )
    }

    fun writeAvasSound(enabled: Boolean): AvasSoundWriteResult {
        val debug = StringBuilder()

        if (enabled) {
            if (FlymeAvasSoundApi.writeSwitchEnabled(context, true)) {
                debug.append("Flyme switch ON: OK")
                return AvasSoundWriteResult(ok = true, requestedEnabled = true, error = null)
            }
            debug.append("Flyme switch ON: failed")
        } else {
            if (FlymeAvasSoundApi.disableCompletely(context)) {
                debug.append("Flyme disableCompletely: OK")
                val readBackSwitch = FlymeAvasSoundApi.readSwitchEnabled(context)
                val readBackType = FlymeAvasSoundApi.readSoundType(context)
                debug.append('\n').append("read-back switch=").append(readBackSwitch)
                debug.append(", type=").append(readBackType)
                return AvasSoundWriteResult(ok = true, requestedEnabled = false, error = null)
            }
            debug.append("Flyme disableCompletely: failed")
        }

        val value = if (enabled) {
            VhalConstants.COMMON_VALUE_ON
        } else {
            VhalConstants.COMMON_VALUE_OFF
        }

        if (!bindings.ensureConnected(debug)) {
            return AvasSoundWriteResult(
                ok = false,
                requestedEnabled = enabled,
                error = debug.toString().ifEmpty { "Car init error" },
            )
        }

        val switchWrite = bindings.writeIntProperty(
            VhalConstants.PROP_SETTING_FUNC_AVAS_SOUND_SWITCH,
            value,
        )
        if (!switchWrite.ok) {
            return AvasSoundWriteResult(
                ok = false,
                requestedEnabled = enabled,
                error = switchWrite.error ?: "write failed",
            )
        }

        if (!enabled) {
            bindings.writeIntProperty(
                VhalConstants.PROP_SETTING_FUNC_AVAS_SOUND_TYPE,
                VhalConstants.AVAS_SOUND_TYPE_NONE,
            )
        }

        debug.append('\n').append("VHAL setIntProperty: OK")
        return AvasSoundWriteResult(ok = true, requestedEnabled = enabled, error = null)
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
