package com.geely.ex2.tools.data.vhal

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.Locale

class CarPropertyVhalReader(context: Context) : VhalSpeedReader {
    private val bindings = CarVhalBindings(context)
    private val handler = Handler(Looper.getMainLooper())
    private var callbackToken: Any? = null
    private var onUpdate: ((SpeedSample) -> Unit)? = null
    private var shouldContinue: () -> Boolean = { true }
    private var isListening = false

    private val pollRunnable = object : Runnable {
        override fun run() {
            if (!isListening) return
            if (!shouldContinue()) {
                handler.removeCallbacks(this)
                return
            }
            onUpdate?.invoke(readSpeed())
            handler.postDelayed(this, VhalConstants.SPEED_POLL_INTERVAL_MS)
        }
    }

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

    override fun startListening(
        onUpdate: (SpeedSample) -> Unit,
        shouldContinue: () -> Boolean,
    ) {
        if (isListening) {
            stopListening()
        }

        this.onUpdate = onUpdate
        this.shouldContinue = shouldContinue
        isListening = true

        val debug = StringBuilder()
        if (!bindings.ensureConnected(debug)) {
            Log.w(TAG, "Speed listener started without Car connection: $debug")
            handler.post(pollRunnable)
            return
        }

        if (shouldContinue()) {
            onUpdate(readSpeed())
        }
        handler.removeCallbacks(pollRunnable)
        handler.postDelayed(pollRunnable, VhalConstants.SPEED_POLL_INTERVAL_MS)
        Log.i(TAG, "Speed listener started, interval=${VhalConstants.SPEED_POLL_INTERVAL_MS}ms")
    }

    override fun stopListening() {
        isListening = false
        handler.removeCallbacks(pollRunnable)
        bindings.unregisterPropertyCallback(callbackToken)
        callbackToken = null
        onUpdate = null
        shouldContinue = { true }
        Log.i(TAG, "Speed listener stopped")
    }

    override fun close() {
        stopListening()
        bindings.close()
    }

    private fun CarVhalBindings.FloatProbe.line(): String {
        return if (ok) {
            String.format(Locale.US, "PERF_VEHICLE_SPEED 0x%08X: float %.1f", propertyId, value)
        } else {
            String.format(Locale.US, "PERF_VEHICLE_SPEED 0x%08X: ERROR %s", propertyId, error ?: "")
        }
    }

    companion object {
        private const val TAG = "GeelyToolsSpeed"
    }
}
