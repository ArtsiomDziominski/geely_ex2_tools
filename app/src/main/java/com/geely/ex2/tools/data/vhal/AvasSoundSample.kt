package com.geely.ex2.tools.data.vhal

data class AvasSoundSample(
    val isEnabled: Boolean?,
    val soundType: Int?,
    val rawSwitchValue: Int,
    val rawTypeValue: Int,
    val isAvailable: Boolean,
    val isWritable: Boolean,
    val source: String,
    val details: String,
)

data class AvasSoundWriteResult(
    val ok: Boolean,
    val requestedEnabled: Boolean,
    val error: String?,
)
