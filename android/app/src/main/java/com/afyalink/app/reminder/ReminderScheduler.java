package com.afyalink.app.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.afyalink.app.storage.ReminderStore;

import java.util.Calendar;

/** Schedules an inexact daily local notification; no server is involved. */
public final class ReminderScheduler {
    public static final int REQUEST_ID = 8417;
    private ReminderScheduler() {}

    public static void update(Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pending = PendingIntent.getBroadcast(context, REQUEST_ID, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        manager.cancel(pending);
        ReminderStore store = new ReminderStore(context);
        if (!store.enabled()) return;
        Calendar next = Calendar.getInstance();
        next.set(Calendar.HOUR_OF_DAY, store.hour());
        next.set(Calendar.MINUTE, store.minute());
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        if (!next.after(Calendar.getInstance())) next.add(Calendar.DAY_OF_YEAR, 1);
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pending);
    }
}
