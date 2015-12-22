package com.anzhuoshoudiantong;

import com.anzhuoshoudiantong.ads.AdController;
import com.umeng.analytics.MobclickAgent;
import com.wandoujia.apkruntime.ias.IASActivity;

import edu.mit.mobile.android.imagecache.ImageCache;
import edu.mit.mobile.android.imagecache.ImageCache.OnImageLoadListener;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class FullscreenAdActivity extends IASActivity implements
        OnImageLoadListener {
    private ImageView imageView;
    private TextView aboutText;
    private AdController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fullscreen_image);

        controller = AdController.getInstance(getApplicationContext());
        imageView = (ImageView) findViewById(R.id.ad_fullscreen);
        aboutText = (TextView) findViewById(R.id.about);

        PackageManager pm = getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
            String versionName = getString(R.string.app_name) + " "
                    + info.versionName;
            aboutText.setText(versionName);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        ImageCache.getInstance(getApplicationContext())
                .registerOnImageLoadListener(this);

        controller.init();
        controller.showFullscreenAd(imageView);

        imageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(getApplicationContext(),
                        Logs.Events.FULLSCREEN_AD_CLICK);
                controller.downloadFromFullScreenAd();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        ImageCache.getInstance(getApplicationContext())
                .unregisterOnImageLoadListener(this);
        super.onDestroy();
    }

    @Override
    public void onImageLoaded(int arg0, Uri arg1, Drawable arg2) {
        if (imageView.getId() == arg0) {
            imageView.setImageDrawable(arg2);
        }
    }

    @Override
    @Deprecated
    public void onImageLoaded(long arg0, Uri arg1, Drawable arg2) {
        // TODO Auto-generated method stub

    }
}
