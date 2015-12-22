
package com.miui.antivirus;

import java.io.File;

import miui.provider.ExtraGuard;
import miui.provider.ExtraGuardVirusInfoEntity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.text.TextUtils;

public class ExtraGuardHelper {

    public static final int VIRUS_SAFE = 1;
    // 病毒扫描之后的结果，2为病毒 ，3为风险
    public static final int VIRUS_BLACK = 2;
    public static final int VIRUS_GRAY = 3;

    private static ExtraGuardHelper INST;

    private Context mContext;

    private WhiteListHelper mWhiteListHelper;

    private ExtraGuardHelper(Context context) {
        mContext = context;
        mWhiteListHelper = WhiteListHelper.getInstance(context);
    }

    public static ExtraGuardHelper getInstance(Context context) {
        if (INST == null) {
            INST = new ExtraGuardHelper(context.getApplicationContext());
        }
        return INST;
    }

    // 增量
    public ExtraGuardVirusInfoEntity incrementalCheckInstalledApp(ApplicationInfo appInfo,
            boolean cloud) {
        if (appInfo == null) {
            return null;
        }
        String pkgName = appInfo.packageName;
        if (mWhiteListHelper.inAppsWhiteList(pkgName)) {
            ExtraGuardVirusInfoEntity virusEntity = new ExtraGuardVirusInfoEntity();
            virusEntity.setPackageName(pkgName);
            virusEntity.setType(VIRUS_SAFE);
            return virusEntity;
        }
        ExtraGuardVirusInfoEntity virusEntity = ExtraGuard.checkApkForVirusInfo(mContext,
                Uri.fromFile(new File(appInfo.sourceDir)), cloud);
        if (virusEntity != null && virusEntity.getType() != VIRUS_BLACK
                && virusEntity.getType() != VIRUS_GRAY) {
            mWhiteListHelper.insertAppsWhiteList(pkgName);
        }
        return virusEntity;
    }

    // 全量
    public ExtraGuardVirusInfoEntity fullAmountCheckInstalledApp(ApplicationInfo appInfo,
            boolean cloud) {
        if (appInfo == null) {
            return null;
        }
        return ExtraGuard.checkApkForVirusInfo(mContext, Uri.fromFile(new File(appInfo.sourceDir)),
                cloud);
    }

    // 增量扫描SDcard apk文件
    public ExtraGuardVirusInfoEntity incrementalCheckUninstalledApk(String apkPath, boolean cloud) {
        if (TextUtils.isEmpty(apkPath)) {
            return null;
        }
        if (mWhiteListHelper.inApkWhiteList(apkPath)) {
            ExtraGuardVirusInfoEntity virusEntity = new ExtraGuardVirusInfoEntity();
            virusEntity.setType(VIRUS_SAFE);
            return virusEntity;
        }

        ExtraGuardVirusInfoEntity virusEntity = ExtraGuard.checkApkForVirusInfo(mContext,
                Uri.fromFile(new File(apkPath)), cloud);
        if (virusEntity != null && virusEntity.getType() != VIRUS_BLACK
                && virusEntity.getType() != VIRUS_GRAY) {
            mWhiteListHelper.insertApkWhiteList(apkPath);
        }
        return virusEntity;
    }

    // 全量增量扫描SDcard apk文件
    public ExtraGuardVirusInfoEntity fullAmountCheckUninstalledApk(String apkPath, boolean cloud) {
        return ExtraGuard.checkApkForVirusInfo(mContext, Uri.fromFile(new File(apkPath)), cloud);
    }
}
