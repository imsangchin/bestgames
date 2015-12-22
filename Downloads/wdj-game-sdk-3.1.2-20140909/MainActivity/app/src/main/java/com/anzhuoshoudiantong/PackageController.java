package com.anzhuoshoudiantong;

import java.util.HashSet;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public class PackageController {
  private static PackageController instance;
  private Context mContext;
  private PackageManager packageManager;
  private HashSet<String> packageNames;

  private PackageController() {
    packageNames = new HashSet<String>();
  }

  public PackageController setContext(Context context) {
    mContext = context;
    packageManager = mContext.getPackageManager();
    return instance;
  }

  public static synchronized PackageController getInstance() {
    if (instance == null) {
      instance = new PackageController();
    }
    return instance;
  }

  public boolean isInstalled(String packageName) {
    if (packageNames.size() > 0) {
      return packageNames.contains(packageName);
    }
    return false;
  }

  public void startLoading() {
    new LoadAppThread().start();
  }

  class LoadAppThread extends Thread {
    @Override
    public void run() {
      packageNames.clear();
      List<ApplicationInfo> applicationInfos =
          packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
      if (applicationInfos != null) {
        for (ApplicationInfo applicationInfo : applicationInfos) {
          packageNames.add(applicationInfo.packageName);
        }
      }
    }
  }

}
