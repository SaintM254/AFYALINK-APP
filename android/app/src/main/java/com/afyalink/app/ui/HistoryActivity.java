package com.afyalink.app.ui;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;

import com.afyalink.app.R;
import com.afyalink.app.domain.BloodPressureReading;
import com.afyalink.app.util.Format;
import com.afyalink.app.util.Lucide;
import com.afyalink.app.util.Ui;

import java.util.ArrayList;
import java.util.List;

public final class HistoryActivity extends BaseActivity {
    private String filter = "all";
    @Override protected void onCreate(@Nullable Bundle state) { super.onCreate(state); render(); }
    @Override protected void onResume() { super.onResume(); render(); }

    private void render() {
        installPage(getString(R.string.history_title), true);
        content.addView(ScreenUi.eyebrow(this, getString(R.string.history_eyebrow)));
        LinearLayout header = ScreenUi.horizontal(this);
        TextView title = ScreenUi.title(this, getString(R.string.history_title));
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1f));
        AppCompatImageButton filterButton = new AppCompatImageButton(this);
        filterButton.setImageResource(Lucide.FILTER);
        filterButton.setImageTintList(ColorStateList.valueOf(Ui.color(this, R.color.brand_teal)));
        filterButton.setContentDescription(getString(R.string.cd_filter));
        filterButton.setBackgroundResource(android.R.drawable.list_selector_background);
        filterButton.setPadding(Ui.dp(this, 12), Ui.dp(this, 12), Ui.dp(this, 12), Ui.dp(this, 12));
        header.addView(filterButton, new LinearLayout.LayoutParams(Ui.dp(this, 50), Ui.dp(this, 50)));
        content.addView(header);
        filterButton.setOnClickListener(v -> { filter = filter.equals("all") ? "high" : filter.equals("high") ? "normal" : "all"; render(); });

        List<BloodPressureReading> all = com.afyalink.app.storage.AppData.readings(this);
        List<BloodPressureReading> rows = new ArrayList<>();
        for (BloodPressureReading r : all) {
            if (filter.equals("all") || (filter.equals("high") && r.stage().isHigh())
                    || (filter.equals("normal") && !r.stage().isHigh() && r.stage() != com.afyalink.app.domain.BpStage.LOW)) rows.add(r);
        }
        renderReadingKey(all.isEmpty() ? null : all.get(0));
        TextView count = ScreenUi.paragraph(this, getString(R.string.history_count, rows.size()) + "  ·  " +
                getString(filter.equals("all") ? R.string.history_filter_all : filter.equals("high") ? R.string.history_filter_high : R.string.history_filter_normal));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2); cp.topMargin = Ui.dp(this, 10); cp.bottomMargin = Ui.dp(this, 14); content.addView(count, cp);
        if (rows.isEmpty()) {
            LinearLayout card = ScreenUi.card(this);
            TextView empty = ScreenUi.paragraph(this, getString(all.isEmpty() ? R.string.dash_empty_body : R.string.history_empty));
            card.addView(empty);
            content.addView(card);
            return;
        }
        for (int i = 0; i < rows.size(); i++) {
            BloodPressureReading r = rows.get(i);
            LinearLayout card = ScreenUi.card(this);
            card.setPadding(Ui.dp(this, 16), Ui.dp(this, 14), Ui.dp(this, 12), Ui.dp(this, 14));
            LinearLayout row = ScreenUi.horizontal(this);
            LinearLayout left = new LinearLayout(this); left.setOrientation(LinearLayout.VERTICAL);
            TextView value = ScreenUi.text(this, Format.pressure(r.systolic, r.diastolic), 25, Ui.color(this, R.color.text_primary), true);
            left.addView(value);
            TextView meta = ScreenUi.paragraph(this, Format.date(this, r.measuredAt)
                    + (r.pulse > 0 ? "  ·  " + r.pulse + " bpm" : "")
                    + "  ·  " + getString(R.string.map_value, r.meanArterialPressureRounded()));
            meta.setTextSize(11);
            LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(-1, -2); mp.topMargin = Ui.dp(this, 5); left.addView(meta, mp);
            row.addView(left, new LinearLayout.LayoutParams(0, -2, 1f));
            TextView pill = Ui.stagePill(this, getString(r.stage().labelRes), Ui.color(this, r.stage().softColorRes), Ui.color(this, r.stage().inkColorRes));
            row.addView(pill);
            androidx.appcompat.widget.AppCompatImageButton open = new androidx.appcompat.widget.AppCompatImageButton(this);
            open.setImageResource(R.drawable.ic_chevron_right);
            open.setImageTintList(android.content.res.ColorStateList.valueOf(Ui.color(this, R.color.text_secondary)));
            open.setBackgroundResource(android.R.drawable.list_selector_background);
            open.setContentDescription(getString(R.string.cd_edit));
            open.setPadding(Ui.dp(this,10),Ui.dp(this,10),Ui.dp(this,10),Ui.dp(this,10));
            LinearLayout.LayoutParams openParams = new LinearLayout.LayoutParams(Ui.dp(this,44),Ui.dp(this,44));
            openParams.leftMargin = Ui.dp(this,4);
            row.addView(open,openParams);
            card.addView(row);
            open.setOnClickListener(v -> { android.content.Intent in = new android.content.Intent(this, LogReadingActivity.class); in.putExtra("reading_id", r.id); startActivity(in); });
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, -2); rp.bottomMargin = Ui.dp(this, 10); content.addView(card, rp);
        }
    }

    private void renderReadingKey(@Nullable BloodPressureReading latest) {
        LinearLayout card = ScreenUi.card(this, Ui.color(this, R.color.surface), false);
        card.addView(ScreenUi.text(this, getString(R.string.readings_key_title), 16,
                Ui.color(this, R.color.text_primary), true));
        TextView body = ScreenUi.paragraph(this, getString(R.string.readings_key_body));
        LinearLayout.LayoutParams bodyParams = new LinearLayout.LayoutParams(-1, -2);
        bodyParams.topMargin = Ui.dp(this, 7);
        card.addView(body, bodyParams);
        StageScaleView scale = new StageScaleView(this);
        scale.setShowLabels(true);
        if (latest != null) scale.setStage(latest.stage());
        LinearLayout.LayoutParams scaleParams = new LinearLayout.LayoutParams(-1, Ui.dp(this, 55));
        scaleParams.topMargin = Ui.dp(this, 12);
        card.addView(scale, scaleParams);
        TextView ranges = ScreenUi.paragraph(this, getString(R.string.readings_scale_ranges));
        ranges.setTextSize(11);
        LinearLayout.LayoutParams rangesParams = new LinearLayout.LayoutParams(-1, -2);
        rangesParams.topMargin = Ui.dp(this, 8);
        card.addView(ranges, rangesParams);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
        cardParams.topMargin = Ui.dp(this, 10);
        cardParams.bottomMargin = Ui.dp(this, 12);
        content.addView(card, cardParams);
    }
}
