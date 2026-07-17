package com.geely.ex2.tools.feature.battery

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geely.ex2.tools.R
import com.geely.ex2.tools.data.battery.BatteryRepository
import com.geely.ex2.tools.data.battery.BatteryWidgetRank
import com.geely.ex2.tools.data.vhal.BatterySample
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class BatteryUiState(
    val isEnabled: Boolean = false,
    val widgetRank: Int = BatteryWidgetRank.DEFAULT,
    val canStepWidgetLeft: Boolean = true,
    val canStepWidgetRight: Boolean = true,
    val statusText: String = "",
    val latestSocText: String = "",
    val sourceText: String = "",
)

class BatteryViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val repository = BatteryRepository(appContext)

    private val _uiState = MutableStateFlow(BatteryUiState())
    val uiState: StateFlow<BatteryUiState> = _uiState.asStateFlow()

    private var sampleJob: Job? = null
    private var resumeJob: Job? = null

    fun onResume() {
        resumeJob?.cancel()
        resumeJob = viewModelScope.launch {
            refreshSettings()
            if (!isActive) return@launch
            startObservingSample()
        }
    }

    fun onPause() {
        resumeJob?.cancel()
        resumeJob = null
        stopObservingSample()
    }

    fun onEnabledCheckedChange(enabled: Boolean) {
        if (enabled == _uiState.value.isEnabled) return
        repository.setEnabled(enabled)
        if (enabled) {
            syncBackgroundWork("UI battery enable")
            startObservingSample()
        } else {
            repository.stopStatusService("UI battery disable")
            repository.cancelStatusIcon()
            stopObservingSample()
        }
        refreshSettings()
    }

    fun onWidgetRankStep(delta: Int) {
        val currentRank = _uiState.value.widgetRank
        val canStep = if (delta < 0) {
            BatteryWidgetRank.canStepLeft(currentRank)
        } else {
            BatteryWidgetRank.canStepRight(currentRank)
        }
        if (!canStep) return

        val newRank = repository.stepStatusIconRank(delta)
        if (_uiState.value.isEnabled) {
            repository.notifyStatusIconIfEnabled("UI widget rank step", newRank)
        }
        _uiState.update {
            it.copy(
                widgetRank = newRank,
                canStepWidgetLeft = BatteryWidgetRank.canStepLeft(newRank),
                canStepWidgetRight = BatteryWidgetRank.canStepRight(newRank),
            )
        }
    }

    override fun onCleared() {
        stopObservingSample()
        super.onCleared()
    }

    private fun syncBackgroundWork(reason: String) {
        if (!repository.isEnabled()) {
            repository.stopStatusService("$reason, disabled")
            repository.cancelStatusIcon()
            return
        }
        repository.startStatusServiceIfEnabled(reason)
        repository.notifyStatusIconIfEnabled(reason)
    }

    private fun startObservingSample() {
        sampleJob?.cancel()
        if (!repository.isEnabled()) {
            applySample(null)
            return
        }
        sampleJob = viewModelScope.launch {
            repository.observeLatestSample().collect { sample ->
                if (!repository.isEnabled()) {
                    applySample(null)
                    return@collect
                }
                applySample(sample)
            }
        }
    }

    private fun stopObservingSample() {
        sampleJob?.cancel()
        sampleJob = null
    }

    private fun refreshSettings() {
        val enabled = repository.isEnabled()
        val widgetRank = repository.getStatusIconRank()
        val sample = if (enabled) repository.latestSample() else null
        _uiState.update {
            BatteryUiState(
                isEnabled = enabled,
                widgetRank = widgetRank,
                canStepWidgetLeft = BatteryWidgetRank.canStepLeft(widgetRank),
                canStepWidgetRight = BatteryWidgetRank.canStepRight(widgetRank),
                statusText = buildStatusText(enabled, sample),
                latestSocText = buildLatestSocText(enabled, sample),
                sourceText = buildSourceText(enabled, sample),
            )
        }
    }

    private fun applySample(sample: BatterySample?) {
        val enabled = repository.isEnabled()
        _uiState.update {
            it.copy(
                isEnabled = enabled,
                statusText = buildStatusText(enabled, sample.takeIf { enabled }),
                latestSocText = buildLatestSocText(enabled, sample.takeIf { enabled }),
                sourceText = buildSourceText(enabled, sample.takeIf { enabled }),
            )
        }
    }

    private fun buildStatusText(enabled: Boolean, sample: BatterySample?): String {
        if (!enabled) {
            return appContext.getString(R.string.battery_status_disabled)
        }
        if (sample == null) {
            return appContext.getString(R.string.battery_status_waiting)
        }
        return if (sample.isAvailable) {
            appContext.getString(R.string.battery_status_ok, sample.source)
        } else {
            appContext.getString(R.string.battery_status_error, sample.source)
        }
    }

    private fun buildLatestSocText(enabled: Boolean, sample: BatterySample?): String {
        if (!enabled) {
            return appContext.getString(R.string.battery_latest_disabled)
        }
        if (sample?.isAvailable == true) {
            return appContext.getString(R.string.battery_latest_value, sample.socPercent.roundToInt())
        }
        return appContext.getString(R.string.battery_latest_unavailable)
    }

    private fun buildSourceText(enabled: Boolean, sample: BatterySample?): String {
        if (!enabled || sample == null) {
            return appContext.getString(R.string.battery_source_empty)
        }
        return sample.details.ifEmpty {
            appContext.getString(R.string.battery_source_empty)
        }
    }
}
