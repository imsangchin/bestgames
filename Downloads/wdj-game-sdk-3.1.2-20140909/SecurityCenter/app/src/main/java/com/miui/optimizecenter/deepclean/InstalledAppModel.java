
package com.miui.optimizecenter.deepclean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.pm.PackageInfo;

import com.android.internal.os.PkgUsageStats;
import com.miui.optimizecenter.PkgSizeStats;
import com.miui.optimizecenter.enums.SecurityStatus;

public class InstalledAppModel implements Serializable {
    private static final long serialVersionUID = -5011006639704798828L;

    private static final Comparator<Map.Entry<String, Long>> TIME_COMPARATOR = new Comparator<Map.Entry<String, Long>>() {
        public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
            if (o1.getValue() > o2.getValue()) {
                return -1;
            } else if (o1.getValue() < o2.getValue()) {
                return 1;
            } else {
                return 0;
            }
        }
    };

    public static final int TIME_INVALID = -1;

    private PkgSizeStats mSizeStats;

    private PackageInfo mPackageInfo;

    private long mLastLunchTime = TIME_INVALID;

    private boolean mAdviseDelete;

    public SecurityStatus mSecurityStatus = SecurityStatus.SAFE;

    private String mSecurityInfo;

    public void setUsageStats(PkgUsageStats usageStats) {
        if (usageStats != null) {
            Map<String, Long> componentResumeTimes = usageStats.componentResumeTimes;
            if (componentResumeTimes != null) {
                if (componentResumeTimes.size() >= 2) {
                    List<Map.Entry<String, Long>> entryList = new ArrayList<Map.Entry<String, Long>>(
                            componentResumeTimes.entrySet());
                    Collections.sort(entryList, TIME_COMPARATOR);
                    mLastLunchTime = entryList.get(0).getValue();
                } else {
                    Set<String> keys = componentResumeTimes.keySet();
                    for (String key : keys) {
                        mLastLunchTime = componentResumeTimes.get(key);
                    }
                }
            }
        }
    }

    public PkgSizeStats getSizeStats() {
        return mSizeStats;
    }

    public void setSizeStats(PkgSizeStats sizeStats) {
        mSizeStats = sizeStats;
    }

    public long getLastLunchTime() {
        return mLastLunchTime;
    }

    public PackageInfo getPackageInfo() {
        return mPackageInfo;
    }

    public void setPackageInfo(PackageInfo info) {
        mPackageInfo = info;
    }

    public boolean adviseDelete() {
        return mAdviseDelete;
    }

    public void setAdviseDelete(boolean adviseDelete) {
        mAdviseDelete = adviseDelete;
    }

    public SecurityStatus getSecurityStatus() {
        return mSecurityStatus;
    }

    public void setSecurityStatus(SecurityStatus status) {
        mSecurityStatus = status;
    }

    public String getSecurityInfo() {
        return mSecurityInfo;
    }

    public void setSecurityInfo(String info) {
        mSecurityInfo = info;
    }

    @Override
    public String toString() {
        return "AppModel PackageInfo = " + mPackageInfo + " PkgSizeStats = " + mSizeStats
                + " LastLunchTime = " + mLastLunchTime + " AdviseDelete = " + mAdviseDelete
                + " SecurityStatus = " + mSecurityStatus + " SecurityInfo = " + mSecurityInfo;
    }
}
