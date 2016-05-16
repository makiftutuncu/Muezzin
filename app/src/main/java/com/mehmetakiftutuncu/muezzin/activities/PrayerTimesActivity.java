package com.mehmetakiftutuncu.muezzin.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.fragments.PrayerTimesFragment;
import com.mehmetakiftutuncu.muezzin.utilities.Log;
import com.mehmetakiftutuncu.muezzin.utilities.optional.None;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Optional;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Some;

public class PrayerTimesActivity extends AppCompatActivity {
    private static final String EXTRA_COUNTRY_ID  = "countryId";
    private static final String EXTRA_CITY_ID     = "cityId";
    private static final String EXTRA_DISTRICT_ID = "districtId";

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prayertimes);

        Bundle extras = getIntent().getExtras();

        int countryId                = extras.getInt(EXTRA_COUNTRY_ID);
        int cityId                   = extras.getInt(EXTRA_CITY_ID);
        Optional<Integer> districtId = extras.containsKey(EXTRA_DISTRICT_ID) ? new Some<>(extras.getInt(EXTRA_DISTRICT_ID)) : new None<Integer>();

        Log.debug(getClass(), "Place selected for country '%d', city '%d' and district '%s'!", countryId, cityId, districtId);

        PrayerTimesFragment prayerTimesFragment = PrayerTimesFragment.with(countryId, cityId, districtId);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout_prayerTimesContainer, prayerTimesFragment, "PrayerTimesFragment")
                .commit();
    }

    public static Bundle getPlaceExtras(int countryId, int cityId, Optional<Integer> districtId) {
        Bundle bundle = new Bundle();

        bundle.putInt(EXTRA_COUNTRY_ID, countryId);
        bundle.putInt(EXTRA_CITY_ID, cityId);

        if (districtId.isDefined) {
            bundle.putInt(EXTRA_DISTRICT_ID, districtId.get());
        }

        return bundle;
    }
}
