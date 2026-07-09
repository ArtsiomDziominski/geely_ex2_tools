package com.geely.ex2.tools.feature.temperature

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geely.ex2.tools.R
import com.geely.ex2.tools.data.temperature.TemperatureReader
import com.geely.ex2.tools.data.temperature.TemperatureRepository
import com.geely.ex2.tools.data.temperature.TemperatureWidgetRank
import com.geely.ex2.tools.data.vhal.VhalConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

data class TemperatureUiState(
    val isEnabled: Boolean = true,
    val widgetRank: Int = TemperatureWidgetRank.DEFAULT,
    val canStepWidgetLeft: Boolean = true,
    val canStepWidgetRight: Boolean = true,
    val statusText: String = "",
    val latestTemperatureText: String = "",
)

class TemperatureViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val repository = TemperatureRepository(appContext)

    private val _uiState = MutableStateFlow(TemperatureUiState())
    val uiState: StateFlow<TemperatureUiState> = _uiState.asStateFlow()

    private var pollJob: Job? = null
    private var refreshJob: Job? = null

    fun onResume() {
        refreshState()
        syncBackgroundWork("TemperatureScreen resume")
        restartPolling()
    }

    fun onPause() {
        stopPolling()
    }

    fun onEnabledCheckedChange(enabled: Boolean) {
        if (enabled == _uiState.value.isEnabled) return
        repository.setEnabled(enabled)
        if (enabled) {
            syncBackgroundWork("UI temperature enable")
            restartPolling()
        } else {
            repository.stopStatusService("UI temperature disable")
            repository.cancelStatusIcon()
            stopPolling()
        }
        refreshState()
    }

    fun onWidgetRankStep(delta: Int) {
        val currentRank = _uiState.value.widgetRank
        val canStep = if (delta < 0) {
            TemperatureWidgetRank.canStepLeft(currentRank)
        } else {
            TemperatureWidgetRank.canStepRight(currentRank)
        }
        if (!canStep) return

        val newRank = repository.stepStatusIconRank(delta)
        if (_uiState.value.isEnabled) {
            repository.notifyStatusIconIfEnabled("UI widget rank step", newRank)
        }
        _uiState.update {
            it.copy(
                widgetRank = newRank,
                canStepWidgetLeft = TemperatureWidgetRank.canStepLeft(newRank),
                canStepWidgetRight = TemperatureWidgetRank.canStepRight(newRank),
            )
        }
    }

    override fun onCleared() {
        stopPolling()
        refreshJob?.cancel()
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
                delay(VhalConstants.TEMPERATURE_POLL_INTERVAL_MS)
                refreshState()
            }
        }
    }

    private fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    private fun refreshState() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            val enabled = repository.isEnabled()
            val result = if (enabled) {
                withContext(Dispatchers.IO) {
                    repository.readTemperature()
                }
            } else {
                null
            }
            val widgetRank = repository.getStatusIconRank()
            val enabledAfter = repository.isEnabled()

            _uiState.update {
                TemperatureUiState(
                    isEnabled = enabledAfter,
                    widgetRank = widgetRank,
                    canStepWidgetLeft = TemperatureWidgetRank.canStepLeft(widgetRank),
                    canStepWidgetRight = TemperatureWidgetRank.canStepRight(widgetRank),
                    statusText = buildStatusText(enabledAfter, result.takeIf { enabledAfter }),
                    latestTemperatureText = buildLatestTemperatureText(
                        enabledAfter,
                        result.takeIf { enabledAfter },
                    ),
                )
            }
        }
    }

    private fun buildStatusText(enabled: Boolean, result: TemperatureReader.Result?): String {
        if (!enabled) {
            return appContext.getString(R.string.temperature_status_disabled)
        }
        if (result == null) {
            return appContext.getString(R.string.temperature_status_waiting)
        }
        return if (result.ok) {
            appContext.getString(R.string.temperature_status_ok, result.source)
        } else {
            appContext.getString(R.string.temperature_status_error, result.source)
        }
    }

    private fun buildLatestTemperatureText(enabled: Boolean, result: TemperatureReader.Result?): String {
        if (!enabled) {
            return appContext.getString(R.string.temperature_latest_disabled)
        }
        if (result?.ok == true) {
            return appContext.getString(R.string.temperature_latest_value, result.value.roundToInt())
        }
        return appContext.getString(R.string.temperature_latest_unavailable)
    }
}
