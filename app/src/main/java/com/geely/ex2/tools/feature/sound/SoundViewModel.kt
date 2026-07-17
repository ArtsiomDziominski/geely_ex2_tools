package com.geely.ex2.tools.feature.sound

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geely.ex2.tools.R
import com.geely.ex2.tools.data.sound.LockSoundRepository
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

data class SoundUiState(
    val lockSoundEnabled: Boolean = false,
    val lockSoundAvailable: Boolean = false,
    val lockSoundWritable: Boolean = true,
    val lockSoundChanging: Boolean = false,
    val lockSoundStatusText: String = "",
)

class SoundViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val lockSoundRepository = LockSoundRepository(appContext)

    private val _uiState = MutableStateFlow(SoundUiState())
    val uiState: StateFlow<SoundUiState> = _uiState.asStateFlow()

    private var pollJob: Job? = null
    private var refreshJob: Job? = null

    fun onResume() {
        refreshState()
        startPolling()
    }

    fun onPause() {
        stopPolling()
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
                            R.string.sound_lock_status_write_error,
                            write.error ?: appContext.getString(R.string.sound_lock_write_error_unknown),
                        ),
                    )
                }
                refreshState()
                return@launch
            }

            refreshState()
        }
    }

    override fun onCleared() {
        stopPolling()
        refreshJob?.cancel()
        super.onCleared()
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive) {
                delay(VhalConstants.LOCK_SOUND_UI_POLL_INTERVAL_MS)
                refreshState()
            }
        }
    }

    private fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
        refreshJob?.cancel()
        refreshJob = null
    }

    private fun refreshState() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            val sample = withContext(Dispatchers.IO) {
                lockSoundRepository.readLockSound()
            }
            _uiState.update { buildUiState(sample) }
        }
    }

    private fun buildUiState(sample: LockSoundSample): SoundUiState {
        return SoundUiState(
            lockSoundEnabled = sample.isEnabled,
            lockSoundAvailable = sample.isAvailable,
            lockSoundChanging = false,
            lockSoundWritable = sample.isAvailable,
            lockSoundStatusText = buildLockSoundStatusText(sample),
        )
    }

    private fun buildLockSoundStatusText(sample: LockSoundSample): String {
        if (!sample.isAvailable) {
            return appContext.getString(R.string.sound_lock_status_error, sample.source)
        }
        return appContext.getString(R.string.sound_lock_status_ok, sample.source)
    }
}
