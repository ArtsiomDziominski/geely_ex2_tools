package com.geely.ex2.tools.data.driving

import android.content.Context
import com.geely.ex2.tools.data.vhal.CarPropertyDrivingModeReader
import com.geely.ex2.tools.data.vhal.CarPropertyIo
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
        DrivingAppStarter.startRestoreServiceIfEnabled(appContext, reason)
    }

    fun stopRestoreService(reason: String) {
        DrivingAppStarter.stopRestoreService(appContext, reason)
    }

    fun restoreSavedModeIfNeeded(reason: String) {
        DrivingModeController.restoreDrivingModeIfNeeded(appContext, reason)
    }

    fun readDrivingMode(): DrivingModeSample = CarPropertyIo.call {
        sharedReader(appContext).readDrivingMode()
    }

    fun setDrivingMode(modeValue: Int): DrivingModeWriteResult = CarPropertyIo.call {
        sharedReader(appContext).writeDrivingMode(modeValue)
    }

    companion object {
        @Volatile
        private var reader: CarPropertyDrivingModeReader? = null

        private fun sharedReader(context: Context): CarPropertyDrivingModeReader {
            return reader ?: synchronized(this) {
                reader ?: CarPropertyDrivingModeReader(context.applicationContext).also { reader = it }
            }
        }
    }
}
