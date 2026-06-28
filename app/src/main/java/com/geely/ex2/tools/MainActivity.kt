package com.geely.ex2.tools

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.geely.ex2.tools.data.battery.BatteryAppWidgetHelper
import com.geely.ex2.tools.data.statuswidget.StatusWidgetBootstrap
import com.geely.ex2.tools.feature.home.ui.HomeScreen
import com.geely.ex2.tools.feature.battery.ui.BatteryScreen
import com.geely.ex2.tools.feature.driving.ui.DrivingScreen
import com.geely.ex2.tools.feature.speed.ui.SpeedScreen
import com.geely.ex2.tools.feature.temperature.ui.TemperatureScreen
import com.geely.ex2.tools.feature.wifi.ui.WifiScreen
import com.geely.ex2.tools.navigation.AppRoutes
import com.geely.ex2.tools.ui.theme.GeelyEx2Background
import com.geely.ex2.tools.ui.theme.GeelyEx2ToolsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusWidgetBootstrap.startEnabledWidgets(this, "MainActivity")
        enableEdgeToEdge()
        setContent {
            GeelyEx2ToolsTheme {
                val navController = rememberNavController()
                val startRoute = intent?.getStringExtra(BatteryAppWidgetHelper.EXTRA_START_ROUTE)

                LaunchedEffect(startRoute) {
                    if (!startRoute.isNullOrBlank() && startRoute != AppRoutes.HOME) {
                        navController.navigate(startRoute)
                        intent?.removeExtra(BatteryAppWidgetHelper.EXTRA_START_ROUTE)
                    }
                }

                GeelyEx2Background(modifier = Modifier.fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = AppRoutes.HOME,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                            composable(AppRoutes.HOME) {
                                HomeScreen(
                                    onToolClick = { route ->
                                        navController.navigate(route)
                                    },
                                )
                            }
                            composable(AppRoutes.WIFI) {
                                WifiScreen(
                                    onBack = { navController.popBackStack() },
                                )
                            }
                            composable(AppRoutes.TEMPERATURE) {
                                TemperatureScreen(
                                    onBack = { navController.popBackStack() },
                                )
                            }
                            composable(AppRoutes.SPEED) {
                                SpeedScreen(
                                    onBack = { navController.popBackStack() },
                                )
                            }
                            composable(AppRoutes.BATTERY) {
                                BatteryScreen(
                                    onBack = { navController.popBackStack() },
                                )
                            }
                            composable(AppRoutes.DRIVING) {
                                DrivingScreen(
                                    onBack = { navController.popBackStack() },
                                )
                            }
                    }
                }
            }
        }
    }
}
