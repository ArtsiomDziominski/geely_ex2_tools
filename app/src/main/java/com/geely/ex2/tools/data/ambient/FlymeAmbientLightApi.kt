package com.geely.ex2.tools.data.ambient

import android.content.Context
import android.util.Log
import com.geely.ex2.tools.data.vhal.VhalConstants

object FlymeAmbientLightApi {
    private const val TAG = "GeelyToolsAmbient"

    @Volatile
    private var atmosphereLampsLiveData: Any? = null

    fun readEnabled(context: Context): Boolean? {
        return try {
            val liveData = getAtmosphereLampsLiveData(context)
            readBooleanLiveDataValue(liveData, "mValue")
        } catch (t: Throwable) {
            Log.w(TAG, "Flyme ambient light read failed", t)
            null
        }
    }

    fun writeEnabled(context: Context, enabled: Boolean): Boolean {
        return try {
            ensureAutoFuncManager(context)
            val liveData = getAtmosphereLampsLiveData(context)
            val update = liveData.javaClass.getMethod("updateFuncValueForce", Any::class.java)
            update.invoke(liveData, enabled)
            Log.i(TAG, "Flyme updateFuncValueForce enabled=$enabled")
            true
        } catch (t: Throwable) {
            Log.w(TAG, "Flyme ambient light write failed enabled=$enabled", t)
            false
        }
    }

    fun isSupported(context: Context): Boolean? {
        return try {
            val liveData = getAtmosphereLampsLiveData(context)
            readBooleanLiveDataValue(liveData, "mSupported")
        } catch (t: Throwable) {
            Log.w(TAG, "Flyme ambient light support check failed", t)
            null
        }
    }

    private fun ensureAutoFuncManager(context: Context) {
        val appContext = context.applicationContext
        val managerClass = Class.forName("com.flyme.auto.api.AutoFuncManager")
        val manager = managerClass.getMethod("getInstance", Context::class.java)
            .invoke(null, appContext)

        try {
            managerClass.getMethod("getAutoFuncInterface", Context::class.java)
                .invoke(manager, appContext)
        } catch (_: NoSuchMethodException) {
            managerClass.getMethod("getAutoFuncInterface")
                .invoke(manager)
        }
    }

    @Synchronized
    private fun getAtmosphereLampsLiveData(context: Context): Any {
        atmosphereLampsLiveData?.let { return it }

        ensureAutoFuncManager(context)

        val autoFuncIdClass = Class.forName("com.flyme.auto.api.AutoFuncId")
        val atmosphereLampsId = autoFuncIdClass.getField("BCM_FUNC_LIGHT_ATMOSPHERE_LAMPS").get(null)

        val liveDataClass = Class.forName("com.flyme.auto.api.data.BooleanFuncLiveData")
        val liveData = liveDataClass.getConstructor(autoFuncIdClass, Boolean::class.javaPrimitiveType)
            .newInstance(atmosphereLampsId, false)
        liveDataClass.getMethod("init").invoke(liveData)

        atmosphereLampsLiveData = liveData
        return liveData
    }

    private fun readBooleanLiveDataValue(liveData: Any, fieldName: String): Boolean? {
        val mutableLiveData = liveData.javaClass.getField(fieldName).get(liveData)
        val value = mutableLiveData.javaClass.getMethod("getValue").invoke(mutableLiveData)
        return value as? Boolean
    }

    fun enabledToRawValue(enabled: Boolean): Int = if (enabled) {
        VhalConstants.COMMON_VALUE_ON
    } else {
        VhalConstants.COMMON_VALUE_OFF
    }
}
