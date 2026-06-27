package com.geely.ex2.tools.data.vhal

import android.content.Context
import com.geely.ex2.tools.data.driving.FlymeDrivingModeApi
import java.util.Locale

class CarPropertyDrivingModeReader(private val context: Context) {
    private val bindings = CarVhalBindings(context)

    fun readDrivingMode(): DrivingModeSample {
        val debug = StringBuilder()

        val flymeValue = FlymeDrivingModeApi.readModeValue(context)
        if (flymeValue != null) {
            debug.append("Flyme mValue: 0x").append(flymeValue.toString(16))
            return DrivingModeSample(
                modeValue = flymeValue,
                isAvailable = true,
                source = "Flyme EnumFuncLiveData",
                details = debug.toString(),
            )
        }
        debug.append("Flyme read: unavailable")

        if (!bindings.ensureConnected(debug)) {
            return DrivingModeSample(
                modeValue = 0,
                isAvailable = false,
                source = "Car init error",
                details = debug.toString(),
            )
        }

        val probe = bindings.readIntProperty(VhalConstants.PROP_DM_FUNC_DRIVE_MODE_SELECT)
        debug.append('\n').append(probe.line("DM_FUNC_DRIVE_MODE_SELECT"))

        if (probe.ok) {
            return DrivingModeSample(
                modeValue = probe.value,
                isAvailable = true,
                source = "DM_FUNC_DRIVE_MODE_SELECT 0x22010100",
                details = debug.toString(),
            )
        }

        return DrivingModeSample(
            modeValue = 0,
            isAvailable = false,
            source = probe.error ?: "drive mode unreadable",
            details = debug.toString(),
        )
    }

    fun writeDrivingMode(modeValue: Int): DrivingModeWriteResult {
        val debug = StringBuilder()

        if (FlymeDrivingModeApi.writeModeValue(context, modeValue)) {
            debug.append("Flyme updateFuncValueForce: OK")
            val readBack = FlymeDrivingModeApi.readModeValue(context)
            if (readBack != null) {
                debug.append('\n').append(
                    String.format(Locale.US, "Flyme read-back: 0x%08X", readBack),
                )
            }
            return DrivingModeWriteResult(
                ok = true,
                requestedValue = modeValue,
                error = null,
            )
        }
        debug.append("Flyme write: failed")

        if (!bindings.ensureConnected(debug)) {
            return DrivingModeWriteResult(
                ok = false,
                requestedValue = modeValue,
                error = debug.toString().ifEmpty { "Car init error" },
            )
        }

        val writeProbe = bindings.writeIntProperty(
            VhalConstants.PROP_DM_FUNC_DRIVE_MODE_SELECT,
            modeValue,
        )
        if (!writeProbe.ok) {
            return DrivingModeWriteResult(
                ok = false,
                requestedValue = modeValue,
                error = writeProbe.error ?: "write failed",
            )
        }

        debug.append('\n').append("VHAL setIntProperty: OK")
        return DrivingModeWriteResult(
            ok = true,
            requestedValue = modeValue,
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
