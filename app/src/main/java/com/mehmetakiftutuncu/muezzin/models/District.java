package com.mehmetakiftutuncu.muezzin.models;

public class District {
    private int id;
    private String name;

    public District(int id, String name) {
        this.id   = id;
        this.name = name;
    }

    public int id() {
        return id;
    }

    public String name() {
        return name;
    }
}
