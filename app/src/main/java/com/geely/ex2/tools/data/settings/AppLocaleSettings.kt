package com.geely.ex2.tools.data.settings

import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.geely.ex2.tools.R
import com.geely.ex2.tools.data.kv.AppKv
import com.tencent.mmkv.MMKV

enum class AppLocale(
    val storageValue: String,
    @StringRes val labelRes: Int,
) {
    SYSTEM("system", R.string.settings_language_system),
    ENGLISH("en", R.string.settings_language_english),
    RUSSIAN("ru", R.string.settings_language_russian),
    PORTUGUESE_BR("pt-BR", R.string.settings_language_portuguese),
    ;

    companion object {
        fun fromStorageValue(value: String?): AppLocale {
            return entries.firstOrNull { it.storageValue == value } ?: SYSTEM
        }
    }
}

object AppLocaleSettings {
    private const val PREFS = "app_locale_prefs"
    private const val KEY_LOCALE = "app_locale"

    fun get(context: Context): AppLocale {
        return AppLocale.fromStorageValue(
            kv(context).decodeString(KEY_LOCALE, AppLocale.SYSTEM.storageValue),
        )
    }

    fun set(context: Context, locale: AppLocale) {
        kv(context).encode(KEY_LOCALE, locale.storageValue)
    }

    private fun kv(context: Context): MMKV {
        AppKv.init(context)
        return AppKv.of(PREFS)
    }
}

object AppLocaleController {
    fun applyStored(context: Context) {
        apply(AppLocaleSettings.get(context))
    }

    fun apply(locale: AppLocale) {
        AppCompatDelegate.setApplicationLocales(toLocaleList(locale))
    }

    fun toLocaleList(locale: AppLocale): LocaleListCompat {
        return when (locale) {
            // Empty list = follow system; do not snapshot Locale.getDefault()
            // (that is already the app locale after a prior setApplicationLocales).
            AppLocale.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
            AppLocale.ENGLISH -> LocaleListCompat.forLanguageTags("en")
            AppLocale.RUSSIAN -> LocaleListCompat.forLanguageTags("ru")
            AppLocale.PORTUGUESE_BR -> LocaleListCompat.forLanguageTags("pt-BR")
        }
    }
}