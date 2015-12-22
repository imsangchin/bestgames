package com.anzhuoshoudiantong.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

public class SystemUtil {
    public static String getIMEI(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }
}
