package com.mehmetakiftutuncu.muezzin.interfaces;

import com.mehmetakiftutuncu.muezzin.models.District;

public interface OnDistrictSelectedListener {
    void onDistrictSelected(District district, int countryId, int cityId);
}
