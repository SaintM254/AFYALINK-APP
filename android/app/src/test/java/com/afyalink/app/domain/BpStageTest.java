package com.afyalink.app.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BpStageTest {
    @Test public void classifiesOptimalNormalAndHighNormalBands() {
        assertEquals(BpStage.OPTIMAL, BpStage.classify(119, 79));
        assertEquals(BpStage.NORMAL, BpStage.classify(125, 82));
        assertEquals(BpStage.HIGH_NORMAL, BpStage.classify(135, 87));
    }

    @Test public void higherCategoryWinsWhenPressureNumbersDiffer() {
        assertEquals(BpStage.GRADE_1, BpStage.classify(138, 92));
        assertEquals(BpStage.GRADE_2, BpStage.classify(160, 85));
        assertEquals(BpStage.GRADE_3, BpStage.classify(179, 110));
    }

    @Test public void separatesLowAndCrisisBands() {
        assertEquals(BpStage.LOW, BpStage.classify(88, 65));
        assertEquals(BpStage.CRISIS, BpStage.classify(181, 90));
        assertEquals(BpStage.CRISIS, BpStage.classify(150, 120));
    }
}
