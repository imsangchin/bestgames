package com.anzhuoshoudiantong.ads;

import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;
import com.anzhuoshoudiantong.AlarmReceiver;
import com.anzhuoshoudiantong.Const;
import com.anzhuoshoudiantong.DownloadController;
import com.anzhuoshoudiantong.PackageController;
import com.umeng.analytics.MobclickAgent;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.ImageView;

public class AdController {
  public static final String WANDOUJIA_PACKAGE_NAME = "com.wandoujia.phoenix2";
  private Context context;
  private static AdController instance;
  private Ad bannerAd;
  private Ad fullscreenAd;
  private Ad notificationAd;

  private AdController(Context context) {
    super();
    this.context = context;
  }

  public static synchronized AdController getInstance(Context context) {
    if (instance != null) {
      return instance;
    } else {
      return new AdController(context);
    }
  }

  public void init() {
    String bannerImageUrl = MobclickAgent.getConfigParams(context,
        Const.AD_BANNER_IMAGE_URL);
    String bannerSwitcher = MobclickAgent.getConfigParams(context,
        Const.AD_BANNER_SWITCHER);
    String bannerContent = MobclickAgent.getConfigParams(context,
        Const.AD_BANNER_CONTENT);
    if (Const.ON.equals(bannerSwitcher)) {
      bannerAd = new Ad(bannerImageUrl, bannerSwitcher, bannerContent);
    }

    String fullscreenImageUrl = MobclickAgent.getConfigParams(context,
        Const.AD_FULLSCREEN_IMAGE_URL);
    String fullscreenSwitcher = MobclickAgent.getConfigParams(context,
        Const.AD_FULLSCREEN_SWITCHER);
    String fullscreenContent = MobclickAgent.getConfigParams(context,
        Const.AD_FULLSCREEN_CONTENT);
    if (Const.ON.equals(fullscreenSwitcher)) {
      fullscreenAd = new Ad(fullscreenImageUrl, fullscreenSwitcher,
          fullscreenContent);
    }

    String notificationImageUrl = MobclickAgent.getConfigParams(context,
        Const.AD_NOTIFICATION_IMAGE_URL);
    String notificationSwitcher = MobclickAgent.getConfigParams(context,
        Const.AD_NOTIFICATION_SWITCHER);
    String notificationContent = MobclickAgent.getConfigParams(context,
        Const.AD_NOTIFICATION_CONTENT);
    if (Const.ON.equals(notificationSwitcher)) {
      notificationAd = new Ad(notificationImageUrl, notificationSwitcher,
          notificationContent);
    }

  }

  public void showBannerAd(ImageView image) {
    if (PackageController.getInstance().isInstalled(WANDOUJIA_PACKAGE_NAME)) {
      return;
    }
    if (bannerAd != null && Const.ON.equals(bannerAd.switcher)) {
      bannerAd.show(context, image);
    }
  }

  public void showFullscreenAd(ImageView image) {
    if (PackageController.getInstance().isInstalled(WANDOUJIA_PACKAGE_NAME)) {
      return;
    }
    if (fullscreenAd != null && Const.ON.equals(fullscreenAd.switcher)) {
      fullscreenAd.show(context, image);
    }
  }

  public void downloadFromBannerAd() {
    if (bannerAd != null) {
      String downloadTitle = MobclickAgent.getConfigParams(context,
          Const.DOWNLOAD_TITLE);
      DownloadController.getInstance().download(bannerAd.content, downloadTitle);
    }
  }

  public void downloadFromFullScreenAd() {
    if (fullscreenAd != null) {
      String downloadTitle = MobclickAgent.getConfigParams(context,
          Const.DOWNLOAD_TITLE);
      DownloadController.getInstance().download(fullscreenAd.content, downloadTitle);
    }
  }

  public void showNotificationAd() {
    if (notificationAd == null) {
      return;
    }

    if (PackageController.getInstance().isInstalled(WANDOUJIA_PACKAGE_NAME)) {
      return;
    }
    Random random = new Random();
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
    calendar.set(Calendar.HOUR_OF_DAY, random.nextInt(24));
    calendar.set(Calendar.MINUTE, random.nextInt(60));
    calendar.set(Calendar.SECOND, random.nextInt(60));

    Intent intent = new Intent(context, AlarmReceiver.class);
    intent.putExtra("ad", notificationAd);

    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
        intent, 0);
    AlarmManager alarmManager = (AlarmManager) context
        .getSystemService(Context.ALARM_SERVICE);
    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
        pendingIntent);
  }

}
