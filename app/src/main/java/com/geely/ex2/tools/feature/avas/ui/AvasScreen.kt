package com.geely.ex2.tools.feature.avas.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geely.ex2.tools.R
import com.geely.ex2.tools.feature.avas.AvasViewModel
import com.geely.ex2.tools.ui.components.FlymeSettingsInfoItem
import com.geely.ex2.tools.ui.components.FlymeSettingsSection
import com.geely.ex2.tools.ui.components.FlymeSettingsSegmentedItem
import com.geely.ex2.tools.ui.components.GeelyTopAppBar
import com.geely.ex2.tools.ui.components.TabVisibilityEffect
import com.geely.ex2.tools.ui.components.isFlymeRailCompact
import com.geely.ex2.tools.ui.rememberOnClickWithSystemSound
import com.geely.ex2.tools.ui.theme.GeelyEx2ToolsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvasScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AvasViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val muteOptions = listOf(
        stringResource(R.string.avas_segment_on),
        stringResource(R.string.avas_segment_off),
    )

    TabVisibilityEffect(
        onVisible = viewModel::onResume,
        onHidden = viewModel::onPause,
    )

    if (uiState.showMuteConfirm) {
        val onMuteConfirmAcceptedWithSound = rememberOnClickWithSystemSound(viewModel::onMuteConfirmAccepted)
        val onMuteConfirmDismissWithSound = rememberOnClickWithSystemSound(viewModel::onMuteConfirmDismiss)

        AlertDialog(
            onDismissRequest = viewModel::onMuteConfirmDismiss,
            title = { Text(stringResource(R.string.avas_mute_confirm_title)) },
            text = { Text(stringResource(R.string.avas_mute_confirm_message)) },
            confirmButton = {
                TextButton(onClick = onMuteConfirmAcceptedWithSound) {
                    Text(stringResource(R.string.avas_mute_confirm_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = onMuteConfirmDismissWithSound) {
                    Text(stringResource(R.string.avas_mute_confirm_no))
                }
            },
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            GeelyTopAppBar(
                title = stringResource(R.string.avas_screen_title),
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
            AvasStatusHeader(
                currentModeText = uiState.currentModeText,
                isMuted = uiState.isMuted,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            )

            FlymeSettingsSection(title = stringResource(R.string.avas_section_control)) {
                FlymeSettingsSegmentedItem(
                    title = stringResource(R.string.avas_mute_title),
                    summary = if (uiState.isMuted) {
                        stringResource(R.string.avas_mute_summary_on)
                    } else {
                        stringResource(R.string.avas_mute_summary_off)
                    },
                    options = muteOptions,
                    selectedIndex = if (uiState.isMuted) 1 else 0,
                    onSelectedIndexChange = viewModel::onMuteSegmentSelected,
                    enabled = !uiState.isChanging,
                    showDivider = false,
                )
            }

            FlymeSettingsSection(title = stringResource(R.string.avas_section_status)) {
                FlymeSettingsInfoItem(
                    title = stringResource(R.string.avas_current_title),
                    summary = uiState.currentModeText,
                )
                FlymeSettingsInfoItem(
                    title = stringResource(R.string.avas_supported_title),
                    summary = if (uiState.isSupported) {
                        stringResource(R.string.avas_supported_yes)
                    } else {
                        stringResource(R.string.avas_supported_no)
                    },
                )
                FlymeSettingsInfoItem(
                    title = stringResource(R.string.avas_status_title),
                    summary = uiState.statusText,
                )
            }
        }
    }
}

@Composable
private fun AvasStatusHeader(
    currentModeText: String,
    isMuted: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = if (isMuted) {
                stringResource(R.string.avas_header_muted)
            } else {
                currentModeText
            },
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.avas_header_hint),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true, name = "AVAS Light")
@Composable
private fun AvasScreenPreviewLight() {
    GeelyEx2ToolsTheme(darkTheme = false) {
        AvasScreen(onBack = {})
    }
}

@Preview(showBackground = true, name = "AVAS Dark")
@Composable
private fun AvasScreenPreviewDark() {
    GeelyEx2ToolsTheme(darkTheme = true) {
        AvasScreen(onBack = {})
    }
}
