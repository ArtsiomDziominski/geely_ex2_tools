package com.geely.ex2.tools.data.vhal

import android.content.Context
import java.util.Locale

class CarPropertyVhalReader(context: Context) : VhalSpeedReader {
    private val bindings = CarVhalBindings(context)

    override fun readSpeed(): SpeedSample {
        val debug = StringBuilder()
        if (!bindings.ensureConnected(debug)) {
            return SpeedSample(
                speedKmh = 0f,
                isAvailable = false,
                source = "Car init error",
                details = debug.toString(),
            )
        }

        val probe = bindings.readFloatProperty(VhalConstants.PROP_PERF_VEHICLE_SPEED)
        debug.append('\n').append(probe.line())

        if (probe.ok) {
            val normalized = SpeedNormalizer.driveModeSpeedKmh(probe.value)
            debug.append('\n').append(
                String.format(
                    Locale.US,
                    "driveModeSpeedKmh: raw=%.1f -> %.0f km/h (+1 if >= %.0f)",
                    probe.value,
                    normalized,
                    SpeedNormalizer.SPEED_OFFSET_MIN_KMH,
                ),
            )
            return SpeedSample(
                speedKmh = normalized,
                isAvailable = true,
                source = "PERF_VEHICLE_SPEED 0x11600207",
                details = debug.toString(),
            )
        }

        return SpeedSample(
            speedKmh = 0f,
            isAvailable = false,
            source = probe.error ?: "PERF_VEHICLE_SPEED unreadable",
            details = debug.toString(),
        )
    }

    override fun close() {
        bindings.close()
    }

    private fun CarVhalBindings.FloatProbe.line(): String {
        return if (ok) {
            String.format(Locale.US, "PERF_VEHICLE_SPEED 0x%08X: float %.1f", propertyId, value)
        } else {
            String.format(Locale.US, "PERF_VEHICLE_SPEED 0x%08X: ERROR %s", propertyId, error ?: "")
        }
    }
}
