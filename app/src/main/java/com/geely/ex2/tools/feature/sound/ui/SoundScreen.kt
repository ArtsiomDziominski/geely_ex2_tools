package com.geely.ex2.tools.feature.sound.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geely.ex2.tools.R
import com.geely.ex2.tools.feature.sound.SoundViewModel
import com.geely.ex2.tools.ui.components.FlymeSettingsSection
import com.geely.ex2.tools.ui.components.FlymeSettingsSwitchItem
import com.geely.ex2.tools.ui.components.GeelyTopAppBar
import com.geely.ex2.tools.ui.components.TabVisibilityEffect
import com.geely.ex2.tools.ui.components.isFlymeRailCompact
import com.geely.ex2.tools.ui.theme.GeelyEx2ToolsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SoundViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TabVisibilityEffect(
        onVisible = viewModel::onResume,
        onHidden = viewModel::onPause,
    )

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            GeelyTopAppBar(
                title = stringResource(R.string.sound_screen_title),
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
            FlymeSettingsSection(title = stringResource(R.string.sound_section_lock)) {
                FlymeSettingsSwitchItem(
                    title = stringResource(R.string.sound_lock_title),
                    summary = when {
                        uiState.lockSoundStatusText.isNotEmpty() -> uiState.lockSoundStatusText
                        uiState.lockSoundEnabled -> stringResource(R.string.sound_lock_summary_on)
                        else -> stringResource(R.string.sound_lock_summary_off)
                    },
                    checked = uiState.lockSoundEnabled,
                    onCheckedChange = viewModel::onLockSoundCheckedChange,
                    enabled = uiState.lockSoundWritable && uiState.lockSoundAvailable,
                    showDivider = false,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 720, heightDp = 400)
@Composable
private fun SoundScreenPreview() {
    GeelyEx2ToolsTheme {
        SoundScreen(onBack = {})
    }
}
