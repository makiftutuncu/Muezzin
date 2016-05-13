package com.mehmetakiftutuncu.interfaces;

import android.support.annotation.NonNull;

import com.mehmetakiftutuncu.muezzin.models.District;

import java.util.ArrayList;

/**
 * Created by akif on 13/05/16.
 */
public interface OnDistrictsDownloadedListener {
    void onDistrictsDownloaded(@NonNull ArrayList<District> districts);
    void onDistrictsDownloadFailed();
}
