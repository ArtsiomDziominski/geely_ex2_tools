package com.geely.ex2.tools.data.driving

import android.content.Context
import android.util.Log
import com.geely.ex2.tools.data.vhal.DrivingMode

object DrivingModeController {
    const val TAG = "GeelyToolsDriving"

    fun restoreDrivingModeIfNeeded(context: Context, reason: String) {
        val appContext = context.applicationContext
        if (!DrivingSettings.isPersistEnabled(appContext)) {
            Log.i(TAG, "Skip driving mode restore, persist disabled: $reason")
            return
        }

        val savedMode = DrivingSettings.getSavedModeValue(appContext)
        if (!DrivingMode.isSelectableValue(savedMode)) {
            Log.w(TAG, "Skip driving mode restore, invalid saved mode: 0x${savedMode.toString(16)} ($reason)")
            return
        }

        val repository = DrivingModeRepository(appContext)
        val current = repository.readDrivingMode()
        if (!current.isAvailable) {
            Log.w(TAG, "Driving mode read unavailable ($reason): ${current.source}")
            return
        }

        if (current.modeValue == savedMode) {
            Log.d(TAG, "Driving mode already saved value 0x${savedMode.toString(16)} ($reason)")
            return
        }

        Log.i(
            TAG,
            "Driving mode mismatch: current=0x${current.modeValue.toString(16)} " +
                "saved=0x${savedMode.toString(16)}, restoring ($reason)",
        )

        repeat(WRITE_ATTEMPTS) { attempt ->
            val writeResult = repository.setDrivingMode(savedMode)
            if (!writeResult.ok) {
                Log.w(
                    TAG,
                    "Driving mode write failed attempt ${attempt + 1}/$WRITE_ATTEMPTS ($reason): ${writeResult.error}",
                )
                return@repeat
            }

            val verify = repository.readDrivingMode()
            if (verify.isAvailable && verify.modeValue == savedMode) {
                Log.i(TAG, "Driving mode restored to 0x${savedMode.toString(16)} ($reason)")
                return
            }
        }

        Log.w(TAG, "Driving mode restore incomplete ($reason)")
    }

    private const val WRITE_ATTEMPTS = 3
}
