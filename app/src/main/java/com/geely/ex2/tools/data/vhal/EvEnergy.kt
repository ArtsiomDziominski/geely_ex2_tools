package com.geely.ex2.tools.data.vhal

object EvEnergy {
    fun decodeBatteryLevelPercent(
        oemPercent: Float?,
        batteryLevel: Float?,
        currentCapacityWh: Float?,
        nominalCapacityWh: Float?,
    ): Float? {
        oemPercent?.let { value ->
            if (value.isFinite() && value in 0f..100f) {
                return value.coerceIn(0f, 100f)
            }
        }

        val level = batteryLevel ?: return null
        if (!level.isFinite()) return null

        val capacityWh = currentCapacityWh?.takeIf { it > 0f }
            ?: nominalCapacityWh?.takeIf { it > 0f }

        if (capacityWh != null && level > 100f) {
            return (level / capacityWh * 100f).coerceIn(0f, 100f)
        }

        if (level in 0f..100f) {
            return level.coerceIn(0f, 100f)
        }

        return null
    }

    fun batterySocPercent(sample: BatterySample): Float =
        if (sample.isAvailable) sample.socPercent else Float.NaN

    /**
     * Декод HV battery temp как в CentralEXAuto.readHvBatteryTemperature:
     * [-40; 90] → °C; [-400; 900] → /10.
     */
    fun decodeHvBatteryTempCelsius(raw: Float): Float? {
        if (!raw.isFinite()) return null
        return when {
            raw in -40f..90f -> raw
            raw in -400f..900f -> raw / 10f
            else -> null
        }
    }
}
