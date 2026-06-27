package com.geely.ex2.tools

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.geely.ex2.tools.data.battery.BatteryAppStarter
import com.geely.ex2.tools.data.speed.SpeedAppStarter
import com.geely.ex2.tools.data.temperature.TemperatureAppStarter
import com.geely.ex2.tools.data.wifi.WifiAppStarter
import com.geely.ex2.tools.data.wifi.WifiStatusIconHelper
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
        WifiAppStarter.startStatusService(this, "MainActivity")
        WifiStatusIconHelper.notifyStatusIcon(this, "MainActivity")
        TemperatureAppStarter.startServiceIfEnabled(this, "MainActivity")
        TemperatureAppStarter.notifyStatusIconIfEnabled(this, "MainActivity")
        SpeedAppStarter.startServiceIfEnabled(this, "MainActivity")
        SpeedAppStarter.notifyStatusIconIfEnabled(this, "MainActivity")
        BatteryAppStarter.startServiceIfEnabled(this, "MainActivity")
        BatteryAppStarter.notifyStatusIconIfEnabled(this, "MainActivity")

        enableEdgeToEdge()
        setContent {
            GeelyEx2ToolsTheme {
                val navController = rememberNavController()

                GeelyEx2Background {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent,
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = AppRoutes.HOME,
                            modifier = Modifier.padding(innerPadding),
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

    override fun onResume() {
        super.onResume()
        WifiAppStarter.startStatusService(this, "MainActivity resume")
        WifiStatusIconHelper.notifyStatusIcon(this, "MainActivity resume")
        TemperatureAppStarter.startServiceIfEnabled(this, "MainActivity resume")
        TemperatureAppStarter.notifyStatusIconIfEnabled(this, "MainActivity resume")
        SpeedAppStarter.startServiceIfEnabled(this, "MainActivity resume")
        SpeedAppStarter.notifyStatusIconIfEnabled(this, "MainActivity resume")
        BatteryAppStarter.startServiceIfEnabled(this, "MainActivity resume")
        BatteryAppStarter.notifyStatusIconIfEnabled(this, "MainActivity resume")
    }
}
