package com.geely.ex2.tools.data.vhal

data class SpeedSample(
    val speedKmh: Float,
    val isAvailable: Boolean,
    val source: String = "",
    val details: String = "",
)

interface VhalSpeedReader {
    fun readSpeed(): SpeedSample

    fun startListening(
        onUpdate: (SpeedSample) -> Unit,
        shouldContinue: () -> Boolean = { true },
    )

    fun stopListening()

    fun close()
}
