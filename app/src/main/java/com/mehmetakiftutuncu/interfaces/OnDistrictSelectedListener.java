package com.mehmetakiftutuncu.interfaces;

import com.mehmetakiftutuncu.muezzin.models.District;

/**
 * Created by akif on 13/05/16.
 */
public interface OnDistrictSelectedListener {
    void onDistrictSelected(District district);
    void onNoDistrictsFound();
}
