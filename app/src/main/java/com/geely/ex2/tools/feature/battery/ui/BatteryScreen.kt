package com.geely.ex2.tools.feature.battery.ui

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
import com.geely.ex2.tools.data.battery.BatteryWidgetRank
import com.geely.ex2.tools.feature.battery.BatteryViewModel
import com.geely.ex2.tools.ui.components.FlymeSettingsInfoItem
import com.geely.ex2.tools.ui.components.FlymeSettingsSection
import com.geely.ex2.tools.ui.components.FlymeSettingsStepperItem
import com.geely.ex2.tools.ui.components.FlymeSettingsSwitchItem
import com.geely.ex2.tools.ui.components.GeelyTopAppBar
import com.geely.ex2.tools.ui.components.isFlymeRailCompact
import com.geely.ex2.tools.ui.theme.GeelyEx2ToolsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BatteryViewModel = viewModel(),
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
                title = stringResource(R.string.battery_screen_title),
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
            BatteryStatusHeader(
                isEnabled = uiState.isEnabled,
                latestSocText = uiState.latestSocText,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            )

            FlymeSettingsSection(title = stringResource(R.string.battery_section_control)) {
                FlymeSettingsSwitchItem(
                    title = stringResource(R.string.battery_enable_title),
                    summary = if (uiState.isEnabled) {
                        stringResource(R.string.battery_enable_summary_on)
                    } else {
                        stringResource(R.string.battery_enable_summary_off)
                    },
                    checked = uiState.isEnabled,
                    onCheckedChange = viewModel::onEnabledCheckedChange,
                    showDivider = false,
                )
            }

            FlymeSettingsSection(title = stringResource(R.string.battery_section_icon)) {
                FlymeSettingsStepperItem(
                    title = stringResource(R.string.battery_widget_position_title),
                    summary = stringResource(
                        R.string.battery_widget_position_summary_rank,
                        uiState.widgetRank,
                    ),
                    onStepLeft = { viewModel.onWidgetRankStep(-BatteryWidgetRank.STEP) },
                    onStepRight = { viewModel.onWidgetRankStep(BatteryWidgetRank.STEP) },
                    canStepLeft = uiState.canStepWidgetLeft,
                    canStepRight = uiState.canStepWidgetRight,
                    leftContentDescription = stringResource(R.string.battery_widget_position_step_left),
                    rightContentDescription = stringResource(R.string.battery_widget_position_step_right),
                    enabled = uiState.isEnabled,
                    showDivider = false,
                )
            }

            FlymeSettingsSection(title = stringResource(R.string.battery_section_status)) {
                FlymeSettingsInfoItem(
                    title = stringResource(R.string.battery_status_title),
                    summary = uiState.statusText,
                )
                FlymeSettingsInfoItem(
                    title = stringResource(R.string.battery_latest_title),
                    summary = uiState.latestSocText,
                )
                FlymeSettingsInfoItem(
                    title = stringResource(R.string.battery_source_title),
                    summary = uiState.sourceText,
                )
            }
        }
    }
}

@Composable
private fun BatteryStatusHeader(
    isEnabled: Boolean,
    latestSocText: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_notification_battery),
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
                latestSocText
            } else {
                stringResource(R.string.battery_header_off)
            },
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview(showBackground = true, name = "Battery Light")
@Composable
private fun BatteryScreenPreviewLight() {
    GeelyEx2ToolsTheme(darkTheme = false) {
        BatteryScreen(onBack = {})
    }
}

@Preview(showBackground = true, name = "Battery Dark")
@Composable
private fun BatteryScreenPreviewDark() {
    GeelyEx2ToolsTheme(darkTheme = true) {
        BatteryScreen(onBack = {})
    }
}
