package com.mehmetakiftutuncu.muezzin.utilities.optional;

/**
 * Created by akif on 08/05/16.
 */
public final class Some<T> extends Optional<T> {
    public Some(T value) {
        this.isDefined = true;
        this.isEmpty   = false;

        this.value = value;
    }

    @Override public T get() {
        return value;
    }
}
