package com.geely.ex2.tools.feature.driving.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import com.geely.ex2.tools.ui.components.GeelyTopAppBar
import com.geely.ex2.tools.ui.components.isFlymeRailCompact
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geely.ex2.tools.R
import com.geely.ex2.tools.data.vhal.DrivingMode
import com.geely.ex2.tools.data.vhal.EnergyRegeneration
import com.geely.ex2.tools.feature.driving.DrivingViewModel
import com.geely.ex2.tools.ui.components.FlymeSettingsInfoItem
import com.geely.ex2.tools.ui.components.FlymeSettingsSection
import com.geely.ex2.tools.ui.components.FlymeSettingsSegmentedItem
import com.geely.ex2.tools.ui.components.FlymeSettingsSwitchItem
import com.geely.ex2.tools.ui.components.TabVisibilityEffect
import com.geely.ex2.tools.ui.theme.GeelyEx2ToolsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrivingScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DrivingViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val modeLabels = DrivingMode.selectable.map { stringResource(it.labelRes) }
    val regenLabels = EnergyRegeneration.selectable.map { stringResource(it.labelRes) }

    TabVisibilityEffect(
        onVisible = viewModel::onResume,
        onHidden = viewModel::onPause,
    )

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            GeelyTopAppBar(
                title = stringResource(R.string.driving_screen_title),
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
            DrivingStatusHeader(
                currentModeText = uiState.currentModeText,
                currentRegenText = uiState.currentRegenText,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            )

            FlymeSettingsSection(title = stringResource(R.string.driving_section_mode)) {
                FlymeSettingsSegmentedItem(
                    title = stringResource(R.string.driving_mode_switch_title),
                    summary = stringResource(R.string.driving_mode_switch_summary),
                    options = modeLabels,
                    selectedIndex = uiState.selectedIndex,
                    onSelectedIndexChange = viewModel::onModeSelected,
                    enabled = uiState.isWritable && !uiState.isChangingMode,
                    showDivider = true,
                )
                FlymeSettingsSwitchItem(
                    title = stringResource(R.string.driving_persist_title),
                    checked = uiState.isPersistEnabled,
                    onCheckedChange = viewModel::onPersistCheckedChange,
                    showDivider = false,
                )
            }

            FlymeSettingsSection(title = stringResource(R.string.driving_section_regen)) {
                FlymeSettingsSegmentedItem(
                    title = stringResource(R.string.driving_regen_switch_title),
                    summary = stringResource(R.string.driving_regen_switch_summary),
                    options = regenLabels,
                    selectedIndex = uiState.regenSelectedIndex,
                    onSelectedIndexChange = viewModel::onRegenSelected,
                    enabled = uiState.isRegenWritable && !uiState.isChangingRegen,
                    showDivider = true,
                )
                FlymeSettingsSwitchItem(
                    title = stringResource(R.string.driving_regen_persist_title),
                    checked = uiState.isRegenPersistEnabled,
                    onCheckedChange = viewModel::onRegenPersistCheckedChange,
                    showDivider = false,
                )
            }

            FlymeSettingsSection(title = stringResource(R.string.driving_section_status)) {
                FlymeSettingsInfoItem(
                    title = stringResource(R.string.driving_current_title),
                    summary = uiState.currentModeText,
                )
                if (uiState.isPersistEnabled) {
                    FlymeSettingsInfoItem(
                        title = stringResource(R.string.driving_saved_title),
                        summary = uiState.savedModeText,
                    )
                }
                FlymeSettingsInfoItem(
                    title = stringResource(R.string.driving_regen_current_title),
                    summary = uiState.currentRegenText,
                )
                if (uiState.isRegenPersistEnabled) {
                    FlymeSettingsInfoItem(
                        title = stringResource(R.string.driving_regen_saved_title),
                        summary = uiState.savedRegenText,
                    )
                }
                FlymeSettingsInfoItem(
                    title = stringResource(R.string.driving_status_title),
                    summary = uiState.statusText,
                )
                FlymeSettingsInfoItem(
                    title = stringResource(R.string.driving_regen_status_title),
                    summary = uiState.regenStatusText,
                )
                FlymeSettingsInfoItem(
                    title = stringResource(R.string.driving_raw_title),
                    summary = uiState.rawValueText,
                )
                FlymeSettingsInfoItem(
                    title = stringResource(R.string.driving_regen_raw_title),
                    summary = uiState.regenRawValueText,
                )
            }
        }
    }
}

@Composable
private fun DrivingStatusHeader(
    currentModeText: String,
    currentRegenText: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DrivingStatusMetric(
            label = stringResource(R.string.driving_current_title),
            value = currentModeText,
            modifier = Modifier.weight(1f),
        )
        VerticalDivider(
            modifier = Modifier.height(52.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        DrivingStatusMetric(
            label = stringResource(R.string.driving_regen_current_title),
            value = currentRegenText,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DrivingStatusMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true, name = "Driving Light")
@Composable
private fun DrivingScreenPreviewLight() {
    GeelyEx2ToolsTheme(darkTheme = false) {
        DrivingScreen(onBack = {})
    }
}

@Preview(showBackground = true, name = "Driving Dark")
@Composable
private fun DrivingScreenPreviewDark() {
    GeelyEx2ToolsTheme(darkTheme = true) {
        DrivingScreen(onBack = {})
    }
}
