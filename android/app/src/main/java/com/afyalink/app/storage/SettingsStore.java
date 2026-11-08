package com.afyalink.app.storage;

import android.content.Context;
import android.content.SharedPreferences;

/** Non-health preferences only. Reading data never enters SharedPreferences. */
public final class SettingsStore {
    private static final String PREFS = "afyalink_preferences_v1";
    private final SharedPreferences prefs;

    public SettingsStore(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public String profileName() { return prefs.getString("profile_name", ""); }
    public void setProfileName(String name) { prefs.edit().putString("profile_name", name == null ? "" : name.trim()).apply(); }
    public boolean remindersEnabled() { return prefs.getBoolean("reminders_enabled", false); }
    public void setRemindersEnabled(boolean enabled) { prefs.edit().putBoolean("reminders_enabled", enabled).apply(); }
    public int reminderHour() { return prefs.getInt("reminder_hour", 8); }
    public int reminderMinute() { return prefs.getInt("reminder_minute", 0); }
    public void setReminderTime(int hour, int minute) {
        prefs.edit().putInt("reminder_hour", hour).putInt("reminder_minute", minute).apply();
    }
    public boolean biometricLockEnabled() { return prefs.getBoolean("biometric_lock", false); }
    public void setBiometricLockEnabled(boolean enabled) { prefs.edit().putBoolean("biometric_lock", enabled).apply(); }
    public String appearance() { return prefs.getString("appearance", "light"); }
    public void setAppearance(String value) { prefs.edit().putString("appearance", value).apply(); }
    public boolean introSeen() { return prefs.getBoolean("intro_seen", false); }
    public void setIntroSeen() { prefs.edit().putBoolean("intro_seen", true).apply(); }
    public void clear() { prefs.edit().clear().apply(); }
}
