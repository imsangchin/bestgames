
package com.miui.antivirus;

import miui.provider.ExtraGuard;

import com.miui.common.AndroidUtils;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.MiuiIntent;
import android.net.Uri;

public class VirusLibAutoUpdateService extends IntentService {
    private static final String TAG = VirusLibAutoUpdateService.class.getSimpleName();

    public VirusLibAutoUpdateService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (AndroidUtils.isWifiConnected(this)
                && com.miui.securitycenter.Preferences.isConnectNetworkAlow()) {
            Uri uri = ExtraGuard.getUri(this, MiuiIntent.EXTRA_PROVIDER_VIRUS_DATABASEUPDATE);
            if (uri != null) {
                ContentResolver cr = getContentResolver();
                int rows = cr.update(uri, new ContentValues(), null, null);

                if (rows > 0) {
                    Preferences.setLatestVirusLibUpdateDate(System.currentTimeMillis());

                    // WhiteListHelper.getInstance(this).clearWhiteList();
                    Preferences.setNeedCleanupWhiteList(true);
                }
            }
        }
    }

}
