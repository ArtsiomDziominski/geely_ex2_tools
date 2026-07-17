package com.geely.ex2.tools.feature.system

import android.app.Application
import android.text.format.Formatter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geely.ex2.tools.data.system.SystemMemoryReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SystemUiState(
    val hasMemory: Boolean = false,
    val usedFraction: Float = 0f,
    val usedPercent: Int = 0,
    val usedLabel: String = "",
    val totalLabel: String = "",
    val availLabel: String = "",
)

class SystemViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext

    private val _uiState = MutableStateFlow(SystemUiState())
    val uiState: StateFlow<SystemUiState> = _uiState.asStateFlow()

    private var refreshJob: Job? = null

    /** One read after opening the tab; cancelled when the tab is left. */
    fun onResume() {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            refreshMemory()
        }
    }

    fun onPause() {
        refreshJob?.cancel()
        refreshJob = null
    }

    /** Immediate read from the refresh button. */
    fun refreshNow() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            refreshMemory()
        }
    }

    override fun onCleared() {
        onPause()
        super.onCleared()
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
}
