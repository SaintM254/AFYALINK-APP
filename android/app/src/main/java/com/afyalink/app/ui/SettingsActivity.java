package com.afyalink.app.ui;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.SwitchCompat;

import com.afyalink.app.R;
import com.afyalink.app.security.SecurityGate;
import com.afyalink.app.security.VaultKeyManager;
import com.afyalink.app.storage.EncryptedReadingStore;
import com.afyalink.app.storage.SettingsStore;
import com.afyalink.app.util.Lucide;
import com.afyalink.app.util.Ui;

public final class SettingsActivity extends BaseActivity {
    private SettingsStore settings;
    private TextView appearanceValue;
    @Override protected void onCreate(@Nullable Bundle state) { super.onCreate(state);settings=new SettingsStore(this);render(); }

    private void render() {
        installPage(getString(R.string.settings_title),true);
        content.addView(ScreenUi.eyebrow(this,getString(R.string.settings_eyebrow)));
        TextView title=ScreenUi.title(this,getString(R.string.settings_title));
        LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(-1,-2);tp.topMargin=Ui.dp(this,7);content.addView(title,tp);

        LinearLayout appearance=ScreenUi.card(this);
        LinearLayout arow=ScreenUi.horizontal(this);
        TextView label=ScreenUi.text(this,getString(R.string.settings_appearance),16,Ui.color(this,R.color.text_primary),true);
        arow.addView(label,new LinearLayout.LayoutParams(0,-2,1f));
        appearanceValue=ScreenUi.paragraph(this,appearanceLabel());appearanceValue.setTextColor(Ui.color(this,R.color.brand_teal));
        arow.addView(appearanceValue);
        AppCompatImageButton toggleAppearance=new AppCompatImageButton(this);
        toggleAppearance.setImageResource(Lucide.MOON);toggleAppearance.setImageTintList(ColorStateList.valueOf(Ui.color(this,R.color.brand_teal)));
        toggleAppearance.setContentDescription(getString(R.string.cd_theme));toggleAppearance.setBackgroundResource(android.R.drawable.list_selector_background);
        toggleAppearance.setPadding(Ui.dp(this,12),Ui.dp(this,12),Ui.dp(this,12),Ui.dp(this,12));
        arow.addView(toggleAppearance,new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48)));
        appearance.addView(arow);
        content.addView(appearance);
        toggleAppearance.setOnClickListener(v->cycleAppearance());

        LinearLayout security=ScreenUi.card(this);
        LinearLayout sr=ScreenUi.horizontal(this);
        android.widget.ImageView shield=Ui.icon(this,Lucide.LOCK,Ui.color(this,R.color.brand_teal),22);
        sr.addView(shield,new LinearLayout.LayoutParams(Ui.dp(this,34),Ui.dp(this,34)));
        LinearLayout scopy=new LinearLayout(this);scopy.setOrientation(LinearLayout.VERTICAL);
        TextView st=ScreenUi.text(this,getString(R.string.settings_biometric),15,Ui.color(this,R.color.text_primary),true);scopy.addView(st);
        TextView sb=ScreenUi.paragraph(this,getString(R.string.settings_biometric_body));sb.setTextSize(12);
        LinearLayout.LayoutParams sbp=new LinearLayout.LayoutParams(-1,-2);sbp.topMargin=Ui.dp(this,4);scopy.addView(sb,sbp);
        LinearLayout.LayoutParams scp=new LinearLayout.LayoutParams(0,-2,1f);scp.leftMargin=Ui.dp(this,8);sr.addView(scopy,scp);
        SwitchCompat lock=new SwitchCompat(this);lock.setChecked(settings.biometricLockEnabled());lock.setContentDescription(getString(R.string.settings_biometric));sr.addView(lock);
        security.addView(sr);
        LinearLayout.LayoutParams secp=new LinearLayout.LayoutParams(-1,-2);secp.topMargin=Ui.dp(this,14);content.addView(security,secp);
        installLockListener(lock);

        LinearLayout reminder=ScreenUi.card(this);
        LinearLayout rr=ScreenUi.horizontal(this);
        TextView rt=ScreenUi.text(this,getString(R.string.menu_reminders),15,Ui.color(this,R.color.text_primary),true);rr.addView(rt,new LinearLayout.LayoutParams(0,-2,1f));
        AppCompatImageButton rb=new AppCompatImageButton(this);rb.setImageResource(Lucide.BELL);rb.setImageTintList(ColorStateList.valueOf(Ui.color(this,R.color.brand_teal)));rb.setContentDescription(getString(R.string.cd_reminders));rb.setBackgroundResource(android.R.drawable.list_selector_background);rb.setPadding(Ui.dp(this,12),Ui.dp(this,12),Ui.dp(this,12),Ui.dp(this,12));rr.addView(rb,new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48)));reminder.addView(rr);
        LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2);rp.topMargin=Ui.dp(this,14);content.addView(reminder,rp);rb.setOnClickListener(v->go(RemindersActivity.class));

        LinearLayout about=ScreenUi.card(this);
        LinearLayout ar=ScreenUi.horizontal(this);
        TextView ab=ScreenUi.text(this,getString(R.string.settings_privacy),15,Ui.color(this,R.color.text_primary),true);ar.addView(ab,new LinearLayout.LayoutParams(0,-2,1f));
        AppCompatImageButton abtn=new AppCompatImageButton(this);abtn.setImageResource(Lucide.SHIELD);abtn.setImageTintList(ColorStateList.valueOf(Ui.color(this,R.color.brand_teal)));abtn.setContentDescription(getString(R.string.cd_security));abtn.setBackgroundResource(android.R.drawable.list_selector_background);abtn.setPadding(Ui.dp(this,12),Ui.dp(this,12),Ui.dp(this,12),Ui.dp(this,12));ar.addView(abtn,new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48)));about.addView(ar);
        LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(-1,-2);ap.topMargin=Ui.dp(this,14);content.addView(about,ap);abtn.setOnClickListener(v->go(AboutActivity.class));

        LinearLayout danger=ScreenUi.card(this,Ui.color(this,R.color.stage_grade3_soft),false);
        LinearLayout dr=ScreenUi.horizontal(this);
        TextView dt=ScreenUi.text(this,getString(R.string.settings_clear_data),15,Ui.color(this,R.color.stage_grade3_ink),true);dr.addView(dt,new LinearLayout.LayoutParams(0,-2,1f));
        AppCompatImageButton wipe=new AppCompatImageButton(this);wipe.setImageResource(Lucide.DELETE);wipe.setImageTintList(ColorStateList.valueOf(Ui.color(this,R.color.stage_grade3_ink)));wipe.setContentDescription(getString(R.string.settings_clear_data));wipe.setBackgroundResource(android.R.drawable.list_selector_background);wipe.setPadding(Ui.dp(this,12),Ui.dp(this,12),Ui.dp(this,12),Ui.dp(this,12));dr.addView(wipe,new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48)));danger.addView(dr);
        TextView dangerNote=ScreenUi.paragraph(this,getString(R.string.settings_clear_data_body));dangerNote.setTextColor(Ui.color(this,R.color.stage_grade3_ink));LinearLayout.LayoutParams dn=new LinearLayout.LayoutParams(-1,-2);dn.topMargin=Ui.dp(this,6);danger.addView(dangerNote,dn);
        LinearLayout.LayoutParams dlp=new LinearLayout.LayoutParams(-1,-2);dlp.topMargin=Ui.dp(this,14);content.addView(danger,dlp);
        wipe.setOnClickListener(v->confirmWipe());
    }


    private void installLockListener(SwitchCompat lock) {
        lock.setOnCheckedChangeListener((button, checked) -> {
            if (checked && !SecurityGate.available(this)) {
                button.setOnCheckedChangeListener(null);
                button.setChecked(false);
                settings.setBiometricLockEnabled(false);
                Toast.makeText(this, R.string.settings_biometric_unavailable, Toast.LENGTH_LONG).show();
                installLockListener(lock);
                return;
            }
            settings.setBiometricLockEnabled(checked);
        });
    }

    private String appearanceLabel() {
        String value=settings.appearance();
        if("light".equals(value))return getString(R.string.settings_theme_light);
        if("dark".equals(value))return getString(R.string.settings_theme_dark);
        return getString(R.string.settings_theme_system);
    }
    private void cycleAppearance() {
        String next="system".equals(settings.appearance())?"light":"light".equals(settings.appearance())?"dark":"system";
        settings.setAppearance(next);
        int mode="light".equals(next)?AppCompatDelegate.MODE_NIGHT_NO:"dark".equals(next)?AppCompatDelegate.MODE_NIGHT_YES:AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        AppCompatDelegate.setDefaultNightMode(mode);
        appearanceValue.setText(appearanceLabel());
    }
    private void confirmWipe() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(Ui.dp(this,22),Ui.dp(this,22),Ui.dp(this,22),Ui.dp(this,18));
        box.setBackground(Ui.roundedBox(this,Ui.color(this,R.color.surface),24));
        TextView title = ScreenUi.title(this,getString(R.string.settings_clear_confirm_title));
        title.setTextSize(20);
        box.addView(title);
        TextView body = ScreenUi.paragraph(this,getString(R.string.settings_clear_confirm_body));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1,-2);bp.topMargin=Ui.dp(this,8);box.addView(body,bp);
        LinearLayout actions = ScreenUi.horizontal(this);actions.setGravity(android.view.Gravity.END);
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(-1,-2);ap.topMargin=Ui.dp(this,16);box.addView(actions,ap);
        AppCompatImageButton cancel = new AppCompatImageButton(this);
        cancel.setImageResource(Lucide.CLOSE);cancel.setContentDescription(getString(R.string.cd_close));
        cancel.setBackgroundResource(android.R.drawable.list_selector_background);
        actions.addView(cancel,new LinearLayout.LayoutParams(Ui.dp(this,50),Ui.dp(this,50)));
        AppCompatImageButton erase = new AppCompatImageButton(this);
        erase.setImageResource(Lucide.DELETE);erase.setContentDescription(getString(R.string.settings_clear_data));
        erase.setImageTintList(ColorStateList.valueOf(Ui.color(this,R.color.brand_white)));
        erase.setBackground(Ui.roundedBox(this,Ui.color(this,R.color.danger),16));
        LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(Ui.dp(this,50),Ui.dp(this,50));ep.leftMargin=Ui.dp(this,8);actions.addView(erase,ep);
        cancel.setOnClickListener(v->dialog.dismiss());
        erase.setOnClickListener(v->{
            try { new EncryptedReadingStore(this).clear(); VaultKeyManager.deleteAll(); Toast.makeText(this,R.string.settings_cleared,Toast.LENGTH_LONG).show(); finish(); }
            catch(Exception e){Toast.makeText(this,R.string.sync_failed,Toast.LENGTH_LONG).show();}
            dialog.dismiss();
        });
        dialog.setContentView(box);dialog.show();
        if(dialog.getWindow()!=null){dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);dialog.getWindow().setLayout((int)(getResources().getDisplayMetrics().widthPixels*.88),-2);}
    }
}
