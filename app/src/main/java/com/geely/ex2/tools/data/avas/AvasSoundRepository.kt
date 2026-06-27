package com.geely.ex2.tools.data.avas

import android.content.Context
import com.geely.ex2.tools.data.vhal.AvasSoundSample
import com.geely.ex2.tools.data.vhal.AvasSoundWriteResult
import com.geely.ex2.tools.data.vhal.CarPropertyAvasSoundReader

class AvasSoundRepository(private val context: Context) {
    fun readAvasSound(): AvasSoundSample {
        val reader = CarPropertyAvasSoundReader(context)
        return try {
            reader.readAvasSound()
        } finally {
            reader.close()
        }
    }

    fun setAvasSoundEnabled(enabled: Boolean): AvasSoundWriteResult {
        val reader = CarPropertyAvasSoundReader(context)
        return try {
            reader.writeAvasSound(enabled)
        } finally {
            reader.close()
        }
    }
}
