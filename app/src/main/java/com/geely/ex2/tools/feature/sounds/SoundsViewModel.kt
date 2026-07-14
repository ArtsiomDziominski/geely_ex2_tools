package com.geely.ex2.tools.feature.sounds

import android.app.Application
import android.net.Uri
import android.os.PowerManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geely.ex2.tools.R
import com.geely.ex2.tools.data.sounds.CarLockSoundCatalog
import com.geely.ex2.tools.data.sounds.CarLockSoundOption
import com.geely.ex2.tools.data.sounds.CarLockSoundRepository
import com.geely.ex2.tools.data.sounds.CarLockSoundStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SoundsUiState(
    val isInstalled: Boolean = false,
    val isChanging: Boolean = false,
    val isImporting: Boolean = false,
    val isSystemUid: Boolean = false,
    val hasSource: Boolean = false,
    val sourcePath: String? = null,
    val sourceDisplayName: String? = null,
    val selectedSoundId: String? = null,
    val catalog: CarLockSoundCatalog = CarLockSoundCatalog(),
    val statusText: String = "",
    val showRebootDialog: Boolean = false,
)

class SoundsViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val repository = CarLockSoundRepository(appContext)

    private val _uiState = MutableStateFlow(SoundsUiState())
    val uiState: StateFlow<SoundsUiState> = _uiState.asStateFlow()

    private var refreshJob: Job? = null

    fun onResume() {
        refreshState()
    }

    fun onSoundSelected(option: CarLockSoundOption) {
        if (_uiState.value.isImporting || _uiState.value.isChanging) return
        if (_uiState.value.selectedSoundId == option.id) return

        _uiState.update { it.copy(isImporting = true) }
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                repository.selectSound(option)
            }
            if (!result.ok) {
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        statusText = appContext.getString(
                            R.string.sounds_error_import,
                            result.error.orEmpty(),
                        ),
                    )
                }
                return@launch
            }
            refreshState()
        }
    }

    fun onSoundPicked(uri: Uri?) {
        if (uri == null) return
        _uiState.update { it.copy(isImporting = true) }
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                repository.importSelectedSound(uri)
            }
            if (!result.ok) {
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        statusText = appContext.getString(
                            R.string.sounds_error_import,
                            result.error.orEmpty(),
                        ),
                    )
                }
                return@launch
            }
            refreshState()
        }
    }

    fun onCarLockSegmentSelected(index: Int) {
        if (_uiState.value.isChanging) return
        val wantInstalled = index == 1
        if (wantInstalled == _uiState.value.isInstalled) return

        _uiState.update { it.copy(isChanging = true) }
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                if (wantInstalled) repository.install() else repository.remove()
            }
            if (!result.ok) {
                val errorText = buildErrorText(result.error, result.details)
                refreshState(preserveStatusText = errorText)
                return@launch
            }

            refreshState(showRebootDialog = result.needsReboot)
        }
    }

    fun onRebootDismiss() {
        _uiState.update { it.copy(showRebootDialog = false) }
    }

    fun onRebootConfirm() {
        _uiState.update { it.copy(showRebootDialog = false) }
        try {
            val powerManager = appContext.getSystemService(PowerManager::class.java)
            powerManager?.reboot(null)
        } catch (_: SecurityException) {
            _uiState.update {
                it.copy(statusText = appContext.getString(R.string.sounds_error_reboot))
            }
        }
    }

    private fun refreshState(
        showRebootDialog: Boolean = false,
        preserveStatusText: String? = null,
    ) {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            val status = withContext(Dispatchers.IO) {
                repository.readStatus()
            }
            val catalog = withContext(Dispatchers.IO) {
                repository.listAvailableSounds()
            }
            _uiState.update {
                val base = buildUiState(status, catalog)
                base.copy(
                    showRebootDialog = when {
                        preserveStatusText != null -> false
                        showRebootDialog -> true
                        else -> it.showRebootDialog
                    },
                    statusText = preserveStatusText ?: base.statusText,
                    isImporting = false,
                )
            }
        }
    }

    private fun buildUiState(
        status: CarLockSoundStatus,
        catalog: CarLockSoundCatalog,
    ): SoundsUiState {
        val hasSource = status.sourcePath != null
        return SoundsUiState(
            isInstalled = status.isInstalled,
            isChanging = false,
            isSystemUid = status.isSystemUid,
            hasSource = hasSource,
            sourcePath = status.sourcePath,
            sourceDisplayName = status.sourceDisplayName,
            selectedSoundId = status.selectedSoundId,
            catalog = catalog,
            statusText = buildStatusText(status, hasSource, catalog),
        )
    }

    private fun buildStatusText(
        status: CarLockSoundStatus,
        hasSource: Boolean,
        catalog: CarLockSoundCatalog,
    ): String {
        if (!status.isSystemUid) {
            return appContext.getString(R.string.sounds_status_need_system)
        }
        if (!hasSource && !status.isInstalled) {
            return if (catalog.options.isEmpty()) {
                appContext.getString(R.string.sounds_catalog_empty)
            } else {
                appContext.getString(R.string.sounds_status_pick_sound)
            }
        }
        return if (status.isInstalled) {
            appContext.getString(R.string.sounds_status_installed)
        } else {
            appContext.getString(R.string.sounds_status_not_installed)
        }
    }

    private fun buildErrorText(error: String?, details: String): String = when (error) {
        "not_system_uid" -> appContext.getString(R.string.sounds_status_need_system)
        "source_missing" -> appContext.getString(R.string.sounds_status_pick_sound)
        "remount_failed" -> appContext.getString(R.string.sounds_error_remount, details)
        "backup_missing" -> appContext.getString(R.string.sounds_error_backup_missing)
        "backup_failed", "restore_failed", "copy_failed", "delete_failed",
        "verify_failed", "mkdir_failed",
        -> appContext.getString(R.string.sounds_error_write, details.ifEmpty { error.orEmpty() })
        else -> appContext.getString(R.string.sounds_error_write, error.orEmpty())
    }
}
