package com.geely.ex2.tools.data.vhal

data class AmbientLightSample(
    val isOn: Boolean?,
    val rawValue: Int,
    val isAvailable: Boolean,
    val source: String,
    val details: String,
)

data class AmbientLightWriteResult(
    val ok: Boolean,
    val requestedOn: Boolean,
    val error: String?,
)
