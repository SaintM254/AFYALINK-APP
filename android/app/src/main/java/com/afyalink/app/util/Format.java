package com.afyalink.app.util;

import android.content.Context;
import android.text.format.DateFormat;

import com.afyalink.app.R;
import com.afyalink.app.domain.BloodPressureReading;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class Format {
    private Format() {}
    public static String pressure(int systolic, int diastolic) { return systolic + "/" + diastolic; }
    public static String pulse(int pulse) { return pulse + " bpm"; }
    public static String date(Context context, long time) {
        String pattern = DateFormat.is24HourFormat(context) ? "EEE, d MMM · HH:mm" : "EEE, d MMM · h:mm a";
        return new SimpleDateFormat(pattern, Locale.getDefault()).format(new Date(time));
    }
    public static String time(Context context, long time) {
        return DateFormat.getTimeFormat(context).format(new Date(time));
    }
    public static String relativeDate(Context context, long time) {
        java.text.DateFormat df = DateFormat.getMediumDateFormat(context);
        return df.format(new Date(time));
    }
    public static String greeting(Context context) {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if (hour < 12) return context.getString(R.string.dash_greeting_morning);
        if (hour < 18) return context.getString(R.string.dash_greeting_afternoon);
        return context.getString(R.string.dash_greeting_evening);
    }
    public static String average(ListStats stats) {
        if (stats.count == 0) return "—";
        return pressure((int) Math.round(stats.meanSys), (int) Math.round(stats.meanDia));
    }
    public static final class ListStats {
        public final int count; public final double meanSys; public final double meanDia;
        public ListStats(int count, double meanSys, double meanDia) {
            this.count = count; this.meanSys = meanSys; this.meanDia = meanDia;
        }
    }
}
