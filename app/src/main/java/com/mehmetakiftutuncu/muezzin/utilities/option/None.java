package com.mehmetakiftutuncu.muezzin.utilities.option;

import java.util.NoSuchElementException;

public final class None<T> extends Option<T> {
    public None() {
        isDefined = false;
        isEmpty   = true;

        value = null;
    }

    @Override public T get() {
        throw new NoSuchElementException("You called get() method on a None!");
    }
}
