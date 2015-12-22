
package com.miui.optimizecenter.tools;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.miui.common.FileIconHelper;
import com.miui.guardprovider.service.IFileProxy;
import com.miui.guardprovider.service.ProxyPackageInfo;
import com.miui.optimizecenter.apk.ApkModel;
import com.miui.optimizecenter.enums.ApkStatus;

import java.io.File;
import java.util.List;

public class ApkUtils {

    /**
     * 判断apk信息，赋值给model
     * 
     * @param context
     * @param model
     * @param apkFile
     */
    public static void checkApkStatus(Context context, ApkModel model, String apkPath,
            IFileProxy proxy) {
        model.setAbsolutePath(apkPath);

        PackageManager pm = context.getPackageManager();

        try {
            ProxyPackageInfo info = proxy.getPackageInfo(apkPath);

            if (info == null) {
                model.setLauncher(FileIconHelper.getDefaultApkIcon(context));
                model.setStatus(ApkStatus.DAMAGED);
                return;
            }

            PackageInfo packageInfo = info.getPackageInfo();
            if (packageInfo == null) {
                model.setLauncher(FileIconHelper.getDefaultApkIcon(context));
                model.setStatus(ApkStatus.DAMAGED);
                return;
            }

            // ApplicationInfo appInfo = packageInfo.applicationInfo;
            //
            // appInfo.sourceDir = apkPath;
            // appInfo.publicSourceDir = apkPath;

            // Drawable launcher = appInfo.loadIcon(pm);
            // model.setLauncher(launcher);

            String packageName = packageInfo.packageName;
            model.setPackageName(packageName);

            CharSequence label = info.getLabel();
            model.setApplicationLabel(label == null ? packageName : label.toString());

            model.setVersionName(packageInfo.versionName);
            int versionCode = packageInfo.versionCode;
            model.setVersionCode(versionCode);

            ApkStatus status = checkApkStatus(pm, packageName, versionCode);
            model.setStatus(status);
        } catch (Exception e) {
            model.setLauncher(FileIconHelper.getDefaultApkIcon(context));
            model.setStatus(ApkStatus.DAMAGED);
            return;
        }

    }

    /*
     * 判断该应用是否在手机上已经安装过，有以下集中情况出现 1.未安装 2.已安装 3.已安装，但是版本有更新
     * @param pm PackageManager
     * @param packageName 要判断应用的包名
     * @param versionCode 要判断应用的版本号
     */
    public static ApkStatus checkApkStatus(PackageManager pm, String packageName, int versionCode) {
        List<PackageInfo> pakageinfos = pm
                .getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (PackageInfo pi : pakageinfos) {
            String pi_packageName = pi.packageName;
            int pi_versionCode = pi.versionCode;
            // 如果这个包名在系统已经安装过的应用中存在
            if (packageName.endsWith(pi_packageName)) {
                if (versionCode == pi_versionCode) {
                    return ApkStatus.INSTALLED;
                } else if (versionCode < pi_versionCode) {
                    return ApkStatus.INSTALLED_OLD;
                }
            }
        }
        return ApkStatus.UNINSTALLED;
    }

    /**
     * 递归获取所有的apk绝对路径
     * 
     * @param file
     * @param apkPaths
     */
    public static void findExternalStorageApks(File file, List<String> apkPaths) {
        if (file.isFile()) {
            String name = file.getName();
            if (name.toLowerCase().endsWith(".apk")) {
                apkPaths.add(file.getAbsolutePath());
            }
        } else {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File f : files) {
                    findExternalStorageApks(f, apkPaths);
                }
            }
        }
    }

    /**
     * @param fileA
     * @param fileB
     * @return fileA == fileB ? true : false.
     */
    public static boolean isApkEquals(Context context, File apkFileA, File apkFileB) {
        if (apkFileA.length() != apkFileB.length()) {
            return false;
        }

        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfoA = pm.getPackageArchiveInfo(apkFileA.getAbsolutePath(),
                PackageManager.GET_ACTIVITIES);
        if (packageInfoA == null) {
            return false;
        }

        PackageInfo packageInfoB = pm.getPackageArchiveInfo(apkFileB.getAbsolutePath(),
                PackageManager.GET_ACTIVITIES);
        if (packageInfoB == null) {
            return false;
        }

        if (!TextUtils.equals(packageInfoA.packageName, packageInfoB.packageName)) {
            return false;
        }

        if (!TextUtils.equals(packageInfoA.versionName, packageInfoB.versionName)) {
            return false;
        }

        if (packageInfoA.versionCode != packageInfoB.versionCode) {
            return false;
        }

        return true;
    }

}
