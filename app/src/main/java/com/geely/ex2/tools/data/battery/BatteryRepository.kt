package com.geely.ex2.tools.data.battery

import android.content.Context
import com.geely.ex2.tools.data.vhal.BatterySample
import com.geely.ex2.tools.data.vhal.VhalBatteryReader
import com.geely.ex2.tools.data.vhal.VhalBatteryReaderFactory

class BatteryRepository(private val context: Context) {
    fun isEnabled(): Boolean = BatterySettings.isEnabled(context)

    fun setEnabled(enabled: Boolean) {
        BatterySettings.setEnabled(context, enabled)
    }

    fun getStatusIconRank(): Int = BatterySettings.getStatusIconRank(context)

    fun stepStatusIconRank(delta: Int): Int = BatterySettings.stepStatusIconRank(context, delta)

    fun startStatusServiceIfEnabled(reason: String) {
        BatteryAppStarter.startServiceIfEnabled(context, reason)
    }

    fun stopStatusService(reason: String) {
        BatteryAppStarter.stopService(context, reason)
    }

    fun notifyStatusIconIfEnabled(reason: String, rank: Int? = null) {
        BatteryAppStarter.notifyStatusIconIfEnabled(context, reason, rank)
    }

    fun cancelStatusIcon() {
        BatteryStatusIconHelper.cancelStatusIcon(context)
    }

    /** Чтение для UI экрана (SOC + temp), без зависимости от свитча шторки. */
    fun readBatterySample(): BatterySample {
        val reader = VhalBatteryReaderFactory.create(context)
        return try {
            reader.readBatterySoc()
        } finally {
            reader.close()
        }
    }

    fun readBatterySoc(): BatterySample {
        if (!BatterySettings.isEnabled(context)) {
            return BatterySample(
                socPercent = 0f,
                isAvailable = false,
                source = "disabled",
                details = "",
            )
        }
        return readBatterySample()
    }

    fun createReader(): VhalBatteryReader = VhalBatteryReaderFactory.create(context)
}
