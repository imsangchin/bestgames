
package com.miui.mdb;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.format.DateUtils;

public class DataUpdateReceiver extends BroadcastReceiver {
    public static final String LAST_UPDATE_TIME = "last_update_time";
    public static final String ACTION_DATA_UPDATE = "action_data_update";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (miui.os.Build.IS_INTERNATIONAL_BUILD) {
            return;
        }

        final String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            // 如果上次更新跟这次超过24小时，重新查询
            if (isUpdateTimeOverDay(context)) {
                tryUpdate(context);
            } else {
                // 如果没有超过24小时，计算下一次查询的时间
                final long lastTime = PreferenceManager.getDefaultSharedPreferences(context)
                        .getLong(LAST_UPDATE_TIME, 0);
                setAlarm(context, lastTime + DateUtils.DAY_IN_MILLIS);
            }
        } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            if (isUpdateTimeOverDay(context)) {
                tryUpdate(context);
            }
        } else if (ACTION_DATA_UPDATE.equals(action)) {
            tryUpdate(context);
        }
    }

    // 尝试更新数据，根据系统更新的仅在wifi下更新, 更新成功后，更新LAST_UPDATE_TIME
    public void tryUpdate(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isUpdate = false;
        NetworkInfo info = connManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            // 非收费网络使用WIFI配置
            if (!connManager.isActiveNetworkMetered()) {
                isUpdate = true;
            }
        }

        if (isUpdate) {
            // 设定定时器，24小时之后再检查
            setAlarm(context, System.currentTimeMillis() + DateUtils.DAY_IN_MILLIS);
            context.startService(new Intent(context, DataUpdateService.class));
        }
    }

    public static void setAlarm(Context context, long triggerAtMillis) {
        Intent intent = new Intent(context, DataUpdateReceiver.class);
        intent.setAction(ACTION_DATA_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.set(AlarmManager.RTC, triggerAtMillis, pendingIntent);
    }

    // 距离上次查询是否超过一天
    public static boolean isUpdateTimeOverDay(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final long currentTime = System.currentTimeMillis();
        final long lastTime = prefs.getLong(LAST_UPDATE_TIME, 0);
        return (currentTime - lastTime) > DateUtils.DAY_IN_MILLIS;
    }
}
