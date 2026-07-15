package com.geely.ex2.tools.data.battery

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.geely.ex2.tools.MainActivity
import com.geely.ex2.tools.R
import com.geely.ex2.tools.data.vhal.BatterySample
import com.geely.ex2.tools.data.vhal.VhalBatteryReaderFactory
import com.geely.ex2.tools.navigation.AppRoutes
import kotlin.math.roundToInt

object BatteryAppWidgetHelper {
    fun updateAll(context: Context, reason: String, sample: BatterySample? = null) {
        val appContext = context.applicationContext
        val manager = AppWidgetManager.getInstance(appContext) ?: return
        val componentName = ComponentName(appContext, BatteryAppWidgetProvider::class.java)
        val widgetIds = manager.getAppWidgetIds(componentName)
        if (widgetIds.isEmpty()) {
            return
        }

        val batterySample = sample
            ?: BatterySampleStore.sample.value
            ?: readBatterySoc(appContext)
        val views = buildRemoteViews(appContext, batterySample)
        manager.updateAppWidget(componentName, views)
        Log.i(TAG, "Battery app widget updated ($reason): ${formatPercent(appContext, batterySample)}")
    }

    fun update(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val appContext = context.applicationContext
        val sample = readBatterySoc(appContext)
        val views = buildRemoteViews(appContext, sample)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun readBatterySoc(context: Context): BatterySample {
        val reader = VhalBatteryReaderFactory.create(context)
        return try {
            reader.readBatterySoc()
        } finally {
            reader.close()
        }
    }

    private fun buildRemoteViews(context: Context, sample: BatterySample): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_battery)
        val percentText = formatPercent(context, sample)
        val progress = if (sample.isAvailable) {
            sample.socPercent.roundToInt().coerceIn(0, 100)
        } else {
            0
        }

        views.setTextViewText(R.id.widget_battery_percent, percentText)
        views.setProgressBar(R.id.widget_battery_progress, 100, progress, false)
        views.setViewVisibility(
            R.id.widget_battery_progress,
            if (sample.isAvailable) View.VISIBLE else View.INVISIBLE,
        )
        views.setOnClickPendingIntent(R.id.widget_battery_root, buildOpenBatteryPendingIntent(context))
        return views
    }

    private fun formatPercent(context: Context, sample: BatterySample): String {
        if (!sample.isAvailable) {
            return context.getString(R.string.battery_app_widget_unavailable)
        }
        return context.getString(R.string.battery_latest_value, sample.socPercent.roundToInt())
    }

    private fun buildOpenBatteryPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(EXTRA_START_ROUTE, AppRoutes.BATTERY)
        }

        var flags = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags = flags or PendingIntent.FLAG_IMMUTABLE
        }
        return PendingIntent.getActivity(context, REQUEST_OPEN_BATTERY, intent, flags)
    }

    const val EXTRA_START_ROUTE = "start_route"
    private const val REQUEST_OPEN_BATTERY = 15043
    private const val TAG = "GeelyToolsBattery"
}
