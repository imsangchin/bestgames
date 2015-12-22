
package com.miui.powercenter.provider;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;
import com.miui.powercenter.Alarm;
import com.miui.powercenter.HolidayHelper;

import java.util.Calendar;

public class ShutdownAlarmIntentService extends IntentService {
    private static final String TAG = "ShutdownOnService";
    private final static String POWER_ONTIME_SHUTDOWN = "power_ontime_shutdown";
    public static final int NO_DAY = 0x00;
    public static final int EVERY_DAY = 0x7f;
    public static final int MONDAY_TO_FRIDAY = 0x1f;
    public static final int LEGAL_WORK_DAY = 0x80;
    private int time2;
    private int repeat2;
    private boolean shutdowncheck;
    private boolean phonestate;
    // private final static String
    // BOOT_SHUTDOWN_ALARM_ACTION="com.miui.powercenter.BOOT_SHUTDOWN_ALARM";
    private final static String ACTION_SHUTDOWN_ORNOT = "com.miui.powercenter.SHUTDOWN_ORNOT";
    private final static String ACTION_SET_SHUTDOWN_ALARM = "com.miui.powercenter.SET_SHUTDOWN_ALARM";
    private final static String ACTION_RESET_SHUTDOWNTIME = "com.miui.powercenter.provider.RESET_SHUTDOWNTIME";
    private DataManager mDataManager;
    private long savealarmtime;


    private ITelephony phone;
    Calendar calendar = Calendar.getInstance();
    AlarmManager am;
    PendingIntent pendingIntent;
    Alarm.DaysOfWeek ad;
    BootShutdownSetTime  bootshutdown;

    public ShutdownAlarmIntentService() {
        super("ShutdownAlarmIntentService");
        // TODO Auto-generated constructor stub
    }

    protected void onHandleIntent(Intent intent) {
        // TODO Auto-generated method stub
        if (intent.getAction().equals(ACTION_SET_SHUTDOWN_ALARM)) {// 点确定后设置关机闹钟
            setShutdownAlarm();
        }
        if (intent.getAction().equals(ACTION_RESET_SHUTDOWNTIME)) {// 如果修改系统时间，则重新设置关机闹钟;关机后闹钟不保存，开机后重新设置
            setNextShutdownAlarm();
        }
        if (intent.getAction().equals(ACTION_SHUTDOWN_ORNOT)) {
            Calendar c = Calendar.getInstance();
            savealarmtime = mDataManager.getLong(POWER_ONTIME_SHUTDOWN + 8, -1);
            Log.d("LDEBUG", "ALARM start");
            if (Math.abs(savealarmtime - System.currentTimeMillis()) < 60000) {
                // 由于闹钟时间小于系统时间时都会触发，所以手动调整系统时间会导致闹钟被触发，所以限制闹钟触发时间
                Log.d("LDEBUG", "start shutdown");
                phonestate = checkPhoneInUse();
                if (!phonestate) {
                    Log.d("LDEBUG", "phone isnot in use");
                    Intent shutdownintent = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
                    shutdownintent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
                    shutdownintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(shutdownintent);
                }
            }
        }
    }

    public void onCreate() {
        super.onCreate();
        Log.d("LDEBUG", "onCreate");
        mDataManager = DataManager.getInstance(this);
        time2 = mDataManager.getInt(POWER_ONTIME_SHUTDOWN + 4, DataManager.SHUTDOWN_TIME_DEFAULT);
        repeat2 = mDataManager.getInt(POWER_ONTIME_SHUTDOWN + 6, DataManager.SHUTDOWN_REPEAT_DEFAULT);
        shutdowncheck = mDataManager.getBoolean(POWER_ONTIME_SHUTDOWN + 2, false);
    }

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

    public void setCalendar() {
        am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ACTION_SHUTDOWN_ORNOT);
        pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, (time2 / 60));
        calendar.set(Calendar.MINUTE, (time2 % 60));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        mDataManager.putLong(POWER_ONTIME_SHUTDOWN + 8, calendar.getTimeInMillis()); // 保存仅一次时的关机闹钟时间，防止用户在关机时间到之前自己关机
        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }
public void alarmReset(String key,int mDays){
    switch (repeat2) {
        case NO_DAY:
            if(key=="First")
                noDaySetAlarm();
            if(key=="Second")
                noDayCheckAlarm();
            break;
        case EVERY_DAY:
            everyDayAlarm();
            break;
        case MONDAY_TO_FRIDAY:
            monday_to_friday_alarm();
            break;
        case LEGAL_WORK_DAY:
            legalDayAlarm();
            break;
        default:
            selfModeAlarm();
            break;
    }
    
}
public void setShutdownAlarm() {
    Log.d("LDEBUG", "bindService");
    bootshutdown= new BootShutdownSetTime(repeat2);
    if (shutdowncheck == true) {
        setCalendar();
        alarmReset("First",repeat2);
    }
    if (shutdowncheck == false) {
        cancleAlarm();
    }
}
    public void setNextShutdownAlarm() {
        Log.d("LDEBUG", "boot startservice");
        bootshutdown= new BootShutdownSetTime(repeat2);
        if (shutdowncheck == true) {
            setCalendar();
            alarmReset("Second",repeat2);
        }
        if (shutdowncheck == false) {
            cancleAlarm();
        }
    }

    public void cancleAlarm() {
        AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(
                Context.ALARM_SERVICE);
        Intent intent = new Intent(ACTION_SHUTDOWN_ORNOT);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        am.cancel(pendingIntent);
    }

    /*关机后重启发现关机闹钟时间小于系统时间，说明已经关过机就不用再关机了*/
    private void noDayCheckAlarm() {
        savealarmtime = mDataManager.getLong(POWER_ONTIME_SHUTDOWN + 8, -1);
        if (savealarmtime <  System.currentTimeMillis()){
            mDataManager.putBoolean(POWER_ONTIME_SHUTDOWN + 2, false);// 已经关过机，则置关机按钮为false
            shutdowncheck = false; //取消闹钟
        }
    }

    private void noDaySetAlarm() {
//        Log.d("LDEBUG", "once");
        if(bootshutdown.noDay(calendar)){
          mDataManager.putLong(POWER_ONTIME_SHUTDOWN + 8, calendar.getTimeInMillis());
          am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    private void everyDayAlarm() {
        noDaySetAlarm();
    }

    private void monday_to_friday_alarm() {
        if(bootshutdown.modayToFriday(calendar)){
          mDataManager.putLong(POWER_ONTIME_SHUTDOWN + 8, calendar.getTimeInMillis());
          am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    private void legalDayAlarm() {
            Log.d("LDEBUG","calendar before="+calendar.getTimeInMillis());
            Log.d("LDEBUG","systemtime="+System.currentTimeMillis());
        if(bootshutdown.legalWorkDay(this, calendar)){
            Log.d("LDEDBUG","calendar after="+calendar.getTimeInMillis());
            mDataManager.putLong(POWER_ONTIME_SHUTDOWN + 8, calendar.getTimeInMillis());
            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    private void selfModeAlarm() {
        if(bootshutdown.selfModeDay(calendar)){
            mDataManager.putLong(POWER_ONTIME_SHUTDOWN + 8, calendar.getTimeInMillis());
            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }
}
