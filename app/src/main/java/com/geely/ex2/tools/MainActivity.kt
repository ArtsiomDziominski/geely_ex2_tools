package com.geely.ex2.tools

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.geely.ex2.tools.feature.ambient.ui.AmbientLightScreen
import com.geely.ex2.tools.feature.avas.ui.AvasScreen
import com.geely.ex2.tools.feature.battery.ui.BatteryScreen
import com.geely.ex2.tools.feature.driving.ui.DrivingScreen
import com.geely.ex2.tools.feature.home.ui.EmptyStartScreen
import com.geely.ex2.tools.feature.speed.ui.SpeedScreen
import com.geely.ex2.tools.feature.temperature.ui.TemperatureScreen
import com.geely.ex2.tools.feature.wifi.ui.WifiScreen
import com.geely.ex2.tools.navigation.AppRoutes
import com.geely.ex2.tools.ui.components.FlymeAppShell
import com.geely.ex2.tools.ui.theme.GeelyEx2Background
import com.geely.ex2.tools.ui.theme.GeelyEx2ToolsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GeelyEx2ToolsTheme {
                val navController = rememberNavController()
                // val startRoute = intent?.getStringExtra(BatteryAppWidgetHelper.EXTRA_START_ROUTE)
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // LaunchedEffect(startRoute) {
                //     if (!startRoute.isNullOrBlank() && startRoute != AppRoutes.HOME) {
                //         navController.navigate(startRoute) {
                //             launchSingleTop = true
                //         }
                //         intent?.removeExtra(BatteryAppWidgetHelper.EXTRA_START_ROUTE)
                //     }
                // }

                GeelyEx2Background(modifier = Modifier.fillMaxSize()) {
                    FlymeAppShell(
                        currentRoute = currentRoute,
                        onDestinationSelected = { route ->
                            if (route == currentRoute) return@FlymeAppShell
                            navController.navigate(route) {
                                // popUpTo(AppRoutes.HOME) {
                                //     saveState = true
                                //     inclusive = route == AppRoutes.HOME
                                // }
                                popUpTo(AppRoutes.NONE) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onBack = {
                            // if (currentRoute == AppRoutes.HOME) return@FlymeAppShell
                            // navController.popBackStack(AppRoutes.HOME, inclusive = false)
                        },
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = AppRoutes.NONE,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            composable(AppRoutes.NONE) {
                                EmptyStartScreen()
                            }
                            // composable(AppRoutes.HOME) {
                            //     HomeScreen(
                            //         onToolClick = { route ->
                            //             navController.navigate(route) {
                            //                 launchSingleTop = true
                            //                 restoreState = true
                            //             }
                            //         },
                            //     )
                            // }
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
                            composable(AppRoutes.AMBIENT_LIGHT) {
                                AmbientLightScreen(
                                    onBack = { navController.popBackStack() },
                                )
                            }
                            composable(AppRoutes.AVAS) {
                                AvasScreen(
                                    onBack = { navController.popBackStack() },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
