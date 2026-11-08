package com.afyalink.app.sync;

import com.afyalink.app.domain.BloodPressureReading;
import com.afyalink.app.security.CryptoEngine;
import com.afyalink.app.security.SessionCipher;
import com.afyalink.app.security.VaultKeyManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Offline-only demonstration transport. It envelope-encrypts a batch with a
 * device-local RSA demo key, verifies the round-trip, then discards it. No
 * socket, cloud endpoint, analytics or third party receives the readings.
 */
public final class DemoRelay implements IReadingSync {
    @Override public SyncReceipt transmit(List<BloodPressureReading> readings) throws Exception {
        if (readings == null || readings.isEmpty()) return new SyncReceipt(0, true, "Nothing queued");
        JSONArray batch = new JSONArray();
        for (BloodPressureReading row : readings) {
            if (row.synced) continue;
            JSONObject item = new JSONObject();
            item.put("id", row.id);
            item.put("measuredAt", row.measuredAt);
            item.put("systolicMmHg", row.systolic);
            item.put("diastolicMmHg", row.diastolic);
            item.put("pulseBpm", row.pulse);
            item.put("note", row.note);
            item.put("source", row.source);
            batch.put(item);
        }
        byte[] plaintext = batch.toString().getBytes(StandardCharsets.UTF_8);
        String context = "afyalink-demo-relay-v1";
        java.security.KeyPair localDemo = VaultKeyManager.demoExchangeKeyPair();
        CryptoEngine.Envelope envelope = SessionCipher.seal(plaintext, context, localDemo.getPublic());
        byte[] verified = SessionCipher.openForDemo(envelope, context, localDemo);
        boolean match = java.util.Arrays.equals(verified, batch.toString().getBytes(StandardCharsets.UTF_8));
        java.util.Arrays.fill(verified, (byte) 0);
        java.util.Arrays.fill(plaintext, (byte) 0);
        if (!match) throw new SecurityException("Demo envelope verification failed");
        return new SyncReceipt(batch.length(), true,
                "Encrypted demo envelope verified locally; no data left this device.");
    }
}
