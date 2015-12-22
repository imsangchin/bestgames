
package com.miui.securitycenter.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import miui.text.ExtraTextUtils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.os.Handler;
import android.util.Log;

import com.miui.common.AndroidUtils;
import com.miui.securitycenter.R;
import com.miui.securitycenter.ScoreConstants;

public class CacheCheckManager {
    public interface CacheScanCallback {
        void onStartScan(int totalCount);

        boolean onScanItem(String descx);

        void onFinishScan();
    }

    public interface CacheCleanupCallback {
        void onStartCleanup();

        boolean onCleanupItem(String descx);

        void onFinishCleanup();
    }

    public static final int CRITERION_CACHE_ONE = 0;
    public static final int CRITERION_CACHE_TWO = 1048576;
    public static final int CRITERION_CACHE_THREE = 5242880;
    public static final int CRITERION_CACHE_FOUR = 10485760;
    public static final int CRITERION_CACHE_FIVE = 20971520;

    private final IPackageStatsObserver.Stub mStatsObserver = new IPackageStatsObserver.Stub() {

        public void onGetStatsCompleted(PackageStats stats, boolean succeeded) {
            TrashAppInfo trashInfo = mTrashAppsMap.get(stats.packageName);
            long memoryUsed = stats.cacheSize + stats.externalCacheSize;
            if (memoryUsed > 0) {
                if (trashInfo == null) {
                    trashInfo = new TrashAppInfo(stats.packageName, memoryUsed,
                            ScoreConstants.LOCK_STATE_UNLOCK);
                    mTrashAppsMap.put(stats.packageName, trashInfo);
                }

                mUsedMemory += memoryUsed;
                calculateScore();
            }

            if (++mScannedAppCount == mTotalAppCount) {
                mHandler.removeCallbacks(mStatsRunnable);
                mCacheScanCallback.onFinishScan();
            }
        }
    };

    private Runnable mStatsRunnable = new Runnable() {

        @Override
        public void run() {
            mCacheScanCallback.onFinishScan();
        }
    };

    private final IPackageDataObserver.Stub mDeleteObserver = new IPackageDataObserver.Stub() {

        @Override
        public void onRemoveCompleted(String packageName, boolean succeeded) {
            TrashAppInfo trashInfo = mTrashAppsMap.get(packageName);
            if (trashInfo != null) {
                mUsedMemory -= trashInfo.memoryUsed;
                calculateScore();
                mTrashAppsMap.remove(packageName);
                mScanFinished = mCacheCleanupCallback.onCleanupItem(AndroidUtils.getAppName(
                        mContext, packageName).toString());
                if (mScanFinished) {
                    return;
                }
            }

            if (++mScannedAppCount == mTotalAppCount) {
                mHandler.removeCallbacks(mDeleteRunnable);
                mCacheCleanupCallback.onFinishCleanup();
            }
        }

    };
    private Runnable mDeleteRunnable = new Runnable() {

        @Override
        public void run() {
            mCacheCleanupCallback.onFinishCleanup();
        }
    };

    private class TrashAppInfo {
        public TrashAppInfo(String pkgName, long memoryUsed, int lockState) {
            this.pkgName = pkgName;
            this.memoryUsed = memoryUsed;
            this.lockState = lockState;
        }

        String pkgName;
        long memoryUsed;
        int lockState;
    }

    private static final long SCAN_LOCK_TIME = 20 * 1000;

    private boolean mScanFinished = false;
    private int mTotalAppCount;
    private int mScannedAppCount;

    private Handler mHandler = new Handler();

    private static CacheCheckManager INST;
    private Context mContext;

    private Map<String, TrashAppInfo> mTrashAppsMap = new HashMap<String, TrashAppInfo>();

    private CacheScanCallback mCacheScanCallback;
    private CacheCleanupCallback mCacheCleanupCallback;
    private long mUsedMemory;

    private int mCacheScore = ScoreConstants.SCORE_CACHE_ONE;

    private CacheCheckManager(Context context) {
        mContext = context;
    }

    public static CacheCheckManager getInstance(Context context) {
        if (INST == null) {
            INST = new CacheCheckManager(context.getApplicationContext());
        }
        return INST;
    }

    public int getScore() {
        return mCacheScore;
    }

    public void resetScore() {
        mCacheScore = ScoreConstants.SCORE_CACHE_ONE;
    }

    public CharSequence getCheckResult() {
        int count = 0;
        Set<String> pkgs = mTrashAppsMap.keySet();
        for (String pkg : pkgs) {
            TrashAppInfo appInfo = mTrashAppsMap.get(pkg);
            if (appInfo != null && appInfo.lockState != ScoreConstants.LOCK_STATE_LOCK) {
                ++count;
            }
        }

        if (count == 0) {
            return mContext.getString(R.string.cache_check_content, count, "");
        }
        return mContext.getString(R.string.cache_check_content, count,
                ExtraTextUtils.formatFileSize(mContext, mUsedMemory));
    }

    public void startScan(CacheScanCallback callback) {
        mCacheScanCallback = callback;

        mTotalAppCount = 0;
        mScannedAppCount = 0;
        mUsedMemory = 0;
        mTrashAppsMap.clear();
        mCacheScore = ScoreConstants.SCORE_CACHE_ONE;
        mScanFinished = false;

        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        PackageManager pm = mContext.getPackageManager();

        // init white list
        Set<String> whiteList = new HashSet<String>();
        whiteList.add("com.google.android.apps.maps");
        List<ActivityManager.RunningAppProcessInfo> appList = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appInfo : appList) {
            for (String packageName : appInfo.pkgList) {
                whiteList.add(packageName);
            }
        }

        List<PackageInfo> packageInfos = pm.getInstalledPackages(0);
        List<String> nameList = new ArrayList<String>();
        for (PackageInfo packageInfo : packageInfos) {
            if (!whiteList.contains(packageInfo.packageName)) {
                if (mScanFinished) {
                    return;
                }
                ++mTotalAppCount;
                nameList.add(packageInfo.packageName);
            }
        }
        callback.onStartScan(mTotalAppCount);

        for (String name : nameList) {
            if (mCacheScanCallback.onScanItem(AndroidUtils.getAppName(mContext,
                    name).toString())) {
                return;
            }
            pm.getPackageSizeInfo(name, mStatsObserver);
        }
        mHandler.postDelayed(mStatsRunnable, SCAN_LOCK_TIME);
    }

    public void startCleanup(CacheCleanupCallback callback) {
        mCacheCleanupCallback = callback;

        mCacheCleanupCallback.onStartCleanup();

        mTotalAppCount = 0;
        mScannedAppCount = 0;
        mScanFinished = false;

        PackageManager pm = mContext.getPackageManager();

        if (mTrashAppsMap.isEmpty()) {
            mCacheCleanupCallback.onFinishCleanup();
            return;
        }

        Map<String, TrashAppInfo> targetMap = new HashMap<String, TrashAppInfo>(mTrashAppsMap);
        Set<String> trashPkgs = targetMap.keySet();
        for (String pkg : trashPkgs) {
            TrashAppInfo traskInfo = targetMap.get(pkg);
            if (mScanFinished) {
                return;
            }
            if (traskInfo != null) {
                if (traskInfo.lockState != ScoreConstants.LOCK_STATE_LOCK) {
                    ++mTotalAppCount;
                    pm.deleteApplicationCacheFiles(traskInfo.pkgName, mDeleteObserver);
                }
            }
        }

        mHandler.postDelayed(mDeleteRunnable, SCAN_LOCK_TIME);
    }

    public List<String> getCacheApps() {
        List<String> pkgList = new ArrayList<String>();
        Map<String, TrashAppInfo> targetMap = new HashMap<String, TrashAppInfo>(mTrashAppsMap);
        Set<String> pkgs = targetMap.keySet();
        for (String pkg : pkgs) {
            pkgList.add(pkg);
        }
        return pkgList;
    }

    public long getMemoryUsed(String pkgName) {
        TrashAppInfo trashInfo = mTrashAppsMap.get(pkgName);
        if (trashInfo == null) {
            return 0;
        }
        return trashInfo.memoryUsed;
    }

    private void calculateScore() {
        if (mUsedMemory > CRITERION_CACHE_FIVE) {
            mCacheScore = ScoreConstants.SCORE_CACHE_SIX;
        } else if (mUsedMemory > CRITERION_CACHE_FOUR) {
            mCacheScore = ScoreConstants.SCORE_CACHE_FIVE;
        } else if (mUsedMemory > CRITERION_CACHE_THREE) {
            mCacheScore = ScoreConstants.SCORE_CACHE_FOUR;
        } else if (mUsedMemory > CRITERION_CACHE_TWO) {
            mCacheScore = ScoreConstants.SCORE_CACHE_THREE;
        } else if (mUsedMemory > CRITERION_CACHE_ONE) {
            mCacheScore = ScoreConstants.SCORE_CACHE_TWO;
        } else {
            mCacheScore = ScoreConstants.SCORE_CACHE_ONE;
        }
    }

    public void setAppLockState(String pkgName, int state) {
        if (state != ScoreConstants.LOCK_STATE_LOCK && state != ScoreConstants.LOCK_STATE_UNLOCK) {
            return;
        }

        TrashAppInfo trashInfo = mTrashAppsMap.get(pkgName);
        if (trashInfo != null && trashInfo.lockState != state) {
            trashInfo.lockState = state;
            if (state == ScoreConstants.LOCK_STATE_LOCK) {
                mUsedMemory -= trashInfo.memoryUsed;
            } else if (state == ScoreConstants.LOCK_STATE_UNLOCK) {
                mUsedMemory += trashInfo.memoryUsed;
            }
            calculateScore();
        }
    }

    public int getAppLockState(String pkgName) {
        if (pkgName == null) {
            return ScoreConstants.LOCK_STATE_LOCK;
        }

        TrashAppInfo trashInfo = mTrashAppsMap.get(pkgName);
        if (trashInfo == null) {
            return ScoreConstants.LOCK_STATE_UNLOCK;
        }
        return trashInfo.lockState;
    }
}
