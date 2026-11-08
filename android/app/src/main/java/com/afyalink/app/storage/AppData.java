package com.afyalink.app.storage;

import android.content.Context;

import com.afyalink.app.domain.BloodPressureReading;
import com.afyalink.app.util.Charts;
import com.afyalink.app.util.Format;

import java.util.ArrayList;
import java.util.List;

/** Safe access boundary for screen code; screens can render a clean empty state on vault failure. */
public final class AppData {
    private AppData() {}
    public static List<BloodPressureReading> readings(Context c) {
        try { return new EncryptedReadingStore(c).all(); }
        catch (Exception e) { return new ArrayList<>(); }
    }
    public static Format.ListStats stats(List<BloodPressureReading> rows) {
        List<BloodPressureReading> week = Charts.since(rows, 7);
        if (week.isEmpty()) return new Format.ListStats(0, 0, 0);
        double sys=0, dia=0;
        for (BloodPressureReading r : week) { sys += r.systolic; dia += r.diastolic; }
        return new Format.ListStats(week.size(), sys / week.size(), dia / week.size());
    }
}
