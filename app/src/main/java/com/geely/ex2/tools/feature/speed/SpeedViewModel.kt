package com.geely.ex2.tools.feature.speed

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geely.ex2.tools.R
import com.geely.ex2.tools.data.speed.SpeedRepository
import com.geely.ex2.tools.data.speed.SpeedWidgetRank
import com.geely.ex2.tools.data.vhal.SpeedSample
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

data class SpeedUiState(
    val isEnabled: Boolean = false,
    val widgetRank: Int = SpeedWidgetRank.DEFAULT,
    val canStepWidgetLeft: Boolean = true,
    val canStepWidgetRight: Boolean = true,
    val statusText: String = "",
    val latestSpeedText: String = "",
    val sourceText: String = "",
)

class SpeedViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val repository = SpeedRepository(appContext)

    private val _uiState = MutableStateFlow(SpeedUiState())
    val uiState: StateFlow<SpeedUiState> = _uiState.asStateFlow()

    private var pollJob: Job? = null
    private var refreshJob: Job? = null

    fun onResume() {
        refreshState()
        syncBackgroundWork("SpeedScreen resume")
        restartPolling()
    }

    fun onPause() {
        stopPolling()
    }

    fun onEnabledCheckedChange(enabled: Boolean) {
        if (enabled == _uiState.value.isEnabled) return
        repository.setEnabled(enabled)
        if (enabled) {
            syncBackgroundWork("UI speed enable")
            restartPolling()
        } else {
            repository.stopStatusService("UI speed disable")
            repository.cancelStatusIcon()
            stopPolling()
        }
        refreshState()
    }

    fun onWidgetRankStep(delta: Int) {
        val currentRank = _uiState.value.widgetRank
        val canStep = if (delta < 0) {
            SpeedWidgetRank.canStepLeft(currentRank)
        } else {
            SpeedWidgetRank.canStepRight(currentRank)
        }
        if (!canStep) return

        val newRank = repository.stepStatusIconRank(delta)
        if (_uiState.value.isEnabled) {
            repository.notifyStatusIconIfEnabled("UI widget rank step", newRank)
        }
        _uiState.update {
            it.copy(
                widgetRank = newRank,
                canStepWidgetLeft = SpeedWidgetRank.canStepLeft(newRank),
                canStepWidgetRight = SpeedWidgetRank.canStepRight(newRank),
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
                delay(VhalConstants.SPEED_POLL_INTERVAL_MS)
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
            val sample = if (enabled) {
                withContext(Dispatchers.IO) {
                    repository.readSpeed()
                }
            } else {
                null
            }
            val widgetRank = repository.getStatusIconRank()
            val enabledAfter = repository.isEnabled()

            _uiState.update {
                SpeedUiState(
                    isEnabled = enabledAfter,
                    widgetRank = widgetRank,
                    canStepWidgetLeft = SpeedWidgetRank.canStepLeft(widgetRank),
                    canStepWidgetRight = SpeedWidgetRank.canStepRight(widgetRank),
                    statusText = buildStatusText(enabledAfter, sample.takeIf { enabledAfter }),
                    latestSpeedText = buildLatestSpeedText(enabledAfter, sample.takeIf { enabledAfter }),
                    sourceText = buildSourceText(enabledAfter, sample.takeIf { enabledAfter }),
                )
            }
        }
    }

    private fun buildStatusText(enabled: Boolean, sample: SpeedSample?): String {
        if (!enabled) {
            return appContext.getString(R.string.speed_status_disabled)
        }
        if (sample == null) {
            return appContext.getString(R.string.speed_status_waiting)
        }
        return if (sample.isAvailable) {
            appContext.getString(R.string.speed_status_ok, sample.source)
        } else {
            appContext.getString(R.string.speed_status_error, sample.source)
        }
    }

    private fun buildLatestSpeedText(enabled: Boolean, sample: SpeedSample?): String {
        if (!enabled) {
            return appContext.getString(R.string.speed_latest_disabled)
        }
        if (sample?.isAvailable == true) {
            return appContext.getString(R.string.speed_latest_value, sample.speedKmh.roundToInt())
        }
        return appContext.getString(R.string.speed_latest_unavailable)
    }

    private fun buildSourceText(enabled: Boolean, sample: SpeedSample?): String {
        if (!enabled || sample == null) {
            return appContext.getString(R.string.speed_source_unavailable)
        }
        return sample.details.ifEmpty {
            appContext.getString(R.string.speed_source_empty)
        }
    }
}
