package com.afyalink.app;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.afyalink.app.storage.SettingsStore;

/** Applies the user's palette preference before any screen is inflated. */
public final class AfyaLinkApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        String appearance = new SettingsStore(this).appearance();
        if ("light".equals(appearance)) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        else if ("dark".equals(appearance)) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }
}
