package com.geely.ex2.tools.data.avas

import android.content.Context
import com.geely.ex2.tools.data.vhal.AvasSoundSample
import com.geely.ex2.tools.data.vhal.AvasSoundWriteResult
import com.geely.ex2.tools.data.vhal.CarPropertyAvasSoundReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AvasSoundRepository(private val context: Context) {
    suspend fun readAvasSound(): AvasSoundSample = withContext(Dispatchers.IO) {
        val reader = CarPropertyAvasSoundReader(context)
        try {
            reader.readAvasSound()
        } finally {
            reader.close()
        }
    }

    suspend fun setAvasSoundEnabled(enabled: Boolean): AvasSoundWriteResult = withContext(Dispatchers.IO) {
        val reader = CarPropertyAvasSoundReader(context)
        try {
            reader.writeAvasSound(enabled)
        } finally {
            reader.close()
        }
    }
}
