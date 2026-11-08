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
import com.afyalink.app.util.Charts;
import com.afyalink.app.util.Format;
import com.afyalink.app.util.Lucide;
import com.afyalink.app.util.Ui;

import java.util.List;

public final class TrendsActivity extends BaseActivity {
    private int days = 7;
    @Override protected void onCreate(@Nullable Bundle state) { super.onCreate(state); render(); }
    @Override protected void onResume() { super.onResume(); render(); }

    private void render() {
        installPage(getString(R.string.trends_title), true);
        content.addView(ScreenUi.eyebrow(this, getString(R.string.trends_eyebrow)));
        TextView title = ScreenUi.title(this, getString(R.string.trends_title));
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(-1,-2); tp.topMargin=Ui.dp(this,7); content.addView(title,tp);

        LinearLayout range = ScreenUi.horizontal(this);
        range.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        addRange(range, 7, R.string.trends_range_7);
        addRange(range, 30, R.string.trends_range_30);
        addRange(range, 90, R.string.trends_range_90);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, -2); rp.topMargin=Ui.dp(this,14); rp.bottomMargin=Ui.dp(this,12); content.addView(range,rp);

        List<BloodPressureReading> all = com.afyalink.app.storage.AppData.readings(this);
        List<BloodPressureReading> rows = Charts.since(all, days);
        LinearLayout chartCard = ScreenUi.card(this);
        LinearLayout legend = ScreenUi.horizontal(this);
        TextView cap = ScreenUi.paragraph(this, getString(R.string.trends_chart_caption));
        legend.addView(cap,new LinearLayout.LayoutParams(0,-2,1f));
        TextView sys = ScreenUi.text(this,"●  " + getString(R.string.trends_systolic),11,Ui.color(this,R.color.brand_teal),true);
        legend.addView(sys);
        LinearLayout.LayoutParams l = new LinearLayout.LayoutParams(-1,-2); l.bottomMargin=Ui.dp(this,8); chartCard.addView(legend,l);
        TrendChartView chart = new TrendChartView(this);
        chart.setReadings(rows, days);
        chartCard.addView(chart,new LinearLayout.LayoutParams(-1,Ui.dp(this,200)));
        TextView dia = ScreenUi.text(this,"●  " + getString(R.string.trends_diastolic),11,Ui.color(this,R.color.stage_grade2),true);
        LinearLayout.LayoutParams dp=new LinearLayout.LayoutParams(-2,-2); dp.topMargin=Ui.dp(this,4); chartCard.addView(dia,dp);
        LinearLayout.LayoutParams cc=new LinearLayout.LayoutParams(-1,-2); cc.bottomMargin=Ui.dp(this,14); content.addView(chartCard,cc);

        if(rows.isEmpty()) {
            LinearLayout empty = ScreenUi.card(this, Ui.color(this,R.color.brand_teal_soft),false);
            empty.addView(ScreenUi.paragraph(this,getString(R.string.trends_empty)));
            LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(-1,-2); ep.bottomMargin=Ui.dp(this,14); content.addView(empty,ep);
        }
        Charts.Stats s = Charts.stats(rows);
        LinearLayout metrics = ScreenUi.horizontal(this);
        metrics.addView(metric(getString(R.string.trends_average), s.count == 0 ? "—" : Format.pressure((int)Math.round(s.systolic),(int)Math.round(s.diastolic))), new LinearLayout.LayoutParams(0,-2,1f));
        metrics.addView(metric(getString(R.string.dash_count), String.valueOf(s.count)), new LinearLayout.LayoutParams(0,-2,1f));
        LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(-1,-2); mp.bottomMargin=Ui.dp(this,14); content.addView(metrics,mp);

        LinearLayout dist = ScreenUi.card(this);
        TextView dh = ScreenUi.text(this,getString(R.string.trends_distribution),16,Ui.color(this,R.color.text_primary),true); dist.addView(dh);
        StageScaleView scale = new StageScaleView(this); scale.setShowLabels(true);
        if(!rows.isEmpty()) scale.setStage(rows.get(rows.size()-1).stage());
        LinearLayout.LayoutParams sl=new LinearLayout.LayoutParams(-1,Ui.dp(this,55)); sl.topMargin=Ui.dp(this,14); dist.addView(scale,sl);
        content.addView(dist);
    }

    private void addRange(LinearLayout parent, int value, int text) {
        boolean selected = value == days;
        TextView button = ScreenUi.text(this, getString(text), 12,
                Ui.color(this, selected ? R.color.brand_white : R.color.text_secondary), true);
        button.setGravity(Gravity.CENTER);
        button.setContentDescription(getString(text));
        button.setBackground(Ui.roundedBox(this,
                Ui.color(this, selected ? R.color.brand_teal : R.color.surface), 14,
                Ui.color(this, R.color.hairline), 1));
        button.setMinWidth(Ui.dp(this, 58));
        button.setMinHeight(Ui.dp(this, 46));
        button.setPadding(Ui.dp(this, 12), Ui.dp(this, 10), Ui.dp(this, 12), Ui.dp(this, 10));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-2, Ui.dp(this, 46));
        p.leftMargin = Ui.dp(this, 8);
        parent.addView(button, p);
        button.setOnClickListener(v -> { days = value; render(); });
    }
    private LinearLayout metric(String label, String value) {
        LinearLayout c=ScreenUi.card(this); c.setPadding(Ui.dp(this,14),Ui.dp(this,14),Ui.dp(this,14),Ui.dp(this,14));
        c.addView(ScreenUi.eyebrow(this,label.toUpperCase()));
        TextView v=ScreenUi.text(this,value,22,Ui.color(this,R.color.text_primary),true); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=Ui.dp(this,8);c.addView(v,p); return c;
    }
}
