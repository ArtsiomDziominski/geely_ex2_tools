package com.geely.ex2.tools.data.vhal

import com.geely.ex2.tools.R

object EnergyRegeneration {
    data class Option(
        val vhalValue: Int,
        val labelRes: Int,
    )

    /** Как в штатном DrivingFragment: levelLow / levelMid / levelHeight (без levelAuto на EX2). */
    val selectable: List<Option> = listOf(
        Option(VhalConstants.REGEN_LEVEL_LOW, R.string.regen_level_low),
        Option(VhalConstants.REGEN_LEVEL_MID, R.string.regen_level_mid),
        Option(VhalConstants.REGEN_LEVEL_HIGH, R.string.regen_level_high),
    )

    fun isSelectableValue(vhalValue: Int): Boolean =
        selectable.any { it.vhalValue == vhalValue }

    fun labelResFor(vhalValue: Int): Int? = when (vhalValue) {
        VhalConstants.REGEN_LEVEL_AUTO -> R.string.regen_level_auto
        VhalConstants.REGEN_LEVEL_LOW -> R.string.regen_level_low
        VhalConstants.REGEN_LEVEL_MID -> R.string.regen_level_mid
        VhalConstants.REGEN_LEVEL_HIGH -> R.string.regen_level_high
        else -> null
    }

    fun selectableIndexFor(vhalValue: Int): Int =
        selectable.indexOfFirst { it.vhalValue == vhalValue }
}
