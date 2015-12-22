
package com.miui.optimizecenter.whitelist;

import android.text.TextUtils;

import com.miui.optimizecenter.whitelist.WhiteListManager.AdWhiteInfo;
import com.miui.optimizecenter.whitelist.WhiteListManager.ApkWhiteInfo;
import com.miui.optimizecenter.whitelist.WhiteListManager.CacheWhiteInfo;
import com.miui.optimizecenter.whitelist.WhiteListManager.LargeFileWhiteInfo;
import com.miui.optimizecenter.whitelist.WhiteListManager.ResidualWhiteInfo;

import java.io.File;

public class WhiteListItemModel {

    public WhiteListItemModel() {
        // ignore
    }

    private WhiteListType mWhiteListType;

    private String mTitle;

    private String mDirPath;

    private String mPkgName;

    private boolean mIsChecked;

    public WhiteListType getWhiteListType() {
        return mWhiteListType;
    }

    public void setWhiteListType(WhiteListType whiteListType) {
        mWhiteListType = whiteListType;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getDirPath() {
        return mDirPath;
    }

    public void setDirPath(String dirPath) {
        this.mDirPath = dirPath;
    }

    public String getPkgName() {
        return mPkgName;
    }

    public void setPkgName(String pkgName) {
        mPkgName = pkgName;
    }

    public boolean isChecked() {
        return mIsChecked;
    }

    public void setChecked(boolean checked) {
        mIsChecked = checked;
    }

    public static WhiteListItemModel createFromCache(CacheWhiteInfo cache) {
        WhiteListItemModel res = new WhiteListItemModel();
        res.mTitle = cache.cacheType;
        res.mDirPath = cache.dirPath;
        res.mPkgName = cache.pkgName;
        res.mWhiteListType = WhiteListType.CACHE;
        return res;
    }

    public static WhiteListItemModel createFromAd(AdWhiteInfo ad) {
        WhiteListItemModel res = new WhiteListItemModel();
        res.mTitle = ad.name;
        res.mDirPath = ad.dirPath;
        res.mWhiteListType = WhiteListType.AD;
        return res;
    }

    public static WhiteListItemModel createFromApk(ApkWhiteInfo apk) {
        WhiteListItemModel res = new WhiteListItemModel();
        res.mTitle = TextUtils.isEmpty(apk.appName) ? new File(apk.dirPath).getName() : apk.appName;
        res.mDirPath = apk.dirPath;
        res.mWhiteListType = WhiteListType.APK;
        return res;
    }

    public static WhiteListItemModel createFromResidual(ResidualWhiteInfo residual) {
        WhiteListItemModel res = new WhiteListItemModel();
        res.mTitle = residual.descName;
        res.mDirPath = residual.dirPath;
        res.mWhiteListType = WhiteListType.RESIDUAL;
        return res;
    }

    public static WhiteListItemModel createFromLargeFile(LargeFileWhiteInfo largeFile) {
        WhiteListItemModel res = new WhiteListItemModel();
        res.mTitle = new File(largeFile.dirPath).getName();
        res.mDirPath = largeFile.dirPath;
        res.mWhiteListType = WhiteListType.LARGE_FILE;
        return res;
    }

    @Override
    public String toString() {
        return "WhiteListType mWhiteListType = " + mWhiteListType + " mTitle = " + mTitle
                + " mDirPath = " + mDirPath + " mPkgName = " + mPkgName + " mIsChecked = "
                + mIsChecked;
    }
}
