package com.geely.ex2.tools.data.statuswidget

import android.content.Context
import com.geely.ex2.tools.data.ambient.AmbientLightAppStarter
import com.geely.ex2.tools.data.ambient.AmbientLightScheduleController
import com.geely.ex2.tools.data.avas.AvasAppStarter
import com.geely.ex2.tools.data.battery.BatteryAppStarter
import com.geely.ex2.tools.data.driving.DrivingAppStarter
import com.geely.ex2.tools.data.speed.SpeedAppStarter
import com.geely.ex2.tools.data.temperature.TemperatureAppStarter
import com.geely.ex2.tools.data.temperature.TemperatureStatusFont
import com.geely.ex2.tools.data.vhal.CarPropertyIo
import com.geely.ex2.tools.data.wifi.WifiAppStarter
import com.geely.ex2.tools.data.wifi.WifiAutoEnableController
import com.geely.ex2.tools.data.wifi.WifiStatusIconHelper

object StatusWidgetBootstrap {
    fun startEnabledWidgets(context: Context, reason: String) {
        val appContext = context.applicationContext
        CarPropertyIo.execute {
            TemperatureStatusFont.warmUp(appContext)

            BatteryAppStarter.startServiceIfEnabled(appContext, reason)
            BatteryAppStarter.notifyStatusIconIfEnabled(appContext, reason)

            TemperatureAppStarter.startServiceIfEnabled(appContext, reason)
            TemperatureAppStarter.notifyStatusIconIfEnabled(appContext, reason)

            SpeedAppStarter.startServiceIfEnabled(appContext, reason)
            SpeedAppStarter.notifyStatusIconIfEnabled(appContext, reason)

            if (WifiAutoEnableController.isAutoEnableEnabled(appContext)) {
                WifiAppStarter.startStatusService(appContext, reason)
                WifiStatusIconHelper.notifyStatusIcon(appContext, reason)
            }

            DrivingAppStarter.startRestoreServiceIfEnabled(appContext, reason)

            AmbientLightScheduleController.syncBackgroundWork(appContext, reason)
            AmbientLightAppStarter.startRestoreServiceIfEnabled(appContext, reason)

            AvasAppStarter.startRestoreServiceIfEnabled(appContext, reason)
        }
    }
}
