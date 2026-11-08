package com.afyalink.app.sync;

import com.afyalink.app.domain.BloodPressureReading;

import java.util.List;

/** Pluggable transport boundary. A real implementation requires compliance review and consent. */
public interface IReadingSync {
    SyncReceipt transmit(List<BloodPressureReading> readings) throws Exception;

    final class SyncReceipt {
        public final int acceptedCount;
        public final boolean demoOnly;
        public final String message;
        public SyncReceipt(int acceptedCount, boolean demoOnly, String message) {
            this.acceptedCount = acceptedCount;
            this.demoOnly = demoOnly;
            this.message = message;
        }
    }
}
