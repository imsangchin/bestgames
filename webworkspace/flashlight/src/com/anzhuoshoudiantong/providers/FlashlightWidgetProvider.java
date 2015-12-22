package com.anzhuoshoudiantong.providers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.anzhuoshoudiantong.R;

public class FlashlightWidgetProvider extends AppWidgetProvider {
  public static final String ACTION_WIDGET_RECEIVER = "com.anzhuoshoudiantong.flashlight";
  public static final String ACTION_WIDGET_ACTIVITY = "com.anzhuoshoudiantong.main";
  private RemoteViews remoteViews;
  private static boolean isLightOn = false;
  private static Camera camera;

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager,
      int[] appWidgetIds) {
    remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
    Intent active = new Intent(context, FlashlightWidgetProvider.class);
    active.setAction(ACTION_WIDGET_RECEIVER);
    PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);
    remoteViews.setOnClickPendingIntent(R.id.widget_button, actionPendingIntent);
    appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    final String action = intent.getAction();
    if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
      final int appWidgetId = intent.getExtras().getInt(
          AppWidgetManager.EXTRA_APPWIDGET_ID,
          AppWidgetManager.INVALID_APPWIDGET_ID);
      if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
        this.onDeleted(context, new int[] {appWidgetId});
      }
    } else if (intent.getAction().equals(ACTION_WIDGET_RECEIVER)) {
      Intent active = new Intent(context, FlashlightWidgetProvider.class);
      active.setAction(ACTION_WIDGET_RECEIVER);
      active.putExtra("msg", context.getText(R.string.turn_on));
      PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);
      NotificationManager notificationManager =
          (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
      Notification noty =
          new Notification(R.drawable.ic_launcher, context.getText(R.string.app_name),
              System.currentTimeMillis());
      noty.setLatestEventInfo(context, context.getText(R.string.app_name),
          intent.getStringExtra("msg"),
          null);
      notificationManager.notify(1, noty);
      RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

      if (isLightOn) {
        views.setImageViewResource(R.id.widget_button, R.drawable.flashlight_switcher_off);
      } else {
        views.setImageViewResource(R.id.widget_button, R.drawable.flashlight_switcher_on);
      }

      views.setOnClickPendingIntent(R.id.widget_button, actionPendingIntent);
      int appWidgetIds[];
      AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
      appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(
          context, FlashlightWidgetProvider.class));
      appWidgetManager.updateAppWidget(appWidgetIds, views);

      if (isLightOn) {
        if (camera != null) {
          camera.stopPreview();
          camera.release();
          camera = null;
          isLightOn = false;
          notificationManager.cancel(1);
        }
      } else {
        camera = Camera.open();
        if (camera == null) {
          Toast.makeText(context, R.string.no_camera, Toast.LENGTH_SHORT).show();
        } else {
          Parameters param = camera.getParameters();
          param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
          try {
            camera.setParameters(param);
            camera.startPreview();
            isLightOn = true;
          } catch (Exception e) {
            Toast.makeText(context, R.string.no_flash, Toast.LENGTH_SHORT).show();
          }
        }
      }
    }
    super.onReceive(context, intent);
  }
}
