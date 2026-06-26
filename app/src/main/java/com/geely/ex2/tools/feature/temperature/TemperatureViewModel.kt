package com.geely.ex2.tools.feature.temperature

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.geely.ex2.tools.R
import com.geely.ex2.tools.data.temperature.TemperatureReader
import com.geely.ex2.tools.data.temperature.TemperatureRepository
import com.geely.ex2.tools.data.temperature.TemperatureWidgetRank
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    init {
        refreshState()
        repository.startStatusServiceIfEnabled("TemperatureViewModel init")
        repository.notifyStatusIconIfEnabled("TemperatureViewModel init")
    }

    fun onResume() {
        refreshState()
        repository.startStatusServiceIfEnabled("TemperatureScreen resume")
        repository.notifyStatusIconIfEnabled("TemperatureScreen resume")
    }

    fun onEnabledCheckedChange(enabled: Boolean) {
        if (enabled == _uiState.value.isEnabled) return
        repository.setEnabled(enabled)
        if (enabled) {
            repository.startStatusServiceIfEnabled("UI temperature enable")
            repository.notifyStatusIconIfEnabled("UI temperature enable")
        } else {
            repository.stopStatusService("UI temperature disable")
            repository.notifyStatusIconIfEnabled("UI temperature disable")
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

    private fun refreshState() {
        val enabled = repository.isEnabled()
        val widgetRank = repository.getStatusIconRank()
        val result = if (enabled) {
            repository.readTemperature()
        } else {
            null
        }

        _uiState.update {
            TemperatureUiState(
                isEnabled = enabled,
                widgetRank = widgetRank,
                canStepWidgetLeft = TemperatureWidgetRank.canStepLeft(widgetRank),
                canStepWidgetRight = TemperatureWidgetRank.canStepRight(widgetRank),
                statusText = buildStatusText(enabled, result),
                latestTemperatureText = buildLatestTemperatureText(enabled, result),
            )
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
