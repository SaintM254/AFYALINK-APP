package com.afyalink.app.reminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.afyalink.app.R;
import com.afyalink.app.storage.ReminderStore;
import com.afyalink.app.ui.MainActivity;

public final class ReminderReceiver extends BroadcastReceiver {
    public static final String CHANNEL = "afyalink_measurement_reminders_v1";
    @Override public void onReceive(Context context, Intent intent) {
        if (!new ReminderStore(context).enabled()) return;
        createChannel(context);
        Intent open = new Intent(context, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent tap = PendingIntent.getActivity(context, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        androidx.core.app.NotificationCompat.Builder notification = new NotificationCompat.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.ic_bell)
                .setContentTitle(context.getString(R.string.reminders_notif_title))
                .setContentText(context.getString(R.string.reminders_notif_body))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.reminders_notif_body)))
                .setContentIntent(tap)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setColor(context.getColor(R.color.brand_teal));
        try { NotificationManagerCompat.from(context).notify(8417, notification.build()); }
        catch (SecurityException ignored) { /* Notification permission was not granted. */ }
    }

    private void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager.getNotificationChannel(CHANNEL) == null) {
                manager.createNotificationChannel(new NotificationChannel(CHANNEL,
                        context.getString(R.string.reminders_channel_name), NotificationManager.IMPORTANCE_DEFAULT));
            }
        }
    }
}
