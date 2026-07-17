package com.geely.ex2.tools.data.vhal

import android.content.Context
import com.geely.ex2.tools.data.sound.FlymeLockSoundApi
import java.util.Locale

class CarPropertyLockSoundReader(private val context: Context) {
    private val bindings = CarVhalBindings(context)

    fun readLockSound(): LockSoundSample {
        val debug = StringBuilder()

        if (FlymeLockSoundApi.isAvailable(context)) {
            val flymeValue = FlymeLockSoundApi.readEnabled(context)
            if (flymeValue != null) {
                debug.append("Flyme mValue: ").append(flymeValue)
                return LockSoundSample(
                    isEnabled = flymeValue,
                    isAvailable = true,
                    source = "Flyme BooleanFuncLiveData",
                    details = debug.toString(),
                )
            }
            debug.append("Flyme read: null mValue")
        } else {
            debug.append("Flyme read: skipped")
        }

        if (!bindings.ensureConnected(debug)) {
            return LockSoundSample(
                isEnabled = false,
                isAvailable = false,
                source = "Car init error",
                details = debug.toString(),
            )
        }

        val probe = bindings.readIntProperty(VhalConstants.PROP_SETTING_FUNC_AUDIBLE_LOCKING_FEEDBACK)
        debug.append('\n').append(probe.line("SETTING_FUNC_AUDIBLE_LOCKING_FEEDBACK"))

        if (probe.ok) {
            return LockSoundSample(
                isEnabled = LockSoundValues.isEnabled(probe.value),
                isAvailable = LockSoundValues.isKnownValue(probe.value),
                source = "VHAL 0x${VhalConstants.PROP_SETTING_FUNC_AUDIBLE_LOCKING_FEEDBACK.toString(16)}",
                details = debug.toString(),
            )
        }

        return LockSoundSample(
            isEnabled = false,
            isAvailable = false,
            source = probe.error ?: "lock sound unreadable",
            details = debug.toString(),
        )
    }

    fun writeLockSound(enabled: Boolean): LockSoundWriteResult {
        val debug = StringBuilder()

        if (FlymeLockSoundApi.writeEnabled(context, enabled)) {
            debug.append("Flyme updateFuncValue: OK")
            val readBack = FlymeLockSoundApi.readEnabled(context)
            if (readBack != null) {
                debug.append('\n').append("Flyme read-back: ").append(readBack)
            }
            return LockSoundWriteResult(
                ok = true,
                requestedEnabled = enabled,
                error = null,
            )
        }
        debug.append("Flyme write: failed")

        if (!bindings.ensureConnected(debug)) {
            return LockSoundWriteResult(
                ok = false,
                requestedEnabled = enabled,
                error = debug.toString().ifEmpty { "Car init error" },
            )
        }

        val writeValue = LockSoundValues.writeValue(enabled)
        val writeProbe = bindings.writeIntProperty(
            VhalConstants.PROP_SETTING_FUNC_AUDIBLE_LOCKING_FEEDBACK,
            writeValue,
        )
        if (!writeProbe.ok) {
            return LockSoundWriteResult(
                ok = false,
                requestedEnabled = enabled,
                error = writeProbe.error ?: "write failed",
            )
        }

        debug.append('\n').append("VHAL writeIntProperty: OK value=$writeValue")
        return LockSoundWriteResult(
            ok = true,
            requestedEnabled = enabled,
            error = null,
        )
    }

    fun close() {
        bindings.close()
    }

    private fun CarVhalBindings.IntProbe.line(name: String): String {
        return if (ok) {
            String.format(
                Locale.US,
                "%s 0x%08X: raw=%d enabled=%s",
                name,
                propertyId,
                value,
                LockSoundValues.isEnabled(value),
            )
        } else {
            String.format(Locale.US, "%s 0x%08X: ERROR %s", name, propertyId, error ?: "")
        }
    }
}

object LockSoundValues {
    fun isEnabled(rawValue: Int): Boolean = rawValue == VhalConstants.LOCK_SOUND_VALUE_ON

    fun isKnownValue(rawValue: Int): Boolean {
        return rawValue == VhalConstants.LOCK_SOUND_VALUE_ON ||
            rawValue == VhalConstants.LOCK_SOUND_VALUE_OFF
    }

    fun writeValue(enabled: Boolean): Int {
        return if (enabled) {
            VhalConstants.LOCK_SOUND_VALUE_ON
        } else {
            VhalConstants.LOCK_SOUND_VALUE_OFF
        }
    }
}
