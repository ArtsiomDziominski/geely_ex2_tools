package com.geely.ex2.tools.data.sound

import android.content.Context
import com.geely.ex2.tools.data.vhal.CarPropertyIo
import com.geely.ex2.tools.data.vhal.CarPropertyLockSoundReader
import com.geely.ex2.tools.data.vhal.LockSoundSample
import com.geely.ex2.tools.data.vhal.LockSoundWriteResult

class LockSoundRepository(context: Context) {
    private val appContext = context.applicationContext

    fun readLockSound(): LockSoundSample = CarPropertyIo.call {
        sharedReader(appContext).readLockSound()
    }

    fun setLockSoundEnabled(enabled: Boolean): LockSoundWriteResult = CarPropertyIo.call {
        sharedReader(appContext).writeLockSound(enabled)
    }

    companion object {
        @Volatile
        private var reader: CarPropertyLockSoundReader? = null

        private fun sharedReader(context: Context): CarPropertyLockSoundReader {
            return reader ?: synchronized(this) {
                reader ?: CarPropertyLockSoundReader(context.applicationContext).also { reader = it }
            }
        }
    }
}
