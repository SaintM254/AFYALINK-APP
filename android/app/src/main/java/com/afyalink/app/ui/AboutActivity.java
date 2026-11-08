package com.afyalink.app.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.afyalink.app.BuildConfig;
import com.afyalink.app.R;
import com.afyalink.app.util.Lucide;
import com.afyalink.app.util.Ui;

public final class AboutActivity extends BaseActivity {
    @Override protected void onCreate(@Nullable Bundle state) { super.onCreate(state);render(); }
    private void render() {
        installPage(getString(R.string.about_title),true);
        LinearLayout brand=ScreenUi.card(this,Ui.color(this,R.color.surface),false);
        ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.ic_afyalink_mark);logo.setContentDescription(getString(R.string.cd_logo));logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(Ui.dp(this,71.4f),Ui.dp(this,96));lp.gravity=android.view.Gravity.CENTER_HORIZONTAL;brand.addView(logo,lp);
        TextView word=ScreenUi.title(this,getString(R.string.wordmark));word.setGravity(android.view.Gravity.CENTER);word.setLetterSpacing(.06f);
        LinearLayout.LayoutParams wp=new LinearLayout.LayoutParams(-1,-2);wp.topMargin=Ui.dp(this,10);brand.addView(word,wp);
        TextView tag=ScreenUi.paragraph(this,getString(R.string.tagline));tag.setGravity(android.view.Gravity.CENTER);tag.setLetterSpacing(.12f);tag.setTextSize(10);
        LinearLayout.LayoutParams tg=new LinearLayout.LayoutParams(-1,-2);tg.topMargin=Ui.dp(this,4);brand.addView(tag,tg);
        TextView version=ScreenUi.paragraph(this,getString(R.string.about_version,BuildConfig.VERSION_NAME,BuildConfig.VERSION_CODE));version.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams vp=new LinearLayout.LayoutParams(-1,-2);vp.topMargin=Ui.dp(this,11);brand.addView(version,vp);
        content.addView(brand);
        TextView mission=ScreenUi.paragraph(this,getString(R.string.about_mission));LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(-1,-2);mp.topMargin=Ui.dp(this,18);mp.bottomMargin=Ui.dp(this,16);content.addView(mission,mp);
        addInfoCard(R.drawable.ic_info,getString(R.string.about_disclaimer_title),getString(R.string.about_disclaimer_body));
        addInfoCard(R.drawable.ic_shield,getString(R.string.about_privacy_title),getString(R.string.about_privacy_body));
        addInfoCard(R.drawable.ic_heart,getString(R.string.about_references_title),getString(R.string.about_references_body));
        LinearLayout repo=ScreenUi.card(this,Ui.color(this,R.color.brand_teal_soft),false);
        LinearLayout rr=ScreenUi.horizontal(this);
        TextView rt=ScreenUi.text(this,getString(R.string.about_source_title),15,Ui.color(this,R.color.brand_teal_deep),true);rr.addView(rt,new LinearLayout.LayoutParams(0,-2,1f));
        androidx.appcompat.widget.AppCompatImageButton open=new androidx.appcompat.widget.AppCompatImageButton(this);open.setImageResource(Lucide.EXPORT);open.setImageTintList(android.content.res.ColorStateList.valueOf(Ui.color(this,R.color.brand_teal)));open.setContentDescription(getString(R.string.about_open_repo));open.setBackgroundResource(android.R.drawable.list_selector_background);open.setPadding(Ui.dp(this,12),Ui.dp(this,12),Ui.dp(this,12),Ui.dp(this,12));rr.addView(open,new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48)));repo.addView(rr);
        TextView rb=ScreenUi.paragraph(this,getString(R.string.about_source_body));LinearLayout.LayoutParams rbp=new LinearLayout.LayoutParams(-1,-2);rbp.topMargin=Ui.dp(this,7);repo.addView(rb,rbp);
        LinearLayout.LayoutParams rcp=new LinearLayout.LayoutParams(-1,-2);rcp.topMargin=Ui.dp(this,14);content.addView(repo,rcp);
        open.setOnClickListener(v->{try{startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/SaintM254/AFYALINK-APP")));}catch(Exception e){Toast.makeText(this,R.string.sync_no_app,Toast.LENGTH_SHORT).show();}});
    }
    private void addInfoCard(int icon,String title,String body) {
        LinearLayout card=ScreenUi.card(this);LinearLayout row=ScreenUi.horizontal(this);
        ImageView img=Ui.icon(this,icon,Ui.color(this,R.color.brand_teal),22);row.addView(img,new LinearLayout.LayoutParams(Ui.dp(this,32),Ui.dp(this,32)));
        TextView t=ScreenUi.text(this,title,15,Ui.color(this,R.color.text_primary),true);LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,-2,1f);tp.leftMargin=Ui.dp(this,8);row.addView(t,tp);card.addView(row);
        TextView b=ScreenUi.paragraph(this,body);LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,-2);bp.topMargin=Ui.dp(this,10);card.addView(b,bp);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=Ui.dp(this,12);content.addView(card,p);
    }
}
