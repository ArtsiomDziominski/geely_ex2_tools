package com.geely.ex2.tools.data.avas

import android.content.Context
import android.util.Log
import com.geely.ex2.tools.data.vhal.VhalConstants

object FlymeAvasSoundApi {
    private const val TAG = "GeelyToolsAvas"

    @Volatile
    private var switchLiveData: Any? = null

    @Volatile
    private var typeLiveData: Any? = null

    fun readSwitchEnabled(context: Context): Boolean? {
        return try {
            val liveData = getSwitchLiveData(context)
            readBooleanLiveDataValue(liveData, "mValue")
        } catch (t: Throwable) {
            Log.w(TAG, "Flyme AVAS switch read failed", t)
            null
        }
    }

    fun readSoundType(context: Context): Int? {
        return try {
            val liveData = getTypeLiveData(context)
            parseTypeValue(readLiveDataFieldValue(liveData, "mValue"))
        } catch (t: Throwable) {
            Log.w(TAG, "Flyme AVAS type read failed", t)
            null
        }
    }

    fun isSupported(context: Context): Boolean? {
        return try {
            val liveData = getSwitchLiveData(context)
            readBooleanLiveDataValue(liveData, "mSupported")
        } catch (t: Throwable) {
            Log.w(TAG, "Flyme AVAS support check failed", t)
            null
        }
    }

    fun writeSwitchEnabled(context: Context, enabled: Boolean): Boolean {
        return try {
            ensureAutoFuncManager(context)
            val liveData = getSwitchLiveData(context)
            val update = liveData.javaClass.getMethod("updateFuncValueForce", Any::class.java)
            update.invoke(liveData, enabled)
            Log.i(TAG, "Flyme updateFuncValueForce switch=$enabled")
            true
        } catch (t: Throwable) {
            Log.w(TAG, "Flyme AVAS switch write failed enabled=$enabled", t)
            false
        }
    }

    fun writeSoundTypeNone(context: Context): Boolean {
        return try {
            ensureAutoFuncManager(context)
            val liveData = getTypeLiveData(context)
            val update = liveData.javaClass.getMethod(
                "updateFuncValueForce",
                Any::class.java,
                Boolean::class.javaPrimitiveType,
            )
            update.invoke(liveData, VhalConstants.AVAS_SOUND_TYPE_NONE, true)
            Log.i(TAG, "Flyme updateFuncValueForce type=NONE")
            true
        } catch (t: Throwable) {
            Log.w(TAG, "Flyme AVAS type write failed", t)
            false
        }
    }

    fun disableCompletely(context: Context): Boolean {
        val switchOk = writeSwitchEnabled(context, false)
        val typeOk = writeSoundTypeNone(context)
        return switchOk || typeOk
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
    private fun getSwitchLiveData(context: Context): Any {
        switchLiveData?.let { return it }

        ensureAutoFuncManager(context)

        val autoFuncIdClass = Class.forName("com.flyme.auto.api.AutoFuncId")
        val funcId = autoFuncIdClass.getField("SETTING_FUNC_AVAS_SOUND_SWITCH").get(null)

        val liveDataClass = Class.forName("com.flyme.auto.api.data.BooleanFuncLiveData")
        val liveData = liveDataClass.getConstructor(autoFuncIdClass, Boolean::class.javaPrimitiveType)
            .newInstance(funcId, false)
        liveDataClass.getMethod("init").invoke(liveData)

        switchLiveData = liveData
        return liveData
    }

    @Synchronized
    private fun getTypeLiveData(context: Context): Any {
        typeLiveData?.let { return it }

        ensureAutoFuncManager(context)

        val autoFuncIdClass = Class.forName("com.flyme.auto.api.AutoFuncId")
        val funcId = autoFuncIdClass.getField("SETTING_FUNC_AVAS_SOUND_TYPE").get(null)

        val liveDataClass = Class.forName("com.flyme.auto.api.data.EnumFuncLiveData")
        val liveData = liveDataClass.getConstructor(autoFuncIdClass, Boolean::class.javaPrimitiveType)
            .newInstance(funcId, false)
        liveDataClass.getMethod("init").invoke(liveData)

        typeLiveData = liveData
        return liveData
    }

    private fun readBooleanLiveDataValue(liveData: Any, fieldName: String): Boolean? {
        val value = readLiveDataFieldValue(liveData, fieldName)
        return value as? Boolean
    }

    private fun readLiveDataFieldValue(liveData: Any, fieldName: String): Any? {
        val mutableLiveData = liveData.javaClass.getField(fieldName).get(liveData)
        return mutableLiveData.javaClass.getMethod("getValue").invoke(mutableLiveData)
    }

    private fun parseTypeValue(value: Any?): Int? {
        return when (value) {
            null -> null
            is Int -> value
            is Number -> value.toInt()
            else -> {
                try {
                    val idField = value.javaClass.getField("id")
                    (idField.get(value) as? Number)?.toInt()
                } catch (_: Throwable) {
                    null
                }
            }
        }
    }

    fun enabledToRawValue(enabled: Boolean): Int = if (enabled) {
        VhalConstants.COMMON_VALUE_ON
    } else {
        VhalConstants.COMMON_VALUE_OFF
    }
}
