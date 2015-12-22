package cn.shidian;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyReceiver extends BroadcastReceiver {
    public static final int AHEAD_TIME_5MIN = 0;
    public static final int AHEAD_TIME_15MIN = 1;
    public static final int AHEAD_TIME_30MIN = 2;

    private NotificationManager notificationManager;
    private Notification notification;
    Intent intent;
    PendingIntent pendingIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Calendar calendar = Calendar.getInstance();
        String current_time = Utils.GetStringFromCalendarWithoutSecond(calendar);
        Calendar current_calendar = Utils.GetCalendarFromString(current_time + ":00");
        ArrayList<Map<String, Object>> reminderList = Utils.GetReminderListFromStored(context);
        for (int i = 0; i < reminderList.size(); i++) {
            String remind_time = reminderList.get(i).get("remind_time").toString();
            Calendar remind_calendar = Utils.GetCalendarFromString(remind_time + ":00");
            if (current_time.equals(remind_time)) {
                String ahead_time = "";
                int ahead_type = (Integer)reminderList.get(i).get("ahead_type");
                switch (ahead_type) {
                case AHEAD_TIME_5MIN:
                    ahead_time = "5分钟";
                    break;
                case AHEAD_TIME_15MIN:
                    ahead_time = "15分钟";
                    break;
                case AHEAD_TIME_30MIN:
                    ahead_time = "30分钟";
                    break;
                }
                
                String description = "来自视点的提醒：您对 " + reminderList.get(i).get("subStation_name")
                    + " " + reminderList.get(i).get("remind_time") + " 播出的 "
                    + reminderList.get(i).get("prog_name") + " 设置了 " + ahead_time + " 提醒";

                notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                intent = new Intent(context, Main_TabHost.class);
                pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                notification = new Notification();
                notification.flags = Notification.FLAG_AUTO_CANCEL;
                notification.icon = R.drawable.icon;
                notification.tickerText = description;
                notification.defaults = Notification.DEFAULT_SOUND;
                notification.setLatestEventInfo(context, "来自视点的提醒", description, pendingIntent);
                notificationManager.notify(0, notification);
                
                reminderList.remove(i);
                Utils.SaveReminderListToStored(context, reminderList);
                i--;
            } else if (current_calendar.after(remind_calendar)) {
                reminderList.remove(i);
                Utils.SaveReminderListToStored(context, reminderList);
                i--;
            }
        }
    }
}