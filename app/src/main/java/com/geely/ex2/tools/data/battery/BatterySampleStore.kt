package com.geely.ex2.tools.data.battery

import com.geely.ex2.tools.data.vhal.BatterySample
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Последний sample от BatteryStatusService / one-shot notify — UI читает отсюда, без createCar. */
object BatterySampleStore {
    private val _sample = MutableStateFlow<BatterySample?>(null)
    val sample: StateFlow<BatterySample?> = _sample.asStateFlow()

    fun publish(sample: BatterySample) {
        _sample.value = sample
    }

    fun clear() {
        _sample.value = null
    }
}
