
package com.miui.powercenter;

import android.os.IPowerManager;
import android.os.RemoteException;

public class PowerAdapterUtils {

    public static void setTemporaryScreenAutoBrightnessAdjustmentSettingOverride(IPowerManager pm,
            float valf) {
        try {
            pm.setAutoBrightnessAdjustment(valf);
        } catch (RemoteException e) {
            // ignore
        }
    }

    public static void setTemporaryScreenBrightnessSettingOverride(IPowerManager pm, int value) {
        try {
            pm.setBacklightBrightness(value);
        } catch (RemoteException e) {
            // ignore
        }
    }
}
