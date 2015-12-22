
package com.miui.optimizecenter.uninstallmonitor;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

public class PackageSyncService extends IntentService {
    static final String TAG = PackageSyncService.class.getSimpleName();

    public PackageSyncService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (PackagesManager.getInstance(this).isPackageEmpty()) {
            doPackageSync(this);
        }
    }

    private void doPackageSync(Context context) {

        PackageManager pm = context.getPackageManager();
        List<PackageInfo> all = pm.getInstalledPackages(0);
        List<PackageInfo> installed = new ArrayList<PackageInfo>();

        // 获取非系统应用程序
        for (PackageInfo info : all) {
            if ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                installed.add(info);
            }
        }

        List<PackageModel> models = new ArrayList<PackageModel>();
        for (PackageInfo info : installed) {
            Drawable launcher = info.applicationInfo.loadIcon(pm);
            String label = info.applicationInfo.loadLabel(pm).toString();
            String packageName = info.packageName;
            String versionName = info.versionName;
            int versionCode = info.versionCode;

            models.add(PackageModel.create(launcher, label, packageName, versionName, versionCode));
        }

        // 缓存非系统应用程序
        PackagesManager.getInstance(context).addPackages(models);

    }
}
