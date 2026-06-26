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

    const val CAR_MANAGER_PROPERTY = "property"

    const val POLL_INTERVAL_MS = 1_000L
    const val BATTERY_POLL_INTERVAL_MS = 30_000L
}
