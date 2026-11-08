package com.afyalink.app.util;

import com.afyalink.app.domain.BloodPressureReading;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public final class Charts {
    private Charts() {}
    public static List<BloodPressureReading> since(List<BloodPressureReading> readings, int days) {
        long cutoff = System.currentTimeMillis() - days * 86_400_000L;
        List<BloodPressureReading> result = new ArrayList<>();
        for (BloodPressureReading r : readings) if (r.measuredAt >= cutoff) result.add(r);
        result.sort((a,b) -> Long.compare(a.measuredAt, b.measuredAt));
        return result;
    }
    public static Stats stats(List<BloodPressureReading> readings) {
        if (readings.isEmpty()) return new Stats(0, 0, 0, 0, 0, 0);
        double s=0,d=0; int minS=Integer.MAX_VALUE, maxS=Integer.MIN_VALUE;
        for (BloodPressureReading r : readings) { s += r.systolic; d += r.diastolic; minS=Math.min(minS,r.systolic); maxS=Math.max(maxS,r.systolic); }
        return new Stats(readings.size(), s/readings.size(), d/readings.size(), minS, maxS,
                readings.get(readings.size()-1).measuredAt - readings.get(0).measuredAt);
    }
    public static final class Stats {
        public final int count; public final double systolic, diastolic; public final int min, max; public final long span;
        Stats(int count, double systolic, double diastolic, int min, int max, long span) {
            this.count=count; this.systolic=systolic; this.diastolic=diastolic; this.min=min; this.max=max; this.span=span;
        }
    }
}
