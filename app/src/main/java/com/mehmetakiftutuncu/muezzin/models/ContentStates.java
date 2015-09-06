package com.mehmetakiftutuncu.muezzin.models;

/**
 * Possible states of content that needs to be loaded/downloaded then shown to user
 *
 * @author mehmetakiftutuncu
 */
public enum ContentStates {
    /** Content is currently being loaded/downloaded */
    LOADING,
    /** Content is successfully loaded/downloaded */
    CONTENT,
    /** Content is loaded/downloaded but it is empty */
    NO_CONTENT,
    /** Loading/downloading content failed */
    ERROR
}
