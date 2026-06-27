package com.geely.ex2.tools.data.regeneration

import android.content.Context
import android.util.Log
import com.geely.ex2.tools.data.vhal.VhalConstants

object FlymeEnergyRegenerationApi {
    private const val TAG = "GeelyToolsRegen"
    private const val WRITE_DELAY_MS = 3000L

    @Volatile
    private var energyRegenLiveData: Any? = null

    fun readLevelValue(context: Context): Int? {
        return try {
            val liveData = getEnergyRegenLiveData(context)
            parseLevelValue(readLiveDataFieldValue(liveData, "mValue"))
        } catch (t: Throwable) {
            Log.w(TAG, "Flyme regen read failed", t)
            null
        }
    }

    fun writeLevelValue(context: Context, levelValue: Int): Boolean {
        return try {
            ensureAutoFuncManager(context)
            val fieldName = levelValueToAutoFuncFieldName(levelValue)
                ?: return false.also {
                    Log.w(TAG, "Unknown regen level 0x${levelValue.toString(16)}")
                }
            val autoFuncIdClass = Class.forName("com.flyme.auto.api.AutoFuncId")
            val autoFuncId = autoFuncIdClass.getField(fieldName).get(null)
            val liveData = getEnergyRegenLiveData(context)
            liveData.javaClass.getMethod("updateValueDelayWriter", Any::class.java)
                .invoke(liveData, autoFuncId)
            Log.i(TAG, "Flyme updateValueDelayWriter $fieldName (0x${levelValue.toString(16)})")
            true
        } catch (t: Throwable) {
            Log.w(TAG, "Flyme regen write failed for 0x${levelValue.toString(16)}", t)
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
    private fun getEnergyRegenLiveData(context: Context): Any {
        energyRegenLiveData?.let { return it }

        ensureAutoFuncManager(context)

        val autoFuncIdClass = Class.forName("com.flyme.auto.api.AutoFuncId")
        val funcId = autoFuncIdClass.getField("SETTING_FUNC_ENERGY_REGENERATION").get(null)

        val liveDataClass = Class.forName("com.flyme.auto.api.data.EnumFuncLiveData")
        val liveData = try {
            liveDataClass.getConstructor(
                autoFuncIdClass,
                Long::class.javaPrimitiveType,
                Boolean::class.javaPrimitiveType,
            ).newInstance(funcId, WRITE_DELAY_MS, false)
        } catch (_: NoSuchMethodException) {
            liveDataClass.getConstructor(autoFuncIdClass, Boolean::class.javaPrimitiveType)
                .newInstance(funcId, false)
        }
        liveDataClass.getMethod("init").invoke(liveData)

        energyRegenLiveData = liveData
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

    private fun levelValueToAutoFuncFieldName(levelValue: Int): String? = when (levelValue) {
        VhalConstants.REGEN_LEVEL_AUTO -> "VALUE_ENERGY_REGENERATION_LEVEL_AUTO"
        VhalConstants.REGEN_LEVEL_LOW -> "VALUE_ENERGY_REGENERATION_LEVEL_LOW"
        VhalConstants.REGEN_LEVEL_MID -> "VALUE_ENERGY_REGENERATION_LEVEL_MID"
        VhalConstants.REGEN_LEVEL_HIGH -> "VALUE_ENERGY_REGENERATION_LEVEL_HIGH"
        else -> null
    }
}
