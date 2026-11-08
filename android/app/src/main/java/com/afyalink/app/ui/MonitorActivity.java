package com.afyalink.app.ui;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;

import com.afyalink.app.R;
import com.afyalink.app.domain.BloodPressureReading;
import com.afyalink.app.monitor.BleBloodPressureMonitor;
import com.afyalink.app.monitor.IBloodPressureMonitor;
import com.afyalink.app.storage.EncryptedReadingStore;
import com.afyalink.app.util.Lucide;
import com.afyalink.app.util.Ui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Pairs with standard Bluetooth SIG Blood Pressure Service cuffs; no internet is used. */
public final class MonitorActivity extends BaseActivity implements IBloodPressureMonitor.Listener {
    private static final int REQUEST_BLUETOOTH = 712;
    private static final int REQUEST_ENABLE_BLUETOOTH = 713;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Map<String, String> devices = new LinkedHashMap<>();
    private BleBloodPressureMonitor monitor;
    private LinearLayout deviceList;
    private TextView status;
    private AppCompatImageButton scanButton;
    private boolean scanning;

    @Override protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        monitor = new BleBloodPressureMonitor(this, this);
        render();
    }

    @Override protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (monitor != null) monitor.close();
        super.onDestroy();
    }

    private void render() {
        installPage(getString(R.string.monitor_title), true);
        content.addView(ScreenUi.eyebrow(this, getString(R.string.monitor_eyebrow)));
        TextView title = ScreenUi.title(this, getString(R.string.monitor_title));
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(-1, -2);
        tp.topMargin = Ui.dp(this, 7);
        content.addView(title, tp);

        LinearLayout intro = ScreenUi.card(this, Ui.color(this, R.color.brand_teal_soft), false);
        intro.addView(ScreenUi.paragraph(this, getString(R.string.monitor_intro)));
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(-1, -2);
        ip.topMargin = Ui.dp(this, 14);
        ip.bottomMargin = Ui.dp(this, 14);
        content.addView(intro, ip);

        LinearLayout scanCard = ScreenUi.card(this);
        LinearLayout scanRow = ScreenUi.horizontal(this);
        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        TextView section = ScreenUi.text(this, getString(R.string.monitor_devices), 16,
                Ui.color(this, R.color.text_primary), true);
        copy.addView(section);
        status = ScreenUi.paragraph(this, getString(R.string.monitor_none));
        status.setTextSize(12);
        LinearLayout.LayoutParams st = new LinearLayout.LayoutParams(-1, -2);
        st.topMargin = Ui.dp(this, 5);
        copy.addView(status, st);
        scanRow.addView(copy, new LinearLayout.LayoutParams(0, -2, 1f));
        scanButton = new AppCompatImageButton(this);
        scanButton.setImageResource(Lucide.SYNC);
        scanButton.setImageTintList(android.content.res.ColorStateList.valueOf(Ui.color(this, R.color.brand_white)));
        scanButton.setBackground(Ui.roundedBox(this, Ui.color(this, R.color.brand_teal), 17));
        scanButton.setContentDescription(getString(R.string.monitor_scan));
        scanButton.setPadding(Ui.dp(this, 15), Ui.dp(this, 15), Ui.dp(this, 15), Ui.dp(this, 15));
        scanRow.addView(scanButton, new LinearLayout.LayoutParams(Ui.dp(this, 54), Ui.dp(this, 54)));
        scanCard.addView(scanRow);
        deviceList = new LinearLayout(this);
        deviceList.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams dl = new LinearLayout.LayoutParams(-1, -2);
        dl.topMargin = Ui.dp(this, 10);
        scanCard.addView(deviceList, dl);
        LinearLayout.LayoutParams sc = new LinearLayout.LayoutParams(-1, -2);
        sc.bottomMargin = Ui.dp(this, 14);
        content.addView(scanCard, sc);
        scanButton.setOnClickListener(v -> scanOrStop());

        LinearLayout manual = ScreenUi.card(this);
        manual.addView(ScreenUi.text(this, getString(R.string.monitor_manual_title), 16,
                Ui.color(this, R.color.text_primary), true));
        TextView body = ScreenUi.paragraph(this, getString(R.string.monitor_manual_body));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, -2);
        bp.topMargin = Ui.dp(this, 8);
        manual.addView(body, bp);
        AppCompatImageButton addManual = new AppCompatImageButton(this);
        addManual.setImageResource(Lucide.ADD);
        addManual.setImageTintList(android.content.res.ColorStateList.valueOf(Ui.color(this, R.color.brand_teal)));
        addManual.setBackgroundResource(android.R.drawable.list_selector_background);
        addManual.setContentDescription(getString(R.string.cd_add_reading));
        addManual.setPadding(Ui.dp(this, 12), Ui.dp(this, 12), Ui.dp(this, 12), Ui.dp(this, 12));
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(Ui.dp(this, 48), Ui.dp(this, 48));
        ap.gravity = Gravity.END;
        manual.addView(addManual, ap);
        addManual.setOnClickListener(v -> go(LogReadingActivity.class));
        LinearLayout.LayoutParams mcp = new LinearLayout.LayoutParams(-1, -2);
        content.addView(manual, mcp);
    }

    private void scanOrStop() {
        if (scanning) {
            monitor.stopScan();
            scanning = false;
            scanButton.setImageResource(Lucide.SYNC);
            status.setText(getString(R.string.monitor_scan_again));
            return;
        }
        if (!hasPermissions()) {
            if (Build.VERSION.SDK_INT >= 31) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH);
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_BLUETOOTH);
            }
            return;
        }
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = manager == null ? null : manager.getAdapter();
        if (adapter == null) { status.setText(getString(R.string.monitor_turn_on_bluetooth)); return; }
        try {
            if (!adapter.isEnabled()) {
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BLUETOOTH);
                return;
            }
        } catch (SecurityException e) {
            status.setText(getString(R.string.monitor_permission));
            return;
        }
        devices.clear();
        deviceList.removeAllViews();
        monitor.startScan();
        scanning = true;
        scanButton.setImageResource(Lucide.CLOSE);
        handler.postDelayed(() -> {
            if (scanning) {
                monitor.stopScan();
                scanning = false;
                scanButton.setImageResource(Lucide.SYNC);
                if (devices.isEmpty()) status.setText(getString(R.string.monitor_none));
            }
        }, 15_000);
    }

    private boolean hasPermissions() {
        if (Build.VERSION.SDK_INT >= 31) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        }
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == REQUEST_BLUETOOTH) {
            boolean granted = results.length > 0;
            for (int result : results) granted &= result == PackageManager.PERMISSION_GRANTED;
            if (granted) scanOrStop(); else status.setText(getString(R.string.monitor_permission));
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BLUETOOTH && resultCode == RESULT_OK) scanOrStop();
        else if (requestCode == REQUEST_ENABLE_BLUETOOTH) status.setText(getString(R.string.monitor_turn_on_bluetooth));
    }

    @Override public void onDevice(String address, String displayName) {
        runOnUiThread(() -> {
            if (devices.containsKey(address)) return;
            devices.put(address, displayName);
            if (scanning) { monitor.stopScan(); scanning = false; scanButton.setImageResource(Lucide.SYNC); }
            TextView row = ScreenUi.text(this, displayName, 15, Ui.color(this, R.color.text_primary), true);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setMinHeight(Ui.dp(this, 54));
            row.setPadding(Ui.dp(this, 8), 0, Ui.dp(this, 8), 0);
            row.setBackgroundResource(android.R.drawable.list_selector_background);
            row.setContentDescription("Connect to " + displayName);
            deviceList.addView(row, new LinearLayout.LayoutParams(-1, -2));
            row.setOnClickListener(v -> {
                status.setText("Connecting…");
                monitor.connect(address);
            });
            status.setText(devices.size() == 1 ? "Choose a nearby cuff to connect." : devices.size() + " compatible cuffs found.");
        });
    }

    @Override public void onState(String message) {
        runOnUiThread(() -> { if (status != null) status.setText(message); });
    }

    @Override public void onMeasurement(int systolic, int diastolic, int pulse) {
        runOnUiThread(() -> {
            try {
                BloodPressureReading reading = BloodPressureReading.create(System.currentTimeMillis(),
                        systolic, diastolic, pulse, "", "bluetooth-standard");
                new EncryptedReadingStore(this).add(reading);
                Toast.makeText(this, R.string.monitor_saved, Toast.LENGTH_LONG).show();
                status.setText("Saved " + systolic + "/" + diastolic + " mmHg encrypted on this device.");
            } catch (Exception e) {
                status.setText(getString(R.string.sync_failed));
            }
        });
    }

    @Override public void onError(String message) {
        runOnUiThread(() -> {
            if (status != null) status.setText(message);
            if (message.contains("does not offer") || message.contains("does not expose"))
                Toast.makeText(this, R.string.monitor_not_supported, Toast.LENGTH_LONG).show();
        });
    }
}
