package com.afyalink.app.monitor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class BleBloodPressureMonitorTest {
    @Test public void decodesIeee11073SfloatLittleEndian() {
        assertEquals(120.0, BleBloodPressureMonitor.sfloat(new byte[]{0x78, 0x00}, 0), 0.0001);
        assertEquals(1.2, BleBloodPressureMonitor.sfloat(new byte[]{0x0c, (byte)0xf0}, 0), 0.0001);
        assertEquals(-1.2, BleBloodPressureMonitor.sfloat(new byte[]{(byte)0xf4, (byte)0xff}, 0), 0.0001);
    }

    @Test public void rejectsReservedSpecialFloatValues() {
        assertFalse(Double.isFinite(BleBloodPressureMonitor.sfloat(new byte[]{(byte)0xfe, 0x07}, 0)));
        assertFalse(Double.isFinite(BleBloodPressureMonitor.sfloat(new byte[]{(byte)0xff, 0x07}, 0)));
    }
}
