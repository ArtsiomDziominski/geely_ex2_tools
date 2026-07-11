package com.geely.ex2.tools.data.avas

data class AvasSample(
    val mode: Int = AvasConstants.MODE_MUTED,
    val isMuted: Boolean = false,
    val isSupported: Boolean = false,
    val isAvailable: Boolean = false,
    val source: String = "",
    val details: String = "",
)

data class AvasWriteResult(
    val ok: Boolean,
    val error: String? = null,
    val details: String = "",
)
