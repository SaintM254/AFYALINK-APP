package com.afyalink.app.ui;

import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import com.afyalink.app.R;
import com.afyalink.app.security.VaultKeyManager;
import com.afyalink.app.storage.EncryptedReadingStore;
import com.afyalink.app.storage.SettingsStore;
import com.afyalink.app.util.Lucide;
import com.afyalink.app.util.Ui;

public final class SettingsActivity extends BaseActivity {
    private SettingsStore settings;

    @Override protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        settings = new SettingsStore(this);
        render();
    }

    private void render() {
        installPage(getString(R.string.settings_title), true);
        content.addView(ScreenUi.eyebrow(this, getString(R.string.settings_eyebrow)));
        TextView title = ScreenUi.title(this, getString(R.string.settings_title));
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(-1, -2);
        titleParams.topMargin = Ui.dp(this, 6);
        titleParams.bottomMargin = Ui.dp(this, 16);
        content.addView(title, titleParams);

        addProfileSection();
        addAppearanceSection();
        addLinkSection(R.string.menu_reminders, R.string.settings_reminders_body,
                RemindersActivity.class);
        addLinkSection(R.string.settings_data, R.string.settings_data_body, SyncActivity.class);
        addLinkSection(R.string.settings_about, R.string.settings_about_body, AboutActivity.class);
        addClearDataSection();
    }

    private void addProfileSection() {
        LinearLayout card = ScreenUi.card(this);
        card.addView(ScreenUi.text(this, getString(R.string.settings_profile), 16,
                Ui.color(this, R.color.text_primary), true));
        TextView hint = ScreenUi.paragraph(this, getString(R.string.settings_name_hint));
        LinearLayout.LayoutParams hintParams = new LinearLayout.LayoutParams(-1, -2);
        hintParams.topMargin = Ui.dp(this, 4);
        card.addView(hint, hintParams);

        EditText name = new EditText(this);
        name.setSingleLine(true);
        name.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        name.setTextSize(16);
        name.setTextColor(Ui.color(this, R.color.text_primary));
        name.setHintTextColor(Ui.color(this, R.color.text_tertiary));
        name.setHint(getString(R.string.settings_name));
        name.setText(settings.profileName());
        name.setBackground(Ui.roundedBox(this, Ui.color(this, R.color.surface_alt), 12));
        name.setPadding(Ui.dp(this, 12), Ui.dp(this, 9), Ui.dp(this, 12), Ui.dp(this, 9));
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(-1, Ui.dp(this, 52));
        nameParams.topMargin = Ui.dp(this, 11);
        card.addView(name, nameParams);

        TextView saveName = actionButton(getString(R.string.settings_save_name));
        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(-1, Ui.dp(this, 46));
        saveParams.topMargin = Ui.dp(this, 9);
        card.addView(saveName, saveParams);
        saveName.setOnClickListener(v -> {
            settings.setProfileName(name.getText().toString());
            Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
        });
        content.addView(card);
    }

    private void addAppearanceSection() {
        LinearLayout card = ScreenUi.card(this);
        card.addView(ScreenUi.text(this, getString(R.string.settings_appearance), 16,
                Ui.color(this, R.color.text_primary), true));
        TextView hint = ScreenUi.paragraph(this, getString(R.string.settings_appearance_body));
        LinearLayout.LayoutParams hintParams = new LinearLayout.LayoutParams(-1, -2);
        hintParams.topMargin = Ui.dp(this, 4);
        card.addView(hint, hintParams);

        LinearLayout choices = ScreenUi.horizontal(this);
        choices.setGravity(Gravity.CENTER_VERTICAL);
        addAppearanceChoice(choices, "light", R.string.settings_theme_light, Lucide.SUN);
        addAppearanceChoice(choices, "dark", R.string.settings_theme_dark, Lucide.MOON);
        addAppearanceChoice(choices, "system", R.string.settings_theme_system, Lucide.SYSTEM);
        LinearLayout.LayoutParams choicesParams = new LinearLayout.LayoutParams(-1, Ui.dp(this, 46));
        choicesParams.topMargin = Ui.dp(this, 12);
        card.addView(choices, choicesParams);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
        cardParams.topMargin = Ui.dp(this, 12);
        content.addView(card, cardParams);
    }

    private void addAppearanceChoice(LinearLayout parent, String value, int labelRes, int iconRes) {
        boolean selected = value.equals(settings.appearance());
        FrameLayout choice = new FrameLayout(this);
        choice.setBackground(Ui.roundedBox(this,
                Ui.color(this, selected ? R.color.brand_teal : R.color.surface_alt), 12,
                Ui.color(this, selected ? R.color.brand_teal : R.color.hairline), 1));
        choice.setFocusable(true);
        choice.setClickable(true);
        choice.setSelected(selected);
        choice.setContentDescription(getString(labelRes) + (selected ? ", selected" : ""));
        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setImageTintList(android.content.res.ColorStateList.valueOf(
                Ui.color(this, selected ? R.color.text_on_brand : R.color.text_secondary)));
        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(Ui.dp(this, 24), Ui.dp(this, 24), Gravity.CENTER);
        choice.addView(icon, iconParams);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -1, 1f);
        if (parent.getChildCount() > 0) params.leftMargin = Ui.dp(this, 6);
        parent.addView(choice, params);
        choice.setOnClickListener(v -> {
            settings.setAppearance(value);
            int mode = "light".equals(value) ? AppCompatDelegate.MODE_NIGHT_NO
                    : "dark".equals(value) ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            AppCompatDelegate.setDefaultNightMode(mode);
        });
    }

    private void addLinkSection(int titleRes, int bodyRes, Class<?> destination) {
        LinearLayout card = ScreenUi.card(this);
        card.setFocusable(true);
        card.setClickable(true);
        card.setBackground(Ui.roundedBox(this, Ui.color(this, R.color.surface), 18,
                Ui.color(this, R.color.hairline), 1));
        card.addView(ScreenUi.text(this, getString(titleRes), 15,
                Ui.color(this, R.color.text_primary), true));
        TextView body = ScreenUi.paragraph(this, getString(bodyRes));
        LinearLayout.LayoutParams bodyParams = new LinearLayout.LayoutParams(-1, -2);
        bodyParams.topMargin = Ui.dp(this, 4);
        card.addView(body, bodyParams);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = Ui.dp(this, 12);
        content.addView(card, params);
        card.setOnClickListener(v -> go(destination));
    }

    private void addClearDataSection() {
        LinearLayout card = ScreenUi.card(this, Ui.color(this, R.color.stage_grade3_soft), false);
        card.addView(ScreenUi.text(this, getString(R.string.settings_clear_data), 15,
                Ui.color(this, R.color.stage_grade3_ink), true));
        TextView body = ScreenUi.paragraph(this, getString(R.string.settings_clear_data_body));
        body.setTextColor(Ui.color(this, R.color.stage_grade3_ink));
        LinearLayout.LayoutParams bodyParams = new LinearLayout.LayoutParams(-1, -2);
        bodyParams.topMargin = Ui.dp(this, 4);
        card.addView(body, bodyParams);
        card.setFocusable(true);
        card.setClickable(true);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = Ui.dp(this, 12);
        content.addView(card, params);
        card.setOnClickListener(v -> confirmWipe());
    }

    private TextView actionButton(String text) {
        TextView button = ScreenUi.text(this, text, 13, Ui.color(this, R.color.text_on_brand), true);
        button.setGravity(Gravity.CENTER);
        button.setBackground(Ui.roundedBox(this, Ui.color(this, R.color.brand_teal), 13));
        button.setFocusable(true);
        button.setClickable(true);
        return button;
    }

    private void confirmWipe() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(Ui.dp(this, 22), Ui.dp(this, 22), Ui.dp(this, 22), Ui.dp(this, 18));
        box.setBackground(Ui.roundedBox(this, Ui.color(this, R.color.surface), 24));
        TextView title = ScreenUi.title(this, getString(R.string.settings_clear_confirm_title));
        title.setTextSize(20);
        box.addView(title);
        TextView body = ScreenUi.paragraph(this, getString(R.string.settings_clear_confirm_body));
        LinearLayout.LayoutParams bodyParams = new LinearLayout.LayoutParams(-1, -2);
        bodyParams.topMargin = Ui.dp(this, 8);
        box.addView(body, bodyParams);
        TextView erase = actionButton(getString(R.string.settings_clear_data));
        erase.setBackground(Ui.roundedBox(this, Ui.color(this, R.color.danger), 14));
        LinearLayout.LayoutParams eraseParams = new LinearLayout.LayoutParams(-1, Ui.dp(this, 50));
        eraseParams.topMargin = Ui.dp(this, 16);
        box.addView(erase, eraseParams);
        erase.setOnClickListener(v -> {
            try {
                new EncryptedReadingStore(this).clear();
                VaultKeyManager.deleteAll();
                Toast.makeText(this, R.string.settings_cleared, Toast.LENGTH_LONG).show();
                dialog.dismiss();
                render();
            } catch (Exception e) {
                Toast.makeText(this, R.string.local_save_failed, Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
        dialog.setContentView(box);
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * .88), -2);
        }
    }
}
