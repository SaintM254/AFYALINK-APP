package com.afyalink.app.monitor;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.ParcelUuid;

import androidx.core.content.ContextCompat;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * BLE adapter for the Bluetooth SIG Blood Pressure Service (0x1810) and Blood
 * Pressure Measurement characteristic (0x2A35). It is local-only and stores no
 * device address. Cuffs using a vendor-only protocol need their vendor adapter.
 */
public final class BleBloodPressureMonitor implements IBloodPressureMonitor {
    public static final UUID SERVICE_UUID = UUID.fromString("00001810-0000-1000-8000-00805f9b34fb");
    public static final UUID MEASUREMENT_UUID = UUID.fromString("00002a35-0000-1000-8000-00805f9b34fb");
    private static final UUID CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final byte[] INDICATE = new byte[]{0x02, 0x00};
    private static final byte[] NOTIFY = new byte[]{0x01, 0x00};

    private final Context context;
    private final Listener listener;
    private final BluetoothAdapter adapter;
    private BluetoothLeScanner scanner;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic measurementCharacteristic;
    private boolean scanning;

    public BleBloodPressureMonitor(Context context, Listener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = manager == null ? null : manager.getAdapter();
    }

    @Override public void startScan() {
        if (!hasScanPermission()) { listener.onError("Bluetooth permission is required to scan for cuffs."); return; }
        if (!hasConnectPermission()) { listener.onError("Bluetooth connect permission is required to check the adapter state."); return; }
        if (adapter == null || !adapter.isEnabled()) { listener.onError("Turn on Bluetooth, then scan again."); return; }
        scanner = adapter.getBluetoothLeScanner();
        if (scanner == null) { listener.onError("Bluetooth scanning is unavailable on this device."); return; }
        try {
            ScanFilter filter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(SERVICE_UUID)).build();
            ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
            scanner.startScan(Collections.singletonList(filter), settings, scanCallback);
            scanning = true;
            listener.onState("Scanning for standard Bluetooth blood-pressure cuffs…");
        } catch (SecurityException e) {
            listener.onError("Bluetooth scan permission was not granted.");
        }
    }

    @Override public void stopScan() {
        if (scanner == null || !scanning) return;
        try { if (hasScanPermission()) scanner.stopScan(scanCallback); }
        catch (SecurityException ignored) { }
        scanning = false;
    }

    @Override public void connect(String address) {
        stopScan();
        if (!hasConnectPermission()) { listener.onError("Bluetooth connect permission was not granted."); return; }
        if (adapter == null) { listener.onError("Bluetooth is unavailable."); return; }
        try {
            BluetoothDevice device = adapter.getRemoteDevice(address);
            listener.onState("Connecting to " + safeName(device) + "…");
            gatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE);
        } catch (IllegalArgumentException | SecurityException e) {
            listener.onError("Could not connect to this cuff. Keep it nearby and try again.");
        }
    }

    @Override public void disconnect() {
        stopScan();
        try { if (gatt != null) { gatt.disconnect(); gatt.close(); } }
        catch (SecurityException ignored) { }
        gatt = null;
        measurementCharacteristic = null;
        listener.onState("Disconnected");
    }

    @Override public void close() {
        stopScan();
        try { if (gatt != null) { gatt.disconnect(); gatt.close(); } }
        catch (SecurityException ignored) { }
        gatt = null;
        measurementCharacteristic = null;
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            String address = device.getAddress();
            String name = safeName(device);
            listener.onDevice(address, name.isEmpty() ? "Blood pressure monitor" : name);
        }
        @Override public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, result);
        }
        @Override public void onScanFailed(int errorCode) {
            scanning = false;
            listener.onError("Bluetooth scan failed (" + errorCode + "). Try again.");
        }
    };

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override public void onConnectionStateChange(BluetoothGatt bluetoothGatt, int status, int newState) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                listener.onError("Cuff connection ended. Keep it awake and try again.");
                close();
                return;
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                listener.onState("Connected. Checking the standard pressure service…");
                if (hasConnectPermission()) bluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                listener.onState("Disconnected");
                try { bluetoothGatt.close(); } catch (SecurityException ignored) { }
                if (gatt == bluetoothGatt) gatt = null;
            }
        }

        @Override public void onServicesDiscovered(BluetoothGatt bluetoothGatt, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) { listener.onError("Could not read cuff services."); return; }
            BluetoothGattService service = bluetoothGatt.getService(SERVICE_UUID);
            if (service == null) { listener.onError("This cuff does not offer the standard BP service."); return; }
            measurementCharacteristic = service.getCharacteristic(MEASUREMENT_UUID);
            if (measurementCharacteristic == null) { listener.onError("The cuff does not expose a standard measurement characteristic."); return; }
            try {
                boolean enabled = bluetoothGatt.setCharacteristicNotification(measurementCharacteristic, true);
                BluetoothGattDescriptor descriptor = measurementCharacteristic.getDescriptor(CCCD_UUID);
                if (!enabled || descriptor == null) { listener.onError("The cuff cannot send measurement indications."); return; }
                byte[] value = (measurementCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0 ? INDICATE : NOTIFY;
                if (Build.VERSION.SDK_INT >= 33) bluetoothGatt.writeDescriptor(descriptor, value);
                else { descriptor.setValue(value); bluetoothGatt.writeDescriptor(descriptor); }
            } catch (SecurityException e) { listener.onError("Bluetooth permission was revoked."); }
        }

        @Override public void onDescriptorWrite(BluetoothGatt bluetoothGatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) listener.onState("Ready. Start a measurement on your cuff; it will be saved encrypted here.");
            else listener.onError("Could not enable cuff measurement notifications.");
        }

        @Override public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic) {
            if (MEASUREMENT_UUID.equals(characteristic.getUuid())) parseMeasurement(characteristic.getValue());
        }

        @Override public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic, byte[] value) {
            if (MEASUREMENT_UUID.equals(characteristic.getUuid())) parseMeasurement(value);
        }
    };

    private void parseMeasurement(byte[] value) {
        try {
            if (value == null || value.length < 7) { listener.onError("The cuff sent an incomplete measurement."); return; }
            int flags = value[0] & 0xff;
            int offset = 1;
            double sys = sfloat(value, offset); offset += 2;
            double dia = sfloat(value, offset); offset += 2;
            offset += 2; // mean arterial pressure
            if ((flags & 0x02) != 0) offset += 7; // optional date and time
            int pulse = 0;
            if ((flags & 0x04) != 0) {
                if (offset + 2 > value.length) { listener.onError("The cuff sent an incomplete pulse value."); return; }
                double rate = sfloat(value, offset); offset += 2;
                if (Double.isFinite(rate) && rate >= 20 && rate <= 250) pulse = (int) Math.round(rate);
            }
            if ((flags & 0x08) != 0) offset += 1; // optional user id
            if ((flags & 0x10) != 0) offset += 2; // optional measurement status
            if ((flags & 0x01) != 0) { sys *= 7.50062; dia *= 7.50062; } // kPa -> mmHg
            int systolic = (int) Math.round(sys);
            int diastolic = (int) Math.round(dia);
            if (systolic < 40 || systolic > 300 || diastolic < 20 || diastolic > 200 || systolic <= diastolic) {
                listener.onError("The cuff reading was outside the supported range; it was not saved.");
                return;
            }
            listener.onMeasurement(systolic, diastolic, pulse);
        } catch (RuntimeException e) {
            listener.onError("Could not decode the cuff reading; it was not saved.");
        }
    }

    static double sfloat(byte[] data, int offset) {
        if (offset + 1 >= data.length) throw new IllegalArgumentException("short sfloat");
        int raw = (data[offset] & 0xff) | ((data[offset + 1] & 0xff) << 8);
        int mantissa = raw & 0x0fff;
        int exponent = (raw >> 12) & 0x0f;
        if (mantissa >= 0x0800) mantissa -= 0x1000;
        if (exponent >= 0x08) exponent -= 0x10;
        // IEEE-11073 special NaN / NRes / reserved values are not readings.
        if ((raw & 0x0fff) >= 0x07fe && (raw & 0x0fff) <= 0x0800) return Double.NaN;
        return mantissa * Math.pow(10, exponent);
    }

    private String safeName(BluetoothDevice device) {
        try { return device.getName() == null ? "" : device.getName().trim(); }
        catch (SecurityException e) { return ""; }
    }
    private boolean hasScanPermission() {
        if (Build.VERSION.SDK_INT >= 31) return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    private boolean hasConnectPermission() {
        return Build.VERSION.SDK_INT < 31 || ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
    }
}
