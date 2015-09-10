package com.mehmetakiftutuncu.muezzin.models;

import com.mehmetakiftutuncu.muezzin.utilities.LocaleUtils;

public class Country {
    private int id;
    private String name;
    private String trName;
    private String nativeName;

    public Country(int id, String name, String trName, String nativeName) {
        this.id         = id;
        this.name       = name;
        this.trName     = trName;
        this.nativeName = nativeName;
    }

    public int id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String trName() {
        return trName;
    }

    public String nativeName() {
        return nativeName;
    }

    public String getLocalizedName() {
        if (LocaleUtils.isLanguageTr()) {
            return trName;
        } else {
            return name;
        }
    }
}
