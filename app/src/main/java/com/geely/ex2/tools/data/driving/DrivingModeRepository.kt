package com.geely.ex2.tools.data.driving

import android.content.Context
import com.geely.ex2.tools.data.vhal.CarPropertyDrivingModeReader
import com.geely.ex2.tools.data.vhal.DrivingModeSample
import com.geely.ex2.tools.data.vhal.DrivingModeWriteResult

class DrivingModeRepository(private val context: Context) {
    private val appContext = context.applicationContext

    fun isPersistEnabled(): Boolean = DrivingSettings.isPersistEnabled(appContext)

    fun setPersistEnabled(enabled: Boolean) {
        DrivingSettings.setPersistEnabled(appContext, enabled)
    }

    fun getSavedModeValue(): Int = DrivingSettings.getSavedModeValue(appContext)

    fun saveSelectedMode(modeValue: Int) {
        DrivingSettings.setSavedModeValue(appContext, modeValue)
    }

    fun startRestoreService(reason: String) {
        DrivingAppStarter.startRestoreService(appContext, reason)
    }

    fun stopRestoreService(reason: String) {
        DrivingAppStarter.stopRestoreService(appContext, reason)
    }

    fun restoreSavedModeIfNeeded(reason: String) {
        DrivingModeController.restoreDrivingModeIfNeeded(appContext, reason)
    }

    fun readDrivingMode(): DrivingModeSample {
        val reader = CarPropertyDrivingModeReader(context)
        return try {
            reader.readDrivingMode()
        } finally {
            reader.close()
        }
    }

    fun setDrivingMode(modeValue: Int): DrivingModeWriteResult {
        val reader = CarPropertyDrivingModeReader(context)
        return try {
            reader.writeDrivingMode(modeValue)
        } finally {
            reader.close()
        }
    }
}
