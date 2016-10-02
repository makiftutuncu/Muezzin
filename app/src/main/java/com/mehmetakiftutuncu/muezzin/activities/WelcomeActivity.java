package com.mehmetakiftutuncu.muezzin.activities;

import com.mehmetakiftutuncu.muezzin.R;
import com.stephentuso.welcome.WelcomeScreenBuilder;
import com.stephentuso.welcome.util.WelcomeScreenConfiguration;

public class WelcomeActivity extends com.stephentuso.welcome.ui.WelcomeActivity {
    @Override protected WelcomeScreenConfiguration configuration() {
        return new WelcomeScreenBuilder(this)
                .theme(R.style.MuezzinWelcomeTheme)
                .defaultBackgroundColor(R.color.background)
                .basicPage(R.drawable.ic_mosque, getString(R.string.welcome_title1), getString(R.string.welcome_content1), R.color.colorPrimary, true)
                .basicPage(R.drawable.ic_place_white, getString(R.string.welcome_title2), getString(R.string.welcome_content2), R.color.colorPrimary, true)
                .basicPage(R.drawable.ic_settings, getString(R.string.welcome_title3), getString(R.string.welcome_content3), R.color.colorPrimary, true)
                .basicPage(R.drawable.ic_home, getString(R.string.welcome_title4), getString(R.string.welcome_content4), R.color.colorPrimary, true)
                .swipeToDismiss(true)
                .animateButtons(true)
                .backButtonNavigatesPages(true)
                .backButtonSkips(false)
                .canSkip(true)
                .build();
    }
}
