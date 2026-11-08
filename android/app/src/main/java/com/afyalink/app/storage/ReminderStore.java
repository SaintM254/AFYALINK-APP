package com.afyalink.app.storage;

import android.content.Context;
import android.content.SharedPreferences;

/** User-chosen local reminder schedule; contains no measurement data. */
public final class ReminderStore {
    private final SharedPreferences prefs;
    public ReminderStore(Context context) {
        prefs = context.getSharedPreferences("afyalink_reminders_v1", Context.MODE_PRIVATE);
    }
    public boolean enabled() { return prefs.getBoolean("enabled", false); }
    public int hour() { return prefs.getInt("hour", 8); }
    public int minute() { return prefs.getInt("minute", 0); }
    public void save(boolean enabled, int hour, int minute) {
        prefs.edit().putBoolean("enabled", enabled).putInt("hour", hour).putInt("minute", minute).apply();
    }
}
