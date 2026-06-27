package com.geely.ex2.tools.feature.driving

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geely.ex2.tools.R
import com.geely.ex2.tools.data.driving.DrivingModeRepository
import com.geely.ex2.tools.data.vhal.DrivingMode
import com.geely.ex2.tools.data.vhal.DrivingModeSample
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

data class DrivingUiState(
    val currentModeText: String = "",
    val selectedIndex: Int = -1,
    val isAvailable: Boolean = false,
    val isWritable: Boolean = true,
    val isPersistEnabled: Boolean = true,
    val savedModeText: String = "",
    val statusText: String = "",
    val rawValueText: String = "",
    val sourceText: String = "",
    val isChangingMode: Boolean = false,
)

class DrivingViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val repository = DrivingModeRepository(appContext)

    private val _uiState = MutableStateFlow(DrivingUiState())
    val uiState: StateFlow<DrivingUiState> = _uiState.asStateFlow()

    private var pollJob: Job? = null

    fun onResume() {
        refreshState()
        startRestoreService("DrivingScreen resume")
        startPolling()
    }

    fun onPause() {
        stopPolling()
        repository.stopRestoreService("DrivingScreen pause")
    }

    fun onPersistCheckedChange(enabled: Boolean) {
        if (enabled == _uiState.value.isPersistEnabled) return
        repository.setPersistEnabled(enabled)
        if (enabled) {
            val sample = repository.readDrivingMode()
            val modeToSave = when {
                sample.isAvailable && DrivingMode.isSelectableValue(sample.modeValue) ->
                    sample.modeValue
                DrivingMode.isSelectableValue(repository.getSavedModeValue()) ->
                    repository.getSavedModeValue()
                else ->
                    VhalConstants.DRIVE_MODE_COMFORT
            }
            repository.saveSelectedMode(modeToSave)
            repository.restoreSavedModeIfNeeded("UI persist enable")
        } else {
            repository.stopRestoreService("UI persist disable")
        }
        startRestoreService("UI persist toggle")
        refreshState()
    }

    fun onModeSelected(index: Int) {
        if (index !in DrivingMode.selectable.indices) return
        if (_uiState.value.isChangingMode) return

        val option = DrivingMode.selectable[index]
        if (index == _uiState.value.selectedIndex) return

        _uiState.update { it.copy(isChangingMode = true) }
        viewModelScope.launch {
            val result = repository.setDrivingMode(option.vhalValue)
            if (result.ok) {
                repository.saveSelectedMode(option.vhalValue)
                if (repository.isPersistEnabled()) {
                    repository.restoreSavedModeIfNeeded("UI mode selected")
                }
            }
            startRestoreService("UI mode selected")
            refreshState(
                writeError = if (result.ok) {
                    null
                } else {
                    result.error ?: appContext.getString(R.string.driving_write_error_unknown)
                },
            )
            _uiState.update { it.copy(isChangingMode = false) }
        }
    }

    override fun onCleared() {
        stopPolling()
        super.onCleared()
    }

    private fun startRestoreService(reason: String) {
        repository.startRestoreService(reason)
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
        val sample = repository.readDrivingMode()
        _uiState.update {
            buildUiState(sample, writeError)
        }
    }

    private fun buildUiState(sample: DrivingModeSample, writeError: String?): DrivingUiState {
        val persistEnabled = repository.isPersistEnabled()
        val savedModeValue = repository.getSavedModeValue()
        val selectedIndex = if (sample.isAvailable) {
            DrivingMode.selectableIndexFor(sample.modeValue)
        } else {
            -1
        }

        return DrivingUiState(
            currentModeText = buildCurrentModeText(sample),
            selectedIndex = selectedIndex,
            isAvailable = sample.isAvailable,
            isWritable = sample.isAvailable && !_uiState.value.isChangingMode,
            isPersistEnabled = persistEnabled,
            savedModeText = buildSavedModeText(savedModeValue),
            statusText = buildStatusText(sample, writeError, persistEnabled),
            rawValueText = buildRawValueText(sample),
            sourceText = buildSourceText(sample),
            isChangingMode = _uiState.value.isChangingMode,
        )
    }

    private fun buildSavedModeText(savedModeValue: Int): String {
        val labelRes = DrivingMode.labelResFor(savedModeValue)
        return if (labelRes != null) {
            appContext.getString(labelRes)
        } else {
            appContext.getString(
                R.string.driving_current_unknown,
                String.format(Locale.US, "0x%08X", savedModeValue),
            )
        }
    }

    private fun buildCurrentModeText(sample: DrivingModeSample): String {
        if (!sample.isAvailable) {
            return appContext.getString(R.string.driving_current_unavailable)
        }

        val labelRes = DrivingMode.labelResFor(sample.modeValue)
        return if (labelRes != null) {
            appContext.getString(labelRes)
        } else {
            appContext.getString(
                R.string.driving_current_unknown,
                String.format(Locale.US, "0x%08X", sample.modeValue),
            )
        }
    }

    private fun buildStatusText(
        sample: DrivingModeSample,
        writeError: String?,
        persistEnabled: Boolean,
    ): String {
        if (writeError != null) {
            return appContext.getString(R.string.driving_status_write_error, writeError)
        }
        if (!sample.isAvailable) {
            return appContext.getString(R.string.driving_status_error, sample.source)
        }
        if (persistEnabled) {
            return appContext.getString(R.string.driving_status_ok_persist, sample.source)
        }
        return appContext.getString(R.string.driving_status_ok, sample.source)
    }

    private fun buildRawValueText(sample: DrivingModeSample): String {
        if (!sample.isAvailable) {
            return appContext.getString(R.string.driving_raw_unavailable)
        }
        return String.format(Locale.US, "0x%08X (%d)", sample.modeValue, sample.modeValue)
    }

    private fun buildSourceText(sample: DrivingModeSample): String {
        if (!sample.isAvailable) {
            return appContext.getString(R.string.driving_source_unavailable)
        }
        return sample.details.ifEmpty {
            appContext.getString(R.string.driving_source_empty)
        }
    }
}
