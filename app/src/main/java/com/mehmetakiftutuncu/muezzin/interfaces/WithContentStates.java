package com.mehmetakiftutuncu.muezzin.interfaces;

import android.view.View;

import com.mehmetakiftutuncu.muezzin.models.ContentStates;

public interface WithContentStates {
    void changeStateTo(ContentStates newState);

    void retryOnError(View retryButton);
}
