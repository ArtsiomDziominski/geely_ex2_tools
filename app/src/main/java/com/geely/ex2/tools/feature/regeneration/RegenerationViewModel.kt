package com.geely.ex2.tools.feature.regeneration

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geely.ex2.tools.R
import com.geely.ex2.tools.data.regeneration.EnergyRegenerationRepository
import com.geely.ex2.tools.data.vhal.EnergyRegeneration
import com.geely.ex2.tools.data.vhal.EnergyRegenerationSample
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

data class RegenerationUiState(
    val currentLevelText: String = "",
    val selectedIndex: Int = -1,
    val isAvailable: Boolean = false,
    val isWritable: Boolean = true,
    val isPersistEnabled: Boolean = true,
    val savedLevelText: String = "",
    val statusText: String = "",
    val rawValueText: String = "",
    val sourceText: String = "",
    val isChangingLevel: Boolean = false,
)

class RegenerationViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val repository = EnergyRegenerationRepository(appContext)

    private val _uiState = MutableStateFlow(RegenerationUiState())
    val uiState: StateFlow<RegenerationUiState> = _uiState.asStateFlow()

    private var pollJob: Job? = null

    fun onResume() {
        refreshState()
        startRestoreService("RegenerationScreen resume")
        startPolling()
    }

    fun onPause() {
        stopPolling()
        repository.stopRestoreService("RegenerationScreen pause")
    }

    fun onPersistCheckedChange(enabled: Boolean) {
        if (enabled == _uiState.value.isPersistEnabled) return
        repository.setPersistEnabled(enabled)
        if (enabled) {
            val sample = repository.readEnergyRegeneration()
            val levelToSave = when {
                sample.isAvailable && EnergyRegeneration.isSelectableValue(sample.levelValue) ->
                    sample.levelValue
                EnergyRegeneration.isSelectableValue(repository.getSavedLevelValue()) ->
                    repository.getSavedLevelValue()
                else ->
                    VhalConstants.REGEN_LEVEL_MID
            }
            repository.saveSelectedLevel(levelToSave)
            repository.restoreSavedLevelIfNeeded("UI persist enable")
        } else {
            repository.stopRestoreService("UI persist disable")
        }
        startRestoreService("UI persist toggle")
        refreshState()
    }

    fun onLevelSelected(index: Int) {
        if (index !in EnergyRegeneration.selectable.indices) return
        if (_uiState.value.isChangingLevel) return

        val option = EnergyRegeneration.selectable[index]
        if (index == _uiState.value.selectedIndex) return

        _uiState.update { it.copy(isChangingLevel = true) }
        viewModelScope.launch {
            val result = repository.setEnergyRegeneration(option.vhalValue)
            if (result.ok) {
                repository.saveSelectedLevel(option.vhalValue)
                if (repository.isPersistEnabled()) {
                    repository.restoreSavedLevelIfNeeded("UI level selected")
                }
            }
            startRestoreService("UI level selected")
            refreshState(
                writeError = if (result.ok) {
                    null
                } else {
                    result.error ?: appContext.getString(R.string.regen_write_error_unknown)
                },
            )
            _uiState.update { it.copy(isChangingLevel = false) }
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
        val sample = repository.readEnergyRegeneration()
        _uiState.update {
            buildUiState(sample, writeError)
        }
    }

    private fun buildUiState(sample: EnergyRegenerationSample, writeError: String?): RegenerationUiState {
        val persistEnabled = repository.isPersistEnabled()
        val savedLevelValue = repository.getSavedLevelValue()
        val selectedIndex = if (sample.isAvailable) {
            EnergyRegeneration.selectableIndexFor(sample.levelValue)
        } else {
            -1
        }

        return RegenerationUiState(
            currentLevelText = buildCurrentLevelText(sample),
            selectedIndex = selectedIndex,
            isAvailable = sample.isAvailable,
            isWritable = sample.isAvailable && !_uiState.value.isChangingLevel,
            isPersistEnabled = persistEnabled,
            savedLevelText = buildSavedLevelText(savedLevelValue),
            statusText = buildStatusText(sample, writeError, persistEnabled),
            rawValueText = buildRawValueText(sample),
            sourceText = buildSourceText(sample),
            isChangingLevel = _uiState.value.isChangingLevel,
        )
    }

    private fun buildSavedLevelText(savedLevelValue: Int): String {
        val labelRes = EnergyRegeneration.labelResFor(savedLevelValue)
        return if (labelRes != null) {
            appContext.getString(labelRes)
        } else {
            appContext.getString(
                R.string.regen_current_unknown,
                String.format(Locale.US, "0x%08X", savedLevelValue),
            )
        }
    }

    private fun buildCurrentLevelText(sample: EnergyRegenerationSample): String {
        if (!sample.isAvailable) {
            return appContext.getString(R.string.regen_current_unavailable)
        }

        val labelRes = EnergyRegeneration.labelResFor(sample.levelValue)
        return if (labelRes != null) {
            appContext.getString(labelRes)
        } else {
            appContext.getString(
                R.string.regen_current_unknown,
                String.format(Locale.US, "0x%08X", sample.levelValue),
            )
        }
    }

    private fun buildStatusText(
        sample: EnergyRegenerationSample,
        writeError: String?,
        persistEnabled: Boolean,
    ): String {
        if (writeError != null) {
            return appContext.getString(R.string.regen_status_write_error, writeError)
        }
        if (!sample.isAvailable) {
            return appContext.getString(R.string.regen_status_error, sample.source)
        }
        if (persistEnabled) {
            return appContext.getString(R.string.regen_status_ok_persist, sample.source)
        }
        return appContext.getString(R.string.regen_status_ok, sample.source)
    }

    private fun buildRawValueText(sample: EnergyRegenerationSample): String {
        if (!sample.isAvailable) {
            return appContext.getString(R.string.regen_raw_unavailable)
        }
        return String.format(Locale.US, "0x%08X (%d)", sample.levelValue, sample.levelValue)
    }

    private fun buildSourceText(sample: EnergyRegenerationSample): String {
        if (!sample.isAvailable) {
            return appContext.getString(R.string.regen_source_unavailable)
        }
        return sample.details.ifEmpty {
            appContext.getString(R.string.regen_source_empty)
        }
    }
}
