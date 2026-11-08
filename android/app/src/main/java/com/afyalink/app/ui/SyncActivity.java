package com.afyalink.app.ui;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;

import com.afyalink.app.R;
import com.afyalink.app.domain.BloodPressureReading;
import com.afyalink.app.storage.EncryptedReadingStore;
import com.afyalink.app.sync.DemoRelay;
import com.afyalink.app.sync.IReadingSync;
import com.afyalink.app.util.Lucide;
import com.afyalink.app.util.Ui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Demo sync UI. The only available relay is local and deliberately has no network code. */
public final class SyncActivity extends BaseActivity {
    private TextView queueText, statusText;
    private AppCompatImageButton syncButton;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    @Override protected void onCreate(@Nullable Bundle state) { super.onCreate(state); render(); }
    @Override protected void onResume() { super.onResume(); render(); }
    @Override protected void onDestroy() { super.onDestroy(); executor.shutdownNow(); super.onDestroy(); }

    private void render() {
        installPage(getString(R.string.sync_title), true);
        content.addView(ScreenUi.eyebrow(this,getString(R.string.sync_eyebrow)));
        TextView title=ScreenUi.title(this,getString(R.string.sync_title));
        LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(-1,-2);tp.topMargin=Ui.dp(this,7);content.addView(title,tp);

        LinearLayout mode=ScreenUi.card(this,Ui.color(this,R.color.brand_teal),false);
        LinearLayout header=ScreenUi.horizontal(this);
        TextView modeTitle=ScreenUi.text(this,getString(R.string.sync_mode_demo),19,Ui.color(this,R.color.text_on_brand),true);
        header.addView(modeTitle,new LinearLayout.LayoutParams(0,-2,1f));
        TextView badge=Ui.stagePill(this,"DEMO",Ui.color(this,R.color.brand_white),Ui.color(this,R.color.brand_teal));header.addView(badge);
        mode.addView(header);
        TextView modeBody=ScreenUi.paragraph(this,getString(R.string.sync_mode_demo_body));modeBody.setTextColor(Ui.color(this,R.color.text_on_brand_muted));
        LinearLayout.LayoutParams mb=new LinearLayout.LayoutParams(-1,-2);mb.topMargin=Ui.dp(this,12);mode.addView(modeBody,mb);
        LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(-1,-2);mp.topMargin=Ui.dp(this,18);mp.bottomMargin=Ui.dp(this,14);content.addView(mode,mp);

        LinearLayout queue=ScreenUi.card(this);
        LinearLayout qrow=ScreenUi.horizontal(this);
        LinearLayout qcopy=new LinearLayout(this);qcopy.setOrientation(LinearLayout.VERTICAL);
        queueText=ScreenUi.text(this,"",25,Ui.color(this,R.color.text_primary),true);qcopy.addView(queueText);
        TextView sub=ScreenUi.paragraph(this,getString(R.string.sync_queued_none));sub.setTextSize(12);
        LinearLayout.LayoutParams subp=new LinearLayout.LayoutParams(-1,-2);subp.topMargin=Ui.dp(this,5);qcopy.addView(sub,subp);
        qrow.addView(qcopy,new LinearLayout.LayoutParams(0,-2,1f));
        syncButton=new AppCompatImageButton(this);syncButton.setImageResource(Lucide.SYNC);
        syncButton.setImageTintList(ColorStateList.valueOf(Ui.color(this,R.color.brand_white)));
        syncButton.setBackground(Ui.roundedBox(this,Ui.color(this,R.color.brand_teal),18));
        syncButton.setContentDescription(getString(R.string.sync_now));syncButton.setPadding(Ui.dp(this,16),Ui.dp(this,16),Ui.dp(this,16),Ui.dp(this,16));
        qrow.addView(syncButton,new LinearLayout.LayoutParams(Ui.dp(this,56),Ui.dp(this,56)));
        queue.addView(qrow);
        statusText=ScreenUi.paragraph(this,getString(R.string.sync_queued_none));
        LinearLayout.LayoutParams stp=new LinearLayout.LayoutParams(-1,-2);stp.topMargin=Ui.dp(this,12);queue.addView(statusText,stp);
        LinearLayout.LayoutParams qp=new LinearLayout.LayoutParams(-1,-2);qp.bottomMargin=Ui.dp(this,14);content.addView(queue,qp);
        syncButton.setOnClickListener(v -> sync());
        refreshQueue();

        LinearLayout why=ScreenUi.card(this,Ui.color(this,R.color.surface),true);
        LinearLayout titleRow=ScreenUi.horizontal(this);
        android.widget.ImageView shield=Ui.icon(this,Lucide.SHIELD,Ui.color(this,R.color.brand_teal),22);
        titleRow.addView(shield,new LinearLayout.LayoutParams(Ui.dp(this,30),Ui.dp(this,30)));
        TextView whyTitle=ScreenUi.text(this,getString(R.string.sync_compliance_title),16,Ui.color(this,R.color.text_primary),true);
        LinearLayout.LayoutParams wtp=new LinearLayout.LayoutParams(0,-2,1f);wtp.leftMargin=Ui.dp(this,8);titleRow.addView(whyTitle,wtp);why.addView(titleRow);
        TextView whyBody=ScreenUi.paragraph(this,getString(R.string.sync_compliance_body));
        LinearLayout.LayoutParams wb=new LinearLayout.LayoutParams(-1,-2);wb.topMargin=Ui.dp(this,12);why.addView(whyBody,wb);
        content.addView(why);

        LinearLayout detail=ScreenUi.card(this,Ui.color(this,R.color.brand_teal_soft),false);
        detail.addView(ScreenUi.text(this,getString(R.string.sync_security_details),15,Ui.color(this,R.color.brand_teal_deep),true));
        TextView detailBody=ScreenUi.paragraph(this,getString(R.string.sync_session_info));
        LinearLayout.LayoutParams db=new LinearLayout.LayoutParams(-1,-2);db.topMargin=Ui.dp(this,9);detail.addView(detailBody,db);
        LinearLayout.LayoutParams dlp=new LinearLayout.LayoutParams(-1,-2);dlp.topMargin=Ui.dp(this,14);content.addView(detail,dlp);
    }

    private void refreshQueue() {
        try {
            int count = new EncryptedReadingStore(this).pendingCount();
            queueText.setText(getString(count == 0 ? R.string.sync_queued_none : R.string.sync_queued, count));
            syncButton.setEnabled(count > 0);
            syncButton.setAlpha(count > 0 ? 1f : .45f);
            if (count == 0 && statusText != null) statusText.setText(getString(R.string.sync_nothing));
        } catch (Exception e) {
            queueText.setText(getString(R.string.sync_failed));
            syncButton.setEnabled(false);
        }
    }

    private void sync() {
        syncButton.setEnabled(false);
        statusText.setText(getString(R.string.sync_running));
        executor.execute(() -> {
            try {
                EncryptedReadingStore store = new EncryptedReadingStore(getApplicationContext());
                List<BloodPressureReading> all=store.all();
                List<BloodPressureReading> pending=new ArrayList<>();List<String> ids=new ArrayList<>();
                for(BloodPressureReading r:all)if(!r.synced){pending.add(r);ids.add(r.id);}
                IReadingSync.SyncReceipt receipt=new DemoRelay().transmit(pending);
                if(receipt.demoOnly && receipt.acceptedCount==pending.size()) store.markSynced(ids);
                runOnUiThread(()->{
                    if(receipt.acceptedCount==0) statusText.setText(getString(R.string.sync_nothing));
                    else statusText.setText(getString(R.string.sync_done,receipt.acceptedCount));
                    refreshQueue();
                });
            } catch(Exception e) {
                runOnUiThread(()->{statusText.setText(getString(R.string.sync_failed));syncButton.setEnabled(true);});
            }
        });
    }
}
