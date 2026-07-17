package com.geely.ex2.tools.data.system

import android.app.ActivityManager
import android.content.Context
import java.io.File

data class SystemMemoryInfo(
    val totalBytes: Long,
    val availBytes: Long,
) {
    val usedBytes: Long
        get() = (totalBytes - availBytes).coerceAtLeast(0L)

    /** 0f..1f */
    val usedFraction: Float
        get() = if (totalBytes <= 0L) 0f else (usedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)

    val usedPercent: Int
        get() = (usedFraction * 100f).toInt().coerceIn(0, 100)
}

/**
 * RAM snapshot aligned with `adb shell cat /proc/meminfo`:
 * total = MemTotal, available = MemAvailable (fallback MemFree + Cached).
 */
object SystemMemoryReader {
    private const val PROC_MEMINFO = "/proc/meminfo"
    private const val KB = 1024L

    fun read(context: Context): SystemMemoryInfo? {
        readProcMeminfo()?.let { return it }
        return readActivityManager(context)
    }

    private fun readProcMeminfo(): SystemMemoryInfo? {
        return try {
            var memTotalKb: Long? = null
            var memAvailableKb: Long? = null
            var memFreeKb: Long? = null
            var cachedKb: Long? = null

            File(PROC_MEMINFO).useLines { lines ->
                for (line in lines) {
                    when {
                        line.startsWith("MemTotal:") -> memTotalKb = parseKb(line)
                        line.startsWith("MemAvailable:") -> memAvailableKb = parseKb(line)
                        line.startsWith("MemFree:") -> memFreeKb = parseKb(line)
                        line.startsWith("Cached:") -> cachedKb = parseKb(line)
                    }
                    if (memTotalKb != null && memAvailableKb != null) {
                        return@useLines
                    }
                }
            }

            val totalKb = memTotalKb?.takeIf { it > 0L } ?: return null
            val availKb = memAvailableKb
                ?: if (memFreeKb != null) {
                    (memFreeKb!! + (cachedKb ?: 0L)).coerceAtMost(totalKb)
                } else {
                    return null
                }

            SystemMemoryInfo(
                totalBytes = totalKb * KB,
                availBytes = availKb.coerceIn(0L, totalKb) * KB,
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun readActivityManager(context: Context): SystemMemoryInfo? {
        val am = context.getSystemService(ActivityManager::class.java) ?: return null
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        if (info.totalMem <= 0L) return null
        return SystemMemoryInfo(
            totalBytes = info.totalMem,
            availBytes = info.availMem.coerceIn(0L, info.totalMem),
        )
    }

    private fun parseKb(line: String): Long {
        return line.split(Regex("\\s+")).getOrNull(1)?.toLongOrNull() ?: 0L
    }
}
