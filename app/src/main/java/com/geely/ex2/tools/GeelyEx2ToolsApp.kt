package com.geely.ex2.tools

import android.app.Application
import com.geely.ex2.tools.data.settings.AppLocaleController
import com.geely.ex2.tools.data.statuswidget.StatusWidgetBootstrap

class GeelyEx2ToolsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLocaleController.applyStored(this)
        StatusWidgetBootstrap.startEnabledWidgets(this, "Application")
    }
}
