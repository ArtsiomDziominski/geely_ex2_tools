package com.geely.ex2.tools.data.sounds

enum class CarLockSoundSource {
    ASSET,
    DOWNLOAD,
    VENDOR,
}

data class CarLockSoundOption(
    val id: String,
    val displayName: String,
    val source: CarLockSoundSource,
    val path: String,
)

data class CarLockSoundCatalog(
    val options: List<CarLockSoundOption> = emptyList(),
    val assetOptions: List<CarLockSoundOption> = emptyList(),
    val downloadOptions: List<CarLockSoundOption> = emptyList(),
    val vendorOptions: List<CarLockSoundOption> = emptyList(),
)
