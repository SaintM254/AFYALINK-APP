package com.afyalink.app.domain;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;

import com.afyalink.app.R;

/**
 * Informational home-monitoring bands based on common WHO/ISH thresholds.
 * These are educational cues, not diagnosis or a substitute for clinical advice.
 * The crisis band is intentionally a separate urgent-care prompt.
 */
public enum BpStage {
    LOW(R.string.stage_low, R.color.stage_low_soft, R.color.stage_low_ink, 0),
    OPTIMAL(R.string.stage_optimal, R.color.stage_normal_soft, R.color.stage_normal_ink, 1),
    NORMAL(R.string.stage_normal, R.color.stage_normal_soft, R.color.stage_normal_ink, 2),
    ELEVATED(R.string.stage_elevated, R.color.stage_elevated_soft, R.color.stage_elevated_ink, 3),
    GRADE_1(R.string.stage_grade1, R.color.stage_grade1_soft, R.color.stage_grade1_ink, 4),
    GRADE_2(R.string.stage_grade2, R.color.stage_grade2_soft, R.color.stage_grade2_ink, 5),
    GRADE_3(R.string.stage_grade3, R.color.stage_grade3_soft, R.color.stage_grade3_ink, 6),
    CRISIS(R.string.stage_crisis, R.color.stage_grade3_soft, R.color.stage_grade3_ink, 7);

    @StringRes public final int labelRes;
    @ColorRes public final int softColorRes;
    @ColorRes public final int inkColorRes;
    public final int severity;

    BpStage(int labelRes, int softColorRes, int inkColorRes, int severity) {
        this.labelRes = labelRes;
        this.softColorRes = softColorRes;
        this.inkColorRes = inkColorRes;
        this.severity = severity;
    }

    public static BpStage classify(int systolic, int diastolic) {
        if (systolic <= 0 || diastolic <= 0) return NORMAL;
        if (systolic >= 180 || diastolic >= 120) return CRISIS;
        if (systolic < 90 || diastolic < 60) return LOW;
        // When the two numbers are in different bands, the higher category wins.
        if (systolic >= 180 || diastolic >= 110) return GRADE_3;
        if (systolic >= 160 || diastolic >= 100) return GRADE_2;
        if (systolic >= 140 || diastolic >= 90) return GRADE_1;
        if (systolic >= 130 || diastolic >= 85) return ELEVATED;
        if (systolic < 120 && diastolic < 80) return OPTIMAL;
        return NORMAL;
    }

    public boolean isUrgent() {
        return this == CRISIS;
    }

    public boolean isHigh() {
        return severity >= GRADE_1.severity;
    }
}
