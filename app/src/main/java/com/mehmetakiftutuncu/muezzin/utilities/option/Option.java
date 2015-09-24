package com.mehmetakiftutuncu.muezzin.utilities.option;

public abstract class Option<T> {
    public boolean isDefined;
    public boolean isEmpty;

    protected T value;

    public abstract T get();

    public T getOrElse(T defaultValue) {
        return isDefined ? get() : defaultValue;
    }

    @Override public String toString() {
        return isDefined ? value.toString() : "None";
    }
}
