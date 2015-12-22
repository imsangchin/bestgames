
package com.miui.optimizecenter.deepclean;

import java.util.Comparator;

import android.text.TextUtils;

import com.miui.common.ApkIconHelper;
import com.miui.optimizecenter.PkgSizeStats;
import com.miui.optimizecenter.enums.InstalledAppsSortType;
import com.miui.optimizecenter.enums.SecurityStatus;

public class InstalledAppComparator implements Comparator<InstalledAppModel> {

    public InstalledAppsSortType mSortType = InstalledAppsSortType.LUNCH_TIME;

    public InstalledAppComparator() {
        mSortType = InstalledAppsSortType.LUNCH_TIME;
    }

    public InstalledAppComparator(InstalledAppsSortType sortType) {
        mSortType = sortType;
    }

    @Override
    public int compare(InstalledAppModel lhs, InstalledAppModel rhs) {

        String lPkgName = lhs.getPackageInfo().packageName;
        String rPkgName = rhs.getPackageInfo().packageName;
        if (TextUtils.equals(lPkgName, ApkIconHelper.PKG_SYSTEM_CACHE)
                && !TextUtils.equals(rPkgName, ApkIconHelper.PKG_SYSTEM_CACHE)) {
            return -1;
        } else if (!TextUtils.equals(lPkgName, ApkIconHelper.PKG_SYSTEM_CACHE)
                && TextUtils.equals(rPkgName, ApkIconHelper.PKG_SYSTEM_CACHE)) {
            return 1;
        } else {
            SecurityStatus lStatus = lhs.getSecurityStatus();
            SecurityStatus rStatus = rhs.getSecurityStatus();
            if (SecurityStatus.SAFE != lStatus && SecurityStatus.SAFE == rStatus) {
                return -1;
            } else if (SecurityStatus.SAFE == lStatus && SecurityStatus.SAFE != rStatus) {
                return 1;
            } else {
                if (mSortType == InstalledAppsSortType.APP_SIZE) {
                    PkgSizeStats lStats = lhs.getSizeStats();
                    PkgSizeStats rStats = rhs.getSizeStats();

                    long lTotalSize = lStats.internalSize + lStats.externalSize;
                    long rTotalSize = rStats.internalSize + rStats.externalSize;

                    if (lTotalSize > rTotalSize) {
                        return -1;
                    } else if (lTotalSize < rTotalSize) {
                        return 1;
                    } else {
                        return 0;
                    }
                } else {
                    if (lhs.getLastLunchTime() > rhs.getLastLunchTime()) {
                        return 1;
                    } else if (lhs.getLastLunchTime() < rhs.getLastLunchTime()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            }
        }
    }

}
