package com.geely.ex2.tools

import android.app.Application
import com.geely.ex2.tools.data.driving.DrivingAppStarter

class GeelyEx2ToolsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DrivingAppStarter.startRestoreService(this, "Application")
    }
}
