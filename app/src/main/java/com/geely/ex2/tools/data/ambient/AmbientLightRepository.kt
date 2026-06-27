package com.geely.ex2.tools.data.ambient

import android.content.Context
import com.geely.ex2.tools.data.vhal.AmbientLightSample
import com.geely.ex2.tools.data.vhal.AmbientLightWriteResult
import com.geely.ex2.tools.data.vhal.CarPropertyAmbientLightReader

class AmbientLightRepository(private val context: Context) {
    fun readAmbientLight(): AmbientLightSample {
        val reader = CarPropertyAmbientLightReader(context)
        return try {
            reader.readAmbientLight()
        } finally {
            reader.close()
        }
    }

    fun setAmbientLight(enabled: Boolean): AmbientLightWriteResult {
        val reader = CarPropertyAmbientLightReader(context)
        return try {
            reader.writeAmbientLight(enabled)
        } finally {
            reader.close()
        }
    }
}
