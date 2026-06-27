package com.geely.ex2.tools

import android.app.Application
import com.geely.ex2.tools.data.statuswidget.StatusWidgetBootstrap

class GeelyEx2ToolsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        StatusWidgetBootstrap.startEnabledWidgets(this, "Application")
    }
}
