package com.geely.ex2.tools.feature.ambient.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geely.ex2.tools.R
import com.geely.ex2.tools.data.ambient.AmbientLightControlMode
import com.geely.ex2.tools.feature.ambient.AmbientLightTimePickerTarget
import com.geely.ex2.tools.feature.ambient.AmbientLightViewModel
import com.geely.ex2.tools.ui.components.FlymeSettingsInfoItem
import com.geely.ex2.tools.ui.components.FlymeSettingsSection
import com.geely.ex2.tools.ui.components.FlymeSettingsSegmentedItem
import com.geely.ex2.tools.ui.components.FlymeSettingsValueItem
import com.geely.ex2.tools.ui.components.FlymeTimePickerDialog
import com.geely.ex2.tools.ui.components.GeelyTopAppBar
import com.geely.ex2.tools.ui.theme.FlymeAccent
import com.geely.ex2.tools.ui.theme.GeelyEx2ToolsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmbientLightScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AmbientLightViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    var showInfoDialog by remember { mutableStateOf(false) }

    if (showInfoDialog) {
        AmbientLightInfoDialog(onDismiss = { showInfoDialog = false })
    }
    val modeLabels = AmbientLightControlMode.selectable.map { mode ->
        when (mode) {
            AmbientLightControlMode.AUTO -> stringResource(R.string.ambient_light_mode_auto)
            AmbientLightControlMode.OFF -> stringResource(R.string.ambient_light_mode_off)
            AmbientLightControlMode.ON -> stringResource(R.string.ambient_light_mode_on)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.onResume()
                Lifecycle.Event.ON_PAUSE -> viewModel.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.onPause()
        }
    }

    when (uiState.timePickerTarget) {
        AmbientLightTimePickerTarget.START -> {
            FlymeTimePickerDialog(
                title = stringResource(R.string.ambient_light_start_time_title),
                initialHour = uiState.startHour,
                initialMinute = uiState.startMinute,
                onConfirm = viewModel::onTimePickerConfirm,
                onDismiss = viewModel::onTimePickerDismiss,
            )
        }
        AmbientLightTimePickerTarget.END -> {
            FlymeTimePickerDialog(
                title = stringResource(R.string.ambient_light_end_time_title),
                initialHour = uiState.endHour,
                initialMinute = uiState.endMinute,
                onConfirm = viewModel::onTimePickerConfirm,
                onDismiss = viewModel::onTimePickerDismiss,
            )
        }
        null -> Unit
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            GeelyTopAppBar(
                title = stringResource(R.string.ambient_light_screen_title),
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = { showInfoDialog = true },
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = stringResource(
                                R.string.ambient_light_info_content_description,
                            ),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            AmbientLightStatusHeader(
                isEnabled = uiState.isEnabled,
                isAvailable = uiState.isAvailable,
                scheduleSummary = uiState.scheduleSummary,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            )

            FlymeSettingsSection(title = stringResource(R.string.ambient_light_section_schedule)) {
                FlymeSettingsSegmentedItem(
                    title = stringResource(R.string.ambient_light_mode_title),
                    summary = uiState.scheduleSummary,
                    options = modeLabels,
                    selectedIndex = uiState.controlModeIndex,
                    onSelectedIndexChange = viewModel::onControlModeSelected,
                    enabled = uiState.isAvailable,
                    showDivider = uiState.controlMode == AmbientLightControlMode.AUTO,
                )
                if (uiState.controlMode == AmbientLightControlMode.AUTO) {
                    FlymeSettingsValueItem(
                        title = stringResource(R.string.ambient_light_start_time_title),
                        value = uiState.startTimeText,
                        onClick = viewModel::onStartTimeClick,
                        enabled = uiState.isAvailable,
                    )
                    FlymeSettingsValueItem(
                        title = stringResource(R.string.ambient_light_end_time_title),
                        value = uiState.endTimeText,
                        onClick = viewModel::onEndTimeClick,
                        enabled = uiState.isAvailable,
                        showDivider = false,
                    )
                }
            }

            FlymeSettingsSection(title = stringResource(R.string.ambient_light_section_status)) {
                FlymeSettingsInfoItem(
                    title = stringResource(R.string.ambient_light_status_title),
                    summary = uiState.statusText,
                )
                FlymeSettingsInfoItem(
                    title = stringResource(R.string.ambient_light_source_title),
                    summary = uiState.sourceText,
                )
            }
        }
    }
}

@Composable
private fun AmbientLightStatusHeader(
    isEnabled: Boolean,
    isAvailable: Boolean,
    scheduleSummary: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.ambient_light_current_title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = when {
                !isAvailable -> stringResource(R.string.ambient_light_current_unavailable)
                isEnabled -> stringResource(R.string.ambient_light_header_on)
                else -> stringResource(R.string.ambient_light_header_off)
            },
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = if (isEnabled && isAvailable) {
                FlymeAccent
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
        if (isAvailable && scheduleSummary.isNotEmpty()) {
            Text(
                text = scheduleSummary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AmbientLightScreenPreview() {
    GeelyEx2ToolsTheme {
        AmbientLightScreen(onBack = {})
    }
}
