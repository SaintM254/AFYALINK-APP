package com.afyalink.app.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;

import com.afyalink.app.R;
import com.afyalink.app.util.Ui;

/** Shared fluid cards, icon-only actions and editorial labels for screens. */
public final class ScreenUi {
    private ScreenUi() {}

    public static LinearLayout card(Context c) { return card(c, Ui.color(c, R.color.surface), true); }
    public static LinearLayout card(Context c, @ColorInt int fill, boolean border) {
        LinearLayout box = new LinearLayout(c);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(Ui.dp(c, 20), Ui.dp(c, 18), Ui.dp(c, 20), Ui.dp(c, 18));
        box.setBackground(Ui.roundedBox(c, fill, 24, border ? Ui.color(c, R.color.hairline) : fill, border ? 1 : 0));
        box.setElevation(Ui.dp(c, 1));
        box.setClipToPadding(false);
        box.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        Ui.pressAnimation(box);
        return box;
    }

    public static LinearLayout horizontal(Context c) {
        LinearLayout row = new LinearLayout(c);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        return row;
    }

    public static TextView text(Context c, String value, float size, @ColorInt int color, boolean medium) {
        TextView t = new TextView(c);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setTypeface(Typeface.create("sans-serif" + (medium ? "-medium" : ""), Typeface.NORMAL));
        t.setIncludeFontPadding(false);
        return t;
    }

    public static ImageButton iconAction(Context c, @DrawableRes int drawable, String description,
                                         @ColorInt int tint, View.OnClickListener click) {
        ImageButton b = new ImageButton(c);
        b.setImageResource(drawable);
        b.setImageTintList(ColorStateList.valueOf(tint));
        b.setBackground(Ui.ripple(c, Ui.roundedBox(c, Ui.color(c, R.color.surface_alt), 24), tint));
        b.setContentDescription(description);
        b.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
        b.setPadding(Ui.dp(c, 14), Ui.dp(c, 14), Ui.dp(c, 14), Ui.dp(c, 14));
        b.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, 52), Ui.dp(c, 52)));
        b.setOnClickListener(click);
        return b;
    }

    public static void margins(Context c, View view, int left, int top, int right, int bottom) {
        ViewGroup.LayoutParams raw = view.getLayoutParams();
        if (raw instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) raw;
            p.setMargins(Ui.dp(c,left), Ui.dp(c,top), Ui.dp(c,right), Ui.dp(c,bottom));
            view.setLayoutParams(p);
        }
    }

    public static TextView eyebrow(Context c, String text) {
        TextView t = text(c, text, 11, Ui.color(c, R.color.text_tertiary), true);
        t.setLetterSpacing(0.16f);
        return t;
    }

    public static TextView title(Context c, String text) {
        TextView t = text(c, text, 26, Ui.color(c, R.color.text_primary), true);
        t.setLetterSpacing(-0.025f);
        return t;
    }

    public static TextView paragraph(Context c, String text) {
        TextView t = text(c, text, 14, Ui.color(c, R.color.text_secondary), false);
        t.setLineSpacing(Ui.dp(c, 3), 1f);
        return t;
    }

    public static View hairline(Context c) { return Ui.divider(c); }

    public static LinearLayout.LayoutParams params(int width, int height) {
        return new LinearLayout.LayoutParams(width, height);
    }
}
