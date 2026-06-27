package com.geely.ex2.tools.feature.avas.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
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
import com.geely.ex2.tools.feature.avas.AvasSoundViewModel
import com.geely.ex2.tools.ui.components.FlymeSettingsInfoItem
import com.geely.ex2.tools.ui.components.FlymeSettingsSection
import com.geely.ex2.tools.ui.components.FlymeSettingsSwitchItem
import com.geely.ex2.tools.ui.theme.GeelyEx2ToolsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvasSoundScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AvasSoundViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

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

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.avas_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.nav_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f),
                ),
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
            AvasSoundStatusHeader(
                currentStateText = uiState.currentStateText,
                soundTypeText = uiState.soundTypeText,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            )

            FlymeSettingsSection(title = stringResource(R.string.avas_section_control)) {
                FlymeSettingsSwitchItem(
                    title = stringResource(R.string.avas_toggle_title),
                    summary = if (uiState.isEnabled) {
                        stringResource(R.string.avas_toggle_summary_on)
                    } else {
                        stringResource(R.string.avas_toggle_summary_off)
                    },
                    checked = uiState.isEnabled,
                    onCheckedChange = viewModel::onEnabledCheckedChange,
                    enabled = uiState.isWritable && uiState.isAvailable && !uiState.isChanging,
                    showDivider = true,
                )
                Button(
                    onClick = viewModel::onDisableCompletelyClick,
                    enabled = uiState.isWritable && uiState.isAvailable &&
                        !uiState.isChanging && !uiState.isFullyDisabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(stringResource(R.string.avas_disable_completely))
                }
            }

            FlymeSettingsSection(title = stringResource(R.string.avas_section_status)) {
                FlymeSettingsInfoItem(
                    title = stringResource(R.string.avas_current_title),
                    summary = uiState.currentStateText,
                )
                FlymeSettingsInfoItem(
                    title = stringResource(R.string.avas_type_title),
                    summary = uiState.soundTypeText,
                )
                FlymeSettingsInfoItem(
                    title = stringResource(R.string.avas_status_title),
                    summary = uiState.statusText,
                )
                FlymeSettingsInfoItem(
                    title = stringResource(R.string.avas_raw_switch_title),
                    summary = uiState.rawSwitchText,
                )
                FlymeSettingsInfoItem(
                    title = stringResource(R.string.avas_raw_type_title),
                    summary = uiState.rawTypeText,
                )
                FlymeSettingsInfoItem(
                    title = stringResource(R.string.avas_source_title),
                    summary = uiState.sourceText,
                )
            }
        }
    }
}

@Composable
private fun AvasSoundStatusHeader(
    currentStateText: String,
    soundTypeText: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.avas_current_title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = currentStateText,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = soundTypeText,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AvasSoundScreenPreview() {
    GeelyEx2ToolsTheme {
        AvasSoundScreen(onBack = {})
    }
}
