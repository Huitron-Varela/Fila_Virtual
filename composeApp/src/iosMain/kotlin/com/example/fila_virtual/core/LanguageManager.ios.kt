package com.example.fila_virtual.core

import platform.Foundation.NSUserDefaults

actual fun changeAppLanguage(languageCode: String) {
    NSUserDefaults.standardUserDefaults.setObject(listOf(languageCode), forKey = "AppleLanguages")
    NSUserDefaults.standardUserDefaults.synchronize()
}

actual fun getCurrentLanguage(): String {
    val languages = NSUserDefaults.standardUserDefaults.stringArrayForKey("AppleLanguages")
    val firstLang = languages?.firstOrNull() as? String
    return firstLang?.substringBefore("-") ?: "es"
}
