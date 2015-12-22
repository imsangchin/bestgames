
package com.miui.antivirus;

import android.content.res.Resources;

import com.miui.securitycenter.R;

public enum AntiVirusStatus {

    SAVE(R.integer.pref_val_antivirus_status_save),
    RISK(R.integer.pref_val_antivirus_status_risk),
    VIRUS(R.integer.pref_val_antivirus_status_virus);

    private static final int DEFAULT = R.integer.pref_val_antivirus_status_save;
    private int mValueId;

    private AntiVirusStatus(int valueId) {
        mValueId = valueId;
    }

    public int getValue(Resources res) {
        return res.getInteger(mValueId);
    }

    public static AntiVirusStatus fromValue(Resources res, int value) {
        for (AntiVirusStatus b : AntiVirusStatus.values()) {
            if (value == b.getValue(res)) {
                return b;
            }
        }
        return getDefault(res);
    }

    public static AntiVirusStatus getDefault(Resources res) {
        for (AntiVirusStatus b : AntiVirusStatus.values()) {
            if (res.getInteger(DEFAULT) == b.getValue(res)) {
                return b;
            }
        }
        return null;
    }

}
