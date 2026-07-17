package com.geely.ex2.tools.feature.system

import android.app.Application
import android.text.format.Formatter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geely.ex2.tools.R
import com.geely.ex2.tools.data.sound.LockSoundRepository
import com.geely.ex2.tools.data.system.SystemMemoryReader
import com.geely.ex2.tools.data.vhal.LockSoundSample
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

data class SystemUiState(
    val hasMemory: Boolean = false,
    val usedFraction: Float = 0f,
    val usedPercent: Int = 0,
    val usedLabel: String = "",
    val totalLabel: String = "",
    val availLabel: String = "",
    val lockSoundEnabled: Boolean = false,
    val lockSoundAvailable: Boolean = false,
    val lockSoundWritable: Boolean = true,
    val lockSoundChanging: Boolean = false,
    val lockSoundStatusText: String = "",
)

class SystemViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val lockSoundRepository = LockSoundRepository(appContext)

    private val _uiState = MutableStateFlow(SystemUiState())
    val uiState: StateFlow<SystemUiState> = _uiState.asStateFlow()

    private var refreshJob: Job? = null
    private var lockSoundPollJob: Job? = null
    private var lockSoundRefreshJob: Job? = null

    /** One delayed read after opening the tab; cancelled when the tab is left. */
    fun onResume() {
        if (refreshJob?.isActive == true) {
            startLockSoundPolling()
            refreshLockSound()
            return
        }
        refreshJob = viewModelScope.launch {
            delay(TAB_REFRESH_DELAY_MS)
            if (isActive) {
                refreshMemory()
            }
        }
        refreshLockSound()
        startLockSoundPolling()
    }

    fun onPause() {
        refreshJob?.cancel()
        refreshJob = null
        stopLockSoundPolling()
    }

    /** Immediate read from the refresh button. */
    fun refreshNow() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            refreshMemory()
        }
    }

    fun onLockSoundCheckedChange(enabled: Boolean) {
        if (enabled == _uiState.value.lockSoundEnabled || _uiState.value.lockSoundChanging) {
            return
        }

        _uiState.update {
            it.copy(
                lockSoundEnabled = enabled,
                lockSoundChanging = true,
                lockSoundWritable = false,
            )
        }

        viewModelScope.launch {
            val write = withContext(Dispatchers.IO) {
                lockSoundRepository.setLockSoundEnabled(enabled)
            }
            if (!write.ok) {
                _uiState.update {
                    it.copy(
                        lockSoundChanging = false,
                        lockSoundStatusText = appContext.getString(
                            R.string.system_lock_sound_status_write_error,
                            write.error ?: appContext.getString(R.string.system_lock_sound_write_error_unknown),
                        ),
                    )
                }
                refreshLockSound()
                return@launch
            }

            refreshLockSound()
        }
    }

    override fun onCleared() {
        onPause()
        lockSoundRefreshJob?.cancel()
        super.onCleared()
    }

    private fun startLockSoundPolling() {
        lockSoundPollJob?.cancel()
        lockSoundPollJob = viewModelScope.launch {
            while (isActive) {
                delay(VhalConstants.LOCK_SOUND_UI_POLL_INTERVAL_MS)
                refreshLockSound()
            }
        }
    }

    private fun stopLockSoundPolling() {
        lockSoundPollJob?.cancel()
        lockSoundPollJob = null
        lockSoundRefreshJob?.cancel()
        lockSoundRefreshJob = null
    }

    private fun refreshLockSound() {
        lockSoundRefreshJob?.cancel()
        lockSoundRefreshJob = viewModelScope.launch {
            val sample = withContext(Dispatchers.IO) {
                lockSoundRepository.readLockSound()
            }
            _uiState.update { current ->
                buildLockSoundState(current, sample)
            }
        }
    }

    private fun buildLockSoundState(current: SystemUiState, sample: LockSoundSample): SystemUiState {
        return current.copy(
            lockSoundEnabled = sample.isEnabled,
            lockSoundAvailable = sample.isAvailable,
            lockSoundChanging = false,
            lockSoundWritable = sample.isAvailable && !current.lockSoundChanging,
            lockSoundStatusText = buildLockSoundStatusText(sample),
        )
    }

    private fun buildLockSoundStatusText(sample: LockSoundSample): String {
        if (!sample.isAvailable) {
            return appContext.getString(R.string.system_lock_sound_status_error, sample.source)
        }
        return appContext.getString(R.string.system_lock_sound_status_ok, sample.source)
    }

    private suspend fun refreshMemory() {
        val snapshot = withContext(Dispatchers.Default) {
            SystemMemoryReader.read(appContext)
        }
        if (snapshot == null) {
            _uiState.update { current ->
                if (current.hasMemory) current.copy(hasMemory = false) else current
            }
            return
        }

        val usedLabel = Formatter.formatShortFileSize(appContext, snapshot.usedBytes)
        val totalLabel = Formatter.formatShortFileSize(appContext, snapshot.totalBytes)
        val availLabel = Formatter.formatShortFileSize(appContext, snapshot.availBytes)

        _uiState.update { current ->
            if (
                current.hasMemory &&
                current.usedPercent == snapshot.usedPercent &&
                current.usedLabel == usedLabel &&
                current.totalLabel == totalLabel &&
                current.availLabel == availLabel
            ) {
                current
            } else {
                current.copy(
                    hasMemory = true,
                    usedFraction = snapshot.usedFraction,
                    usedPercent = snapshot.usedPercent,
                    usedLabel = usedLabel,
                    totalLabel = totalLabel,
                    availLabel = availLabel,
                )
            }
        }
    }

    companion object {
        private const val TAB_REFRESH_DELAY_MS = 1_000L
    }
}
