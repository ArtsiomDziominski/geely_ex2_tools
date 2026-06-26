package com.geely.ex2.tools.data.vhal

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.Locale

class CarPropertyBatteryVhalReader(context: Context) : VhalBatteryReader {
    private val bindings = CarVhalBindings(context)
    private val handler = Handler(Looper.getMainLooper())
    private var onUpdate: ((BatterySample) -> Unit)? = null
    private var shouldContinue: () -> Boolean = { true }
    private var isListening = false

    private val pollRunnable = object : Runnable {
        override fun run() {
            if (!isListening) return
            if (!shouldContinue()) {
                handler.removeCallbacks(this)
                return
            }
            onUpdate?.invoke(readBatterySoc())
            handler.postDelayed(this, VhalConstants.BATTERY_POLL_INTERVAL_MS)
        }
    }

    override fun readBatterySoc(): BatterySample {
        val debug = StringBuilder()
        if (!bindings.ensureConnected(debug)) {
            return BatterySample(
                socPercent = 0f,
                isAvailable = false,
                source = "Car init error",
                details = debug.toString(),
            )
        }

        val oemProbe = bindings.readFloatProperty(VhalConstants.PROP_ED_EV_BATTERY_PERCENTAGE)
        val levelProbe = bindings.readFloatProperty(VhalConstants.PROP_EV_BATTERY_LEVEL)
        val currentCapProbe = bindings.readFloatProperty(VhalConstants.PROP_EV_CURRENT_BATTERY_CAPACITY)
        val nominalCapProbe = bindings.readFloatProperty(VhalConstants.PROP_INFO_EV_BATTERY_CAPACITY)

        debug.append('\n').append(oemProbe.line("PROP_ED_EV_BATTERY_PERCENTAGE"))
        debug.append('\n').append(levelProbe.line("EV_BATTERY_LEVEL"))
        debug.append('\n').append(currentCapProbe.line("EV_CURRENT_BATTERY_CAPACITY"))
        debug.append('\n').append(nominalCapProbe.line("INFO_EV_BATTERY_CAPACITY"))

        val oemPercent = oemProbe.value.takeIf { oemProbe.ok }
        val decoded = EvEnergy.decodeBatteryLevelPercent(
            oemPercent = oemPercent,
            batteryLevel = levelProbe.value.takeIf { levelProbe.ok },
            currentCapacityWh = currentCapProbe.value.takeIf { currentCapProbe.ok },
            nominalCapacityWh = nominalCapProbe.value.takeIf { nominalCapProbe.ok },
        )

        if (decoded != null) {
            val source = when {
                oemPercent != null && oemPercent in 0f..100f && oemProbe.ok ->
                    "PROP_ED_EV_BATTERY_PERCENTAGE 0x2140a6ed"
                levelProbe.ok ->
                    "EV_BATTERY_LEVEL fallback 0x11600309"
                else ->
                    "battery SOC"
            }
            debug.append('\n').append(
                String.format(Locale.US, "batterySocPercent: %.1f%%", decoded),
            )
            return BatterySample(
                socPercent = decoded,
                isAvailable = true,
                source = source,
                details = debug.toString(),
            )
        }

        return BatterySample(
            socPercent = 0f,
            isAvailable = false,
            source = "battery SOC unreadable",
            details = debug.toString(),
        )
    }

    override fun startListening(
        onUpdate: (BatterySample) -> Unit,
        shouldContinue: () -> Boolean,
    ) {
        if (isListening) {
            stopListening()
        }

        this.onUpdate = onUpdate
        this.shouldContinue = shouldContinue
        isListening = true

        if (!bindings.ensureConnected(StringBuilder())) {
            Log.w(TAG, "Battery listener started without Car connection")
        }

        if (shouldContinue()) {
            onUpdate(readBatterySoc())
        }
        handler.removeCallbacks(pollRunnable)
        handler.postDelayed(pollRunnable, VhalConstants.BATTERY_POLL_INTERVAL_MS)
        Log.i(TAG, "Battery poll started, interval=${VhalConstants.BATTERY_POLL_INTERVAL_MS}ms")
    }

    override fun stopListening() {
        isListening = false
        handler.removeCallbacks(pollRunnable)
        onUpdate = null
        shouldContinue = { true }
        Log.i(TAG, "Battery listener stopped")
    }

    override fun close() {
        stopListening()
        bindings.close()
    }

    private fun CarVhalBindings.FloatProbe.line(name: String): String {
        return if (ok) {
            String.format(Locale.US, "%s 0x%08X: float %.1f", name, propertyId, value)
        } else {
            String.format(Locale.US, "%s 0x%08X: ERROR %s", name, propertyId, error ?: "")
        }
    }

    companion object {
        private const val TAG = "GeeKitBattery"
    }
}
