package com.anzhuoshoudiantong;

import android.app.Application;
import android.app.DownloadManager;
import android.content.IntentFilter;
import android.os.StrictMode;

public class FlashlightApplication extends Application {
  private static final boolean DEVELOPER_MODE = false;

  @Override
  public void onCreate() {

    if (DEVELOPER_MODE) {
      StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
          .detectAll()
          .penaltyLog()
          .build());
      StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
          .detectLeakedSqlLiteObjects()
          .detectLeakedClosableObjects()
          .penaltyLog()
          .penaltyDeath()
          .build());
    }
    super.onCreate();
    PackageController.getInstance().setContext(getApplicationContext()).startLoading();
    DownloadController.getInstance().setContext(getApplicationContext());
    registerReceiver(DownloadController.getInstance().getDownloadReceiver(), new IntentFilter(
      DownloadManager.ACTION_DOWNLOAD_COMPLETE));
  }

  @Override
  public void onTerminate() {
    super.onTerminate();
  }

}
