package com.geely.ex2.tools.feature.avas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geely.ex2.tools.R
import com.geely.ex2.tools.data.avas.AvasSoundRepository
import com.geely.ex2.tools.data.vhal.AvasSoundSample
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

data class AvasSoundUiState(
    val isEnabled: Boolean = false,
    val isFullyDisabled: Boolean = false,
    val isAvailable: Boolean = false,
    val isWritable: Boolean = true,
    val currentStateText: String = "",
    val soundTypeText: String = "",
    val statusText: String = "",
    val rawSwitchText: String = "",
    val rawTypeText: String = "",
    val sourceText: String = "",
    val isChanging: Boolean = false,
)

class AvasSoundViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val repository = AvasSoundRepository(appContext)

    private val _uiState = MutableStateFlow(AvasSoundUiState())
    val uiState: StateFlow<AvasSoundUiState> = _uiState.asStateFlow()

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
        if (_uiState.value.isAvailable && enabled == _uiState.value.isEnabled) return

        _uiState.update { it.copy(isChanging = true) }
        viewModelScope.launch {
            val result = repository.setAvasSoundEnabled(enabled)
            refreshState(
                writeError = if (result.ok) {
                    null
                } else {
                    result.error ?: appContext.getString(R.string.avas_write_error_unknown)
                },
            )
            _uiState.update { it.copy(isChanging = false) }
        }
    }

    fun onDisableCompletelyClick() {
        if (_uiState.value.isChanging || !_uiState.value.isAvailable) return
        if (_uiState.value.isFullyDisabled) return

        onEnabledCheckedChange(false)
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
        val sample = repository.readAvasSound()
        _uiState.update {
            buildUiState(sample, writeError)
        }
    }

    private fun buildUiState(sample: AvasSoundSample, writeError: String?): AvasSoundUiState {
        val isEnabled = sample.isEnabled == true
        val isFullyDisabled = sample.isEnabled == false &&
            (sample.soundType == null || sample.soundType == VhalConstants.AVAS_SOUND_TYPE_NONE)
        return AvasSoundUiState(
            isEnabled = isEnabled,
            isFullyDisabled = isFullyDisabled,
            isAvailable = sample.isAvailable,
            isWritable = sample.isAvailable && !_uiState.value.isChanging,
            currentStateText = buildCurrentStateText(sample),
            soundTypeText = buildSoundTypeText(sample),
            statusText = buildStatusText(sample, writeError),
            rawSwitchText = buildRawSwitchText(sample),
            rawTypeText = buildRawTypeText(sample),
            sourceText = buildSourceText(sample),
            isChanging = _uiState.value.isChanging,
        )
    }

    private fun buildCurrentStateText(sample: AvasSoundSample): String {
        if (!sample.isAvailable) {
            return appContext.getString(R.string.avas_current_unavailable)
        }
        return when (sample.isEnabled) {
            true -> appContext.getString(R.string.avas_state_on)
            false -> appContext.getString(R.string.avas_state_off)
            null -> appContext.getString(
                R.string.avas_current_unknown,
                String.format(Locale.US, "0x%08X", sample.rawSwitchValue),
            )
        }
    }

    private fun buildSoundTypeText(sample: AvasSoundSample): String {
        if (!sample.isAvailable) {
            return appContext.getString(R.string.avas_type_unavailable)
        }
        return when (sample.soundType) {
            null -> appContext.getString(R.string.avas_type_unknown)
            VhalConstants.AVAS_SOUND_TYPE_NONE -> appContext.getString(R.string.avas_type_none)
            VhalConstants.AVAS_SOUND_TYPE_1 -> appContext.getString(R.string.avas_type_1)
            VhalConstants.AVAS_SOUND_TYPE_2 -> appContext.getString(R.string.avas_type_2)
            VhalConstants.AVAS_SOUND_TYPE_3 -> appContext.getString(R.string.avas_type_3)
            else -> String.format(Locale.US, "0x%08X (%d)", sample.soundType, sample.soundType)
        }
    }

    private fun buildStatusText(sample: AvasSoundSample, writeError: String?): String {
        if (writeError != null) {
            return appContext.getString(R.string.avas_status_write_error, writeError)
        }
        if (!sample.isAvailable) {
            return appContext.getString(R.string.avas_status_error, sample.source)
        }
        return appContext.getString(R.string.avas_status_ok, sample.source)
    }

    private fun buildRawSwitchText(sample: AvasSoundSample): String {
        if (!sample.isAvailable) {
            return appContext.getString(R.string.avas_raw_unavailable)
        }
        return String.format(Locale.US, "0x%08X (%d)", sample.rawSwitchValue, sample.rawSwitchValue)
    }

    private fun buildRawTypeText(sample: AvasSoundSample): String {
        if (!sample.isAvailable) {
            return appContext.getString(R.string.avas_raw_unavailable)
        }
        return String.format(Locale.US, "0x%08X (%d)", sample.rawTypeValue, sample.rawTypeValue)
    }

    private fun buildSourceText(sample: AvasSoundSample): String {
        if (!sample.isAvailable) {
            return appContext.getString(R.string.avas_source_unavailable)
        }
        return sample.details.ifEmpty {
            appContext.getString(R.string.avas_source_empty)
        }
    }
}
