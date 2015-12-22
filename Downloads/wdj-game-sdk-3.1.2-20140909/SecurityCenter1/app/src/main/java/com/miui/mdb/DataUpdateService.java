
package com.miui.mdb;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DataUpdateService extends IntentService {
    private static final String TAG = "DataUpdateService";
    private static final boolean DBG = true;
    private SharedPreferences prefs;

    public DataUpdateService() {
        super("DataUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!DataUpdateReceiver.isUpdateTimeOverDay(this)) {
            // 如果距离上次更新的时间小于24小时，不更新
            return;
        }

        String str = null;
        try {
            str = httpPost();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (str != null) {
            try {
                // 成功
                final long waterMark = Long.parseLong(str);
                Log.d(TAG, "==============waterMark = " + waterMark);
                Intent i = new Intent(DataUpdateUtils.DATA_UPDATE_RECEIVE);
                i.putExtra(DataUpdateUtils.EXTRA_WATER_MARK, waterMark);
                sendBroadcast(i);

                // 记录本次更新时间
                if (DBG) {
                    final long lastTime = prefs.getLong(DataUpdateReceiver.LAST_UPDATE_TIME, 0);
                    if (lastTime > 0) {
                        Log.d(TAG, "The distance last update time:"
                                + (System.currentTimeMillis() - lastTime));
                    }
                }
                prefs.edit()
                        .putLong(DataUpdateReceiver.LAST_UPDATE_TIME,
                                System.currentTimeMillis()).commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String httpPost() throws JSONException, ClientProtocolException, IOException {
        final HttpGet get = new HttpGet(DataUpdateUtils.VERSION_URL);
        final HttpResponse resp = new DefaultHttpClient().execute(get);
        int statusCode = resp.getStatusLine().getStatusCode();
        Log.d(TAG, "=============statusCode = " + statusCode);
        if (statusCode == HttpStatus.SC_OK) {
            StringBuilder builder = new StringBuilder();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(resp.getEntity().getContent()), 1024);
            try {
                String line;
                while ((line = br.readLine()) != null) {
                    Log.d(TAG, "=============line = " + line);
                    builder.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (DBG)
                    Log.d(TAG, "Server error: " + e.getMessage());
            } finally {
                br.close();
            }
            Log.d(TAG, "=============builder = " + builder.toString());
            return builder.toString();
        } else {
            if (DBG)
                Log.d(TAG, "Server error: " + statusCode + " " + resp.getStatusLine());
        }
        return null;
    }
}
