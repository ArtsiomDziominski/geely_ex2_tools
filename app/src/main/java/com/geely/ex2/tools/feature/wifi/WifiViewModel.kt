package com.geely.ex2.tools.feature.wifi

import android.app.Application
import android.net.wifi.WifiManager
import androidx.lifecycle.AndroidViewModel
import com.geely.ex2.tools.data.wifi.WifiRepository
import com.geely.ex2.tools.data.wifi.WifiWidgetRank
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class WifiUiState(
    val wifiState: Int = WifiManager.WIFI_STATE_UNKNOWN,
    val wifiStateLabel: String = "",
    val isAutoEnableEnabled: Boolean = true,
    val isWifiOn: Boolean = false,
    val widgetRank: Int = WifiWidgetRank.DEFAULT,
    val canStepWidgetLeft: Boolean = true,
    val canStepWidgetRight: Boolean = true,
)

class WifiViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WifiRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(WifiUiState())
    val uiState: StateFlow<WifiUiState> = _uiState.asStateFlow()

    init {
        refreshState()
        startServiceAndIcon("WifiViewModel init")
    }

    fun onResume() {
        refreshState()
        startServiceAndIcon("WifiScreen resume")
    }

    fun onWifiCheckedChange(enabled: Boolean) {
        if (enabled == _uiState.value.isWifiOn) return
        repository.setWifiEnabledFromUi(enabled)
        startServiceAndIcon("UI Wi-Fi switch")
        refreshState()
    }

    fun onAutoEnableCheckedChange(enabled: Boolean) {
        if (enabled == _uiState.value.isAutoEnableEnabled) return
        repository.setAutoEnableEnabled(enabled)
        startServiceAndIcon("UI auto-enable switch")
        refreshState()
    }

    fun onWidgetRankStep(delta: Int) {
        val currentRank = _uiState.value.widgetRank
        val canStep = if (delta < 0) {
            WifiWidgetRank.canStepLeft(currentRank)
        } else {
            WifiWidgetRank.canStepRight(currentRank)
        }
        if (!canStep) return

        val newRank = repository.stepStatusIconRank(delta)
        repository.notifyStatusIcon("UI widget rank step", newRank)
        _uiState.update {
            it.copy(
                widgetRank = newRank,
                canStepWidgetLeft = WifiWidgetRank.canStepLeft(newRank),
                canStepWidgetRight = WifiWidgetRank.canStepRight(newRank),
            )
        }
    }

    private fun refreshState() {
        val wifiState = repository.getWifiState()
        val isAutoEnable = repository.isAutoEnableEnabled()
        val isWifiOn = repository.isWifiEnabledOrEnabling()
        val widgetRank = repository.getStatusIconRank()

        _uiState.update {
            WifiUiState(
                wifiState = wifiState,
                wifiStateLabel = repository.wifiStateToLabel(wifiState),
                isAutoEnableEnabled = isAutoEnable,
                isWifiOn = isWifiOn,
                widgetRank = widgetRank,
                canStepWidgetLeft = WifiWidgetRank.canStepLeft(widgetRank),
                canStepWidgetRight = WifiWidgetRank.canStepRight(widgetRank),
            )
        }
    }

    private fun startServiceAndIcon(reason: String) {
        repository.startStatusService(reason)
        repository.notifyStatusIcon(reason)
    }
}
