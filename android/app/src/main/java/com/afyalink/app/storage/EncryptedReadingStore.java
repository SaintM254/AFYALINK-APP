package com.afyalink.app.storage;

import android.content.Context;
import android.util.AtomicFile;

import com.afyalink.app.domain.BloodPressureReading;
import com.afyalink.app.security.CryptoEngine;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Single encrypted-at-rest vault. Even dates, notes, device source and sync
 * flags are inside the authenticated AES-GCM ciphertext, not file metadata.
 */
public final class EncryptedReadingStore {
    private static final byte[] AAD = "com.afyalink.app.readings.v1".getBytes(StandardCharsets.UTF_8);
    private final Context context;
    private final AtomicFile file;

    public EncryptedReadingStore(Context context) {
        this.context = context.getApplicationContext();
        File dir = new File(this.context.getNoBackupFilesDir(), "vault");
        if (!dir.exists() && !dir.mkdirs()) throw new IllegalStateException("Cannot create private vault directory");
        file = new AtomicFile(new File(dir, "readings.bin"));
    }

    public synchronized List<BloodPressureReading> all() throws Exception {
        if (!file.getBaseFile().exists()) return new ArrayList<>();
        byte[] encrypted;
        try (FileInputStream in = file.openRead(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int n;
            while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
            encrypted = out.toByteArray();
        }
        byte[] plaintext = CryptoEngine.decryptLocal(context, encrypted, AAD);
        try {
            JSONArray array = new JSONArray(new String(plaintext, StandardCharsets.UTF_8));
            List<BloodPressureReading> rows = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.getJSONObject(i);
                rows.add(new BloodPressureReading(o.getString("id"), o.getLong("at"),
                        o.getInt("sys"), o.getInt("dia"), o.getInt("pulse"),
                        o.optString("note", ""), o.optString("source", "manual"),
                        o.optBoolean("synced", false)));
            }
            rows.sort(Comparator.comparingLong((BloodPressureReading r) -> r.measuredAt).reversed());
            return rows;
        } finally {
            java.util.Arrays.fill(plaintext, (byte) 0);
        }
    }

    public synchronized void add(BloodPressureReading reading) throws Exception {
        List<BloodPressureReading> rows = all();
        for (BloodPressureReading row : rows) {
            if (row.id.equals(reading.id)) return;
        }
        rows.add(reading);
        write(rows);
    }

    public synchronized void update(BloodPressureReading reading) throws Exception {
        List<BloodPressureReading> rows = all();
        boolean replaced = false;
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).id.equals(reading.id)) { rows.set(i, reading); replaced = true; break; }
        }
        if (!replaced) rows.add(reading);
        write(rows);
    }

    public synchronized void delete(String id) throws Exception {
        List<BloodPressureReading> rows = all();
        rows.removeIf(row -> row.id.equals(id));
        write(rows);
    }

    public synchronized void markSynced(List<String> ids) throws Exception {
        if (ids == null || ids.isEmpty()) return;
        List<BloodPressureReading> rows = all();
        for (int i = 0; i < rows.size(); i++) {
            if (ids.contains(rows.get(i).id)) rows.set(i, rows.get(i).withSynced(true));
        }
        write(rows);
    }

    public synchronized int pendingCount() throws Exception {
        int count = 0;
        for (BloodPressureReading reading : all()) if (!reading.synced) count++;
        return count;
    }

    public synchronized void clear() throws Exception {
        file.delete();
        new AtomicFile(new File(file.getBaseFile().getParentFile(), "fallback-key.bin")).delete();
    }

    private void write(List<BloodPressureReading> rows) throws Exception {
        JSONArray array = new JSONArray();
        for (BloodPressureReading row : rows) {
            JSONObject o = new JSONObject();
            o.put("id", row.id);
            o.put("at", row.measuredAt);
            o.put("sys", row.systolic);
            o.put("dia", row.diastolic);
            o.put("pulse", row.pulse);
            o.put("note", row.note);
            o.put("source", row.source);
            o.put("synced", row.synced);
            array.put(o);
        }
        byte[] plaintext = array.toString().getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = CryptoEngine.encryptLocal(context, plaintext, AAD);
        FileOutputStream out = null;
        try {
            out = file.startWrite();
            out.write(encrypted);
            out.flush();
            out.getFD().sync();
            file.finishWrite(out);
        } catch (Exception e) {
            if (out != null) file.failWrite(out);
            throw e;
        } finally {
            java.util.Arrays.fill(plaintext, (byte) 0);
            java.util.Arrays.fill(encrypted, (byte) 0);
        }
    }
}
