package com.geely.ex2.tools.data.sound

import android.content.Context
import android.util.Log

object FlymeLockSoundApi {
    private const val TAG = "GeelyToolsLockSound"

    @Volatile
    private var lockSoundLiveData: Any? = null

    @Volatile
    private var flymeAvailability: Boolean? = null

    fun resetCache() {
        lockSoundLiveData = null
    }

    fun isAvailable(context: Context): Boolean {
        flymeAvailability?.let { return it }
        return synchronized(this) {
            flymeAvailability?.let { return it }
            val available = probeFlymeClasses()
            flymeAvailability = available
            if (!available) {
                Log.i(TAG, "Flyme lock sound API unavailable on this head unit")
            } else {
                try {
                    ensureAutoFuncManager(context)
                } catch (t: Throwable) {
                    Log.w(TAG, "Flyme lock sound API init failed", t)
                    flymeAvailability = false
                    return@synchronized false
                }
            }
            available
        }
    }

    fun readEnabled(context: Context): Boolean? {
        if (!isAvailable(context)) {
            return null
        }
        return try {
            val liveData = getLockSoundLiveData(context)
            parseBooleanValue(readLiveDataFieldValue(liveData, "mValue"))
        } catch (t: Throwable) {
            Log.w(TAG, "Flyme lock sound read failed", t)
            null
        }
    }

    fun writeEnabled(context: Context, enabled: Boolean): Boolean {
        if (!isAvailable(context)) {
            return false
        }
        return try {
            ensureAutoFuncManager(context)
            val liveData = getLockSoundLiveData(context)
            if (invokeUpdate(liveData, "updateFuncValue", enabled)) {
                Log.i(TAG, "Flyme updateFuncValue enabled=$enabled")
                return true
            }
            if (invokeUpdate(liveData, "updateFuncValueForce", enabled)) {
                Log.i(TAG, "Flyme updateFuncValueForce enabled=$enabled")
                return true
            }
            false
        } catch (t: Throwable) {
            Log.w(TAG, "Flyme lock sound write failed for enabled=$enabled", t)
            false
        }
    }

    private fun invokeUpdate(liveData: Any, methodName: String, enabled: Boolean): Boolean {
        val parameterTypes = listOf(
            Boolean::class.javaPrimitiveType,
            Boolean::class.javaObjectType,
            Any::class.java,
        )
        for (parameterType in parameterTypes) {
            try {
                val update = liveData.javaClass.getMethod(methodName, parameterType)
                update.invoke(liveData, enabled)
                return true
            } catch (_: NoSuchMethodException) {
                continue
            }
        }
        return false
    }

    private fun probeFlymeClasses(): Boolean {
        return try {
            Class.forName("com.flyme.auto.api.AutoFuncManager")
            Class.forName("com.flyme.auto.api.data.BooleanFuncLiveData")
            Class.forName("com.flyme.auto.api.AutoFuncId")
            true
        } catch (_: Throwable) {
            false
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
    private fun getLockSoundLiveData(context: Context): Any {
        lockSoundLiveData?.let { return it }

        ensureAutoFuncManager(context)

        val autoFuncIdClass = Class.forName("com.flyme.auto.api.AutoFuncId")
        val funcId = resolveLockSoundFuncId(autoFuncIdClass)
            ?: throw IllegalStateException("SETTING_FUNC_AUDIBLE_LOCKING_FEEDBACK not found")

        val liveDataClass = Class.forName("com.flyme.auto.api.data.BooleanFuncLiveData")
        val liveData = liveDataClass.getConstructor(autoFuncIdClass, Boolean::class.javaPrimitiveType)
            .newInstance(funcId, false)
        liveDataClass.getMethod("init").invoke(liveData)

        lockSoundLiveData = liveData
        return liveData
    }

    private fun resolveLockSoundFuncId(autoFuncIdClass: Class<*>): Any? {
        val fieldNames = listOf(
            "SETTING_FUNC_AUDIBLE_LOCKING_FEEDBACK",
            "IAudio_SETTING_FUNC_AUDIBLE_LOCKING_FEEDBACK",
        )
        for (fieldName in fieldNames) {
            try {
                return autoFuncIdClass.getField(fieldName).get(null)
            } catch (_: Throwable) {
                continue
            }
        }

        return try {
            val iAudioClass = Class.forName("com.flyme.auto.api.IAudio")
            iAudioClass.getField("SETTING_FUNC_AUDIBLE_LOCKING_FEEDBACK").get(null)
        } catch (_: Throwable) {
            null
        }
    }

    private fun readLiveDataFieldValue(liveData: Any, fieldName: String): Any? {
        val mutableLiveData = liveData.javaClass.getField(fieldName).get(liveData)
        return mutableLiveData.javaClass.getMethod("getValue").invoke(mutableLiveData)
    }

    private fun parseBooleanValue(value: Any?): Boolean? {
        return when (value) {
            null -> null
            is Boolean -> value
            is Number -> value.toInt() != 0
            else -> null
        }
    }
}
