package com.geely.ex2.tools.data.avas

object AvasConstants {
    /** Car.getCarManager("audio") → CarAudioManager */
    const val CAR_MANAGER_AUDIO = "audio"

    /** setAVASMode(0) — mute / Desligado (CentralEXAuto) */
    const val MODE_MUTED = 0

    /** Активный режим по умолчанию при unmute (Тип 1) */
    const val MODE_DEFAULT_ACTIVE = 1

    /**
     * VHAL OEM (VehicleProperty) — fallback, если нет CAR_CONTROL_AUDIO_VOLUME.
     * CentralEXAuto ходит через CarAudioManager; у обычного APK часто только VHAL.
     */
    const val PROP_AVAS_SWITCH = 557883720 // 0x2140A148
    const val PROP_AVAS_VOLUME = 557883721 // 0x2140A149
    const val PROP_AVAS_DISABLED_SET = 557887627 // 0x2140B08B

    /** Settings.Global keys из CarAudioService (Common.*) */
    const val SETTINGS_AVAS_MODE = "audio_avas_mode"
    const val SETTINGS_LAST_AVAS_MODE = "audio_last_avas_mode"

    const val AVAS_UI_POLL_INTERVAL_MS = 3_000L

    /** CentralEXAuto scheduleAvasRestore delay после boot */
    const val AVAS_RESTORE_DELAY_MS = 12_000L

    const val AVAS_RESTORE_VERIFY_BASE_MS = 800L
    const val AVAS_RESTORE_VERIFY_STEP_MS = 800L
    const val AVAS_RESTORE_WRITE_ATTEMPTS = 3

    val AVAS_AREAS: IntArray = intArrayOf(0, 1)
}
