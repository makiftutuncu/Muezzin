package com.mehmetakiftutuncu.muezzin.utilities;

import android.content.Context;

import java.text.Collator;

/**
 * Created by akif on 11/05/16.
 */
public class LocaleUtils {
    public static String getCurrentLanguage(Context context) {
        return context.getResources().getConfiguration().locale.getLanguage();
    }

    public static boolean isLanguageTurkish(Context context) {
        return getCurrentLanguage(context).contains("tr");
    }

    public static boolean isLanguageEnglish(Context context) {
        return getCurrentLanguage(context).contains("en");
    }

    public static Collator getCollator(Context context) {
        return Collator.getInstance(context.getResources().getConfiguration().locale);
    }
}
