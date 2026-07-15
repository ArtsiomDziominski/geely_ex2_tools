package com.geely.ex2.tools.data.sounds

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import java.io.File

object CarLockSoundSettings {
    private const val PREFS = "geelytools_car_lock_sound"
    private const val KEY_SELECTED_ID = "selected_sound_id"
    private const val KEY_SELECTED_NAME = "selected_sound_name"

    const val SELECTED_FILE_NAME = "selected.wav"

    fun selectedFile(context: Context): File =
        File(selectedDir(context), SELECTED_FILE_NAME)

    fun selectedDir(context: Context): File =
        File(context.applicationContext.filesDir, "carlock")

    fun getSelectedId(context: Context): String? =
        prefs(context).getString(KEY_SELECTED_ID, null)

    fun getSelectedName(context: Context): String? =
        prefs(context).getString(KEY_SELECTED_NAME, null)

    fun setSelected(context: Context, id: String, name: String) {
        prefs(context).edit()
            .putString(KEY_SELECTED_ID, id)
            .putString(KEY_SELECTED_NAME, name)
            .apply()
    }

    fun clearSelected(context: Context) {
        prefs(context).edit()
            .remove(KEY_SELECTED_ID)
            .remove(KEY_SELECTED_NAME)
            .apply()
        selectedFile(context).delete()
    }

    fun hasSelectedFile(context: Context): Boolean {
        val file = selectedFile(context)
        return file.isFile && file.length() > 0L
    }

    private fun prefs(context: Context): SharedPreferences {
        val appContext = context.applicationContext
        val storageContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            appContext.createDeviceProtectedStorageContext()
        } else {
            appContext
        }
        return storageContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }
}
