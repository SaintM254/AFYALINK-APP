package com.afyalink.app.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;

import com.afyalink.app.R;
import com.afyalink.app.domain.BloodPressureReading;
import com.afyalink.app.domain.BpStage;
import com.afyalink.app.storage.EncryptedReadingStore;
import com.afyalink.app.util.Format;
import com.afyalink.app.util.Lucide;
import com.afyalink.app.util.Ui;

import java.util.Calendar;
import java.util.UUID;

public final class LogReadingActivity extends BaseActivity {
    private EditText systolic, diastolic, pulse, note;
    private long measuredAt = System.currentTimeMillis();
    private BloodPressureReading editing;
    private TextView dateValue;

    @Override protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        String id = getIntent().getStringExtra("reading_id");
        if (id != null) {
            for (BloodPressureReading r : com.afyalink.app.storage.AppData.readings(this)) if (r.id.equals(id)) editing = r;
        }
        render();
    }

    private void render() {
        installPage(getString(editing == null ? R.string.log_new_title : R.string.log_detail_title), true);
        content.addView(ScreenUi.eyebrow(this, getString(editing == null ? R.string.log_new_eyebrow : R.string.log_detail_eyebrow)));
        TextView title = ScreenUi.title(this, getString(editing == null ? R.string.log_new_title : R.string.log_detail_title));
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(-1, -2);
        tp.topMargin = Ui.dp(this, 7);
        tp.bottomMargin = Ui.dp(this, 18);
        content.addView(title, tp);

        LinearLayout card = ScreenUi.card(this);
        addPressureField(card, getString(R.string.log_systolic), getString(R.string.log_systolic_hint), 260, true);
        card.addView(ScreenUi.hairline(this));
        addPressureField(card, getString(R.string.log_diastolic), getString(R.string.log_diastolic_hint), 150, false);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.bottomMargin = Ui.dp(this, 14);
        content.addView(card, cp);

        LinearLayout meta = ScreenUi.card(this);
        addRegularField(meta, getString(R.string.log_pulse), getString(R.string.log_pulse_hint), true);
        meta.addView(ScreenUi.hairline(this));
        addDateRow(meta);
        meta.addView(ScreenUi.hairline(this));
        addNotes(meta);
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(-1, -2);
        mp.bottomMargin = Ui.dp(this, 14);
        content.addView(meta, mp);

        LinearLayout preview = ScreenUi.horizontal(this);
        TextView previewText = ScreenUi.paragraph(this, getString(R.string.log_encrypted_note));
        previewText.setTextSize(12);
        preview.addView(previewText, new LinearLayout.LayoutParams(0, -2, 1f));
        AppCompatImageButton save = new AppCompatImageButton(this);
        save.setImageResource(Lucide.CHECK);
        save.setImageTintList(ColorStateList.valueOf(Ui.color(this, R.color.brand_white)));
        save.setBackground(Ui.roundedBox(this, Ui.color(this, R.color.brand_teal), 18));
        save.setContentDescription(getString(R.string.log_save));
        save.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 16));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(Ui.dp(this, 58), Ui.dp(this, 58));
        sp.leftMargin = Ui.dp(this, 12);
        preview.addView(save, sp);
        content.addView(preview);
        save.setOnClickListener(v -> save());

        if (editing != null) {
            AppCompatImageButton delete = new AppCompatImageButton(this);
            delete.setImageResource(Lucide.DELETE);
            delete.setImageTintList(ColorStateList.valueOf(Ui.color(this, R.color.danger)));
            delete.setBackgroundResource(android.R.drawable.list_selector_background);
            delete.setContentDescription(getString(R.string.cd_delete));
            delete.setPadding(Ui.dp(this, 13), Ui.dp(this, 13), Ui.dp(this, 13), Ui.dp(this, 13));
            LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(Ui.dp(this, 52), Ui.dp(this, 52));
            dp.gravity = Gravity.END;
            dp.topMargin = Ui.dp(this, 14);
            content.addView(delete, dp);
            delete.setOnClickListener(v -> confirmDelete());
        }

        if (editing != null) {
            systolic.setText(String.valueOf(editing.systolic));
            diastolic.setText(String.valueOf(editing.diastolic));
            pulse.setText(editing.pulse == 0 ? "" : String.valueOf(editing.pulse));
            note.setText(editing.note);
            measuredAt = editing.measuredAt;
            updateDate();
        }
    }

    private void addPressureField(LinearLayout parent, String title, String hint, int max, boolean isSys) {
        LinearLayout row = ScreenUi.horizontal(this);
        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        TextView label = ScreenUi.text(this, title, 14, Ui.color(this, R.color.text_primary), true);
        labels.addView(label);
        TextView unit = ScreenUi.paragraph(this, "mmHg · " + hint);
        unit.setTextSize(11);
        LinearLayout.LayoutParams up = new LinearLayout.LayoutParams(-1, -2);
        up.topMargin = Ui.dp(this, 5);
        labels.addView(unit, up);
        row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1f));
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setTextSize(29);
        input.setTypeface(android.graphics.Typeface.create("sans-serif-medium", 0));
        input.setTextColor(Ui.color(this, R.color.text_primary));
        input.setHintTextColor(Ui.color(this, R.color.text_tertiary));
        input.setHint(isSys ? "120" : "80");
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        input.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        input.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        input.setSelectAllOnFocus(true);
        input.setContentDescription(title + " in millimetres of mercury");
        row.addView(input, new LinearLayout.LayoutParams(Ui.dp(this, 110), Ui.dp(this, 68)));
        parent.addView(row);
        if (isSys) systolic = input; else diastolic = input;
    }

    private void addRegularField(LinearLayout parent, String title, String hint, boolean pulseField) {
        LinearLayout row = ScreenUi.horizontal(this);
        TextView label = ScreenUi.text(this, title, 14, Ui.color(this, R.color.text_primary), true);
        row.addView(label, new LinearLayout.LayoutParams(0, -2, 1f));
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setTextSize(18);
        input.setTextColor(Ui.color(this, R.color.text_primary));
        input.setHintTextColor(Ui.color(this, R.color.text_tertiary));
        input.setHint(hint);
        input.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        input.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        input.setContentDescription(title);
        row.addView(input, new LinearLayout.LayoutParams(Ui.dp(this, 142), Ui.dp(this, 54)));
        parent.addView(row);
        pulse = input;
    }

    private void addDateRow(LinearLayout parent) {
        LinearLayout row = ScreenUi.horizontal(this);
        TextView label = ScreenUi.text(this, getString(R.string.log_measured_at), 14,
                Ui.color(this, R.color.text_primary), true);
        row.addView(label, new LinearLayout.LayoutParams(0, -2, 1f));
        dateValue = ScreenUi.paragraph(this, Format.date(this, measuredAt));
        dateValue.setTextColor(Ui.color(this, R.color.text_primary));
        row.addView(dateValue);
        AppCompatImageButton choose = new AppCompatImageButton(this);
        choose.setImageResource(Lucide.CALENDAR);
        choose.setImageTintList(ColorStateList.valueOf(Ui.color(this, R.color.brand_teal)));
        choose.setBackgroundResource(android.R.drawable.list_selector_background);
        choose.setContentDescription(getString(R.string.cd_calendar));
        choose.setPadding(Ui.dp(this, 11), Ui.dp(this, 11), Ui.dp(this, 11), Ui.dp(this, 11));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(Ui.dp(this, 46), Ui.dp(this, 46));
        row.addView(choose, cp);
        choose.setOnClickListener(v -> datePicker());
        parent.addView(row);
    }

    private void addNotes(LinearLayout parent) {
        TextView label = ScreenUi.text(this, getString(R.string.log_notes), 14,
                Ui.color(this, R.color.text_primary), true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = Ui.dp(this, 14);
        parent.addView(label, lp);
        note = new EditText(this);
        note.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        note.setMinLines(2);
        note.setMaxLines(4);
        note.setTextSize(14);
        note.setTextColor(Ui.color(this, R.color.text_primary));
        note.setHintTextColor(Ui.color(this, R.color.text_tertiary));
        note.setHint(getString(R.string.log_notes_hint));
        note.setBackground(Ui.roundedBox(this, Ui.color(this, R.color.surface_alt), 14));
        note.setPadding(Ui.dp(this, 12), Ui.dp(this, 11), Ui.dp(this, 12), Ui.dp(this, 11));
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(-1, -2);
        np.topMargin = Ui.dp(this, 9);
        parent.addView(note, np);
    }

    private void datePicker() {
        Calendar cal = Calendar.getInstance(); cal.setTimeInMillis(measuredAt);
        new DatePickerDialog(this, (picker, year, month, day) -> {
            Calendar picked = Calendar.getInstance();
            picked.setTimeInMillis(measuredAt);
            picked.set(Calendar.YEAR, year); picked.set(Calendar.MONTH, month); picked.set(Calendar.DAY_OF_MONTH, day);
            measuredAt = picked.getTimeInMillis();
            Calendar now = Calendar.getInstance();
            new TimePickerDialog(this, (timePicker, hour, minute) -> {
                picked.set(Calendar.HOUR_OF_DAY, hour); picked.set(Calendar.MINUTE, minute); picked.set(Calendar.SECOND, 0);
                measuredAt = picked.getTimeInMillis(); updateDate();
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), android.text.format.DateFormat.is24HourFormat(this)).show();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDate() { if (dateValue != null) dateValue.setText(Format.date(this, measuredAt)); }

    private void save() {
        try {
            int sys = parse(systolic), dia = parse(diastolic);
            int bpm = pulse.getText().toString().trim().isEmpty() ? 0 : parse(pulse);
            if (sys < 60 || sys > 260) { systolic.setError(getString(R.string.log_error_systolic)); return; }
            if (dia < 30 || dia > 150) { diastolic.setError(getString(R.string.log_error_diastolic)); return; }
            if (bpm != 0 && (bpm < 30 || bpm > 220)) { pulse.setError(getString(R.string.log_error_pulse)); return; }
            if (sys <= dia) { diastolic.setError(getString(R.string.log_error_order)); return; }
            BloodPressureReading reading = new BloodPressureReading(
                    editing == null ? UUID.randomUUID().toString() : editing.id, measuredAt, sys, dia, bpm,
                    note.getText().toString(), "manual", false);
            EncryptedReadingStore store = new EncryptedReadingStore(this);
            if (editing == null) store.add(reading); else store.update(reading);
            Toast.makeText(this, getString(editing == null ? R.string.log_saved : R.string.log_updated), Toast.LENGTH_SHORT).show();
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(note.getWindowToken(), 0);
            finish();
        } catch (NumberFormatException e) {
            systolic.setError(getString(R.string.log_error_systolic));
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.sync_failed), Toast.LENGTH_LONG).show();
        }
    }

    private int parse(EditText field) { return Integer.parseInt(field.getText().toString().trim()); }

    private void confirmDelete() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(Ui.dp(this, 22), Ui.dp(this, 22), Ui.dp(this, 22), Ui.dp(this, 18));
        box.setBackground(Ui.roundedBox(this, Ui.color(this, R.color.surface), 24));
        TextView title = ScreenUi.title(this, getString(R.string.log_delete_title));
        title.setTextSize(20);
        box.addView(title);
        TextView body = ScreenUi.paragraph(this, getString(R.string.log_delete_body));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, -2); bp.topMargin = Ui.dp(this, 8); box.addView(body, bp);
        LinearLayout actions = ScreenUi.horizontal(this);
        actions.setGravity(Gravity.END);
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(-1, -2); ap.topMargin = Ui.dp(this, 16); box.addView(actions, ap);
        AppCompatImageButton cancel = new AppCompatImageButton(this);
        cancel.setImageResource(Lucide.CLOSE); cancel.setContentDescription(getString(R.string.cd_close));
        cancel.setBackgroundResource(android.R.drawable.list_selector_background);
        actions.addView(cancel, new LinearLayout.LayoutParams(Ui.dp(this, 50), Ui.dp(this, 50)));
        AppCompatImageButton yes = new AppCompatImageButton(this);
        yes.setImageResource(Lucide.DELETE); yes.setContentDescription(getString(R.string.cd_delete));
        yes.setImageTintList(ColorStateList.valueOf(Ui.color(this, R.color.brand_white)));
        yes.setBackground(Ui.roundedBox(this, Ui.color(this, R.color.danger), 16));
        LinearLayout.LayoutParams yp = new LinearLayout.LayoutParams(Ui.dp(this, 50), Ui.dp(this, 50)); yp.leftMargin = Ui.dp(this, 8); actions.addView(yes, yp);
        cancel.setOnClickListener(v -> dialog.dismiss());
        yes.setOnClickListener(v -> { try { new EncryptedReadingStore(this).delete(editing.id); Toast.makeText(this,R.string.log_deleted,Toast.LENGTH_SHORT).show(); finish(); } catch (Exception e) { Toast.makeText(this,R.string.sync_failed,Toast.LENGTH_SHORT).show(); } dialog.dismiss(); });
        dialog.setContentView(box); if (dialog.getWindow()!=null) { dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); dialog.getWindow().setLayout((int)(getResources().getDisplayMetrics().widthPixels*.88),-2); }
        dialog.show(); if (dialog.getWindow()!=null) { dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); dialog.getWindow().setLayout((int)(getResources().getDisplayMetrics().widthPixels*.88),-2); }
    }
}
