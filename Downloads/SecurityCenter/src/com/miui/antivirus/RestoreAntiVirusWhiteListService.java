
package com.miui.antivirus;

import android.app.IntentService;
import android.content.Intent;

public class RestoreAntiVirusWhiteListService extends IntentService {

    public RestoreAntiVirusWhiteListService(String name) {
        super(RestoreAntiVirusWhiteListService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // WhiteListHelper.getInstance(this).clearWhiteList();
        Preferences.setNeedCleanupWhiteList(true);
    }
}
