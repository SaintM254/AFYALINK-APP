package com.afyalink.app.sync;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.afyalink.app.domain.BloodPressureReading;
import com.afyalink.app.storage.EncryptedReadingStore;

import java.util.ArrayList;
import java.util.List;

/** One-shot, offline demo sync job. This worker never opens a network connection. */
public final class PendingSyncWorker extends Worker {
    public static final String KEY_ACCEPTED = "accepted_count";
    public static final String KEY_DEMO_ONLY = "demo_only";

    public PendingSyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull @Override public Result doWork() {
        try {
            EncryptedReadingStore store = new EncryptedReadingStore(getApplicationContext());
            List<BloodPressureReading> all = store.all();
            List<BloodPressureReading> pending = new ArrayList<>();
            List<String> ids = new ArrayList<>();
            for (BloodPressureReading row : all) {
                if (!row.synced) {
                    pending.add(row);
                    ids.add(row.id);
                }
            }
            if (pending.isEmpty()) return Result.success(new androidx.work.Data.Builder()
                    .putInt(KEY_ACCEPTED, 0).putBoolean(KEY_DEMO_ONLY, true).build());
            IReadingSync.SyncReceipt receipt = new DemoRelay().transmit(pending);
            if (!receipt.demoOnly || receipt.acceptedCount != pending.size()) {
                return Result.failure();
            }
            store.markSynced(ids);
            return Result.success(new androidx.work.Data.Builder()
                    .putInt(KEY_ACCEPTED, receipt.acceptedCount)
                    .putBoolean(KEY_DEMO_ONLY, true).build());
        } catch (Exception e) {
            return Result.failure();
        }
    }
}
