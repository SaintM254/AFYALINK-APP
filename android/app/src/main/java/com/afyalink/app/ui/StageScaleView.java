package com.afyalink.app.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.afyalink.app.R;
import com.afyalink.app.domain.BpStage;
import com.afyalink.app.util.Ui;

/** Compact WHO/ISH progression scale, drawn as a calm stage strip. */
public final class StageScaleView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private boolean showLabels = false;
    private int markerValue = 0;
    private final int[] colors;
    private final String[] labels;

    public StageScaleView(Context context) { this(context, null); }
    public StageScaleView(Context context, @Nullable AttributeSet attrs) { this(context, attrs, 0); }
    public StageScaleView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        colors = new int[] {
                ContextCompat.getColor(context, R.color.stage_low),
                ContextCompat.getColor(context, R.color.stage_normal),
                ContextCompat.getColor(context, R.color.stage_elevated),
                ContextCompat.getColor(context, R.color.stage_grade1),
                ContextCompat.getColor(context, R.color.stage_grade2),
                ContextCompat.getColor(context, R.color.stage_grade3)
        };
        labels = new String[] {"Low", "Normal", "High normal", "Grade 1", "Grade 2", "Grade 3+"};
        if (attrs != null) {
            android.content.res.TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StageScaleView);
            showLabels = a.getBoolean(R.styleable.StageScaleView_afya_showLabels, false);
            markerValue = a.getInt(R.styleable.StageScaleView_afya_markerValue, 0);
            a.recycle();
        }
        setContentDescription("Blood pressure category scale from low, normal, and elevated to severe hypertension");
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public void setStage(@Nullable BpStage stage) {
        if (stage == null) markerValue = 0;
        else switch (stage) {
            case LOW: markerValue = 1; break;
            case OPTIMAL:
            case NORMAL: markerValue = 2; break;
            case HIGH_NORMAL: markerValue = 3; break;
            case GRADE_1: markerValue = 4; break;
            case GRADE_2: markerValue = 5; break;
            case GRADE_3:
            case CRISIS: markerValue = 6; break;
        }
        invalidate();
    }

    public void setShowLabels(boolean show) { showLabels = show; requestLayout(); invalidate(); }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int desired = Ui.dp(getContext(), showLabels ? 54 : 14);
        setMeasuredDimension(width, resolveSize(desired, heightMeasureSpec));
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float gap = Ui.dpF(getContext(), 3);
        float left = getPaddingLeft();
        float right = getWidth() - getPaddingRight();
        float top = Ui.dpF(getContext(), showLabels ? 1 : 2);
        float barHeight = Ui.dpF(getContext(), 9);
        float segment = (right - left - gap * (colors.length - 1)) / colors.length;
        for (int i = 0; i < colors.length; i++) {
            float x = left + i * (segment + gap);
            rect.set(x, top, x + segment, top + barHeight);
            paint.setColor(colors[i]);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(rect, barHeight / 2, barHeight / 2, paint);
            if (showLabels) {
                paint.setColor(ContextCompat.getColor(getContext(), R.color.text_tertiary));
                paint.setTextSize(Ui.dpF(getContext(), 9));
                paint.setTypeface(android.graphics.Typeface.create("sans-serif-medium", 0));
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(labels[i], x + segment / 2, Ui.dpF(getContext(), 29), paint);
            }
        }
        if (markerValue > 0) {
            int i = Math.min(colors.length - 1, markerValue - 1);
            float x = left + i * (segment + gap) + segment / 2;
            paint.setColor(ContextCompat.getColor(getContext(), R.color.text_primary));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Ui.dpF(getContext(), 2));
            canvas.drawCircle(x, top + barHeight / 2, Ui.dpF(getContext(), 6), paint);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(x, top + barHeight / 2, Ui.dpF(getContext(), 2.2f), paint);
        }
    }
}
