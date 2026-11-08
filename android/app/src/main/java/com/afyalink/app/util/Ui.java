package com.afyalink.app.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.afyalink.app.R;

/**
 * Small, dependency-free UI toolkit so screens stay declarative: spacing, pills,
 * rows and tiles are built once here and reused everywhere. Keeping this in code
 * (rather than dozens of near-identical layout files) is what keeps the app
 * visually consistent as it grows.
 */
public final class Ui {

    private Ui() {
    }

    public static int dp(Context context, float value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static float dpF(Context context, float value) {
        return value * context.getResources().getDisplayMetrics().density;
    }

    @ColorInt
    public static int color(Context context, int res) {
        return ContextCompat.getColor(context, res);
    }

    @ColorInt
    public static int alpha(@ColorInt int color, float alpha) {
        return Color.argb((int) (alpha * 255f), Color.red(color), Color.green(color), Color.blue(color));
    }

    /** Rounded, optionally tinted box with an ink ripple. */
    public static GradientDrawable roundedBox(Context context, @ColorInt int fill, float radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(dpF(context, radiusDp));
        drawable.setColor(fill);
        return drawable;
    }

    public static GradientDrawable roundedBox(Context context, @ColorInt int fill, float radiusDp, @ColorInt int stroke, float strokeWidthDp) {
        GradientDrawable drawable = roundedBox(context, fill, radiusDp);
        drawable.setStroke(dp(context, strokeWidthDp), stroke);
        return drawable;
    }

    public static RippleDrawable ripple(Context context, GradientDrawable content, @ColorInt int rippleColor) {
        return new RippleDrawable(ColorStateList.valueOf(alpha(rippleColor, 0.10f)), content, null);
    }

    /** The stage pill used on every screen. */
    public static TextView stagePill(Context context, String label, @ColorInt int soft, @ColorInt int ink) {
        TextView pill = new TextView(context);
        pill.setText(label);
        pill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        pill.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        pill.setLetterSpacing(0.06f);
        pill.setTextColor(ink);
        pill.setBackground(roundedBox(context, soft, 100f));
        int padH = dp(context, 12);
        int padV = dp(context, 6);
        pill.setPadding(padH, padV, padH, padV);
        pill.setSingleLine(true);
        return pill;
    }

    public static ImageView icon(Context context, @DrawableRes int res, @ColorInt int tint, int sizeDp) {
        ImageView view = new ImageView(context);
        view.setImageResource(res);
        view.setImageTintList(ColorStateList.valueOf(tint));
        view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(context, sizeDp), dp(context, sizeDp));
        view.setLayoutParams(params);
        return view;
    }

    public static TextView text(Context context, CharSequence value, int styleRes) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextAppearance(styleRes);
        return view;
    }

    public static LinearLayout column(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return layout;
    }

    public static LinearLayout row(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return layout;
    }

    public static View spacer(Context context, int heightDp) {
        View view = new View(context);
        view.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(context, heightDp)));
        return view;
    }

    public static View divider(Context context) {
        View view = new View(context);
        view.setBackgroundColor(color(context, R.color.hairline));
        view.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, dp(context, 1))));
        return view;
    }

    public static void margins(View view, int leftDp, int topDp, int rightDp, int bottomDp) {
        Context context = view.getContext();
        ViewGroup.LayoutParams raw = view.getLayoutParams();
        if (!(raw instanceof ViewGroup.MarginLayoutParams)) {
            return;
        }
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) raw;
        params.setMargins(dp(context, leftDp), dp(context, topDp), dp(context, rightDp), dp(context, bottomDp));
        view.setLayoutParams(params);
    }

    public static void setMarginsPreserve(View view, int left, int top, int right, int bottom) {
        ViewGroup.LayoutParams raw = view.getLayoutParams();
        if (raw instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) raw;
            params.leftMargin = left;
            params.topMargin = top;
            params.rightMargin = right;
            params.bottomMargin = bottom;
            view.setLayoutParams(params);
        }
    }

    /** Fluid press feedback: cards and rows shrink slightly, Apple style. */
    public static void pressAnimation(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.975f).scaleY(0.975f).setDuration(120).start();
                    break;
                case android.view.MotionEvent.ACTION_CANCEL:
                case android.view.MotionEvent.ACTION_UP:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(220).start();
                    break;
                default:
                    break;
            }
            return false;
        });
    }

    public static int statusBarHeight(Context context) {
        int id = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return id > 0 ? context.getResources().getDimensionPixelSize(id) : dp(context, 24);
    }

    public static int attrColor(Context context, int attr) {
        TypedValue value = new TypedValue();
        if (context.getTheme().resolveAttribute(attr, value, true)) {
            return value.data;
        }
        return Color.TRANSPARENT;
    }

    public static void tintLeftDrawable(Context context, TextView view, @DrawableRes int res, @ColorInt int tint) {
        view.setCompoundDrawablesRelativeWithIntrinsicBounds(
                AppCompatResources.getDrawable(context, res), null, null, null);
        if (view.getCompoundDrawablesRelative()[0] != null) {
            view.getCompoundDrawablesRelative()[0].setTint(tint);
        }
    }

    public static void haptic(View view) {
        view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP,
                android.view.HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
    }

    public static void hapticLong(View view) {
        view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS,
                android.view.HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
    }

    public static void setWindowBackground(Activity activity, @ColorInt int color) {
        activity.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(color));
    }
}
