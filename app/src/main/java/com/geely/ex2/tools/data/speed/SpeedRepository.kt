package com.geely.ex2.tools.data.speed

import android.content.Context
import com.geely.ex2.tools.data.vhal.SpeedSample
import com.geely.ex2.tools.data.vhal.VhalSpeedReader
import com.geely.ex2.tools.data.vhal.VhalSpeedReaderFactory

class SpeedRepository(private val context: Context) {
    fun isEnabled(): Boolean = SpeedSettings.isEnabled(context)

    fun setEnabled(enabled: Boolean) {
        SpeedSettings.setEnabled(context, enabled)
    }

    fun getStatusIconRank(): Int = SpeedSettings.getStatusIconRank(context)

    fun stepStatusIconRank(delta: Int): Int = SpeedSettings.stepStatusIconRank(context, delta)

    fun startStatusServiceIfEnabled(reason: String) {
        SpeedAppStarter.startServiceIfEnabled(context, reason)
    }

    fun stopStatusService(reason: String) {
        SpeedAppStarter.stopService(context, reason)
    }

    fun notifyStatusIconIfEnabled(reason: String, rank: Int? = null) {
        SpeedAppStarter.notifyStatusIconIfEnabled(context, reason, rank)
    }

    fun cancelStatusIcon() {
        SpeedStatusIconHelper.cancelStatusIcon(context)
    }

    fun readSpeed(): SpeedSample {
        if (!SpeedSettings.isEnabled(context)) {
            return SpeedSample(
                speedKmh = 0f,
                isAvailable = false,
                source = "disabled",
                details = "",
            )
        }

        val reader = VhalSpeedReaderFactory.create(context)
        return try {
            reader.readSpeed()
        } finally {
            reader.close()
        }
    }

    fun createReader(): VhalSpeedReader = VhalSpeedReaderFactory.create(context)
}
