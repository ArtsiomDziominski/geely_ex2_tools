package com.geely.ex2.tools.data.vhal

import android.content.Context
import com.geely.ex2.tools.data.avas.FlymeAvasSoundApi
import java.util.Locale

class CarPropertyAvasSoundReader(private val context: Context) {
    private val bindings = CarVhalBindings(context)

    fun readAvasSound(): AvasSoundSample {
        val debug = StringBuilder()
        var flymeEnabled: Boolean? = null
        var flymeType: Int? = null
        var flymeSupported: Boolean? = null
        var flymeConnected = false

        if (FlymeAvasSoundApi.isConnected(context)) {
            flymeConnected = true
            flymeEnabled = FlymeAvasSoundApi.readSwitchEnabled(context)
            flymeType = FlymeAvasSoundApi.readSoundType(context)
            flymeSupported = FlymeAvasSoundApi.isSupported(context)
            debug.append("Flyme API: connected")
            debug.append(", switch: ").append(flymeEnabled)
            debug.append(", type: ").append(flymeType?.let { String.format(Locale.US, "0x%08X", it) } ?: "null")
            if (flymeSupported != null) {
                debug.append(", mSupported: ").append(flymeSupported)
            }
        } else {
            debug.append("Flyme read: unavailable")
        }

        if (!bindings.ensureConnected(debug)) {
            if (flymeConnected && (flymeEnabled != null || flymeType != null)) {
                return flymeSample(
                    flymeEnabled = flymeEnabled,
                    flymeType = flymeType,
                    flymeSupported = flymeSupported,
                    details = debug.toString(),
                )
            }
            return unavailableSample(
                source = "Car init error",
                details = debug.toString(),
            )
        }

        val switchProbe = bindings.readIntProperty(VhalConstants.PROP_SETTING_FUNC_AVAS_SOUND_SWITCH)
        debug.append('\n').append(switchProbe.line("SETTING_FUNC_AVAS_SOUND_SWITCH"))

        val typeProbe = bindings.readIntProperty(VhalConstants.PROP_SETTING_FUNC_AVAS_SOUND_TYPE)
        debug.append('\n').append(typeProbe.line("SETTING_FUNC_AVAS_SOUND_TYPE"))

        val avasProbe = bindings.readIntProperty(VhalConstants.PROP_AVAS_SWITCH)
        debug.append('\n').append(avasProbe.line("AVASSwitch"))

        val permissionBlocked = hasPermissionError(switchProbe.error) ||
            hasPermissionError(typeProbe.error)
        val oemReadable = switchProbe.ok || typeProbe.ok
        val avasReadable = avasProbe.ok
        val flymeHasData = flymeEnabled != null || flymeType != null
        val canWriteViaVhal = avasReadable || oemReadable || !permissionBlocked

        if (flymeHasData) {
            return flymeSample(
                flymeEnabled = flymeEnabled,
                flymeType = flymeType,
                flymeSupported = flymeSupported,
                isWritable = flymeSupported != false || canWriteViaVhal,
                details = debug.toString(),
            )
        }

        if (oemReadable) {
            return AvasSoundSample(
                isEnabled = if (switchProbe.ok) decodeOnOff(switchProbe.value) else null,
                soundType = if (typeProbe.ok) typeProbe.value else null,
                rawSwitchValue = if (switchProbe.ok) switchProbe.value else 0,
                rawTypeValue = if (typeProbe.ok) typeProbe.value else 0,
                isAvailable = true,
                isWritable = true,
                source = "SETTING_FUNC_AVAS_SOUND_SWITCH 0x2030B600",
                details = debug.toString(),
            )
        }

        if (avasReadable) {
            return AvasSoundSample(
                isEnabled = decodeOnOff(avasProbe.value),
                soundType = null,
                rawSwitchValue = avasProbe.value,
                rawTypeValue = 0,
                isAvailable = true,
                isWritable = true,
                source = "AVASSwitch 0x2140A148",
                details = debug.toString(),
            )
        }

        if (flymeConnected && flymeSupported != false) {
            return flymeSample(
                flymeEnabled = flymeEnabled,
                flymeType = flymeType,
                flymeSupported = flymeSupported,
                isWritable = true,
                details = debug.toString(),
            )
        }

        return unavailableSample(
            source = switchProbe.error ?: avasProbe.error ?: "AVAS unreadable",
            details = debug.toString(),
            permissionBlocked = permissionBlocked,
        )
    }

    fun writeAvasSound(enabled: Boolean): AvasSoundWriteResult {
        val debug = StringBuilder()
        val value = if (enabled) {
            VhalConstants.COMMON_VALUE_ON
        } else {
            VhalConstants.COMMON_VALUE_OFF
        }

        if (FlymeAvasSoundApi.isConnected(context)) {
            val flymeOk = if (enabled) {
                FlymeAvasSoundApi.writeSwitchEnabled(context, true)
            } else {
                FlymeAvasSoundApi.disableCompletely(context)
            }
            if (flymeOk) {
                debug.append("Flyme write: verified OK")
                return AvasSoundWriteResult(ok = true, requestedEnabled = enabled, error = null)
            }
            debug.append("Flyme write: no effect")
        } else {
            debug.append("Flyme write: unavailable")
        }

        if (!bindings.ensureConnected(debug)) {
            return AvasSoundWriteResult(
                ok = false,
                requestedEnabled = enabled,
                error = debug.toString().ifEmpty { "Car init error" },
            )
        }

        val avasWrite = bindings.writeIntProperty(VhalConstants.PROP_AVAS_SWITCH, value)
        debug.append('\n').append(avasWrite.line("AVASSwitch write"))
        if (avasWrite.ok) {
            return AvasSoundWriteResult(ok = true, requestedEnabled = enabled, error = null)
        }

        val switchWrite = bindings.writeIntProperty(
            VhalConstants.PROP_SETTING_FUNC_AVAS_SOUND_SWITCH,
            value,
        )
        debug.append('\n').append(switchWrite.line("SETTING_FUNC_AVAS_SOUND_SWITCH write"))
        if (switchWrite.ok) {
            if (!enabled) {
                bindings.writeIntProperty(
                    VhalConstants.PROP_SETTING_FUNC_AVAS_SOUND_TYPE,
                    VhalConstants.AVAS_SOUND_TYPE_NONE,
                )
            }
            return AvasSoundWriteResult(ok = true, requestedEnabled = enabled, error = null)
        }

        val error = listOfNotNull(avasWrite.error, switchWrite.error)
            .distinct()
            .joinToString("; ")
            .ifEmpty { debug.toString().ifEmpty { "write failed" } }

        return AvasSoundWriteResult(
            ok = false,
            requestedEnabled = enabled,
            error = error,
        )
    }

    fun close() {
        bindings.close()
    }

    private fun flymeSample(
        flymeEnabled: Boolean?,
        flymeType: Int?,
        flymeSupported: Boolean?,
        details: String,
        isWritable: Boolean = flymeSupported != false,
    ): AvasSoundSample {
        return AvasSoundSample(
            isEnabled = flymeEnabled,
            soundType = flymeType,
            rawSwitchValue = flymeEnabled?.let { FlymeAvasSoundApi.enabledToRawValue(it) } ?: 0,
            rawTypeValue = flymeType ?: 0,
            isAvailable = flymeSupported != false,
            isWritable = isWritable,
            source = "Flyme BooleanFuncLiveData / EnumFuncLiveData",
            details = details,
        )
    }

    private fun unavailableSample(
        source: String,
        details: String,
        permissionBlocked: Boolean = false,
    ): AvasSoundSample {
        return AvasSoundSample(
            isEnabled = null,
            soundType = null,
            rawSwitchValue = 0,
            rawTypeValue = 0,
            isAvailable = false,
            isWritable = !permissionBlocked,
            source = source,
            details = details,
        )
    }

    private fun hasPermissionError(error: String?): Boolean {
        if (error.isNullOrEmpty()) return false
        return error.contains("SecurityException", ignoreCase = true) ||
            error.contains("CAR_CONTROL_AUDIO_VOLUME", ignoreCase = true)
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

    private fun CarVhalBindings.WriteProbe.line(name: String): String {
        return if (ok) {
            String.format(Locale.US, "%s 0x%08X: OK", name, propertyId)
        } else {
            String.format(Locale.US, "%s 0x%08X: ERROR %s", name, propertyId, error ?: "")
        }
    }
}
