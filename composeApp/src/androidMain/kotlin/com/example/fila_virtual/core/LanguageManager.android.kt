package com.example.fila_virtual.core

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

actual fun changeAppLanguage(languageCode: String) {
    val localeList = LocaleListCompat.forLanguageTags(languageCode)
    AppCompatDelegate.setApplicationLocales(localeList)
}

actual fun getCurrentLanguage(): String {
    return AppCompatDelegate.getApplicationLocales().toLanguageTags().substringBefore("-").ifEmpty {
        Locale.getDefault().language
    }
}
