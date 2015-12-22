
package com.miui.securitycenter;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.miui.securitycenter.manualitem.ExaminationResult;
import com.miui.securitycenter.manualitem.ItemListManager;
import com.miui.securitycenter.manualitem.WebApiAccessHelper;

public class AutoUpdateManualListService extends Service {

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        mItemListManager = ItemListManager.getInstance(this);
        new UpdateTask().execute();
    }

    private class UpdateTask extends AsyncTask<Void, Void, Void> {

        WebApiAccessHelper mWebApiAccessHelper;
        ExaminationResult mResult;

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            mWebApiAccessHelper = WebApiAccessHelper.getInstance(AutoUpdateManualListService.this);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            mResult = mWebApiAccessHelper.dataConnection(dataVersion, isDiff);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            if (mResult.isSuccess()) {
                mItemListManager.insertAllToItemList(mResult);
            }
        }

    }

    private ItemListManager mItemListManager;
    private static int dataVersion = 1;
    private final static boolean isDiff = true;
}
