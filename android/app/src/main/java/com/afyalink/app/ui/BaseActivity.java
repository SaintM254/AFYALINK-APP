package com.afyalink.app.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.afyalink.app.R;
import com.afyalink.app.util.Lucide;
import com.afyalink.app.util.Ui;

/** Shared edge-to-edge scaffold, icon-only toolbar actions, and the reference-style full-screen menu. */
public abstract class BaseActivity extends AppCompatActivity {
    protected FrameLayout frame;
    protected LinearLayout page;
    protected LinearLayout content;
    protected ScrollView scroll;
    private View menuOverlay;

    @Override protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Ui.color(this, R.color.canvas));
        window.setNavigationBarColor(Ui.color(this, R.color.canvas));
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            window.setNavigationBarContrastEnforced(false);
            window.setStatusBarContrastEnforced(false);
        }
        frame = new FrameLayout(this);
        page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setBackgroundColor(Ui.color(this, R.color.canvas));
        frame.addView(page, new FrameLayout.LayoutParams(-1, -1));
        setContentView(frame);
        ViewCompat.setOnApplyWindowInsetsListener(page, (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            v.setPadding(0, top, 0, bottom);
            return insets;
        });
    }

    protected void installPage(String title, boolean hasBack) {
        page.removeAllViews();
        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setPadding(Ui.dp(this, 12), 0, Ui.dp(this, 12), 0);
        toolbar.setLayoutParams(new LinearLayout.LayoutParams(-1, Ui.dp(this, 60)));
        if (hasBack) {
            toolbar.addView(iconButton(Lucide.BACK, getString(R.string.cd_back), v -> finish()));
        } else {
            View spacer = new View(this);
            toolbar.addView(spacer, new LinearLayout.LayoutParams(Ui.dp(this, 48), Ui.dp(this, 48)));
        }
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(16);
        titleView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        titleView.setTextColor(Ui.color(this, R.color.text_primary));
        titleView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, -1, 1f);
        toolbar.addView(titleView, titleParams);
        toolbar.addView(iconButton(Lucide.MENU, getString(R.string.cd_menu), v -> showMenu()));
        page.addView(toolbar);

        scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(Ui.dp(this, 20), Ui.dp(this, 8), Ui.dp(this, 20), Ui.dp(this, 32));
        scroll.addView(content, new ScrollView.LayoutParams(-1, -2));
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
    }

    protected ImageButton iconButton(int drawable, String description, View.OnClickListener listener) {
        ImageButton button = new ImageButton(this);
        button.setImageResource(drawable);
        button.setImageTintList(android.content.res.ColorStateList.valueOf(Ui.color(this, R.color.text_primary)));
        button.setBackgroundResource(android.R.drawable.list_selector_background);
        button.setContentDescription(description);
        button.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
        button.setPadding(Ui.dp(this, 13), Ui.dp(this, 13), Ui.dp(this, 13), Ui.dp(this, 13));
        button.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(this, 48), Ui.dp(this, 48)));
        button.setOnClickListener(listener);
        return button;
    }

    protected TextView label(String text, int style) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextAppearance(style);
        return view;
    }

    protected TextView heading(String text) {
        return label(text, R.style.Text_AfyaLink_Headline);
    }

    protected TextView bodyText(String text) {
        return label(text, R.style.Text_AfyaLink_Body);
    }

    protected TextView eyebrow(String text) {
        return label(text, R.style.Text_AfyaLink_Eyebrow);
    }

    protected void go(Class<?> screen) {
        Intent intent = new Intent(this, screen);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        if (!(this instanceof MainActivity)) finish();
    }

    private void showMenu() {
        if (menuOverlay != null) return;
        LinearLayout overlay = new LinearLayout(this);
        overlay.setOrientation(LinearLayout.VERTICAL);
        overlay.setBackgroundColor(Ui.color(this, R.color.brand_teal_logo));
        overlay.setFocusableInTouchMode(true);
        overlay.setElevation(Ui.dp(this, 16));
        FrameLayout.LayoutParams whole = new FrameLayout.LayoutParams(-1, -1);
        frame.addView(overlay, whole);
        menuOverlay = overlay;

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        top.setPadding(Ui.dp(this, 20), Ui.dp(this, 8), Ui.dp(this, 16), Ui.dp(this, 8));
        overlay.addView(top, new LinearLayout.LayoutParams(-1, Ui.dp(this, 72)));
        ImageButton close = new ImageButton(this);
        close.setImageResource(Lucide.CLOSE);
        close.setImageTintList(android.content.res.ColorStateList.valueOf(Ui.color(this, R.color.text_on_brand)));
        close.setBackgroundResource(android.R.drawable.list_selector_background);
        close.setContentDescription(getString(R.string.cd_close));
        close.setPadding(Ui.dp(this, 13), Ui.dp(this, 13), Ui.dp(this, 13), Ui.dp(this, 13));
        top.addView(close, new LinearLayout.LayoutParams(Ui.dp(this, 52), Ui.dp(this, 52)));
        close.setOnClickListener(v -> hideMenu());

        TextView eyebrow = new TextView(this);
        eyebrow.setText(getString(R.string.menu_eyebrow));
        eyebrow.setTextColor(Ui.color(this, R.color.text_on_brand_muted));
        eyebrow.setTextSize(11);
        eyebrow.setTypeface(Typeface.create("sans-serif-medium", 0));
        eyebrow.setLetterSpacing(0.24f);
        eyebrow.setPadding(0, Ui.dp(this, 8), Ui.dp(this, 26), Ui.dp(this, 10));
        LinearLayout.LayoutParams eyebrowParams = new LinearLayout.LayoutParams(-1, -2);
        eyebrowParams.topMargin = Ui.dp(this, 30);
        overlay.addView(eyebrow, eyebrowParams);

        ScrollView menuScroll = new ScrollView(this);
        menuScroll.setFillViewport(false);
        menuScroll.setClipToPadding(false);
        LinearLayout items = new LinearLayout(this);
        items.setOrientation(LinearLayout.VERTICAL);
        items.setGravity(Gravity.END);
        items.setPadding(Ui.dp(this, 20), 0, Ui.dp(this, 26), Ui.dp(this, 14));
        menuScroll.addView(items, new ScrollView.LayoutParams(-1, -2));
        overlay.addView(menuScroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        addMenuItem(items, R.string.menu_dashboard, MainActivity.class, 34);
        addMenuItem(items, R.string.menu_readings, HistoryActivity.class, 34);
        addMenuItem(items, R.string.menu_monitor, MonitorActivity.class, 34);
        addMenuItem(items, R.string.menu_trends, TrendsActivity.class, 34);
        addMenuItem(items, R.string.menu_reminders, RemindersActivity.class, 34);
        addMenuItem(items, R.string.menu_sync, SyncActivity.class, 34);
        addMenuItem(items, R.string.menu_settings, SettingsActivity.class, 34);
        addMenuItem(items, R.string.menu_about, AboutActivity.class, 34);

        LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.VERTICAL);
        bottom.setPadding(Ui.dp(this, 20), Ui.dp(this, 10), Ui.dp(this, 20), Ui.dp(this, 22));
        overlay.addView(bottom, new LinearLayout.LayoutParams(-1, -2));
        ImageButton log = new ImageButton(this);
        log.setImageResource(Lucide.ADD);
        log.setImageTintList(android.content.res.ColorStateList.valueOf(Ui.color(this, R.color.brand_teal_logo)));
        log.setBackground(Ui.roundedBox(this, Ui.color(this, R.color.menu_button_surface), 18));
        log.setContentDescription(getString(R.string.cd_add_reading));
        log.setPadding(Ui.dp(this, 17), Ui.dp(this, 17), Ui.dp(this, 17), Ui.dp(this, 17));
        LinearLayout.LayoutParams logParams = new LinearLayout.LayoutParams(-1, Ui.dp(this, 64));
        logParams.bottomMargin = Ui.dp(this, 10);
        bottom.addView(log, logParams);
        log.setOnClickListener(v -> { hideMenu(); startActivity(new Intent(this, LogReadingActivity.class)); });

        ImageButton closeBottom = new ImageButton(this);
        closeBottom.setImageResource(Lucide.CLOSE);
        closeBottom.setImageTintList(android.content.res.ColorStateList.valueOf(Ui.color(this, R.color.text_on_brand)));
        closeBottom.setBackground(Ui.roundedBox(this, Ui.color(this, R.color.brand_teal_logo), 16,
                Ui.color(this, R.color.text_on_brand_muted), 1));
        closeBottom.setContentDescription(getString(R.string.cd_close));
        closeBottom.setPadding(Ui.dp(this, 19), Ui.dp(this, 15), Ui.dp(this, 19), Ui.dp(this, 15));
        bottom.addView(closeBottom, new LinearLayout.LayoutParams(-1, Ui.dp(this, 58)));
        closeBottom.setOnClickListener(v -> hideMenu());

        overlay.setAlpha(0f);
        overlay.animate().alpha(1f).setDuration(180).start();
        overlay.requestFocus();
    }

    private void addMenuItem(LinearLayout parent, int labelRes, Class<?> screen, int size) {
        TextView item = new TextView(this);
        item.setText(getString(labelRes));
        item.setTextColor(Ui.color(this, R.color.text_on_brand));
        item.setTextSize(size);
        item.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        item.setLetterSpacing(-0.025f);
        item.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        item.setIncludeFontPadding(false);
        item.setPadding(Ui.dp(this, 12), Ui.dp(this, 12), 0, Ui.dp(this, 12));
        item.setMinHeight(Ui.dp(this, 58));
        item.setBackgroundResource(android.R.drawable.list_selector_background);
        item.setOnClickListener(v -> { hideMenu(); if (screen == MainActivity.class) { if (!(this instanceof MainActivity)) go(screen); } else go(screen); });
        parent.addView(item, new LinearLayout.LayoutParams(-1, -2));
    }

    private void hideMenu() {
        if (menuOverlay == null) return;
        View current = menuOverlay;
        menuOverlay = null;
        current.animate().alpha(0f).setDuration(120).withEndAction(() -> frame.removeView(current)).start();
    }

    @Override public void onBackPressed() {
        if (menuOverlay != null) hideMenu();
        else super.onBackPressed();
    }
}
