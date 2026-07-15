package com.geely.ex2.tools.feature.sounds.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geely.ex2.tools.R
import com.geely.ex2.tools.data.sounds.CarLockSoundCatalog
import com.geely.ex2.tools.data.sounds.CarLockSoundOption
import com.geely.ex2.tools.data.sounds.CarLockSoundPaths
import com.geely.ex2.tools.data.sounds.CarLockSoundSource
import com.geely.ex2.tools.feature.sounds.SoundsViewModel
import com.geely.ex2.tools.ui.components.FlymeSettingsInfoItem
import com.geely.ex2.tools.ui.components.FlymeSettingsSection
import com.geely.ex2.tools.ui.components.FlymeSettingsSegmentedItem
import com.geely.ex2.tools.ui.components.FlymeSettingsValueItem
import com.geely.ex2.tools.ui.components.GeelyTopAppBar
import com.geely.ex2.tools.ui.components.TabVisibilityEffect
import com.geely.ex2.tools.ui.components.isFlymeRailCompact
import com.geely.ex2.tools.ui.theme.GeelyEx2ToolsTheme

private val SoundGroupCorner = 16.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SoundsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val segmentOptions = listOf(
        stringResource(R.string.sounds_segment_off),
        stringResource(R.string.sounds_segment_on),
    )

    val pickSoundLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri -> viewModel.onSoundPicked(uri) }

    TabVisibilityEffect(onVisible = viewModel::onResume)

    if (uiState.showRebootDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onRebootDismiss,
            title = { Text(stringResource(R.string.sounds_reboot_title)) },
            text = { Text(stringResource(R.string.sounds_reboot_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::onRebootConfirm) {
                    Text(stringResource(R.string.sounds_reboot_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onRebootDismiss) {
                    Text(stringResource(R.string.sounds_reboot_no))
                }
            },
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            GeelyTopAppBar(
                title = stringResource(R.string.sounds_screen_title),
                onBack = onBack.takeIf { isFlymeRailCompact() },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
        ) {
            item(key = "header") {
                SoundsStatusHeader(
                    isInstalled = uiState.isInstalled,
                    modifier = Modifier.padding(bottom = 20.dp),
                )
            }

            soundsCatalogItems(
                catalog = uiState.catalog,
                selectedSoundId = uiState.selectedSoundId,
                enabled = !uiState.isImporting && !uiState.isChanging,
                onSoundSelected = viewModel::onSoundSelected,
            )

            item(key = "carlock") {
                FlymeSettingsSection(
                    title = stringResource(R.string.sounds_section_carlock),
                    modifier = Modifier.padding(top = 20.dp),
                ) {
                    FlymeSettingsValueItem(
                        title = stringResource(R.string.sounds_pick_other_title),
                        value = stringResource(R.string.sounds_pick_other_summary),
                        onClick = { pickSoundLauncher.launch(arrayOf("audio/*")) },
                        enabled = !uiState.isImporting && !uiState.isChanging,
                        showDivider = true,
                    )
                    FlymeSettingsSegmentedItem(
                        title = stringResource(R.string.sounds_carlock_title),
                        summary = if (uiState.isInstalled) {
                            stringResource(R.string.sounds_carlock_summary_on)
                        } else {
                            stringResource(R.string.sounds_carlock_summary_off)
                        },
                        options = segmentOptions,
                        selectedIndex = if (uiState.isInstalled) 1 else 0,
                        onSelectedIndexChange = viewModel::onCarLockSegmentSelected,
                        enabled = !uiState.isChanging &&
                            !uiState.isImporting &&
                            uiState.isSystemUid &&
                            (uiState.isInstalled || uiState.hasSource),
                        showDivider = false,
                    )
                }
            }

            item(key = "status") {
                FlymeSettingsSection(
                    title = stringResource(R.string.sounds_section_status),
                    modifier = Modifier.padding(top = 20.dp),
                ) {
                    FlymeSettingsInfoItem(
                        title = stringResource(R.string.sounds_target_title),
                        summary = CarLockSoundPaths.TARGET_FILE,
                    )
                    FlymeSettingsInfoItem(
                        title = stringResource(R.string.sounds_source_title),
                        summary = uiState.sourceDisplayName
                            ?: stringResource(R.string.sounds_pick_not_selected),
                    )
                    FlymeSettingsInfoItem(
                        title = stringResource(R.string.sounds_status_title),
                        summary = uiState.statusText,
                    )
                }
            }
        }
    }
}

private fun LazyListScope.soundsCatalogItems(
    catalog: CarLockSoundCatalog,
    selectedSoundId: String?,
    enabled: Boolean,
    onSoundSelected: (CarLockSoundOption) -> Unit,
) {
    if (catalog.options.isEmpty()) {
        item(key = "catalog_empty") {
            FlymeSettingsSection(
                title = stringResource(R.string.sounds_section_pick),
                modifier = Modifier.padding(bottom = 0.dp),
            ) {
                FlymeSettingsInfoItem(
                    title = stringResource(R.string.sounds_catalog_empty),
                    summary = stringResource(R.string.sounds_catalog_empty_hint),
                )
            }
        }
        return
    }

    var isFirstGroup = true

    fun nextTopPadding(): Dp {
        val padding = if (isFirstGroup) 0.dp else 20.dp
        isFirstGroup = false
        return padding
    }

    if (catalog.downloadOptions.isNotEmpty()) {
        soundGroupItems(
            groupKey = "download",
            titleRes = R.string.sounds_section_download,
            options = catalog.downloadOptions,
            selectedSoundId = selectedSoundId,
            enabled = enabled,
            onSoundSelected = onSoundSelected,
            topPadding = nextTopPadding(),
        )
    }

    if (catalog.vendorOptions.isNotEmpty()) {
        soundGroupItems(
            groupKey = "vendor",
            titleRes = R.string.sounds_section_vendor,
            options = catalog.vendorOptions,
            selectedSoundId = selectedSoundId,
            enabled = enabled,
            onSoundSelected = onSoundSelected,
            topPadding = nextTopPadding(),
        )
    }

    if (catalog.assetOptions.isNotEmpty()) {
        soundGroupItems(
            groupKey = "assets",
            titleRes = R.string.sounds_section_assets,
            options = catalog.assetOptions,
            selectedSoundId = selectedSoundId,
            enabled = enabled,
            onSoundSelected = onSoundSelected,
            topPadding = nextTopPadding(),
        )
    }
}

private fun LazyListScope.soundGroupItems(
    groupKey: String,
    titleRes: Int,
    options: List<CarLockSoundOption>,
    selectedSoundId: String?,
    enabled: Boolean,
    onSoundSelected: (CarLockSoundOption) -> Unit,
    topPadding: Dp,
) {
    item(key = "${groupKey}_title") {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                start = 4.dp,
                top = topPadding,
                bottom = 8.dp,
            ),
        )
    }

    itemsIndexed(
        items = options,
        key = { _, option -> option.id },
    ) { index, option ->
        val shape = when {
            options.size == 1 -> RoundedCornerShape(SoundGroupCorner)
            index == 0 -> RoundedCornerShape(
                topStart = SoundGroupCorner,
                topEnd = SoundGroupCorner,
            )
            index == options.lastIndex -> RoundedCornerShape(
                bottomStart = SoundGroupCorner,
                bottomEnd = SoundGroupCorner,
            )
            else -> RoundedCornerShape(0.dp)
        }

        SoundOptionRow(
            title = option.displayName,
            subtitle = soundSourceLabel(option.source),
            selected = selectedSoundId == option.id,
            enabled = enabled,
            onClick = { onSoundSelected(option) },
            showDivider = index < options.lastIndex,
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface),
        )
    }
}

@Composable
private fun soundSourceLabel(source: CarLockSoundSource): String = when (source) {
    CarLockSoundSource.DOWNLOAD -> stringResource(R.string.sounds_source_download)
    CarLockSoundSource.VENDOR -> stringResource(R.string.sounds_source_vendor)
    CarLockSoundSource.ASSET -> stringResource(R.string.sounds_source_asset)
}

@Composable
private fun SoundOptionRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    showDivider: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 16.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun SoundsStatusHeader(
    isInstalled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = if (isInstalled) {
                stringResource(R.string.sounds_header_installed)
            } else {
                stringResource(R.string.sounds_header_default)
            },
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.sounds_header_hint),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true, name = "Sounds Light")
@Composable
private fun SoundsScreenPreviewLight() {
    GeelyEx2ToolsTheme(darkTheme = false) {
        SoundsScreen(onBack = {})
    }
}

@Preview(showBackground = true, name = "Sounds Dark")
@Composable
private fun SoundsScreenPreviewDark() {
    GeelyEx2ToolsTheme(darkTheme = true) {
        SoundsScreen(onBack = {})
    }
}
