package com.mehmetakiftutuncu.muezzin.utilities

import android.content.Context
import com.mehmetakiftutuncu.muezzin.models.Country
import java.text.Collator
import java.util.*

object LocaleUtils {
    val turkishCollator: Collator = Collator.getInstance(Locale("tr", "TR"))

    fun isLanguageTurkish(ctx: Context): Boolean =
        getCurrentLanguage(ctx).contains("tr")

    fun isLanguageEnglish(ctx: Context): Boolean =
        getCurrentLanguage(ctx).contains("en")


    fun getCollator(ctx: Context): Collator =
        when {
            isLanguageTurkish(ctx) -> turkishCollator
            isLanguageEnglish(ctx) -> Collator.getInstance(Locale.ENGLISH)
            else                       -> Collator.getInstance()
        }

    fun List<Country>.turkeyFirstSorted(ctx: Context): List<Country> {
        val collator  = getCollator(ctx)
        val isTurkish = isLanguageTurkish(ctx)
        val isEnglish = isLanguageEnglish(ctx)

        val (turkey, others) = this.sortedWith { c1, c2 ->
            collator.compare(
                if (isTurkish) c1.nameTurkish else if (isEnglish) c1.name else c1.nameNative,
                if (isTurkish) c2.nameTurkish else if (isEnglish) c2.name else c2.nameNative
            )
        }.partition { it.isTurkey }

        return turkey + others
    }

    private fun getCurrentLanguage(ctx: Context): String =
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N) {
            ctx.resources.configuration.locale.language
        } else {
            ctx.resources.configuration.locales[0].language
        }
}