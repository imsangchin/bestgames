
package com.miui.analytics;

import miui.analytics.Analytics;

import android.content.Context;

import com.miui.securitycenter.AppPackageInfo;
import com.miui.securitycenter.DateTimeUtils;

import java.util.HashMap;
import java.util.Map;

public class SecurityCenterAnalytics extends ISecurityCenterAnalytics.Stub {
    private static SecurityCenterAnalytics INST;

    private Context mContext;

    private SecurityCenterAnalytics(Context context) {
        mContext = context;
    }

    public static SecurityCenterAnalytics getInstance(Context context) {
        if (INST == null) {
            INST = new SecurityCenterAnalytics(context.getApplicationContext());
        }
        return INST;
    }

    @Override
    public void trackEvent(String eventId) {
        trackEventWithParams(eventId, null);
    }

    @Override
    public void trackEventWithValue(String eventId, long value) {
        trackEventWithValueAndParams(eventId, value, null);
    }

    @Override
    public void trackEventWithParams(String eventId, Map parameters) {
        Map<String, String> params = new HashMap<String, String>(getPresetParam(mContext));
        if (parameters != null) {
            params.putAll(parameters);
        }
        Analytics tracker = Analytics.getInstance();
        tracker.startSession(mContext);
        tracker.trackEvent(eventId, params);
        tracker.endSession();
    }

    @Override
    public void trackEventWithValueAndParams(String eventId, long value, Map parameters) {
        Map<String, String> params = new HashMap<String, String>(getPresetParam(mContext));
        if (parameters != null) {
            params.putAll(parameters);
        }
        Analytics tracker = Analytics.getInstance();
        tracker.startSession(mContext);
        tracker.trackEvent(eventId, params, value);
        tracker.endSession();
    }

    private HashMap<String, String> getPresetParam(Context context) {
        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put(AnalyticsUtil.TRACK_ID_COMMON_MIUI_VERSION,
                AnalyticsUtil.TRACK_VALUE_COMMON_MIUI_VERSION);
        parameters
                .put(AnalyticsUtil.TRACK_ID_COMMON_APP_VERSION, AppPackageInfo.sVersionName);
        parameters.put(AnalyticsUtil.TRACK_ID_COMMON_DATA_TIME, DateTimeUtils.formatDataTime(
                System.currentTimeMillis(), DateTimeUtils.DATE_FORMAT_DAY));
        return parameters;
    }
}
