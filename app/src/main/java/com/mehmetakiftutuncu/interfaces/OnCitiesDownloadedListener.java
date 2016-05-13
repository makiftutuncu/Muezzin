package com.mehmetakiftutuncu.interfaces;

import android.support.annotation.NonNull;

import com.mehmetakiftutuncu.muezzin.models.City;

import java.util.ArrayList;

/**
 * Created by akif on 13/05/16.
 */
public interface OnCitiesDownloadedListener {
    void onCitiesDownloaded(@NonNull ArrayList<City> cities);
    void onCitiesDownloadFailed();
}
