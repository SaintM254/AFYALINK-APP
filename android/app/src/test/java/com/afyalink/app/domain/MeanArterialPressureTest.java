package com.afyalink.app.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MeanArterialPressureTest {
    @Test public void computesMapUsingSystolicPlusTwiceDiastolic() {
        BloodPressureReading reading = BloodPressureReading.create(1L, 120, 80, 0, "", "manual");
        assertEquals(280.0 / 3.0, reading.meanArterialPressure(), 0.0001);
        assertEquals(93, reading.meanArterialPressureRounded());
    }

    @Test public void roundsMapToTheNearestWholeMmHg() {
        BloodPressureReading reading = BloodPressureReading.create(1L, 119, 85, 0, "", "manual");
        assertEquals(96, reading.meanArterialPressureRounded());
    }
}
