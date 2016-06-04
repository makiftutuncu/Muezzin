package com.mehmetakiftutuncu.muezzin.activities;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by akif on 04/06/16.
 */
public abstract class MuezzinActivity extends AppCompatActivity {
    public void setTitle(String title) {
        ActionBar supportActionBar = getSupportActionBar();

        if (supportActionBar != null) {
            supportActionBar.setTitle(title);
        }
    }

    public void setTitle(int titleResId) {
        ActionBar supportActionBar = getSupportActionBar();

        if (supportActionBar != null) {
            supportActionBar.setTitle(titleResId);
        }
    }

    public void setSubtitle(String subtitle) {
        ActionBar supportActionBar = getSupportActionBar();

        if (supportActionBar != null) {
            supportActionBar.setSubtitle(subtitle);
        }
    }

    public void setSubtitle(int subtitleResId) {
        ActionBar supportActionBar = getSupportActionBar();

        if (supportActionBar != null) {
            supportActionBar.setSubtitle(subtitleResId);
        }
    }
}
