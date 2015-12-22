package com.anzhuoshoudiantong;

import com.anzhuoshoudiantong.ads.Ad;
import com.umeng.analytics.MobclickAgent;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    Ad notificationAd = (Ad) intent.getSerializableExtra("ad");
    if (notificationAd == null) {
      return;
    }
    NotificationManager notificationManager = (NotificationManager) context
        .getSystemService(Context.NOTIFICATION_SERVICE);
    NotificationCompat.Builder builder = new NotificationCompat.Builder(
        context).setSmallIcon(R.drawable.noti).setContentText(
        MobclickAgent.getConfigParams(context,
            Const.AD_NOTIFICATION_SUBTITLE)).setContentTitle(
        MobclickAgent.getConfigParams(context,
            Const.AD_NOTIFICATION_TITLE));
    Uri actionUri = Uri.parse(notificationAd.content);
    if (actionUri == null) {
      return;
    }

    Intent launchIntent = new Intent();
    launchIntent.setAction(Intent.ACTION_VIEW);
    launchIntent.setData(actionUri);

    PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
        launchIntent, 0);
    builder.setContentIntent(contentIntent);

    Notification notification = builder.getNotification();
    notification.flags = Notification.FLAG_ONLY_ALERT_ONCE
        | Notification.FLAG_AUTO_CANCEL;
    notificationManager.notify(0, notification);

    MobclickAgent.onError(context,
        Logs.Events.NOTIFICATION_AD_CLICK);

  }
}
