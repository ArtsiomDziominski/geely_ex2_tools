package com.geely.ex2.tools.data.shell

import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

data class ShellResult(
    val exitCode: Int,
    val output: String,
)

object SystemShell {
    fun exec(command: String, timeoutSeconds: Long = 30): ShellResult {
        val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
        val completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
        if (!completed) {
            process.destroyForcibly()
            return ShellResult(exitCode = -1, output = "timeout: $command")
        }
        val output = BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            reader.readText()
        } + BufferedReader(InputStreamReader(process.errorStream)).use { reader ->
            reader.readText()
        }
        return ShellResult(exitCode = process.exitValue(), output = output.trim())
    }

    fun remountVendorRw(): ShellResult = exec("mount -o rw,remount /vendor")

    fun sync(): ShellResult = exec("sync")
}
