package com.mehmetakiftutuncu.muezzin.utilities.option;

public final class Some<T> extends Option<T> {
    public Some(T value) {
        this.isDefined = true;
        this.isEmpty   = false;

        this.value = value;
    }

    @Override public T get() {
        return value;
    }
}
