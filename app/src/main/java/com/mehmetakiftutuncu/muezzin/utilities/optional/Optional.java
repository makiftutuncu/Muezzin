package com.mehmetakiftutuncu.muezzin.utilities.optional;

/**
 * Created by akif on 08/05/16.
 */
public abstract class Optional<T> {
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

    @Override public boolean equals(Object o) {
        return this == o
                || (isDefined && (o instanceof Optional && ((Optional) o).isDefined) && ((Optional) o).get().equals(value))
                || (isEmpty && (o instanceof Optional && ((Optional) o).isEmpty));
    }
}
