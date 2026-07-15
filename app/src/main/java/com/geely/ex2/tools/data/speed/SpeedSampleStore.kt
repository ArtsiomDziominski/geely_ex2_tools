package com.geely.ex2.tools.data.speed

import com.geely.ex2.tools.data.vhal.SpeedSample
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Последний sample от SpeedStatusService / one-shot notify — UI читает отсюда, без createCar. */
object SpeedSampleStore {
    private val _sample = MutableStateFlow<SpeedSample?>(null)
    val sample: StateFlow<SpeedSample?> = _sample.asStateFlow()

    fun publish(sample: SpeedSample) {
        _sample.value = sample
    }

    fun clear() {
        _sample.value = null
    }
}
