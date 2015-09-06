package com.mehmetakiftutuncu.muezzin.models;

public class City {
    private int id;
    private String name;

    public City(int id, String name) {
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
