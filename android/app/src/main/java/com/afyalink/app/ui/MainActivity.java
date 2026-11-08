package com.afyalink.app.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.afyalink.app.R;
import com.afyalink.app.domain.BloodPressureReading;
import com.afyalink.app.storage.EncryptedReadingStore;
import com.afyalink.app.storage.SettingsStore;
import com.afyalink.app.util.Format;
import com.afyalink.app.util.Ui;

import java.util.List;

/** Home: the latest result and a local-first reading form are immediately available. */
public final class MainActivity extends BaseActivity {
    private EditText systolicInput;
    private EditText diastolicInput;
    private EditText pulseInput;
    private TextView mapPreview;
    private boolean initialResume = true;

    @Override protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        render();
    }

    @Override protected void onResume() {
        super.onResume();
        if (initialResume) initialResume = false;
        else if (content != null) render();
    }

    private void render() {
        installPage(getString(R.string.app_name), false);
        List<BloodPressureReading> rows = com.afyalink.app.storage.AppData.readings(this);

        String name = new SettingsStore(this).profileName().trim();
        String greeting = Format.greeting(this) + (name.isEmpty() ? "" : ", " + name);
        TextView hello = ScreenUi.eyebrow(this, greeting);
        content.addView(hello);
        TextView title = ScreenUi.title(this, getString(R.string.dash_home_title));
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(-1, -2);
        titleParams.topMargin = Ui.dp(this, 6);
        content.addView(title, titleParams);

        LinearLayout.LayoutParams latestSpacing = new LinearLayout.LayoutParams(-1, -2);
        latestSpacing.topMargin = Ui.dp(this, 18);
        latestSpacing.bottomMargin = Ui.dp(this, 22);
        content.addView(renderLatest(rows), latestSpacing);
        renderQuickEntry();
        renderNavigation();
    }

    private LinearLayout renderLatest(List<BloodPressureReading> rows) {
        LinearLayout card = ScreenUi.card(this, Ui.color(this, R.color.surface), false);
        TextView heading = ScreenUi.eyebrow(this, getString(R.string.dash_latest).toUpperCase());
        card.addView(heading);
        if (rows.isEmpty()) {
            TextView empty = ScreenUi.text(this, getString(R.string.dash_empty_title), 20,
                    Ui.color(this, R.color.text_primary), true);
            LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(-1, -2);
            ep.topMargin = Ui.dp(this, 12);
            card.addView(empty, ep);
            TextView body = ScreenUi.paragraph(this, getString(R.string.dash_empty_body));
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, -2);
            bp.topMargin = Ui.dp(this, 5);
            card.addView(body, bp);
            return card;
        }

        BloodPressureReading latest = rows.get(0);
        LinearLayout valueRow = ScreenUi.horizontal(this);
        TextView pressure = ScreenUi.text(this, Format.pressure(latest.systolic, latest.diastolic), 42,
                Ui.color(this, R.color.text_primary), true);
        pressure.setLetterSpacing(-.04f);
        valueRow.addView(pressure, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView unit = ScreenUi.text(this, "mmHg", 12, Ui.color(this, R.color.text_tertiary), true);
        unit.setGravity(Gravity.BOTTOM);
        LinearLayout.LayoutParams unitParams = new LinearLayout.LayoutParams(-2, -2);
        unitParams.bottomMargin = Ui.dp(this, 6);
        valueRow.addView(unit, unitParams);
        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(-1, -2);
        valueParams.topMargin = Ui.dp(this, 9);
        card.addView(valueRow, valueParams);

        LinearLayout metaRow = ScreenUi.horizontal(this);
        TextView meta = ScreenUi.paragraph(this, Format.date(this, latest.measuredAt)
                + (latest.pulse > 0 ? "  ·  " + latest.pulse + " bpm" : "")
                + "  ·  " + getString(R.string.map_value, latest.meanArterialPressureRounded()));
        metaRow.addView(meta, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView stage = Ui.stagePill(this, getString(latest.stage().labelRes),
                Ui.color(this, latest.stage().softColorRes), Ui.color(this, latest.stage().inkColorRes));
        metaRow.addView(stage);
        LinearLayout.LayoutParams metaParams = new LinearLayout.LayoutParams(-1, -2);
        metaParams.topMargin = Ui.dp(this, 6);
        card.addView(metaRow, metaParams);

        View statsDivider = ScreenUi.hairline(this);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(-1, Ui.dp(this, 1));
        dividerParams.topMargin = Ui.dp(this, 13);
        dividerParams.bottomMargin = Ui.dp(this, 10);
        card.addView(statsDivider, dividerParams);

        com.afyalink.app.util.Format.ListStats week = com.afyalink.app.storage.AppData.stats(rows);
        String average = com.afyalink.app.util.Format.average(week);
        if (week.count > 0) average += " mmHg";
        LinearLayout statsRow = ScreenUi.horizontal(this);
        statsRow.setGravity(Gravity.CENTER_VERTICAL);
        addDashboardStat(statsRow, R.string.dash_average, average, true);
        addDashboardStat(statsRow, R.string.dash_count, String.valueOf(rows.size()), false);
        card.addView(statsRow);
        return card;
    }

    private void addDashboardStat(LinearLayout row, int labelRes, String value, boolean first) {
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(first ? Gravity.START : Gravity.END);
        TextView label = ScreenUi.eyebrow(this, getString(labelRes).toUpperCase());
        label.setTextSize(9);
        label.setGravity(first ? Gravity.START : Gravity.END);
        column.addView(label);
        TextView number = ScreenUi.text(this, value, 16, Ui.color(this, R.color.text_primary), true);
        number.setGravity(first ? Gravity.START : Gravity.END);
        LinearLayout.LayoutParams numberParams = new LinearLayout.LayoutParams(-1, -2);
        numberParams.topMargin = Ui.dp(this, 4);
        column.addView(number, numberParams);
        LinearLayout.LayoutParams columnParams = new LinearLayout.LayoutParams(0, -2, 1f);
        if (!first) columnParams.leftMargin = Ui.dp(this, 10);
        row.addView(column, columnParams);
    }

    private void renderQuickEntry() {
        LinearLayout card = ScreenUi.card(this);
        TextView title = ScreenUi.text(this, getString(R.string.quick_entry_title), 20,
                Ui.color(this, R.color.text_primary), true);
        card.addView(title);
        TextView hint = ScreenUi.paragraph(this, getString(R.string.quick_entry_body));
        LinearLayout.LayoutParams hintParams = new LinearLayout.LayoutParams(-1, -2);
        hintParams.topMargin = Ui.dp(this, 4);
        card.addView(hint, hintParams);

        LinearLayout pressureRow = ScreenUi.horizontal(this);
        pressureRow.setGravity(Gravity.TOP);
        LinearLayout sysColumn = new LinearLayout(this);
        sysColumn.setOrientation(LinearLayout.VERTICAL);
        sysColumn.addView(fieldLabel(getString(R.string.log_systolic)));
        systolicInput = numberInput("120", 3);
        sysColumn.addView(systolicInput, new LinearLayout.LayoutParams(-1, Ui.dp(this, 56)));
        LinearLayout diaColumn = new LinearLayout(this);
        diaColumn.setOrientation(LinearLayout.VERTICAL);
        diaColumn.addView(fieldLabel(getString(R.string.log_diastolic)));
        diastolicInput = numberInput("80", 3);
        diaColumn.addView(diastolicInput, new LinearLayout.LayoutParams(-1, Ui.dp(this, 56)));
        LinearLayout.LayoutParams sysParams = new LinearLayout.LayoutParams(0, -2, 1f);
        sysParams.rightMargin = Ui.dp(this, 6);
        pressureRow.addView(sysColumn, sysParams);
        LinearLayout.LayoutParams diaParams = new LinearLayout.LayoutParams(0, -2, 1f);
        diaParams.leftMargin = Ui.dp(this, 6);
        pressureRow.addView(diaColumn, diaParams);
        LinearLayout.LayoutParams pressureParams = new LinearLayout.LayoutParams(-1, -2);
        pressureParams.topMargin = Ui.dp(this, 13);
        card.addView(pressureRow, pressureParams);

        LinearLayout pulseRow = ScreenUi.horizontal(this);
        pulseRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView pulseLabel = fieldLabel(getString(R.string.log_pulse));
        pulseRow.addView(pulseLabel, new LinearLayout.LayoutParams(0, -2, 1f));
        pulseInput = numberInput(getString(R.string.log_pulse_hint), 3);
        pulseInput.setContentDescription(getString(R.string.log_pulse));
        LinearLayout.LayoutParams pulseInputParams = new LinearLayout.LayoutParams(Ui.dp(this, 116), Ui.dp(this, 48));
        pulseRow.addView(pulseInput, pulseInputParams);
        LinearLayout.LayoutParams pulseParams = new LinearLayout.LayoutParams(-1, -2);
        pulseParams.topMargin = Ui.dp(this, 5);
        card.addView(pulseRow, pulseParams);

        mapPreview = ScreenUi.paragraph(this, getString(R.string.map_preview_empty));
        mapPreview.setTextColor(Ui.color(this, R.color.brand_teal));
        mapPreview.setTypeface(android.graphics.Typeface.create("sans-serif-medium", 0));
        LinearLayout.LayoutParams mapParams = new LinearLayout.LayoutParams(-1, -2);
        mapParams.topMargin = Ui.dp(this, 8);
        card.addView(mapPreview, mapParams);
        TextWatcher mapWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateMapPreview(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        systolicInput.addTextChangedListener(mapWatcher);
        diastolicInput.addTextChangedListener(mapWatcher);

        TextView save = ScreenUi.text(this, getString(R.string.quick_entry_save), 15,
                Ui.color(this, R.color.text_on_brand), true);
        save.setGravity(Gravity.CENTER);
        save.setBackground(Ui.roundedBox(this, Ui.color(this, R.color.brand_teal), 15));
        save.setMinHeight(Ui.dp(this, 54));
        save.setFocusable(true);
        save.setClickable(true);
        save.setContentDescription(getString(R.string.quick_entry_save));
        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(-1, Ui.dp(this, 54));
        saveParams.topMargin = Ui.dp(this, 13);
        card.addView(save, saveParams);
        save.setOnClickListener(v -> saveQuickReading());

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
        cardParams.topMargin = Ui.dp(this, 4);
        content.addView(card, cardParams);
        updateMapPreview();
    }

    private TextView fieldLabel(String text) {
        TextView label = ScreenUi.text(this, text, 12, Ui.color(this, R.color.text_secondary), true);
        return label;
    }

    private EditText numberInput(String hint, int length) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setTextSize(22);
        input.setTextColor(Ui.color(this, R.color.text_primary));
        input.setHintTextColor(Ui.color(this, R.color.text_tertiary));
        input.setHint(hint);
        input.setGravity(Gravity.CENTER);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(length)});
        input.setBackground(Ui.roundedBox(this, Ui.color(this, R.color.surface_alt), 12));
        input.setPadding(Ui.dp(this, 8), Ui.dp(this, 4), Ui.dp(this, 8), Ui.dp(this, 4));
        input.setSelectAllOnFocus(true);
        return input;
    }

    private void updateMapPreview() {
        if (mapPreview == null || systolicInput == null || diastolicInput == null) return;
        try {
            int sys = Integer.parseInt(systolicInput.getText().toString().trim());
            int dia = Integer.parseInt(diastolicInput.getText().toString().trim());
            if (sys <= dia || sys < 40 || dia < 20) throw new NumberFormatException();
            int map = Math.round((sys + 2f * dia) / 3f);
            mapPreview.setText(getString(R.string.map_preview, map));
        } catch (NumberFormatException e) {
            mapPreview.setText(getString(R.string.map_preview_empty));
        }
    }

    private void saveQuickReading() {
        final int sys;
        final int dia;
        final int pulse;
        try {
            sys = parse(systolicInput);
            dia = parse(diastolicInput);
            String pulseText = pulseInput.getText().toString().trim();
            pulse = pulseText.isEmpty() ? 0 : Integer.parseInt(pulseText);
        } catch (NumberFormatException e) {
            systolicInput.setError(getString(R.string.log_error_systolic));
            return;
        }
        if (sys < 60 || sys > 260) { systolicInput.setError(getString(R.string.log_error_systolic)); return; }
        if (dia < 30 || dia > 150) { diastolicInput.setError(getString(R.string.log_error_diastolic)); return; }
        if (pulse != 0 && (pulse < 30 || pulse > 220)) { pulseInput.setError(getString(R.string.log_error_pulse)); return; }
        if (sys <= dia) { diastolicInput.setError(getString(R.string.log_error_order)); return; }

        BloodPressureReading reading = BloodPressureReading.create(
                System.currentTimeMillis(), sys, dia, pulse, "", "manual");
        try {
            new EncryptedReadingStore(this).add(reading);
        } catch (Exception e) {
            android.util.Log.e("AfyaLinkLocalSave", "Could not persist BP reading", e);
            Toast.makeText(this, R.string.local_save_failed, Toast.LENGTH_LONG).show();
            return;
        }

        android.view.inputmethod.InputMethodManager keyboard =
                (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (keyboard != null) keyboard.hideSoftInputFromWindow(systolicInput.getWindowToken(), 0);
        Toast.makeText(this, R.string.quick_entry_saved_offline, Toast.LENGTH_SHORT).show();
        try {
            render();
        } catch (RuntimeException e) {
            android.util.Log.e("AfyaLinkLocalSave", "Reading persisted; dashboard refresh failed", e);
        }
    }

    private int parse(EditText field) { return Integer.parseInt(field.getText().toString().trim()); }

    private void renderNavigation() {
        // Keep the two most-used destinations docked above the system navigation bar.
        // The long entry form remains in the scroll view, so neither button is clipped.
        LinearLayout nav = ScreenUi.horizontal(this);
        nav.setPadding(Ui.dp(this, 20), Ui.dp(this, 10), Ui.dp(this, 20), Ui.dp(this, 8));
        nav.setBackgroundColor(Ui.color(this, R.color.canvas));
        TextView readings = navButton(getString(R.string.menu_readings));
        readings.setOnClickListener(v -> go(HistoryActivity.class));
        TextView trends = navButton(getString(R.string.menu_trends));
        trends.setOnClickListener(v -> go(TrendsActivity.class));
        LinearLayout.LayoutParams first = new LinearLayout.LayoutParams(0, Ui.dp(this, 52), 1f);
        first.rightMargin = Ui.dp(this, 6);
        nav.addView(readings, first);
        LinearLayout.LayoutParams second = new LinearLayout.LayoutParams(0, Ui.dp(this, 52), 1f);
        second.leftMargin = Ui.dp(this, 6);
        nav.addView(trends, second);
        page.addView(nav, new LinearLayout.LayoutParams(-1, -2));
    }

    private TextView navButton(String text) {
        TextView button = ScreenUi.text(this, text, 13, Ui.color(this, R.color.brand_teal), true);
        button.setGravity(Gravity.CENTER);
        button.setBackground(Ui.roundedBox(this, Ui.color(this, R.color.surface), 14,
                Ui.color(this, R.color.hairline), 1));
        button.setFocusable(true);
        button.setClickable(true);
        return button;
    }
}
