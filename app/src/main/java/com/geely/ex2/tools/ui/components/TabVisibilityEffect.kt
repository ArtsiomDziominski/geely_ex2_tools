package com.geely.ex2.tools.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Runs [onVisible] when the tab is composed and the host is resumed,
 * and [onHidden] when the tab leaves composition or the host pauses.
 */
@Composable
fun TabVisibilityEffect(
    onVisible: () -> Unit,
    onHidden: () -> Unit = {},
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnVisible by rememberUpdatedState(onVisible)
    val currentOnHidden by rememberUpdatedState(onHidden)

    DisposableEffect(lifecycleOwner) {
        var isVisible = false

        fun show() {
            if (isVisible) return
            isVisible = true
            currentOnVisible()
        }

        fun hide() {
            if (!isVisible) return
            isVisible = false
            currentOnHidden()
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> show()
                Lifecycle.Event.ON_PAUSE -> hide()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            show()
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            hide()
        }
    }
}
