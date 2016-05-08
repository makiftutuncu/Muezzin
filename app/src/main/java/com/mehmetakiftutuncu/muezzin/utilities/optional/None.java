package com.mehmetakiftutuncu.muezzin.utilities.optional;

import java.util.NoSuchElementException;

/**
 * Created by akif on 08/05/16.
 */
public final class None<T> extends Optional<T> {
    public None() {
        isDefined = false;
        isEmpty   = true;

        value = null;
    }

    @Override public T get() {
        throw new NoSuchElementException("Called get() on a None!");
    }
}
