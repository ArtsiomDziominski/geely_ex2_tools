package com.geely.ex2.tools.feature.ambient.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.geely.ex2.tools.feature.ambient.AmbientLightViewModel
import com.geely.ex2.tools.ui.components.FlymeSettingsInfoItem
import com.geely.ex2.tools.ui.components.FlymeSettingsSection
import com.geely.ex2.tools.ui.components.FlymeSettingsSwitchItem
import com.geely.ex2.tools.ui.components.GeelyTopAppBar
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
            GeelyTopAppBar(
                title = stringResource(R.string.ambient_screen_title),
                onBack = onBack,
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
                currentStateText = uiState.currentStateText,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            )

            FlymeSettingsSection(title = stringResource(R.string.ambient_section_control)) {
                FlymeSettingsSwitchItem(
                    title = stringResource(R.string.ambient_toggle_title),
                    summary = if (uiState.isOn) {
                        stringResource(R.string.ambient_toggle_summary_on)
                    } else {
                        stringResource(R.string.ambient_toggle_summary_off)
                    },
                    checked = uiState.isOn,
                    onCheckedChange = viewModel::onEnabledCheckedChange,
                    enabled = uiState.isWritable && uiState.isAvailable && !uiState.isChanging,
                    showDivider = false,
                )
            }

            FlymeSettingsSection(title = stringResource(R.string.ambient_section_status)) {
                FlymeSettingsInfoItem(
                    title = stringResource(R.string.ambient_current_title),
                    summary = uiState.currentStateText,
                )
                FlymeSettingsInfoItem(
                    title = stringResource(R.string.ambient_status_title),
                    summary = uiState.statusText,
                )
                FlymeSettingsInfoItem(
                    title = stringResource(R.string.ambient_raw_title),
                    summary = uiState.rawValueText,
                )
                FlymeSettingsInfoItem(
                    title = stringResource(R.string.ambient_source_title),
                    summary = uiState.sourceText,
                )
            }
        }
    }
}

@Composable
private fun AmbientLightStatusHeader(
    currentStateText: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.ambient_current_title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = currentStateText,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AmbientLightScreenPreview() {
    GeelyEx2ToolsTheme {
        AmbientLightScreen(onBack = {})
    }
}
