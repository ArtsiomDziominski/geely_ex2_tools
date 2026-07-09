package com.geely.ex2.tools.feature.wifi.ui

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
import com.geely.ex2.tools.data.wifi.WifiWidgetRank
import com.geely.ex2.tools.feature.wifi.WifiViewModel
import com.geely.ex2.tools.ui.components.FlymeSettingsSection
import com.geely.ex2.tools.ui.components.FlymeSettingsStepperItem
import com.geely.ex2.tools.ui.components.FlymeSettingsSwitchItem
import com.geely.ex2.tools.ui.components.GeelyTopAppBar
import com.geely.ex2.tools.ui.components.isFlymeRailCompact
import com.geely.ex2.tools.ui.theme.GeelyEx2ToolsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WifiViewModel = viewModel(),
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
                title = stringResource(R.string.wifi_screen_title),
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
            WifiStatusHeader(
                isWifiOn = uiState.isWifiOn,
                wifiStateLabel = uiState.wifiStateLabel,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            )

            FlymeSettingsSection(title = stringResource(R.string.wifi_section_control)) {
                FlymeSettingsSwitchItem(
                    title = stringResource(R.string.wifi_toggle_title),
                    summary = if (uiState.isWifiOn) {
                        stringResource(R.string.wifi_toggle_summary_on, uiState.wifiStateLabel)
                    } else {
                        stringResource(R.string.wifi_toggle_summary_off)
                    },
                    checked = uiState.isWifiOn,
                    onCheckedChange = viewModel::onWifiCheckedChange,
                )
                FlymeSettingsSwitchItem(
                    title = stringResource(R.string.wifi_auto_enable_title),
                    summary = stringResource(R.string.wifi_auto_enable_summary),
                    checked = uiState.isAutoEnableEnabled,
                    onCheckedChange = viewModel::onAutoEnableCheckedChange,
                    showDivider = false,
                )
            }

            FlymeSettingsSection(title = stringResource(R.string.wifi_section_icon)) {
                FlymeSettingsStepperItem(
                    title = stringResource(R.string.wifi_widget_position_title),
                    summary = stringResource(
                        R.string.wifi_widget_position_summary_rank,
                        uiState.widgetRank,
                    ),
                    onStepLeft = { viewModel.onWidgetRankStep(-WifiWidgetRank.STEP) },
                    onStepRight = { viewModel.onWidgetRankStep(WifiWidgetRank.STEP) },
                    canStepLeft = uiState.canStepWidgetLeft,
                    canStepRight = uiState.canStepWidgetRight,
                    leftContentDescription = stringResource(R.string.wifi_widget_position_step_left),
                    rightContentDescription = stringResource(R.string.wifi_widget_position_step_right),
                    showDivider = false,
                )
            }
        }
    }
}

@Composable
private fun WifiStatusHeader(
    isWifiOn: Boolean,
    wifiStateLabel: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(
            painter = painterResource(
                if (isWifiOn) R.drawable.ic_wifi_4 else R.drawable.ic_wifi_off,
            ),
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            colorFilter = ColorFilter.tint(
                if (isWifiOn) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            ),
        )
        Text(
            text = if (isWifiOn) {
                stringResource(R.string.wifi_header_on, wifiStateLabel)
            } else {
                stringResource(R.string.wifi_header_off)
            },
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview(showBackground = true, name = "Wi-Fi Light")
@Composable
private fun WifiScreenPreviewLight() {
    GeelyEx2ToolsTheme(darkTheme = false) {
        WifiScreen(onBack = {})
    }
}

@Preview(showBackground = true, name = "Wi-Fi Dark")
@Composable
private fun WifiScreenPreviewDark() {
    GeelyEx2ToolsTheme(darkTheme = true) {
        WifiScreen(onBack = {})
    }
}
