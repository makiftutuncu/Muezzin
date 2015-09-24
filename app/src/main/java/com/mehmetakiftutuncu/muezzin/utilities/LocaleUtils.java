package com.mehmetakiftutuncu.muezzin.utilities;

import java.text.Collator;
import java.util.Locale;

public class LocaleUtils {
    public static boolean isLanguageTr() {
        return Locale.getDefault().getLanguage().equals("tr");
    }

    public static Collator getCollator() {
        return Collator.getInstance();
    }
}
