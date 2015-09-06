package com.mehmetakiftutuncu.muezzin.models;

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
}
