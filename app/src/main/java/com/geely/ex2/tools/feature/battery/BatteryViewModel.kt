package com.geely.ex2.tools.feature.battery

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geely.ex2.tools.R
import com.geely.ex2.tools.data.battery.BatteryRepository
import com.geely.ex2.tools.data.battery.BatteryWidgetRank
import com.geely.ex2.tools.data.vhal.BatterySample
import com.geely.ex2.tools.data.vhal.VhalConstants
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    private var pollJob: Job? = null

    fun onResume() {
        refreshState()
        syncBackgroundWork("BatteryScreen resume")
        restartPolling()
    }

    fun onPause() {
        stopPolling()
    }

    fun onEnabledCheckedChange(enabled: Boolean) {
        if (enabled == _uiState.value.isEnabled) return
        repository.setEnabled(enabled)
        if (enabled) {
            syncBackgroundWork("UI battery enable")
            restartPolling()
        } else {
            repository.stopStatusService("UI battery disable")
            repository.cancelStatusIcon()
            stopPolling()
        }
        refreshState()
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
        stopPolling()
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

    private fun restartPolling() {
        stopPolling()
        if (repository.isEnabled()) {
            startPolling()
        }
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive) {
                delay(VhalConstants.BATTERY_POLL_INTERVAL_MS)
                refreshState()
            }
        }
    }

    private fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    private fun refreshState() {
        val enabled = repository.isEnabled()
        val widgetRank = repository.getStatusIconRank()
        val sample = if (enabled) {
            repository.readBatterySoc()
        } else {
            null
        }

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
            return appContext.getString(R.string.battery_source_unavailable)
        }
        return sample.details.ifEmpty {
            appContext.getString(R.string.battery_source_empty)
        }
    }
}
