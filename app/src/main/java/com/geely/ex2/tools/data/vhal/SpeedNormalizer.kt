package com.geely.ex2.tools.data.vhal

import kotlin.math.abs

object SpeedNormalizer {
    const val MAX_SPEED_KMH = 140f

    fun driveModeSpeedKmh(rawKmh: Float): Float = abs(rawKmh).coerceIn(0f, MAX_SPEED_KMH)
}
