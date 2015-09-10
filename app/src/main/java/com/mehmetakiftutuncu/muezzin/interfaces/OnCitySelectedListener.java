package com.mehmetakiftutuncu.muezzin.interfaces;

import com.mehmetakiftutuncu.muezzin.models.City;

public interface OnCitySelectedListener {
    void onCitySelected(City city, int countryId);
}
