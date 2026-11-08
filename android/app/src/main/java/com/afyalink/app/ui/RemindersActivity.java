package com.afyalink.app.ui;

import android.app.TimePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.afyalink.app.R;
import com.afyalink.app.reminder.ReminderScheduler;
import com.afyalink.app.storage.ReminderStore;
import com.afyalink.app.util.Format;
import com.afyalink.app.util.Lucide;
import com.afyalink.app.util.Ui;

import java.util.Calendar;

public final class RemindersActivity extends BaseActivity {
    private ReminderStore store;
    private TextView timeValue;
    @Override protected void onCreate(@Nullable Bundle state) { super.onCreate(state); store=new ReminderStore(this); render(); }
    private void render() {
        installPage(getString(R.string.reminders_title), true);
        content.addView(ScreenUi.eyebrow(this,getString(R.string.reminders_eyebrow)));
        TextView title=ScreenUi.title(this,getString(R.string.reminders_title));
        LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(-1,-2);tp.topMargin=Ui.dp(this,7);content.addView(title,tp);
        LinearLayout card=ScreenUi.card(this);
        LinearLayout row=ScreenUi.horizontal(this);
        TextView icon=ScreenUi.text(this,"",20,Ui.color(this,R.color.brand_teal),true);
        icon.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_bell,0,0,0);
        icon.setCompoundDrawableTintList(android.content.res.ColorStateList.valueOf(Ui.color(this,R.color.brand_teal)));
        icon.setContentDescription(getString(R.string.cd_reminders));
        row.addView(icon,new LinearLayout.LayoutParams(Ui.dp(this,40),Ui.dp(this,40)));
        LinearLayout copy=new LinearLayout(this);copy.setOrientation(LinearLayout.VERTICAL);
        TextView name=ScreenUi.text(this,getString(R.string.reminders_type_measure),15,Ui.color(this,R.color.text_primary),true);copy.addView(name);
        TextView description=ScreenUi.paragraph(this,getString(R.string.reminders_empty));description.setTextSize(12);
        LinearLayout.LayoutParams dp=new LinearLayout.LayoutParams(-1,-2);dp.topMargin=Ui.dp(this,4);copy.addView(description,dp);
        LinearLayout.LayoutParams cpy=new LinearLayout.LayoutParams(0,-2,1f);cpy.leftMargin=Ui.dp(this,9);row.addView(copy,cpy);
        SwitchCompat toggle=new SwitchCompat(this);toggle.setChecked(store.enabled());toggle.setContentDescription(getString(R.string.reminders_type_measure));row.addView(toggle);
        card.addView(row);
        card.addView(ScreenUi.hairline(this));
        LinearLayout timeRow=ScreenUi.horizontal(this);
        TextView timeLabel=ScreenUi.text(this,getString(R.string.log_measured_at),14,Ui.color(this,R.color.text_primary),true);
        timeRow.addView(timeLabel,new LinearLayout.LayoutParams(0,-2,1f));
        timeValue=ScreenUi.text(this,timeString(),16,Ui.color(this,R.color.brand_teal),true);timeRow.addView(timeValue);
        androidx.appcompat.widget.AppCompatImageButton choose=new androidx.appcompat.widget.AppCompatImageButton(this);
        choose.setImageResource(Lucide.CLOCK);choose.setImageTintList(android.content.res.ColorStateList.valueOf(Ui.color(this,R.color.brand_teal)));
        choose.setContentDescription(getString(R.string.cd_clock));choose.setBackgroundResource(android.R.drawable.list_selector_background);
        choose.setPadding(Ui.dp(this,12),Ui.dp(this,12),Ui.dp(this,12),Ui.dp(this,12));
        timeRow.addView(choose,new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48)));card.addView(timeRow);
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.topMargin=Ui.dp(this,18);content.addView(card,cp);
        TextView note=ScreenUi.paragraph(this,"A gentle daily notification, scheduled locally on this device. It contains no blood-pressure values and works without a network connection.");
        LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(-1,-2);np.topMargin=Ui.dp(this,14);content.addView(note,np);
        toggle.setOnCheckedChangeListener((button,checked)->{
            store.save(checked,store.hour(),store.minute()); ReminderScheduler.update(this);
            if(checked && Build.VERSION.SDK_INT>=33 && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)!=android.content.pm.PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS},401);
        });
        choose.setOnClickListener(v->new TimePickerDialog(this,(picker,hour,minute)->{
            store.save(store.enabled(),hour,minute);ReminderScheduler.update(this);timeValue.setText(timeString());
            Toast.makeText(this,R.string.reminders_saved,Toast.LENGTH_SHORT).show();
        },store.hour(),store.minute(),android.text.format.DateFormat.is24HourFormat(this)).show());
    }
    private String timeString() {
        Calendar c=Calendar.getInstance();c.set(Calendar.HOUR_OF_DAY,store.hour());c.set(Calendar.MINUTE,store.minute());
        return android.text.format.DateFormat.getTimeFormat(this).format(c.getTime());
    }
}
