
package com.miui.optimizecenter.tools;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.IPackageStatsObserver;
import android.os.Handler;

public class ScanSystemCacheThread extends Thread {

    private final IPackageStatsObserver.Stub mStatsObserver = new IPackageStatsObserver.Stub() {

        public void onGetStatsCompleted(PackageStats stats, boolean succeeded) {
            if (!mScanFinished) {
                mCallback.onScanItem(stats.packageName, stats);

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

    private ScanSystemCacheCallback mCallback;

    private PackageManager mPm;
    private ActivityManager mAm;

    public ScanSystemCacheThread(Context context, ScanSystemCacheCallback callback) {
        mCallback = callback;
        mPm = context.getPackageManager();
        mAm = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        mHandler = new Handler();
    }

    @Override
    public void run() {
        mCallback.onScanStart();

        mHandler.removeCallbacks(mRunnable);

        mTotalAppCount = 0;
        mScannedAppCount = 0;

        // init white list
        Set<String> whiteList = new HashSet<String>();
        whiteList.add("com.google.android.apps.maps");
        List<ActivityManager.RunningAppProcessInfo> appList = mAm.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appInfo : appList) {
            for (String packageName : appInfo.pkgList) {
                whiteList.add(packageName);
            }
        }

        List<PackageInfo> packageInfos = mPm.getInstalledPackages(0);
        for (PackageInfo packageInfo : packageInfos) {
            if (!whiteList.contains(packageInfo.packageName)) {
                if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    if (mScanFinished) {
                        return;
                    }
                    ++mTotalAppCount;
                    mPm.getPackageSizeInfo(packageInfo.packageName, mStatsObserver);
                }
            }
        }

        if (mTotalAppCount != 0) {
            mHandler.sendMessageDelayed(mHandler.obtainMessage(), SCAN_LOCK_TIME);
        } else {
            mCallback.onScanFinish();
        }
    }
}
