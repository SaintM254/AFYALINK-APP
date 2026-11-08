package com.afyalink.app.monitor;

/** Monitor adapter boundary; v1 implements the Bluetooth SIG Blood Pressure Service. */
public interface IBloodPressureMonitor {
    void startScan();
    void stopScan();
    void connect(String address);
    void disconnect();
    void close();

    interface Listener {
        void onDevice(String address, String displayName);
        void onState(String message);
        void onMeasurement(int systolicMmHg, int diastolicMmHg, int pulseBpm);
        void onError(String message);
    }
}
