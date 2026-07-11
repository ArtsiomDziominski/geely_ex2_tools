package com.geely.ex2.tools.data.driving

import android.content.Context
import android.util.Log
import com.geely.ex2.tools.data.vhal.VhalConstants

object FlymeEnergyRegenerationApi {
    private const val TAG = "GeelyToolsDriving"

    @Volatile
    private var regenLiveData: Any? = null

    @Volatile
    private var flymeAvailability: Boolean? = null

    fun resetCache() {
        regenLiveData = null
    }

    fun isAvailable(context: Context): Boolean {
        flymeAvailability?.let { return it }
        return synchronized(this) {
            flymeAvailability?.let { return it }
            val available = probeFlymeClasses()
            flymeAvailability = available
            if (!available) {
                Log.i(TAG, "Flyme regen API unavailable on this head unit")
            } else {
                try {
                    ensureAutoFuncManager(context)
                } catch (t: Throwable) {
                    Log.w(TAG, "Flyme regen API init failed", t)
                    flymeAvailability = false
                    return@synchronized false
                }
            }
            available
        }
    }

    fun readLevelValue(context: Context): Int? {
        if (!isAvailable(context)) {
            return null
        }
        return try {
            val liveData = getRegenLiveData(context)
            parseLevelValue(readLiveDataFieldValue(liveData, "mValue"))
        } catch (t: Throwable) {
            Log.w(TAG, "Flyme regen read failed", t)
            null
        }
    }

    fun writeLevelValue(context: Context, levelValue: Int): Boolean {
        if (!isAvailable(context)) {
            return false
        }
        return try {
            ensureAutoFuncManager(context)
            val liveData = getRegenLiveData(context)
            val update = liveData.javaClass.getMethod("updateFuncValueForce", Any::class.java)
            update.invoke(liveData, levelValue)
            Log.i(TAG, "Flyme regen updateFuncValueForce 0x${levelValue.toString(16)}")
            true
        } catch (t: Throwable) {
            Log.w(TAG, "Flyme regen force write failed for 0x${levelValue.toString(16)}", t)
            writeLevelValueDelayed(context, levelValue)
        }
    }

    private fun writeLevelValueDelayed(context: Context, levelValue: Int): Boolean {
        return try {
            val liveData = getRegenLiveData(context)
            val update = liveData.javaClass.getMethod("updateValueDelayWriter", Any::class.java)
            update.invoke(liveData, levelValue)
            Log.i(TAG, "Flyme regen updateValueDelayWriter 0x${levelValue.toString(16)}")
            true
        } catch (t: Throwable) {
            Log.w(TAG, "Flyme regen delayed write failed for 0x${levelValue.toString(16)}", t)
            false
        }
    }

    private fun probeFlymeClasses(): Boolean {
        return try {
            Class.forName("com.flyme.auto.api.AutoFuncManager")
            Class.forName("com.flyme.auto.api.data.EnumFuncLiveData")
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
    private fun getRegenLiveData(context: Context): Any {
        regenLiveData?.let { return it }

        ensureAutoFuncManager(context)

        val autoFuncIdClass = Class.forName("com.flyme.auto.api.AutoFuncId")
        val regenId = autoFuncIdClass.getField("SETTING_FUNC_ENERGY_REGENERATION").get(null)

        val liveDataClass = Class.forName("com.flyme.auto.api.data.EnumFuncLiveData")
        val liveData = liveDataClass.getConstructor(autoFuncIdClass, Boolean::class.javaPrimitiveType)
            .newInstance(regenId, false)
        liveDataClass.getMethod("init").invoke(liveData)

        regenLiveData = liveData
        return liveData
    }

    private fun readLiveDataFieldValue(liveData: Any, fieldName: String): Any? {
        val mutableLiveData = liveData.javaClass.getField(fieldName).get(liveData)
        return mutableLiveData.javaClass.getMethod("getValue").invoke(mutableLiveData)
    }

    private fun parseLevelValue(value: Any?): Int? {
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

    fun levelValueToAutoFuncName(levelValue: Int): String? = when (levelValue) {
        VhalConstants.ENERGY_REGENERATION_LEVEL_LOW -> "VALUE_ENERGY_REGENERATION_LEVEL_LOW"
        VhalConstants.ENERGY_REGENERATION_LEVEL_MID -> "VALUE_ENERGY_REGENERATION_LEVEL_MID"
        VhalConstants.ENERGY_REGENERATION_LEVEL_HIGH -> "VALUE_ENERGY_REGENERATION_LEVEL_HIGH"
        else -> null
    }
}
