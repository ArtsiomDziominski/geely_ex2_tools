package com.geely.ex2.tools.data.vhal

data class LockSoundSample(
    val isEnabled: Boolean,
    val isAvailable: Boolean,
    val source: String,
    val details: String,
)

data class LockSoundWriteResult(
    val ok: Boolean,
    val requestedEnabled: Boolean,
    val error: String?,
)
