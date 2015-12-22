
package com.miui.optimizecenter.tools;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.RemoteException;
import android.text.TextUtils;

import com.miui.common.AndroidUtils;
import com.miui.guardprovider.service.ProxyFileInfo;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

public class CacheUtils {

    public static boolean isRunningProcess(List<RunningAppProcessInfo> processes, String packageName) {
        for (RunningAppProcessInfo proc : processes) {
            String[] pkgList = proc.pkgList;
            if (pkgList != null) {
                for (String pkg : pkgList) {
                    if (TextUtils.equals(packageName, pkg)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 清空系统缓存 need permission <uses-permission
     * android:name="android.permission.CLEAR_APP_CACHE" />
     * 
     * @param context
     * @param observer
     */
    public static void clearSystemCache(Context context) {
        IPackageDataObserver observer = new IPackageDataObserver.Stub() {
            @Override
            public void onRemoveCompleted(String packageName, boolean succeeded)
                    throws RemoteException {
                // ignore
            }
        };

        try {
            PackageManager pm = context.getPackageManager();
            List<ApplicationInfo> apps = pm.getInstalledApplications(0);
            for (ApplicationInfo info : apps) {
                clearSystemCache(context, info.packageName, observer);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearSystemCache(Context context, String pkgName,
            IPackageDataObserver observer) {
        try {
            PackageManager pm = context.getPackageManager();
            pm.deleteApplicationCacheFiles(pkgName, observer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param context
     * @param statsObserver
     * @param packageName
     */
    public static void getPackageCacheSize(Context context, IPackageStatsObserver statsObserver,
            String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            Method getPackageSizeInfo = pm.getClass().getMethod("getPackageSizeInfo", String.class,
                    IPackageStatsObserver.class);
            getPackageSizeInfo.invoke(pm, packageName, statsObserver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param context
     * @param statsObserver
     * @return app count
     */
    public static int getSystemCacheSize(Context context, IPackageStatsObserver statsObserver) {
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        PackageManager pm = context.getPackageManager();

        List<RunningAppProcessInfo> processes = am.getRunningAppProcesses();
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);
        for (ApplicationInfo info : apps) {
            if (!isRunningProcess(processes, info.packageName)) {
                getPackageCacheSize(context, statsObserver, info.packageName);
            }
        }
        return apps.size();
    }

}
