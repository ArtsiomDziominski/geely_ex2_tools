package com.geely.ex2.tools.feature.ambient

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geely.ex2.tools.R
import com.geely.ex2.tools.data.ambient.AmbientLightRepository
import com.geely.ex2.tools.data.vhal.AmbientLightSample
import com.geely.ex2.tools.data.vhal.VhalConstants
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

data class AmbientLightUiState(
    val isOn: Boolean = false,
    val isAvailable: Boolean = false,
    val isWritable: Boolean = true,
    val currentStateText: String = "",
    val statusText: String = "",
    val rawValueText: String = "",
    val sourceText: String = "",
    val isChanging: Boolean = false,
)

class AmbientLightViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val repository = AmbientLightRepository(appContext)

    private val _uiState = MutableStateFlow(AmbientLightUiState())
    val uiState: StateFlow<AmbientLightUiState> = _uiState.asStateFlow()

    private var pollJob: Job? = null

    init {
        refreshState()
    }

    fun onResume() {
        refreshState()
        startPolling()
    }

    fun onPause() {
        stopPolling()
    }

    fun onEnabledCheckedChange(enabled: Boolean) {
        if (_uiState.value.isChanging) return
        if (_uiState.value.isAvailable && enabled == _uiState.value.isOn) return

        _uiState.update { it.copy(isChanging = true) }
        viewModelScope.launch {
            val result = repository.setAmbientLight(enabled)
            refreshState(
                writeError = if (result.ok) {
                    null
                } else {
                    result.error ?: appContext.getString(R.string.ambient_write_error_unknown)
                },
            )
            _uiState.update { it.copy(isChanging = false) }
        }
    }

    override fun onCleared() {
        stopPolling()
        super.onCleared()
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive) {
                delay(VhalConstants.POLL_INTERVAL_MS)
                refreshState()
            }
        }
    }

    private fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    private fun refreshState(writeError: String? = null) {
        val sample = repository.readAmbientLight()
        _uiState.update {
            buildUiState(sample, writeError)
        }
    }

    private fun buildUiState(sample: AmbientLightSample, writeError: String?): AmbientLightUiState {
        val isOn = sample.isOn == true
        return AmbientLightUiState(
            isOn = isOn,
            isAvailable = sample.isAvailable,
            isWritable = sample.isAvailable && !_uiState.value.isChanging,
            currentStateText = buildCurrentStateText(sample),
            statusText = buildStatusText(sample, writeError),
            rawValueText = buildRawValueText(sample),
            sourceText = buildSourceText(sample),
            isChanging = _uiState.value.isChanging,
        )
    }

    private fun buildCurrentStateText(sample: AmbientLightSample): String {
        if (!sample.isAvailable) {
            return appContext.getString(R.string.ambient_current_unavailable)
        }
        return when (sample.isOn) {
            true -> appContext.getString(R.string.ambient_state_on)
            false -> appContext.getString(R.string.ambient_state_off)
            null -> appContext.getString(
                R.string.ambient_current_unknown,
                String.format(Locale.US, "0x%08X", sample.rawValue),
            )
        }
    }

    private fun buildStatusText(sample: AmbientLightSample, writeError: String?): String {
        if (writeError != null) {
            return appContext.getString(R.string.ambient_status_write_error, writeError)
        }
        if (!sample.isAvailable) {
            return appContext.getString(R.string.ambient_status_error, sample.source)
        }
        return appContext.getString(R.string.ambient_status_ok, sample.source)
    }

    private fun buildRawValueText(sample: AmbientLightSample): String {
        if (!sample.isAvailable) {
            return appContext.getString(R.string.ambient_raw_unavailable)
        }
        return String.format(Locale.US, "0x%08X (%d)", sample.rawValue, sample.rawValue)
    }

    private fun buildSourceText(sample: AmbientLightSample): String {
        if (!sample.isAvailable) {
            return appContext.getString(R.string.ambient_source_unavailable)
        }
        return sample.details.ifEmpty {
            appContext.getString(R.string.ambient_source_empty)
        }
    }
}
