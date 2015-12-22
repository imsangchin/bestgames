
package com.miui.optimizecenter.tools;

import android.content.pm.PackageStats;

public interface ScanSystemCacheCallback {

    void onScanStart();

    boolean onScanItem(String pkgName, PackageStats usageStats);

    void onScanFinish();

}
