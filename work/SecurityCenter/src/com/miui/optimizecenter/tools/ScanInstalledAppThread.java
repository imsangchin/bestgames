
package com.miui.optimizecenter.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.internal.os.PkgUsageStats;
import com.miui.optimizecenter.PkgSizeStats;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.Handler;

public class ScanInstalledAppThread extends Thread {

    private final IPackageStatsObserver.Stub mStatsObserver = new IPackageStatsObserver.Stub() {

        public void onGetStatsCompleted(PackageStats stats, boolean succeeded) {
            if (!mScanFinished) {
                PkgSizeStats sizeStats = new PkgSizeStats();

                long externalCodeSize = stats.externalCodeSize
                        + stats.externalObbSize;
                long externalDataSize = stats.externalDataSize
                        + stats.externalMediaSize + stats.externalCacheSize;
                long newSize = externalCodeSize + externalDataSize
                        + sizeStats.getTotalInternalSize(stats);

                sizeStats.packageName = stats.packageName;
                sizeStats.cacheSize = stats.cacheSize;
                sizeStats.codeSize = stats.codeSize;
                sizeStats.dataSize = stats.dataSize;
                sizeStats.externalCodeSize = externalCodeSize;
                sizeStats.externalDataSize = externalDataSize;
                sizeStats.externalCacheSize = stats.externalCacheSize;
                sizeStats.internalSize = sizeStats.getTotalInternalSize(stats);
                sizeStats.externalSize = sizeStats.getTotalExternalSize(stats);

                mCallback.onScanItemSizeChanged(stats.packageName, sizeStats);

                if (++mScannedAppCount == mTotalAppCount) {
                    mHandler.removeCallbacks(mRunnable);
                    mCallback.onScanFinish();
                }
            }
        }
    };

    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            mCallback.onScanFinish();
        }
    };

    private static final long SCAN_LOCK_TIME = 20 * 1000;

    private boolean mScanFinished = false;
    private int mTotalAppCount;
    private int mScannedAppCount;

    private Handler mHandler;

    private Context mContext;
    private ScanInstalledAppsCallback mCallback;

    private PackageManager mPm;
    private ActivityManager mAm;

    private Map<String, PkgUsageStats> mAllPackageUsageStats;

    public ScanInstalledAppThread(Context context, ScanInstalledAppsCallback callback) {
        mContext = context;
        mCallback = callback;
        mPm = context.getPackageManager();
        mHandler = new Handler();
        mAm = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    }

    @Override
    public void run() {
        mCallback.onScanStart();

        mHandler.removeCallbacks(mRunnable);

        mTotalAppCount = 0;
        mScannedAppCount = 0;

        loadAllPackageUsageStats();

        List<PackageInfo> packageInfos = mPm.getInstalledPackages(0);
        for (PackageInfo packageInfo : packageInfos) {
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                ++mTotalAppCount;
                mScanFinished = mCallback.onScanItem(packageInfo.packageName, packageInfo,
                        mAllPackageUsageStats.get(packageInfo.packageName));
                if (mScanFinished) {
                    return;
                }
                mPm.getPackageSizeInfo(packageInfo.packageName, mStatsObserver);
            }
        }

        if (mTotalAppCount == 0) {
            mCallback.onScanFinish();
            return;
        }

        mHandler.sendMessageDelayed(mHandler.obtainMessage(), SCAN_LOCK_TIME);
    }

    private void loadAllPackageUsageStats() {
        mAllPackageUsageStats = new HashMap<String, PkgUsageStats>();
        PkgUsageStats[] usageStats = mAm.getAllPackageUsageStats();
        if (usageStats != null) {
            for (PkgUsageStats pus : usageStats) {
                mAllPackageUsageStats.put(pus.packageName, pus);
            }
        }
    }
}
