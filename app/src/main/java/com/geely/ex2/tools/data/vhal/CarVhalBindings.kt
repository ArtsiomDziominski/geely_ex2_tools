package com.geely.ex2.tools.data.vhal

import android.content.Context
import android.util.Log
import java.lang.reflect.Method

class CarVhalBindings(context: Context) {
    private val appContext = context.applicationContext
    private var carClass: Class<*>? = null
    private var car: Any? = null
    private var propertyManager: Any? = null

    val isReady: Boolean
        get() = propertyManager != null

    fun ensureConnected(debug: StringBuilder? = null): Boolean {
        try {
            ensureInitialized(debug)
            return propertyManager != null
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to initialize car property manager", t)
            debug?.append('\n')?.append(shortError(t))
            return false
        }
    }

    fun getPropertyManager(): Any? = propertyManager

    fun readFloatProperty(propertyId: Int, areaId: Int = VhalConstants.GLOBAL_AREA_ID): FloatProbe {
        val manager = propertyManager ?: return FloatProbe.error(propertyId, "CarPropertyManager is null")
        return readFloat(manager, propertyId, areaId)
    }

    fun readIntProperty(propertyId: Int, areaId: Int = VhalConstants.GLOBAL_AREA_ID): IntProbe {
        val manager = propertyManager ?: return IntProbe.error(propertyId, "CarPropertyManager is null")
        return readInt(manager, propertyId, areaId)
    }

    fun writeIntProperty(
        propertyId: Int,
        value: Int,
        areaId: Int = VhalConstants.GLOBAL_AREA_ID,
    ): WriteProbe {
        val manager = propertyManager ?: return WriteProbe.error(propertyId, "CarPropertyManager is null")
        return writeInt(manager, propertyId, areaId, value)
    }

    fun registerPropertyCallback(
        propertyId: Int,
        updateRateHz: Float,
        onValue: (Float) -> Unit,
        onError: (String) -> Unit,
    ): Any? {
        val manager = propertyManager ?: return null

        return try {
            val callbackClass = Class.forName(
                "android.car.hardware.property.CarPropertyManager\$CarPropertyEventCallback",
            )
            val callback = java.lang.reflect.Proxy.newProxyInstance(
                callbackClass.classLoader,
                arrayOf(callbackClass),
            ) { _, method, args ->
                when (method.name) {
                    "onChangeEvent" -> {
                        val value = args?.getOrNull(0) ?: return@newProxyInstance null
                        parseFloatValue(value)?.let(onValue)
                    }
                    "onErrorEvent" -> {
                        val propId = args?.getOrNull(0) as? Int ?: propertyId
                        val zone = args?.getOrNull(1) as? Int ?: 0
                        onError("property 0x${propId.toString(16)} zone $zone")
                    }
                }
                null
            }

            val register = manager.javaClass.getMethod(
                "registerCallback",
                callbackClass,
                Int::class.javaPrimitiveType,
                Float::class.javaPrimitiveType,
            )
            register.invoke(manager, callback, propertyId, updateRateHz)
            callback
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to register VHAL callback for 0x${propertyId.toString(16)}", t)
            onError(shortError(t))
            null
        }
    }

    fun registerIntPropertyCallback(
        propertyId: Int,
        updateRateHz: Float,
        onValue: (Int) -> Unit,
        onError: (String) -> Unit,
    ): Any? {
        val manager = propertyManager ?: return null

        return try {
            val callbackClass = Class.forName(
                "android.car.hardware.property.CarPropertyManager\$CarPropertyEventCallback",
            )
            val callback = java.lang.reflect.Proxy.newProxyInstance(
                callbackClass.classLoader,
                arrayOf(callbackClass),
            ) { _, method, args ->
                when (method.name) {
                    "onChangeEvent" -> {
                        val value = args?.getOrNull(0) ?: return@newProxyInstance null
                        parseIntValue(value)?.let(onValue)
                    }
                    "onErrorEvent" -> {
                        val propId = args?.getOrNull(0) as? Int ?: propertyId
                        val zone = args?.getOrNull(1) as? Int ?: 0
                        onError("property 0x${propId.toString(16)} zone $zone")
                    }
                }
                null
            }

            val register = manager.javaClass.getMethod(
                "registerCallback",
                callbackClass,
                Int::class.javaPrimitiveType,
                Float::class.javaPrimitiveType,
            )
            register.invoke(manager, callback, propertyId, updateRateHz)
            callback
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to register int VHAL callback for 0x${propertyId.toString(16)}", t)
            onError(shortError(t))
            null
        }
    }

    fun unregisterPropertyCallback(callback: Any?) {
        val manager = propertyManager ?: return
        val callbackObject = callback ?: return

        try {
            val callbackClass = Class.forName(
                "android.car.hardware.property.CarPropertyManager\$CarPropertyEventCallback",
            )
            val unregister = manager.javaClass.getMethod("unregisterCallback", callbackClass)
            unregister.invoke(manager, callbackObject)
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to unregister VHAL callback", t)
        }
    }

    fun close() {
        val carObject = car ?: return
        try {
            val disconnect = carObject.javaClass.getMethod("disconnect")
            disconnect.invoke(carObject)
        } catch (_: Throwable) {
        }
        car = null
        propertyManager = null
    }

    private fun ensureInitialized(debug: StringBuilder?) {
        if (propertyManager != null) {
            debug?.append("CarPropertyManager: OK cached")
            return
        }

        if (carClass == null) {
            carClass = Class.forName("android.car.Car")
            debug?.append("android.car.Car: OK")
        } else {
            debug?.append("android.car.Car: OK cached")
        }

        val classRef = carClass ?: return

        if (car == null) {
            val createCar = classRef.getMethod("createCar", Context::class.java)
            car = createCar.invoke(null, appContext)
            debug?.let { it.append("\ncreateCar: ").append(if (car == null) "NULL" else "OK") }
        } else {
            debug?.append("\ncreateCar: OK cached")
        }

        val carObject = car ?: return

        try {
            val isConnected = classRef.getMethod("isConnected")
            val connected = (isConnected.invoke(carObject) as? Boolean) == true
            debug?.let { it.append("\nisConnected: ").append(connected) }
            if (!connected) {
                val connect = classRef.getMethod("connect")
                connect.invoke(carObject)
                debug?.append("\nconnect: called")
            }
        } catch (e: IllegalStateException) {
            debug?.let { it.append("\nconnect: already connecting/connected: ").append(e.message) }
        } catch (_: NoSuchMethodException) {
            debug?.append("\nconnect: no method")
        }

        val getCarManager = classRef.getMethod("getCarManager", String::class.java)
        propertyManager = getCarManager.invoke(carObject, VhalConstants.CAR_MANAGER_PROPERTY)
        debug?.let {
            it.append("\ngetCarManager(property): ")
                .append(if (propertyManager == null) "NULL" else "OK")
        }
    }

    private fun readInt(manager: Any, propertyId: Int, areaId: Int): IntProbe {
        return try {
            val method: Method = manager.javaClass.getMethod(
                "getIntProperty",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
            )
            val value = method.invoke(manager, propertyId, areaId)
            if (value is Int) {
                IntProbe.ok(propertyId, value)
            } else {
                IntProbe.error(propertyId, "not Int: $value")
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to read int property 0x${propertyId.toString(16)}", t)
            IntProbe.error(propertyId, shortError(t))
        }
    }

    private fun writeInt(manager: Any, propertyId: Int, areaId: Int, value: Int): WriteProbe {
        return try {
            val method: Method = manager.javaClass.getMethod(
                "setIntProperty",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
            )
            method.invoke(manager, propertyId, areaId, value)
            WriteProbe.ok(propertyId)
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to write int property 0x${propertyId.toString(16)}", t)
            WriteProbe.error(propertyId, shortError(t))
        }
    }

    private fun readFloat(manager: Any, propertyId: Int, areaId: Int): FloatProbe {
        return try {
            val method: Method = manager.javaClass.getMethod(
                "getFloatProperty",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
            )
            val value = method.invoke(manager, propertyId, areaId)
            if (value is Float) {
                FloatProbe.ok(propertyId, value)
            } else {
                FloatProbe.error(propertyId, "not Float: $value")
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to read float property 0x${propertyId.toString(16)}", t)
            FloatProbe.error(propertyId, shortError(t))
        }
    }

    private fun parseIntValue(carPropertyValue: Any): Int? {
        return try {
            val getValue = carPropertyValue.javaClass.getMethod("getValue")
            when (val value = getValue.invoke(carPropertyValue)) {
                is Int -> value
                is Byte -> value.toInt()
                is Short -> value.toInt()
                else -> null
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to parse int CarPropertyValue", t)
            null
        }
    }

    private fun parseFloatValue(carPropertyValue: Any): Float? {
        return try {
            val getValue = carPropertyValue.javaClass.getMethod("getValue")
            when (val value = getValue.invoke(carPropertyValue)) {
                is Float -> value
                is Double -> value.toFloat()
                is Int -> value.toFloat()
                else -> null
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to parse CarPropertyValue", t)
            null
        }
    }

    data class FloatProbe(
        val propertyId: Int,
        val ok: Boolean,
        val value: Float,
        val error: String?,
    ) {
        companion object {
            fun ok(propertyId: Int, value: Float): FloatProbe =
                FloatProbe(propertyId, true, value, null)

            fun error(propertyId: Int, error: String): FloatProbe =
                FloatProbe(propertyId, false, 0f, error)
        }
    }

    data class IntProbe(
        val propertyId: Int,
        val ok: Boolean,
        val value: Int,
        val error: String?,
    ) {
        companion object {
            fun ok(propertyId: Int, value: Int): IntProbe =
                IntProbe(propertyId, true, value, null)

            fun error(propertyId: Int, error: String): IntProbe =
                IntProbe(propertyId, false, 0, error)
        }
    }

    data class WriteProbe(
        val propertyId: Int,
        val ok: Boolean,
        val error: String?,
    ) {
        companion object {
            fun ok(propertyId: Int): WriteProbe =
                WriteProbe(propertyId, true, null)

            fun error(propertyId: Int, error: String): WriteProbe =
                WriteProbe(propertyId, false, error)
        }
    }

    companion object {
        private const val TAG = "GeelyToolsVhal"

        private fun shortError(error: Throwable): String {
            var cause = error
            while (cause.cause != null) {
                cause = cause.cause!!
            }
            val name = cause::class.java.simpleName
            val message = cause.message
            return if (message.isNullOrEmpty()) name else "$name: $message"
        }
    }
}
