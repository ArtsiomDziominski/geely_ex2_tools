package com.geely.ex2.tools.data.vhal

object VhalConstants {
    const val GLOBAL_AREA_ID = 0

    /** android.car.VehiclePropertyIds.PERF_VEHICLE_SPEED */
    const val PROP_PERF_VEHICLE_SPEED = 0x11600207

    /** Geely OEM SOC % (Flyme) — приоритет на IHU629G */
    const val PROP_ED_EV_BATTERY_PERCENTAGE = 0x2140a6ed

    /** android.car.VehiclePropertyIds.EV_BATTERY_LEVEL */
    const val PROP_EV_BATTERY_LEVEL = 0x11600309

    /** android.car.VehiclePropertyIds.EV_CURRENT_BATTERY_CAPACITY */
    const val PROP_EV_CURRENT_BATTERY_CAPACITY = 0x1160030d

    /** android.car.VehiclePropertyIds.INFO_EV_BATTERY_CAPACITY */
    const val PROP_INFO_EV_BATTERY_CAPACITY = 0x11600106

    /** Flyme OEM: DM_FUNC_DRIVE_MODE_SELECT */
    const val PROP_DM_FUNC_DRIVE_MODE_SELECT = 0x22010100

    /** Flyme OEM drive mode values (VALUE_DRIVE_MODE_SELECTION_*) */
    const val DRIVE_MODE_ECO = 0x22010101
    const val DRIVE_MODE_COMFORT = 0x22010102
    const val DRIVE_MODE_DYNAMIC = 0x22010103
    const val DRIVE_MODE_XC = 0x22010104
    const val DRIVE_MODE_PURE = 0x22010106
    const val DRIVE_MODE_HYBRID = 0x22010107
    const val DRIVE_MODE_SNOW = 0x22010109
    const val DRIVE_MODE_ADAPTIVE = 0x22010116

    /** Flyme OEM: SETTING_FUNC_ENERGY_REGENERATION (动能回收) */
    const val PROP_SETTING_FUNC_ENERGY_REGENERATION = 0x22020500

    /** Flyme OEM energy regen levels (VALUE_ENERGY_REGENERATION_LEVEL_*) */
    const val REGEN_LEVEL_LOW = 0x22020501
    const val REGEN_LEVEL_MID = 0x22020502
    const val REGEN_LEVEL_HIGH = 0x22020503
    const val REGEN_LEVEL_AUTO = 0x22020504

    /** Flyme OEM: BCM_FUNC_LIGHT_ATMOSPHERE_LAMPS (氛围灯开关) */
    const val PROP_BCM_FUNC_LIGHT_ATMOSPHERE_LAMPS = 0x21051000

    /** Flyme OEM: SETTING_FUNC_AVAS_SOUND_SWITCH (低速提示音量开关 / ESM) */
    const val PROP_SETTING_FUNC_AVAS_SOUND_SWITCH = 0x2030b600

    /** Flyme OEM: SETTING_FUNC_AVAS_SOUND_TYPE (低速提示音模式) */
    const val PROP_SETTING_FUNC_AVAS_SOUND_TYPE = 0x201a0700

    /** AVAS sound type values (AVAS_SOUND_TYPE_*) */
    const val AVAS_SOUND_TYPE_NONE = 0
    const val AVAS_SOUND_TYPE_1 = 0x201a0701
    const val AVAS_SOUND_TYPE_2 = 0x201a0702
    const val AVAS_SOUND_TYPE_3 = 0x201a0703

    /** android.hardware.automotive.vehicle.V2_0.VehicleProperty.AVASSwitch */
    const val PROP_AVAS_SWITCH = 0x2140a148

    /** ICarFunction.COMMON_VALUE_ON / COMMON_VALUE_OFF */
    const val COMMON_VALUE_ON = 1
    const val COMMON_VALUE_OFF = 0

    const val CAR_MANAGER_PROPERTY = "property"

    const val POLL_INTERVAL_MS = 1_000L

    /** Интервал опроса виджетов в шторке (батарея, температура, скорость) */
    const val STATUS_WIDGET_POLL_INTERVAL_MS = 100_000L

    const val BATTERY_POLL_INTERVAL_MS = STATUS_WIDGET_POLL_INTERVAL_MS
    const val TEMPERATURE_POLL_INTERVAL_MS = STATUS_WIDGET_POLL_INTERVAL_MS
}
