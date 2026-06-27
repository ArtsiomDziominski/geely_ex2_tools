package com.geely.ex2.tools.feature.home.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geely.ex2.tools.R
import com.geely.ex2.tools.navigation.AppRoutes
import com.geely.ex2.tools.ui.theme.GeelyEx2ToolsTheme

data class ToolItem(
    val route: String,
    val titleRes: Int,
    val descriptionRes: Int,
)

private val tools = listOf(
    ToolItem(
        route = AppRoutes.WIFI,
        titleRes = R.string.tool_wifi_title,
        descriptionRes = R.string.tool_wifi_description,
    ),
    ToolItem(
        route = AppRoutes.TEMPERATURE,
        titleRes = R.string.tool_temperature_title,
        descriptionRes = R.string.tool_temperature_description,
    ),
    ToolItem(
        route = AppRoutes.SPEED,
        titleRes = R.string.tool_speed_title,
        descriptionRes = R.string.tool_speed_description,
    ),
    ToolItem(
        route = AppRoutes.BATTERY,
        titleRes = R.string.tool_battery_title,
        descriptionRes = R.string.tool_battery_description,
    ),
    ToolItem(
        route = AppRoutes.DRIVING,
        titleRes = R.string.tool_driving_title,
        descriptionRes = R.string.tool_driving_description,
    ),
    ToolItem(
        route = AppRoutes.REGENERATION,
        titleRes = R.string.tool_regen_title,
        descriptionRes = R.string.tool_regen_description,
    ),
    ToolItem(
        route = AppRoutes.AMBIENT,
        titleRes = R.string.tool_ambient_title,
        descriptionRes = R.string.tool_ambient_description,
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onToolClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAboutDialog by remember { mutableStateOf(false) }

    if (showAboutDialog) {
        AppAboutDialog(onDismiss = { showAboutDialog = false })
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_tools_title)) },
                actions = {
                    IconButton(onClick = { showAboutDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = stringResource(R.string.home_about_content_description),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f),
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(tools) { tool ->
                ToolCard(
                    tool = tool,
                    onClick = { onToolClick(tool.route) },
                )
            }
        }
    }
}

@Composable
private fun ToolCard(
    tool: ToolItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(tool.titleRes),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(tool.descriptionRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    GeelyEx2ToolsTheme {
        HomeScreen(onToolClick = {})
    }
}
