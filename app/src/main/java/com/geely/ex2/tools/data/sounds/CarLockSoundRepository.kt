package com.geely.ex2.tools.data.sounds

import android.content.Context
import android.net.Uri
import android.os.Process
import android.provider.OpenableColumns
import com.geely.ex2.tools.data.shell.SystemShell
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

object CarLockSoundPaths {
    const val CARLOCK_DIR = "/vendor/etc/carlock"
    const val TARGET_FILE = "$CARLOCK_DIR/nengliangshouheng.wav"
    const val BACKUP_FILE = "$CARLOCK_DIR/nengliangshouheng.wav.stock"
    const val ASSETS_DIR = "sounds"
    const val DOWNLOAD_DIR = "/storage/emulated/0/Download"
}

data class CarLockSoundStatus(
    val isInstalled: Boolean,
    val targetPath: String = CarLockSoundPaths.TARGET_FILE,
    val sourcePath: String? = null,
    val sourceDisplayName: String? = null,
    val selectedSoundId: String? = null,
    val isSystemUid: Boolean = Process.myUid() == Process.SYSTEM_UID,
    val details: String = "",
)

data class CarLockSoundWriteResult(
    val ok: Boolean,
    val error: String? = null,
    val details: String = "",
    val needsReboot: Boolean = false,
)

data class CarLockSoundImportResult(
    val ok: Boolean,
    val displayName: String? = null,
    val error: String? = null,
)

class CarLockSoundRepository(
    private val context: Context,
) {
    fun listAvailableSounds(): CarLockSoundCatalog {
        val assetOptions = listAssetSounds()
        val downloadOptions = listDownloadSounds()
        val vendorOptions = listVendorSounds()
        val options = (assetOptions + downloadOptions + vendorOptions)
            .sortedBy { it.displayName.lowercase() }
        return CarLockSoundCatalog(
            options = options,
            assetOptions = assetOptions,
            downloadOptions = downloadOptions,
            vendorOptions = vendorOptions,
        )
    }

    fun readStatus(): CarLockSoundStatus {
        val target = File(CarLockSoundPaths.TARGET_FILE)
        val backup = File(CarLockSoundPaths.BACKUP_FILE)
        val sourceFile = resolveSourceFile()
        val installed = when {
            backup.isFile && target.isFile -> sha256(target) != sha256(backup)
            sourceFile != null && target.isFile -> sha256(target) == sha256(sourceFile)
            else -> false
        }
        val source = resolveSourcePath()
        return CarLockSoundStatus(
            isInstalled = installed,
            sourcePath = source,
            sourceDisplayName = CarLockSoundSettings.getSelectedName(context),
            selectedSoundId = CarLockSoundSettings.getSelectedId(context),
            isSystemUid = Process.myUid() == Process.SYSTEM_UID,
            details = when {
                !installed -> ""
                source != null -> source
                else -> CarLockSoundPaths.TARGET_FILE
            },
        )
    }

    fun selectSound(option: CarLockSoundOption): CarLockSoundImportResult {
        val dir = CarLockSoundSettings.selectedDir(context)
        if (!dir.exists() && !dir.mkdirs()) {
            return CarLockSoundImportResult(ok = false, error = "mkdir_failed")
        }

        val dest = CarLockSoundSettings.selectedFile(context)
        try {
            when (option.source) {
                CarLockSoundSource.ASSET -> {
                    context.assets.open(option.path).use { input ->
                        dest.outputStream().use { output -> input.copyTo(output) }
                    }
                }
                CarLockSoundSource.DOWNLOAD,
                CarLockSoundSource.VENDOR,
                -> copyFile(File(option.path), dest)
            }
        } catch (e: IOException) {
            return CarLockSoundImportResult(
                ok = false,
                error = "copy_failed",
                displayName = e.message,
            )
        }

        if (!dest.isFile || dest.length() <= 0L) {
            return CarLockSoundImportResult(ok = false, error = "empty_file")
        }

        CarLockSoundSettings.setSelected(context, option.id, option.displayName)
        return CarLockSoundImportResult(ok = true, displayName = option.displayName)
    }

    fun importSelectedSound(uri: Uri): CarLockSoundImportResult {
        val displayName = queryDisplayName(uri)
            ?: return CarLockSoundImportResult(ok = false, error = "invalid_uri")

        val dir = CarLockSoundSettings.selectedDir(context)
        if (!dir.exists() && !dir.mkdirs()) {
            return CarLockSoundImportResult(ok = false, error = "mkdir_failed")
        }

        val dest = CarLockSoundSettings.selectedFile(context)
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                dest.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return CarLockSoundImportResult(ok = false, error = "open_failed")
        } catch (e: IOException) {
            return CarLockSoundImportResult(
                ok = false,
                error = "copy_failed",
                displayName = e.message,
            )
        }

        if (!dest.isFile || dest.length() <= 0L) {
            return CarLockSoundImportResult(ok = false, error = "empty_file")
        }

        CarLockSoundSettings.setSelected(context, uri.toString(), displayName)
        return CarLockSoundImportResult(ok = true, displayName = displayName)
    }

    fun install(): CarLockSoundWriteResult {
        if (Process.myUid() != Process.SYSTEM_UID) {
            return CarLockSoundWriteResult(ok = false, error = "not_system_uid")
        }

        val sourceFile = resolveSourceFile()
            ?: return CarLockSoundWriteResult(ok = false, error = "source_missing")

        val remount = SystemShell.remountVendorRw()
        if (remount.exitCode != 0) {
            return CarLockSoundWriteResult(
                ok = false,
                error = "remount_failed",
                details = remount.output,
            )
        }

        val carlockDir = File(CarLockSoundPaths.CARLOCK_DIR)
        if (!carlockDir.exists() && !carlockDir.mkdirs()) {
            return CarLockSoundWriteResult(
                ok = false,
                error = "mkdir_failed",
                details = CarLockSoundPaths.CARLOCK_DIR,
            )
        }

        val target = File(CarLockSoundPaths.TARGET_FILE)
        val backup = File(CarLockSoundPaths.BACKUP_FILE)
        if (target.isFile && !backup.exists() && sha256(target) != sha256(sourceFile)) {
            try {
                copyFile(target, backup)
            } catch (e: IOException) {
                return CarLockSoundWriteResult(
                    ok = false,
                    error = "backup_failed",
                    details = e.message.orEmpty(),
                )
            }
            SystemShell.exec("chmod 644 ${CarLockSoundPaths.BACKUP_FILE}")
        }

        try {
            copyFile(sourceFile, target)
        } catch (e: IOException) {
            return CarLockSoundWriteResult(
                ok = false,
                error = "copy_failed",
                details = e.message.orEmpty(),
            )
        }

        val chmod = SystemShell.exec("chmod 644 ${CarLockSoundPaths.TARGET_FILE}")
        if (chmod.exitCode != 0) {
            target.setReadable(true, false)
            target.setWritable(false, false)
        }

        val sync = SystemShell.sync()
        val ok = target.isFile && target.length() > 0L
        return CarLockSoundWriteResult(
            ok = ok,
            error = if (ok) null else "verify_failed",
            details = sync.output,
            needsReboot = true,
        )
    }

    fun remove(): CarLockSoundWriteResult {
        if (Process.myUid() != Process.SYSTEM_UID) {
            return CarLockSoundWriteResult(ok = false, error = "not_system_uid")
        }

        if (!readStatus().isInstalled) {
            return CarLockSoundWriteResult(ok = true, needsReboot = false)
        }

        val remount = SystemShell.remountVendorRw()
        if (remount.exitCode != 0) {
            return CarLockSoundWriteResult(
                ok = false,
                error = "remount_failed",
                details = remount.output,
            )
        }

        val target = File(CarLockSoundPaths.TARGET_FILE)
        val backup = File(CarLockSoundPaths.BACKUP_FILE)
        if (!backup.isFile) {
            return CarLockSoundWriteResult(
                ok = false,
                error = "backup_missing",
                details = CarLockSoundPaths.BACKUP_FILE,
            )
        }

        try {
            copyFile(backup, target)
        } catch (e: IOException) {
            return CarLockSoundWriteResult(
                ok = false,
                error = "restore_failed",
                details = e.message.orEmpty(),
            )
        }

        SystemShell.exec("chmod 644 ${CarLockSoundPaths.TARGET_FILE}")
        val sync = SystemShell.sync()
        return CarLockSoundWriteResult(
            ok = true,
            details = sync.output,
            needsReboot = true,
        )
    }

    private fun listAssetSounds(): List<CarLockSoundOption> {
        val names = context.assets.list(CarLockSoundPaths.ASSETS_DIR)
            ?.filter { it.endsWith(".wav", ignoreCase = true) }
            .orEmpty()
        return names.map { name ->
            val path = "${CarLockSoundPaths.ASSETS_DIR}/$name"
            CarLockSoundOption(
                id = "asset://$path",
                displayName = name,
                source = CarLockSoundSource.ASSET,
                path = path,
            )
        }.sortedBy { it.displayName.lowercase() }
    }

    private fun listDownloadSounds(): List<CarLockSoundOption> =
        listWavFilesInDir(
            dir = File(CarLockSoundPaths.DOWNLOAD_DIR),
            source = CarLockSoundSource.DOWNLOAD,
            idPrefix = "download://",
        )

    private fun listVendorSounds(): List<CarLockSoundOption> {
        val carlockDir = File(CarLockSoundPaths.CARLOCK_DIR)
        return listWavFilesInDir(
            dir = carlockDir,
            source = CarLockSoundSource.VENDOR,
            idPrefix = "vendor://",
            excludeNames = setOf(
                CarLockSoundPaths.TARGET_FILE.substringAfterLast('/'),
                CarLockSoundPaths.BACKUP_FILE.substringAfterLast('/'),
            ),
        )
    }

    private fun listWavFilesInDir(
        dir: File,
        source: CarLockSoundSource,
        idPrefix: String,
        excludeNames: Set<String> = emptySet(),
    ): List<CarLockSoundOption> {
        if (!dir.isDirectory) return emptyList()
        return dir.listFiles()
            ?.asSequence()
            ?.filter { file ->
                file.isFile &&
                    file.name.endsWith(".wav", ignoreCase = true) &&
                    !file.name.endsWith(".stock", ignoreCase = true) &&
                    file.name !in excludeNames
            }
            ?.map { file ->
                CarLockSoundOption(
                    id = "$idPrefix${file.absolutePath}",
                    displayName = file.name,
                    source = source,
                    path = file.absolutePath,
                )
            }
            ?.sortedBy { it.displayName.lowercase() }
            ?.toList()
            .orEmpty()
    }

    private fun resolveSourcePath(): String? {
        if (CarLockSoundSettings.hasSelectedFile(context)) {
            return CarLockSoundSettings.selectedFile(context).absolutePath
        }
        return null
    }

    private fun resolveSourceFile(): File? {
        if (CarLockSoundSettings.hasSelectedFile(context)) {
            return CarLockSoundSettings.selectedFile(context)
        }
        return null
    }

    private fun queryDisplayName(uri: Uri): String? {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) {
                return cursor.getString(index)
            }
        }
        return uri.lastPathSegment
    }

    private fun copyFile(from: File, to: File) {
        from.inputStream().use { input ->
            to.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        invalidateHashCache(to)
    }

    private fun sha256(file: File): String {
        val size = file.length()
        val lastModified = file.lastModified()
        val path = file.absolutePath
        val cached = hashCache[path]
        if (cached != null &&
            cached.size == size &&
            cached.lastModified == lastModified
        ) {
            return cached.hash
        }

        val hash = computeSha256(file)
        hashCache[path] = HashCacheEntry(
            size = size,
            lastModified = lastModified,
            hash = hash,
        )
        return hash
    }

    private fun computeSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun invalidateHashCache(file: File) {
        hashCache.remove(file.absolutePath)
    }

    private data class HashCacheEntry(
        val size: Long,
        val lastModified: Long,
        val hash: String,
    )

    companion object {
        private val hashCache = ConcurrentHashMap<String, HashCacheEntry>()
    }
}
