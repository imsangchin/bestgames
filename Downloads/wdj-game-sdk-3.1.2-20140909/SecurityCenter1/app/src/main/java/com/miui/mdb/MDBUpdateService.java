
package com.miui.mdb;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import miui.util.DataUpdateUtils;
import miui.util.Network;

public class MDBUpdateService extends IntentService {
    public static final String TAG = MDBUpdateService.class.getSimpleName();

    public MDBUpdateService() {
        this("MDBUpdateService");
    }

    public MDBUpdateService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (DataUpdateUtils.DATA_UPDATE_RECEIVE.equals(intent.getAction())) {
            final long waterMark = intent.getLongExtra(DataUpdateUtils.EXTRA_WATER_MARK, 0);
            Log.d(MDBUpdateUtil.LOG_TAG, "current water mark is " + waterMark);
            if (waterMark != MDBUpdateUtil.getCurrentVersionCode()) {
                if (Network.isNetworkConnected(getApplicationContext())) {
                    if (MDBUpdateUtil.updateDataFile()) {
                        MDBUpdateUtil.setCurrentVersionCode((int) waterMark);
                        MDBUpdateUtil.setFileOwnerAndPermission();
                        Log.i(MDBUpdateUtil.LOG_TAG, "success to update mdb.");
                    }
                }
            }
        }
    }

}
