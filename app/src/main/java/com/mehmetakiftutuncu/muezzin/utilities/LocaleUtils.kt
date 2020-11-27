package com.mehmetakiftutuncu.muezzin.utilities

import android.content.Context
import java.text.Collator
import java.util.*

object LocaleUtils {
    val turkishCollator: Collator = Collator.getInstance(Locale("tr", "TR"))

    fun isLanguageTurkish(context: Context): Boolean =
        getCurrentLanguage(context).contains("tr")

    fun isLanguageEnglish(context: Context): Boolean =
        getCurrentLanguage(context).contains("en")


    fun getCollator(context: Context): Collator =
        when {
            isLanguageTurkish(context) -> turkishCollator
            isLanguageEnglish(context) -> Collator.getInstance(Locale.ENGLISH)
            else                       -> Collator.getInstance()
        }

    private fun getCurrentLanguage(context: Context): String =
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N) {
            context.resources.configuration.locale.language
        } else {
            context.resources.configuration.locales[0].language
        }
}