package com.mehmetakiftutuncu.muezzin.interfaces;

import com.mehmetakiftutuncu.muezzin.models.District;
import com.mehmetakiftutuncu.muezzin.utilities.option.Option;

public interface OnDistrictSelectedListener {
    void onDistrictSelected(int countryId, int cityId, Option<District> district);
}
