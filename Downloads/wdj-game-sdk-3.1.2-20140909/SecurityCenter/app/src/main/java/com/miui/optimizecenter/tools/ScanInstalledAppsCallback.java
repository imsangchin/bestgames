
package com.miui.optimizecenter.tools;

import android.content.pm.PackageInfo;

import com.android.internal.os.PkgUsageStats;
import com.miui.optimizecenter.PkgSizeStats;

public interface ScanInstalledAppsCallback {

    void onScanStart();

    boolean onScanItem(String pkgName, PackageInfo packageInfo, PkgUsageStats usageStats);

    void onScanItemSizeChanged(String pkgName, PkgSizeStats sizeStats);

    void onScanFinish();

}
