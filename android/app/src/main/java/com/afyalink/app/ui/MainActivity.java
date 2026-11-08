package com.afyalink.app.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.afyalink.app.R;
import com.afyalink.app.domain.BloodPressureReading;
import com.afyalink.app.storage.AppData;
import com.afyalink.app.storage.EncryptedReadingStore;
import com.afyalink.app.ui.StageScaleView;
import com.afyalink.app.util.Format;
import com.afyalink.app.util.Lucide;
import com.afyalink.app.util.Ui;

import java.util.List;

/** Home dashboard. No account or network is needed to log, chart, or review readings. */
public final class MainActivity extends BaseActivity {
    private LinearLayout dashboardContent;
    private static boolean sessionUnlocked;
    private View lockCover;

    @Override protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        render();
        gateIfEnabled();
    }

    private void gateIfEnabled() {
        com.afyalink.app.storage.SettingsStore settings = new com.afyalink.app.storage.SettingsStore(this);
        if (sessionUnlocked || !settings.biometricLockEnabled()) return;
        if (!com.afyalink.app.security.SecurityGate.available(this)) return;
        LinearLayout cover = new LinearLayout(this);
        cover.setOrientation(LinearLayout.VERTICAL);
        cover.setGravity(Gravity.CENTER);
        cover.setBackgroundColor(Ui.color(this, R.color.canvas));
        android.widget.ImageView lock = Ui.icon(this, Lucide.LOCK, Ui.color(this, R.color.brand_teal), 30);
        cover.addView(lock, new LinearLayout.LayoutParams(Ui.dp(this, 56), Ui.dp(this, 56)));
        TextView message = ScreenUi.title(this, getString(R.string.security_unlock_title));
        message.setTextSize(22);
        message.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(-2, -2);
        messageParams.topMargin = Ui.dp(this, 18);
        cover.addView(message, messageParams);
        TextView body = ScreenUi.paragraph(this, getString(R.string.security_unlock_body));
        LinearLayout.LayoutParams bodyParams = new LinearLayout.LayoutParams(-2, -2);
        bodyParams.topMargin = Ui.dp(this, 8);
        cover.addView(body, bodyParams);
        frame.addView(cover, new android.widget.FrameLayout.LayoutParams(-1, -1));
        lockCover = cover;
        com.afyalink.app.security.SecurityGate.authenticate(this, () -> {
            sessionUnlocked = true;
            if (lockCover != null) frame.removeView(lockCover);
            lockCover = null;
        }, this::finish);
    }

    @Override protected void onResume() {
        super.onResume();
        if (dashboardContent != null) render();
    }

    private void render() {
        installPage(getString(R.string.app_name), false);
        dashboardContent = content;
        List<BloodPressureReading> rows = AppData.readings(this);

        TextView greeting = ScreenUi.eyebrow(this, Format.greeting(this).toUpperCase());
        content.addView(greeting);
        TextView title = ScreenUi.title(this, getString(R.string.app_name));
        title.setTextSize(34);
        LinearLayout.LayoutParams titleP = new LinearLayout.LayoutParams(-1, -2);
        titleP.topMargin = Ui.dp(this, 8);
        content.addView(title, titleP);
        TextView subtitle = ScreenUi.paragraph(this, getString(R.string.tagline));
        subtitle.setLetterSpacing(.10f);
        subtitle.setTextSize(11);
        LinearLayout.LayoutParams subP = new LinearLayout.LayoutParams(-1, -2);
        subP.topMargin = Ui.dp(this, 5);
        content.addView(subtitle, subP);

        View gap = Ui.spacer(this, 24);
        content.addView(gap);
        if (rows.isEmpty()) renderEmpty(); else renderLatest(rows);
        renderOverview(rows);
        renderScale(rows.isEmpty() ? null : rows.get(0));
        renderRecent(rows);
        renderFooter();
    }

    private void renderEmpty() {
        LinearLayout card = ScreenUi.card(this, Ui.color(this, R.color.brand_teal), false);
        card.setPadding(Ui.dp(this, 22), Ui.dp(this, 23), Ui.dp(this, 22), Ui.dp(this, 22));
        TextView eyebrow = ScreenUi.eyebrow(this, getString(R.string.dash_latest).toUpperCase());
        eyebrow.setTextColor(Ui.color(this, R.color.text_on_brand_muted));
        card.addView(eyebrow);
        TextView title = ScreenUi.text(this, getString(R.string.dash_empty_title), 24,
                Ui.color(this, R.color.text_on_brand), true);
        title.setLetterSpacing(-.03f);
        LinearLayout.LayoutParams t = new LinearLayout.LayoutParams(-1, -2);
        t.topMargin = Ui.dp(this, 15);
        card.addView(title, t);
        TextView body = ScreenUi.paragraph(this, getString(R.string.dash_empty_body));
        body.setTextColor(Ui.color(this, R.color.text_on_brand_muted));
        LinearLayout.LayoutParams b = new LinearLayout.LayoutParams(-1, -2);
        b.topMargin = Ui.dp(this, 9);
        card.addView(body, b);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = Ui.dp(this, 14);
        content.addView(card, p);
        androidx.appcompat.widget.AppCompatImageButton add = new androidx.appcompat.widget.AppCompatImageButton(this);
        add.setImageResource(Lucide.ADD);
        add.setImageTintList(android.content.res.ColorStateList.valueOf(Ui.color(this, R.color.brand_teal)));
        add.setBackground(Ui.roundedBox(this, Ui.color(this, R.color.surface), 16));
        add.setContentDescription(getString(R.string.cd_add_reading));
        add.setPadding(Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14));
        LinearLayout.LayoutParams action = new LinearLayout.LayoutParams(-1, Ui.dp(this, 58));
        content.addView(add, action);
        add.setOnClickListener(v -> go(LogReadingActivity.class));
    }

    private void renderLatest(List<BloodPressureReading> rows) {
        BloodPressureReading r = rows.get(0);
        LinearLayout card = ScreenUi.card(this, Ui.color(this, R.color.surface), false);
        LinearLayout top = ScreenUi.horizontal(this);
        TextView eyebrow = ScreenUi.eyebrow(this, getString(R.string.dash_latest).toUpperCase());
        top.addView(eyebrow, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView stage = Ui.stagePill(this, getString(r.stage().labelRes),
                Ui.color(this, r.stage().softColorRes), Ui.color(this, r.stage().inkColorRes));
        top.addView(stage);
        card.addView(top);

        LinearLayout values = ScreenUi.horizontal(this);
        TextView pressure = ScreenUi.text(this, Format.pressure(r.systolic, r.diastolic), 48,
                Ui.color(this, R.color.text_primary), true);
        pressure.setLetterSpacing(-.05f);
        values.addView(pressure, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView unit = ScreenUi.text(this, "mmHg", 12, Ui.color(this, R.color.text_tertiary), true);
        unit.setGravity(Gravity.BOTTOM);
        LinearLayout.LayoutParams unitP = new LinearLayout.LayoutParams(-2, -2);
        unitP.bottomMargin = Ui.dp(this, 7);
        values.addView(unit, unitP);
        LinearLayout.LayoutParams valueP = new LinearLayout.LayoutParams(-1, -2);
        valueP.topMargin = Ui.dp(this, 13);
        card.addView(values, valueP);

        TextView meta = ScreenUi.paragraph(this, Format.date(this, r.measuredAt) + (r.pulse > 0 ? "   ·   " + r.pulse + " bpm" : ""));
        LinearLayout.LayoutParams metaP = new LinearLayout.LayoutParams(-1, -2);
        metaP.topMargin = Ui.dp(this, 4);
        card.addView(meta, metaP);
        if (r.stage().isUrgent() || r.stage() == com.afyalink.app.domain.BpStage.LOW) {
            int color = Ui.color(this, r.stage().isUrgent() ? R.color.stage_grade3_soft : R.color.stage_low_soft);
            TextView notice = ScreenUi.paragraph(this, getString(r.stage().isUrgent() ? R.string.dash_crisis : R.string.dash_low_warning));
            notice.setTextColor(Ui.color(this, r.stage().isUrgent() ? R.color.stage_grade3_ink : R.color.stage_low_ink));
            notice.setBackground(Ui.roundedBox(this, color, 14));
            notice.setPadding(Ui.dp(this, 12), Ui.dp(this, 11), Ui.dp(this, 12), Ui.dp(this, 11));
            LinearLayout.LayoutParams noticeP = new LinearLayout.LayoutParams(-1, -2);
            noticeP.topMargin = Ui.dp(this, 15);
            card.addView(notice, noticeP);
        }
        LinearLayout.LayoutParams cardP = new LinearLayout.LayoutParams(-1, -2);
        cardP.bottomMargin = Ui.dp(this, 14);
        content.addView(card, cardP);

        androidx.appcompat.widget.AppCompatImageButton add = new androidx.appcompat.widget.AppCompatImageButton(this);
        add.setImageResource(Lucide.ADD);
        add.setImageTintList(android.content.res.ColorStateList.valueOf(Ui.color(this, R.color.brand_teal)));
        add.setBackground(Ui.roundedBox(this, Ui.color(this, R.color.brand_teal_soft), 16));
        add.setContentDescription(getString(R.string.cd_add_reading));
        add.setPadding(Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14));
        content.addView(add, new LinearLayout.LayoutParams(-1, Ui.dp(this, 54)));
        add.setOnClickListener(v -> go(LogReadingActivity.class));
    }

    private void renderOverview(List<BloodPressureReading> rows) {
        LinearLayout row = ScreenUi.horizontal(this);
        LinearLayout a = metricCard(getString(R.string.dash_average), average(rows), rows.size() == 0 ? "" : "7d");
        LinearLayout b = metricCard(getString(R.string.dash_count), String.valueOf(rows.size()), getString(R.string.dash_recent));
        LinearLayout.LayoutParams half = new LinearLayout.LayoutParams(0, -2, 1f);
        half.rightMargin = Ui.dp(this, 7);
        row.addView(a, half);
        LinearLayout.LayoutParams half2 = new LinearLayout.LayoutParams(0, -2, 1f);
        half2.leftMargin = Ui.dp(this, 7);
        row.addView(b, half2);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = Ui.dp(this, 14);
        content.addView(row, p);
    }

    private LinearLayout metricCard(String label, String value, String foot) {
        LinearLayout card = ScreenUi.card(this);
        card.setPadding(Ui.dp(this, 15), Ui.dp(this, 15), Ui.dp(this, 15), Ui.dp(this, 15));
        TextView l = ScreenUi.eyebrow(this, label.toUpperCase());
        l.setTextSize(9);
        card.addView(l);
        TextView v = ScreenUi.text(this, value, 21, Ui.color(this, R.color.text_primary), true);
        LinearLayout.LayoutParams vp = new LinearLayout.LayoutParams(-1, -2);
        vp.topMargin = Ui.dp(this, 10);
        card.addView(v, vp);
        TextView f = ScreenUi.paragraph(this, foot);
        f.setTextSize(11);
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(-1, -2);
        fp.topMargin = Ui.dp(this, 3);
        card.addView(f, fp);
        return card;
    }

    private String average(List<BloodPressureReading> rows) {
        com.afyalink.app.util.Format.ListStats stats = AppData.stats(rows);
        return stats.count == 0 ? "—" : Format.pressure((int)Math.round(stats.meanSys), (int)Math.round(stats.meanDia));
    }

    private void renderScale(@Nullable BloodPressureReading latest) {
        LinearLayout card = ScreenUi.card(this);
        LinearLayout top = ScreenUi.horizontal(this);
        TextView title = ScreenUi.text(this, getString(R.string.dash_scale_title), 16,
                Ui.color(this, R.color.text_primary), true);
        top.addView(title, new LinearLayout.LayoutParams(0, -2, 1f));
        androidx.appcompat.widget.AppCompatImageButton info = new androidx.appcompat.widget.AppCompatImageButton(this);
        info.setImageResource(Lucide.INFO);
        info.setImageTintList(android.content.res.ColorStateList.valueOf(Ui.color(this, R.color.text_secondary)));
        info.setBackgroundResource(android.R.drawable.list_selector_background);
        info.setContentDescription(getString(R.string.cd_about));
        info.setPadding(Ui.dp(this, 10), Ui.dp(this, 10), Ui.dp(this, 10), Ui.dp(this, 10));
        top.addView(info, new LinearLayout.LayoutParams(Ui.dp(this, 44), Ui.dp(this, 44)));
        info.setOnClickListener(v -> go(AboutActivity.class));
        card.addView(top);
        StageScaleView scale = new StageScaleView(this);
        scale.setShowLabels(true);
        if (latest != null) scale.setStage(latest.stage());
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, Ui.dp(this, 56));
        sp.topMargin = Ui.dp(this, 8);
        card.addView(scale, sp);
        TextView note = ScreenUi.paragraph(this, getString(R.string.dash_scale_body));
        note.setTextSize(12);
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(-1, -2);
        np.topMargin = Ui.dp(this, 10);
        card.addView(note, np);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = Ui.dp(this, 14);
        content.addView(card, p);
    }

    private void renderRecent(List<BloodPressureReading> rows) {
        LinearLayout header = ScreenUi.horizontal(this);
        TextView recent = ScreenUi.text(this, getString(R.string.dash_recent), 20,
                Ui.color(this, R.color.text_primary), true);
        header.addView(recent, new LinearLayout.LayoutParams(0, -2, 1f));
        androidx.appcompat.widget.AppCompatImageButton more = new androidx.appcompat.widget.AppCompatImageButton(this);
        more.setImageResource(com.afyalink.app.R.drawable.ic_chevron_right);
        more.setImageTintList(android.content.res.ColorStateList.valueOf(Ui.color(this, R.color.text_secondary)));
        more.setBackgroundResource(android.R.drawable.list_selector_background);
        more.setContentDescription(getString(R.string.cd_history));
        more.setPadding(Ui.dp(this, 10), Ui.dp(this, 10), Ui.dp(this, 10), Ui.dp(this, 10));
        header.addView(more, new LinearLayout.LayoutParams(Ui.dp(this, 44), Ui.dp(this, 44)));
        more.setOnClickListener(v -> go(HistoryActivity.class));
        content.addView(header);
        if (rows.isEmpty()) {
            TextView empty = ScreenUi.paragraph(this, getString(R.string.trends_empty));
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
            p.topMargin = Ui.dp(this, 8);
            content.addView(empty, p);
            return;
        }
        int count = Math.min(3, rows.size());
        for (int i = 0; i < count; i++) {
            BloodPressureReading r = rows.get(i);
            LinearLayout row = ScreenUi.horizontal(this);
            TextView values = ScreenUi.text(this, Format.pressure(r.systolic, r.diastolic), 18,
                    Ui.color(this, R.color.text_primary), true);
            row.addView(values, new LinearLayout.LayoutParams(0, -2, 1f));
            TextView date = ScreenUi.paragraph(this, Format.date(this, r.measuredAt));
            date.setTextSize(11);
            row.addView(date);
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, Ui.dp(this, 58));
            content.addView(row, rp);
            if (i < count - 1) content.addView(ScreenUi.hairline(this));
        }
    }

    private void renderFooter() {
        TextView privacy = ScreenUi.paragraph(this, getString(R.string.dash_never_synced));
        privacy.setTextSize(11);
        privacy.setGravity(Gravity.CENTER);
        privacy.setPadding(0, Ui.dp(this, 22), 0, Ui.dp(this, 8));
        content.addView(privacy, new LinearLayout.LayoutParams(-1, -2));
    }
}
