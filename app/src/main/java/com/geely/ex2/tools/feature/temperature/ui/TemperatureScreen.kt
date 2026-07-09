package com.geely.ex2.tools.feature.temperature.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
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
import com.geely.ex2.tools.data.temperature.TemperatureWidgetRank
import com.geely.ex2.tools.feature.temperature.TemperatureViewModel
import com.geely.ex2.tools.ui.components.FlymeSettingsInfoItem
import com.geely.ex2.tools.ui.components.FlymeSettingsStepperItem
import com.geely.ex2.tools.ui.components.FlymeSettingsSection
import com.geely.ex2.tools.ui.components.FlymeSettingsSwitchItem
import com.geely.ex2.tools.ui.components.GeelyTopAppBar
import com.geely.ex2.tools.ui.components.isFlymeRailCompact
import com.geely.ex2.tools.ui.theme.GeelyEx2ToolsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemperatureScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TemperatureViewModel = viewModel(),
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
                title = stringResource(R.string.temperature_screen_title),
                onBack = onBack.takeIf { isFlymeRailCompact() },
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
            TemperatureStatusHeader(
                isEnabled = uiState.isEnabled,
                latestTemperatureText = uiState.latestTemperatureText,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            )

            FlymeSettingsSection(title = stringResource(R.string.temperature_section_control)) {
                FlymeSettingsSwitchItem(
                    title = stringResource(R.string.temperature_enable_title),
                    summary = if (uiState.isEnabled) {
                        stringResource(R.string.temperature_enable_summary_on)
                    } else {
                        stringResource(R.string.temperature_enable_summary_off)
                    },
                    checked = uiState.isEnabled,
                    onCheckedChange = viewModel::onEnabledCheckedChange,
                    showDivider = false,
                )
            }

            FlymeSettingsSection(title = stringResource(R.string.temperature_section_icon)) {
                FlymeSettingsStepperItem(
                    title = stringResource(R.string.temperature_widget_position_title),
                    summary = stringResource(
                        R.string.temperature_widget_position_summary_rank,
                        uiState.widgetRank,
                    ),
                    onStepLeft = { viewModel.onWidgetRankStep(-TemperatureWidgetRank.STEP) },
                    onStepRight = { viewModel.onWidgetRankStep(TemperatureWidgetRank.STEP) },
                    canStepLeft = uiState.canStepWidgetLeft,
                    canStepRight = uiState.canStepWidgetRight,
                    leftContentDescription = stringResource(R.string.temperature_widget_position_step_left),
                    rightContentDescription = stringResource(R.string.temperature_widget_position_step_right),
                    enabled = uiState.isEnabled,
                    showDivider = false,
                )
            }

            FlymeSettingsSection(title = stringResource(R.string.temperature_section_status)) {
                FlymeSettingsInfoItem(
                    title = stringResource(R.string.temperature_status_title),
                    summary = uiState.statusText,
                )
                FlymeSettingsInfoItem(
                    title = stringResource(R.string.temperature_latest_title),
                    summary = uiState.latestTemperatureText,
                )
            }
        }
    }
}

@Composable
private fun TemperatureStatusHeader(
    isEnabled: Boolean,
    latestTemperatureText: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_notification_temp),
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            colorFilter = ColorFilter.tint(
                if (isEnabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            ),
        )
        Text(
            text = if (isEnabled) {
                latestTemperatureText
            } else {
                stringResource(R.string.temperature_header_off)
            },
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview(showBackground = true, name = "Temperature Light")
@Composable
private fun TemperatureScreenPreviewLight() {
    GeelyEx2ToolsTheme(darkTheme = false) {
        TemperatureScreen(onBack = {})
    }
}

@Preview(showBackground = true, name = "Temperature Dark")
@Composable
private fun TemperatureScreenPreviewDark() {
    GeelyEx2ToolsTheme(darkTheme = true) {
        TemperatureScreen(onBack = {})
    }
}
