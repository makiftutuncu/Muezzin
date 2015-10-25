package com.mehmetakiftutuncu.muezzin.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.mehmetakiftutuncu.muezzin.fragment.PrayerTimesFragment;

import java.util.ArrayList;
import java.util.HashMap;

public class PrayerTimesPagerAdapter extends FragmentPagerAdapter {
    private final HashMap<Integer, PrayerTimesFragment> prayerTimesFragments = new HashMap<>();

    public PrayerTimesPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public PrayerTimesPagerAdapter(FragmentManager fragmentManager, ArrayList<PrayerTimesFragment> prayerTimesFragments) {
        super(fragmentManager);

        for (int i = 0, size = prayerTimesFragments.size(); i < size; i++) {
            PrayerTimesFragment prayerTimesFragment = prayerTimesFragments.get(i);
            this.prayerTimesFragments.put(i, prayerTimesFragment);
        }
    }

    public PrayerTimesPagerAdapter add(PrayerTimesFragment prayerTimesFragment) {
        prayerTimesFragments.put(prayerTimesFragments.size(), prayerTimesFragment);

        return this;
    }

    @Override
    public Fragment getItem(int position) {
        return prayerTimesFragments.get(position);
    }

    @Override
    public int getCount() {
        return prayerTimesFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return prayerTimesFragments.get(position).getShortTitle();
    }
}
