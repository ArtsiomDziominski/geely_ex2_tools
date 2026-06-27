package com.geely.ex2.tools.feature.home.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.geely.ex2.tools.R
import com.geely.ex2.tools.ui.theme.FlymeAccent

private const val DEVELOPER_TELEGRAM = "i_am_artsiom"

@Composable
fun AppAboutDialog(
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val versionName = remember {
        runCatching {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull().orEmpty()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.home_about_close))
            }
        },
        title = {
            Text(
                text = stringResource(R.string.home_about_title),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_app_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp)),
                )
                Text(
                    text = stringResource(R.string.home_about_version, versionName),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.home_about_developer),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.home_about_telegram),
                        style = MaterialTheme.typography.bodyLarge,
                        color = FlymeAccent,
                        modifier = Modifier.clickable {
                            openTelegram(context, DEVELOPER_TELEGRAM)
                        },
                    )
                }
            }
        },
    )
}

private fun openTelegram(context: android.content.Context, username: String) {
    val uri = Uri.parse("https://t.me/$username")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    runCatching {
        context.startActivity(intent)
    }
}
