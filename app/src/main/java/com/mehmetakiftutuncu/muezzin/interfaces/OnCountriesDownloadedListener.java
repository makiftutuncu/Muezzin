package com.mehmetakiftutuncu.muezzin.interfaces;

import android.support.annotation.NonNull;

import com.mehmetakiftutuncu.muezzin.models.Country;

import java.util.ArrayList;

/**
 * Created by akif on 13/05/16.
 */
public interface OnCountriesDownloadedListener {
    void onCountriesDownloaded(@NonNull ArrayList<Country> countries);
    void onCountriesDownloadFailed();
}
