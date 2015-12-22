
package com.miui.powercenter.provider;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.telephony.ITelephony;
import com.miui.powercenter.Alarm;
import com.miui.powercenter.HolidayHelper;
import com.miui.powercenter.PowerShutdownOnTime;

import java.util.Calendar;

public class ShutdownAlarmIntentService extends IntentService {
    private static final String TAG = "ShutdownOnService";
    private final static String SHUTDOWN_MILLISECOND_KEY = "shutdown_millisecond_key";
    private final static String POWER_ONTIME_SHUTDOWN = PowerShutdownOnTime.POWER_ONTIME_SHUTDOWN;
    private int shutdownTime;
    private int shutdownRepeatType;
    private boolean shutdownAlarmEnable;
    public final static String ACTION_SHUTDOWN_ORNOT = "com.miui.powercenter.SHUTDOWN_ORNOT";
    public final static String ACTION_SET_SHUTDOWN_ALARM = "com.miui.powercenter.SET_SHUTDOWN_ALARM";
    public final static String ACTION_SHUTDOWN_NOTIFICATION = "com.miui.powercenter.SHUTDOWN_NOTIFICATION";
    private final static String ACTION_CANCEL_SHUTDOWN="com.miui.powercenter.provider.CANCEL_SHUTDOWN";
    public final static String ACTION_RESET_SHUTDOWNTIME =
            "com.miui.powercenter.provider.RESET_SHUTDOWNTIME";
    private final static int MINUTE = 60 * 1000;
    private DataManager mDataManager;
    private long savedAlarmTime;

    private ITelephony phone;
    Calendar calendar = Calendar.getInstance();
    AlarmManager am;

    public ShutdownAlarmIntentService() {
        super("ShutdownAlarmIntentService");
    }

    protected void onHandleIntent(Intent intent) {
        ShutdownOnTimeState shutdownOnTimeState = ShutdownOnTimeState.getInstance(this);
        // maybe some error condition start the service, just ignore.
        if (intent == null || TextUtils.isEmpty(intent.getAction())) {
            return;
        }

        mDataManager = DataManager.getInstance(this);
        shutdownTime =
                mDataManager.getInt(POWER_ONTIME_SHUTDOWN + 4, DataManager.SHUTDOWN_TIME_DEFAULT);
        shutdownRepeatType = mDataManager.getInt(POWER_ONTIME_SHUTDOWN + 6,
                DataManager.SHUTDOWN_REPEAT_DEFAULT);
        shutdownAlarmEnable = mDataManager.getBoolean(POWER_ONTIME_SHUTDOWN + 2, false);
//        if(intent.getAction().equals(ACTION_CANCEL_SHUTDOWN)){
//            shutdownOnTimeState.sendCancelShutdownMessage();
//        }
        if (intent.getAction().equals(ACTION_SET_SHUTDOWN_ALARM)) {// 点确定后设置关机闹钟
            setShutdownAlarm();
        } else if (intent.getAction().equals(ACTION_RESET_SHUTDOWNTIME)) {
            // TODO is need?
            if (checkAlarm()) {
                setShutdownAlarm();
            }
        } else if (intent.getAction().equals(ACTION_SHUTDOWN_ORNOT)) {
            Log.d("LDEBUG", "start shutdown");
            savedAlarmTime = mDataManager.getLong(SHUTDOWN_MILLISECOND_KEY, -1);
            savedAlarmTime = savedAlarmTime / MINUTE;
            Log.d("LDEBUG", "savedAlarmTime=" + savedAlarmTime);
            Log.d("LDEBUG", "System.currentTimeMillis()=" + System.currentTimeMillis() / MINUTE);
            Log.d("LDEBUG", "result="
                    + (System.currentTimeMillis() / MINUTE - savedAlarmTime));
            if ((System.currentTimeMillis() / MINUTE - savedAlarmTime) < 1) {// 这里限制在1分钟内
                // 由于闹钟时间小于系统时间时都会触发，所以手动调整系统时间会导致闹钟被触发，所以限制闹钟触发时间
                boolean isValid = isAlarmValid(calendar);
                boolean phoneState = checkPhoneInUse();
                if (!phoneState && isValid && shutdownAlarmEnable) {
                    Log.d("LDEBUG", "phone isnot in use");
                    // shutdownPhone();
                    
                    Intent notifyIntent = new Intent(ACTION_SHUTDOWN_NOTIFICATION);
                    sendBroadcast(notifyIntent);
                }
            }
        }
    }

    // private void shutdownPhone() {
    // Intent shutdownIntent = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
    // shutdownIntent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
    // shutdownIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    // startActivity(shutdownIntent);
    // }

    public void onDestroy() {
        super.onDestroy();
    }

    public boolean checkPhoneInUse() {
        boolean phoneInUse = false;
        try {
            phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            if (phone != null)
                phoneInUse = !phone.isIdle();
        } catch (RemoteException e) {
            Log.e(TAG, "phone.isIdle() failed", e);
        }
        return phoneInUse;
    }

    private void setShutdownAlarm() {
        if (shutdownAlarmEnable) {
            am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(ACTION_SHUTDOWN_ORNOT);
            PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0,
                    intent, PendingIntent.FLAG_CANCEL_CURRENT);
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, (shutdownTime / 60));
            calendar.set(Calendar.MINUTE, (shutdownTime % 60));
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Log.d("LDEBUG", "set calendar.getTimeInMillis()=" + calendar.getTimeInMillis());
            mDataManager.putLong(SHUTDOWN_MILLISECOND_KEY, calendar.getTimeInMillis()); // 保存仅一次时的关机闹钟时间，防止用户在关机时间到之前自己关机
            // am.setRepeating(AlarmManager.RTC_WAKEUP,
            // calendar.getTimeInMillis(), ONE_DAY,
            // pendingIntent);
            am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            // setNotificationAlarm(calendar);
        }
    }

    // private void setNotificationAlarm(Calendar calendar){
    // Calendar notificationCalendar = calendar;
    // notificationCalendar.set(Calendar.SECOND,1);
    // Intent intent = new Intent(ACTION_SHUTDOWN_NOTIFICATION);
    // PendingIntent pendingIntent =
    // PendingIntent.getBroadcast(getApplicationContext(), 0, intent,
    // PendingIntent.FLAG_CANCEL_CURRENT);
    // am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
    // pendingIntent);
    // }

    /* 关机后重启发现关机闹钟时间小于系统时间，说明已经关过机就不用再关机了 */
    private boolean checkAlarm() {
        if (shutdownRepeatType == Alarm.DaysOfWeek.NO_DAY) {
            savedAlarmTime = mDataManager.getLong(SHUTDOWN_MILLISECOND_KEY, -1);
            if (savedAlarmTime < System.currentTimeMillis()) {
                mDataManager.putBoolean(PowerShutdownOnTime.POWER_ONTIME_SHUTDOWN + 2, false);// 已经关过机，则置关机按钮为false
                shutdownAlarmEnable = false; // 取消闹钟
                return false;
            }
        }
        return true;
    }

    private boolean isAlarmValid(Calendar calendar) {
        switch (shutdownRepeatType) {
            case Alarm.DaysOfWeek.NO_DAY:
                mDataManager.putBoolean(PowerShutdownOnTime.POWER_ONTIME_SHUTDOWN + 2, false);// 已经关过机，则置关机按钮为false
                return true;
            case Alarm.DaysOfWeek.EVERY_DAY:
                return true;
            case Alarm.DaysOfWeek.MONDAY_TO_FRIDAY:
                if (HolidayHelper.isWeekEnd(calendar)) {
                    resetAlarm(calendar);
                    return false;
                }
                return true;
            case Alarm.DaysOfWeek.LEGAL_WORK_DAY:
                if (HolidayHelper.isHoliday(this, calendar)) {
                    resetAlarm(calendar);
                    return false;
                }
                return true;
            default:
                Alarm.DaysOfWeek ad = new Alarm.DaysOfWeek(shutdownRepeatType);
                if (!ad.isAlarmDay(calendar)) {
                    resetAlarm(calendar);
                    return false;
                }
                return true;
        }
    }

    private void resetAlarm(Calendar calendar) {
        if (am == null) {
            am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        }
        Intent intent = new Intent(ACTION_SHUTDOWN_ORNOT);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        calendar.add(Calendar.DAY_OF_WEEK, 1);
        mDataManager.putLong(SHUTDOWN_MILLISECOND_KEY, calendar.getTimeInMillis());
        Log.d("LDEBUG", "reset calendar=" + calendar.getTimeInMillis());
        am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }
}
