
package com.miui.analytics;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SecurityCenterAnalyticsService extends Service {
    private static final String TAG = "SecurityCenterAnalyticsService";
    /**
     * trackEvent 打点单个id 请在意图中 intent.putExtra(EXTRA_ID,(String)value);
     */
    public static String ACTION_TRACK_EVENT = "trackEvent";
    /**
     * trackEvent 打点单个id + long值的value 请在意图中
     * intent.putExtra(EXTRA_ID,(String)value);
     * intent.putExtra(EXTRA_ID_LONG_VALUE, (int)value);
     */
    public static String ACTION_TRACK_EVENT_WITH_VALUE = "trackEventWithValue";
    /**
     * trackEvent 打点单个id + map<String,String> 请在意图中
     * intent.putExtra(EXTRA_ID,(String)value);
     * intent.putExtra(EXTRA_MAP_BUNDLE, (Bundle)value);
     */
    public static String ACTION_TRACK_EVENT_WITH_PARAMS = "trackEventWithParams";
    /**
     * trackEvent 打点单个id + long值的value + map<String,String> 请在意图中
     * intent.putExtra(EXTRA_ID,(String)value);
     * intent.putExtra(EXTRA_ID_LONG_VALUE, (int)value);
     * intent.putExtra(EXTRA_MAP_BUNDLE, (Bundle)value);
     */
    public static String ACTION_TRACK_EVENT_VALUE_AND_PARAMS = "trackEventWithValueAndParams";

    public static String EXTRA_ID = "extra_id";
    public static String EXTRA_LONG_VALUE = "extra_long_value";
    public static String EXTRA_MAP_BUNDLE = "extra_map_bundle";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }
        if (ACTION_TRACK_EVENT.equals(intent.getAction())) {
            Log.i(TAG,"ACTION_TRACK_EVENT");
            SecurityCenterAnalytics.getInstance(this).trackEvent(intent.getStringExtra(EXTRA_ID));
        } else if (ACTION_TRACK_EVENT_WITH_VALUE.equals(intent.getAction())) {
            Log.i(TAG,"ACTION_TRACK_EVENT_WITH_VALUE");
            SecurityCenterAnalytics.getInstance(this).trackEventWithValue(
                    intent.getStringExtra(EXTRA_ID), intent.getLongExtra(EXTRA_LONG_VALUE, -1));
        } else if (ACTION_TRACK_EVENT_WITH_PARAMS.equals(intent.getAction())) {
            SecurityCenterAnalytics.getInstance(this).trackEventWithParams(
                    intent.getStringExtra(EXTRA_ID),
                    getMap(intent.getBundleExtra(EXTRA_MAP_BUNDLE)));
        } else if (ACTION_TRACK_EVENT_VALUE_AND_PARAMS.equals(intent.getAction())) {
            SecurityCenterAnalytics.getInstance(this).trackEventWithValueAndParams(
                    intent.getStringExtra(EXTRA_ID), intent.getLongExtra(EXTRA_LONG_VALUE, -1),
                    getMap(intent.getBundleExtra(EXTRA_MAP_BUNDLE)));
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return SecurityCenterAnalytics.getInstance(this).asBinder();
    }

    private Map<String, String> getMap(Bundle value) {
        Iterator<String> iterator = value.keySet().iterator();
        Map<String, String> map = new HashMap<String, String>();
        while (iterator.hasNext()) {
            String key = iterator.next();
            map.put(key, value.getString(key));
        }
        return map;
    }
}
