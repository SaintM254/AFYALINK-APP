package com.afyalink.app.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Android clears alarms after reboot; rebuild only the user's local schedule. */
public final class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) ReminderScheduler.update(context);
    }
}
