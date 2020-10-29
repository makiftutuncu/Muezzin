package com.stephentuso.welcome.ui;

import androidx.viewpager.widget.ViewPager;

import com.stephentuso.welcome.util.WelcomeScreenConfiguration;

/**
 * Created by stephentuso on 11/16/15.
 */
public interface OnWelcomeScreenPageChangeListener extends ViewPager.OnPageChangeListener {
    void setup(WelcomeScreenConfiguration config);
}

