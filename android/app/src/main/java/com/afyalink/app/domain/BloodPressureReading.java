package com.afyalink.app.domain;

import androidx.annotation.NonNull;

import java.util.Objects;
import java.util.UUID;

/** A single immutable home blood-pressure measurement. Values are mmHg / bpm. */
public final class BloodPressureReading {
    @NonNull public final String id;
    public final long measuredAt;
    public final int systolic;
    public final int diastolic;
    public final int pulse;
    @NonNull public final String note;
    @NonNull public final String source;
    public final boolean synced;

    public BloodPressureReading(@NonNull String id, long measuredAt, int systolic,
                                int diastolic, int pulse, @NonNull String note,
                                @NonNull String source, boolean synced) {
        if (systolic < 40 || systolic > 300) throw new IllegalArgumentException("Systolic must be 40–300 mmHg");
        if (diastolic < 20 || diastolic > 200) throw new IllegalArgumentException("Diastolic must be 20–200 mmHg");
        if (pulse != 0 && (pulse < 20 || pulse > 250)) throw new IllegalArgumentException("Pulse must be 20–250 bpm or 0 when unknown");
        if (measuredAt <= 0) throw new IllegalArgumentException("Measurement time must be positive");
        this.id = id;
        this.measuredAt = measuredAt;
        this.systolic = systolic;
        this.diastolic = diastolic;
        this.pulse = pulse;
        this.note = note == null ? "" : note.trim();
        this.source = source == null || source.trim().isEmpty() ? "manual" : source.trim();
        this.synced = synced;
    }

    public static BloodPressureReading create(long measuredAt, int systolic, int diastolic,
                                               int pulse, @NonNull String note,
                                               @NonNull String source) {
        return new BloodPressureReading(UUID.randomUUID().toString(), measuredAt,
                systolic, diastolic, pulse, note, source, false);
    }

    public BpStage stage() {
        return BpStage.classify(systolic, diastolic);
    }

    /** Estimated mean arterial pressure in mmHg: (SBP + 2 × DBP) / 3. */
    public double meanArterialPressure() {
        return (systolic + 2.0 * diastolic) / 3.0;
    }

    /** Rounded whole-mmHg value for compact UI display. */
    public int meanArterialPressureRounded() {
        return (int) Math.round(meanArterialPressure());
    }

    public BloodPressureReading withSynced(boolean value) {
        return new BloodPressureReading(id, measuredAt, systolic, diastolic, pulse, note, source, value);
    }

    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof BloodPressureReading)) return false;
        BloodPressureReading that = (BloodPressureReading) other;
        return measuredAt == that.measuredAt && systolic == that.systolic && diastolic == that.diastolic
                && pulse == that.pulse && synced == that.synced && id.equals(that.id)
                && note.equals(that.note) && source.equals(that.source);
    }

    @Override public int hashCode() {
        return Objects.hash(id, measuredAt, systolic, diastolic, pulse, note, source, synced);
    }
}
