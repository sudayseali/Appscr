package com.noxscreen.app

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {
    private const val PREFS_NAME = "BlackScreenStats"
    private const val KEY_LANGUAGE = "app_language"
    const val DEFAULT_LANGUAGE = "en"

    val SUPPORTED_LANGUAGES: List<Pair<String, String>> = listOf(
        "en" to "English",
        "so" to "Soomaali",
        "ar" to "العربية",
        "es" to "Español",
        "fr" to "Français",
        "de" to "Deutsch",
        "tr" to "Türkçe",
        "sw" to "Kiswahili",
        "pt" to "Português",
        "ru" to "Русский",
        "zh" to "中文",
        "hi" to "हिन्दी",
        "ur" to "اردو",
        "fa" to "فارسی",
        "id" to "Bahasa Indonesia",
        "it" to "Italiano",
        "ja" to "日本語",
        "ko" to "한국어",
        "vi" to "Tiếng Việt",
        "bn" to "বাংলা",
        "mr" to "मराठी",
        "pa" to "ਪੰਜਾਬੀ",
        "ta" to "தமிழ்",
        "te" to "తెలుగు",
        "gu" to "ગુજરાતી"
    )

    /**
     * Returns the saved app language code, defaulting strictly to English ("en")
     * when the user first downloads/installs the app.
     */
    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_LANGUAGE, null)
        if (saved.isNullOrBlank()) {
            prefs.edit().putString(KEY_LANGUAGE, DEFAULT_LANGUAGE).apply()
            return DEFAULT_LANGUAGE
        }
        return saved
    }

    fun applyLocale(context: Context, languageCode: String = getSavedLanguage(context)): Context {
        val locale = Locale.forLanguageTag(languageCode)
        Locale.setDefault(locale)

        val resources = context.resources
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        config.setLocales(LocaleList(locale))

        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)

        return context.createConfigurationContext(config)
    }

    fun setNewLocale(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, languageCode).commit()
        applyLocale(context, languageCode)
        try {
            context.applicationContext?.let { applyLocale(it, languageCode) }
            context.sendBroadcast(
                android.content.Intent("com.noxscreen.app.SETTINGS_UPDATED").setPackage(context.packageName)
            )
        } catch (_: Exception) {}
        if (context is Activity) {
            context.recreate()
        }
    }
}
