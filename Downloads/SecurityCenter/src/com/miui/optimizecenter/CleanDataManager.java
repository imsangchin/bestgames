
package com.miui.optimizecenter;

import android.content.Context;

import com.miui.optimizecenter.ad.AdModel;
import com.miui.optimizecenter.apk.ApkModel;
import com.miui.optimizecenter.cache.CacheModel;
import com.miui.optimizecenter.deepclean.InstalledAppModel;
import com.miui.optimizecenter.deepclean.LargeFileModel;
import com.miui.optimizecenter.residual.ResidualModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CleanDataManager {

    private static CleanDataManager INST;

    private CleanDataManager(Context context) {
        // ignore
    }

    public static CleanDataManager getInstance(Context context) {
        if (INST == null) {
            INST = new CleanDataManager(context.getApplicationContext());
        }
        return INST;
    }

    private Map<String, CacheModel> mDeepCleanCacheMaps = new HashMap<String, CacheModel>();

    public void clearDeepCleanCacheMaps() {
        mDeepCleanCacheMaps.clear();
    }

    public Map<String, CacheModel> getDeepCleanCacheMaps() {
        return mDeepCleanCacheMaps;
    }

    public CacheModel getDeepCleanCacheModel(String dirPath) {
        return mDeepCleanCacheMaps.get(dirPath);
    }

    public void addDeepCleanCacheModel(String dirPath, CacheModel cache) {
        mDeepCleanCacheMaps.put(dirPath, cache);
    }

    public void removeDeepCleanCacheModel(String dirPath) {
        mDeepCleanCacheMaps.remove(dirPath);
    }

    // Map<DirPath, Model>
    private Map<String, CacheModel> mCacheMaps = new HashMap<String, CacheModel>();
    private Map<String, AdModel> mAdMaps = new HashMap<String, AdModel>();
    private Map<String, ResidualModel> mResidualMaps = new HashMap<String, ResidualModel>();
    private Map<String, ApkModel> mApkMaps = new HashMap<String, ApkModel>();

    private List<String> mEmptyFolderPaths = new ArrayList<String>();

    private boolean mHasUpdateAdMaps = false;
    private boolean mHasUpdateApkMaps = false;
    private boolean mHasUpdateCacheMaps = false;
    private boolean mHasUpdateResidualMaps = false;

    public List<String> getEmptyFolderPaths() {
        return mEmptyFolderPaths;
    }

    public void setEmptyFolderPaths(List<String> paths) {
        mEmptyFolderPaths.clear();
        mEmptyFolderPaths.addAll(paths);
    }

    public void clearCacheMaps() {
        mCacheMaps.clear();
    }

    public Map<String, CacheModel> getCacheMaps() {
        return mCacheMaps;
    }

    public CacheModel getCacheModel(String dirPath) {
        return mCacheMaps.get(dirPath);
    }

    public boolean addCacheModel(String dirPath, CacheModel cache) {
        if (mCacheMaps.containsKey(dirPath)) {
            return false;
        } else {
            mCacheMaps.put(dirPath, cache);
            return true;
        }
    }

    public void removeCacheModel(String dirPath) {
        mCacheMaps.remove(dirPath);
    }

    public Map<String, CacheModel> getAdviseDeleteCacheMaps() {
        Map<String, CacheModel> adviseDeleteMaps = new HashMap<String, CacheModel>();

        Set<String> keys = mCacheMaps.keySet();
        for (String key : keys) {
            CacheModel model = mCacheMaps.get(key);
            if (model.adviseDelete()) {
                adviseDeleteMaps.put(key, model);
            }
        }
        return adviseDeleteMaps;
    }

    public long getAdviseDeleteCacheSize() {
        long cacheSize = 0;
        Set<String> keys = mCacheMaps.keySet();
        for (String key : keys) {
            CacheModel model = mCacheMaps.get(key);
            if (model.adviseDelete()) {
                cacheSize += model.getFileSize();
            }
        }
        return cacheSize;
    }

    public boolean hasUpdateCacheMaps() {
        return mHasUpdateCacheMaps;
    }

    public void setHasUpdateCacheMaps(boolean update) {
        mHasUpdateCacheMaps = update;
    }

    public void clearAdMaps() {
        mAdMaps.clear();
    }

    public Map<String, AdModel> getAdMaps() {
        return mAdMaps;
    }

    public AdModel getAdModel(String dirPath) {
        return mAdMaps.get(dirPath);
    }

    public void addAdModel(String dirPath, AdModel ad) {
        mAdMaps.put(dirPath, ad);
    }

    public boolean isAdModelAdviseDelete(String dirPath, boolean defValue) {
        AdModel ad = mAdMaps.get(dirPath);
        if (ad == null) {
            return defValue;
        }
        return ad.adviseDelete();
    }

    public void removeAdModel(String dirPath) {
        mAdMaps.remove(dirPath);
    }

    public boolean isAdviseDeleteAdMapsEmpty() {
        Set<String> keys = mAdMaps.keySet();
        for (String key : keys) {
            if (mAdMaps.get(key).adviseDelete()) {
                return false;
            }
        }
        return true;
    }

    public Map<String, AdModel> getAdviseDeleteAdMaps() {
        Map<String, AdModel> adviseDeleteMaps = new HashMap<String, AdModel>();

        Set<String> keys = mAdMaps.keySet();
        for (String key : keys) {
            AdModel model = mAdMaps.get(key);
            if (model.adviseDelete()) {
                adviseDeleteMaps.put(key, model);
            }
        }
        return adviseDeleteMaps;
    }

    public long getAdviseDeleteAdSize() {
        long adSize = 0;
        Set<String> keys = mAdMaps.keySet();
        for (String key : keys) {
            AdModel model = mAdMaps.get(key);
            if (model.adviseDelete()) {
                adSize += model.getFileSize();
            }
        }
        return adSize;
    }

    public boolean hasUpdateAdMaps() {
        return mHasUpdateAdMaps;
    }

    public void setHasUpdateAdMaps(boolean update) {
        mHasUpdateAdMaps = update;
    }

    public void clearResidualMaps() {
        mResidualMaps.clear();
    }

    public Map<String, ResidualModel> getResidualMaps() {
        return mResidualMaps;
    }

    public ResidualModel getResidualModel(String dirPath) {
        return mResidualMaps.get(dirPath);
    }

    public void addResidualModel(String dirPath, ResidualModel residual) {
        mResidualMaps.put(dirPath, residual);
    }

    public boolean isResidualModelAdviseDelete(String dirPath, boolean defValue) {
        ResidualModel residual = mResidualMaps.get(dirPath);
        if (residual == null) {
            return defValue;
        }
        return residual.adviseDelete();
    }

    public void removeResidualModel(String dirPath) {
        mResidualMaps.remove(dirPath);
    }

    public boolean isAdviseDeleteResidualMapsEmpty() {
        Set<String> keys = mResidualMaps.keySet();
        for (String key : keys) {
            if (mResidualMaps.get(key).adviseDelete()) {
                return false;
            }
        }
        return true;
    }

    public Map<String, ResidualModel> getAdviseDeleteResidualMaps() {
        Map<String, ResidualModel> adviseDeleteMaps = new HashMap<String, ResidualModel>();

        Set<String> keys = mResidualMaps.keySet();
        for (String key : keys) {
            ResidualModel model = mResidualMaps.get(key);
            if (model.adviseDelete()) {
                adviseDeleteMaps.put(key, model);
            }
        }
        return adviseDeleteMaps;
    }

    public long getAdviseDeleteResidualSize() {
        long residualSize = 0;
        Set<String> keys = mResidualMaps.keySet();
        for (String key : keys) {
            ResidualModel model = mResidualMaps.get(key);
            if (model.adviseDelete()) {
                residualSize += model.getFileSize();
            }
        }
        return residualSize;
    }

    public boolean hasUpdateResidualMaps() {
        return mHasUpdateResidualMaps;
    }

    public void setHasUpdateResidualMaps(boolean update) {
        mHasUpdateResidualMaps = update;
    }

    public void clearApkMaps() {
        mApkMaps.clear();
    }

    public Map<String, ApkModel> getApkMaps() {
        return mApkMaps;
    }

    public ApkModel getApkModel(String dirPath) {
        return mApkMaps.get(dirPath);
    }

    public void addApkModel(String dirPath, ApkModel apk) {
        mApkMaps.put(dirPath, apk);
    }

    public boolean isApkModelAdviseDelete(String dirPath, boolean defValue) {
        ApkModel apk = mApkMaps.get(dirPath);
        if (apk == null) {
            return defValue;
        }
        return apk.adviseDelete();
    }

    public void removeApkModel(String dirPath) {
        mApkMaps.remove(dirPath);
    }

    public boolean isAdviseDeleteApkMapsEmpty() {
        Set<String> keys = mApkMaps.keySet();
        for (String key : keys) {
            if (mApkMaps.get(key).adviseDelete()) {
                return false;
            }
        }
        return true;
    }

    public Map<String, ApkModel> getAdviseDeleteApkMaps() {
        Map<String, ApkModel> adviseDeleteMaps = new HashMap<String, ApkModel>();

        Set<String> keys = mApkMaps.keySet();
        for (String key : keys) {
            ApkModel model = mApkMaps.get(key);
            if (model.adviseDelete()) {
                adviseDeleteMaps.put(key, model);
            }
        }
        return adviseDeleteMaps;
    }

    public long getAdviseDeleteApkSize() {
        long apkSize = 0;
        Set<String> keys = mApkMaps.keySet();
        for (String key : keys) {
            ApkModel model = mApkMaps.get(key);
            if (model.adviseDelete()) {
                apkSize += model.getFileSize();
            }
        }
        return apkSize;
    }

    public boolean hasUpdateApkMaps() {
        return mHasUpdateApkMaps;
    }

    public void setHasUpdateApkMaps(boolean update) {
        mHasUpdateApkMaps = update;
    }

    public void clearData() {
        mHasUpdateApkMaps = false;
        mHasUpdateAdMaps = false;
        mHasUpdateCacheMaps = false;
        mHasUpdateResidualMaps = false;

        mAdMaps.clear();
        mApkMaps.clear();
        mCacheMaps.clear();
        mResidualMaps.clear();
    }

    private Map<String, LargeFileModel> mLargeFiles = new HashMap<String, LargeFileModel>();

    public void clearLargeFilesMap() {
        mLargeFiles.clear();
    }

    public void addLargeFileModel(LargeFileModel largeFile) {
        mLargeFiles.put(largeFile.getPath(), largeFile);
    }

    public LargeFileModel getLargeFileModel(String dirPath) {
        return mLargeFiles.get(dirPath);
    }

    public void removeLargeFileModel(String dirPath) {
        mLargeFiles.remove(dirPath);
    }

    public Map<String, LargeFileModel> getLargeFilesMap() {
        return mLargeFiles;
    }

    private Map<String, InstalledAppModel> mInstalledApps = new HashMap<String, InstalledAppModel>();

    public void clearInstalledAppsMap() {
        mInstalledApps.clear();
    }

    public void addInstalledAppModel(InstalledAppModel installedApp) {
        mInstalledApps.put(installedApp.getPackageInfo().packageName, installedApp);
    }

    public InstalledAppModel getInstalledAppModel(String pkgName) {
        return mInstalledApps.get(pkgName);
    }

    public void removeInstalledAppModel(String pkgName) {
        mInstalledApps.remove(pkgName);
    }

    public Map<String, InstalledAppModel> getInstalledAppsMap() {
        return mInstalledApps;
    }
}
