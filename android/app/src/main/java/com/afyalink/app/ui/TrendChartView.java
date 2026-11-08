package com.afyalink.app.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.afyalink.app.R;
import com.afyalink.app.domain.BloodPressureReading;
import com.afyalink.app.util.Ui;

import java.util.ArrayList;
import java.util.List;

/** Lightweight, accessible two-series trend chart with no external chart SDK. */
public final class TrendChartView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private List<BloodPressureReading> readings = new ArrayList<>();
    private int days = 7;
    private final int systolicColor;
    private final int diastolicColor;
    private final int gridColor;

    public TrendChartView(Context context) { this(context, null); }
    public TrendChartView(Context context, @Nullable AttributeSet attrs) { this(context, attrs, 0); }
    public TrendChartView(Context context, @Nullable AttributeSet attrs, int style) {
        super(context, attrs, style);
        systolicColor = ContextCompat.getColor(context, R.color.brand_teal);
        diastolicColor = ContextCompat.getColor(context, R.color.stage_grade2);
        gridColor = ContextCompat.getColor(context, R.color.hairline);
        if (attrs != null) {
            android.content.res.TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TrendChartView);
            days = a.getInt(R.styleable.TrendChartView_afya_chartDays, 7);
            a.recycle();
        }
        setMinimumHeight(Ui.dp(context, 180));
        setContentDescription("Blood pressure trend chart, systolic and diastolic values");
    }

    public void setReadings(List<BloodPressureReading> values, int rangeDays) {
        readings = values == null ? new ArrayList<>() : new ArrayList<>(values);
        days = rangeDays;
        invalidate();
        StringBuilder desc = new StringBuilder("Blood pressure trends for the last ").append(days).append(" days. ");
        if (readings.isEmpty()) desc.append("No readings.");
        else desc.append(readings.size()).append(" readings.");
        setContentDescription(desc.toString());
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float left = Ui.dpF(getContext(), 34);
        float right = getWidth() - Ui.dpF(getContext(), 10);
        float top = Ui.dpF(getContext(), 12);
        float bottom = getHeight() - Ui.dpF(getContext(), 24);
        if (right <= left || bottom <= top) return;

        paint.setColor(gridColor);
        paint.setStrokeWidth(Ui.dpF(getContext(), 1));
        paint.setStyle(Paint.Style.STROKE);
        for (int i = 0; i < 4; i++) {
            float y = top + (bottom - top) * i / 3f;
            canvas.drawLine(left, y, right, y, paint);
        }
        if (readings.isEmpty()) return;

        int min = 50, max = 200;
        for (BloodPressureReading reading : readings) {
            min = Math.min(min, Math.min(reading.systolic, reading.diastolic) - 5);
            max = Math.max(max, Math.max(reading.systolic, reading.diastolic) + 5);
        }
        if (max - min < 25) { min -= 12; max += 12; }
        drawSeries(canvas, left, right, top, bottom, min, max, true, systolicColor);
        drawSeries(canvas, left, right, top, bottom, min, max, false, diastolicColor);
    }

    private void drawSeries(Canvas canvas, float left, float right, float top, float bottom,
                            int min, int max, boolean systolic, int color) {
        paint.setColor(color);
        paint.setStrokeWidth(Ui.dpF(getContext(), 2.6f));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        path.reset();
        int n = readings.size();
        for (int i = 0; i < n; i++) {
            BloodPressureReading r = readings.get(i);
            float x = n == 1 ? (left + right) / 2f : left + (right - left) * i / (n - 1f);
            int value = systolic ? r.systolic : r.diastolic;
            float y = bottom - (value - min) * (bottom - top) / (max - min);
            if (i == 0) path.moveTo(x, y); else path.lineTo(x, y);
        }
        canvas.drawPath(path, paint);
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < n; i++) {
            BloodPressureReading r = readings.get(i);
            float x = n == 1 ? (left + right) / 2f : left + (right - left) * i / (n - 1f);
            int value = systolic ? r.systolic : r.diastolic;
            float y = bottom - (value - min) * (bottom - top) / (max - min);
            canvas.drawCircle(x, y, Ui.dpF(getContext(), 3.5f), paint);
        }
    }
}
