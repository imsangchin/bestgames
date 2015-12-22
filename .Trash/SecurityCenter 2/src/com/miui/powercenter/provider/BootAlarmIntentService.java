
package com.miui.powercenter.provider;

import android.app.AlarmManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.util.Log;

import com.miui.powercenter.Alarm;
import com.miui.powercenter.HolidayHelper;

import java.util.Calendar;

import miui.security.SecurityManager;

public class BootAlarmIntentService extends IntentService {

    private final static String ACTION_SET_BOOTTIME = "com.miui.powercenter.SET_BOOTTIME";
    private final static String ACTION_RESET_BOOTTIME = "com.miui.powercenter.provider.RESET_BOOTTIME";
    private DataManager mDataManager;
    private int time1;
    private boolean bootcheck;
    private int repeat1;
    private long boottime;
    public static final int NO_DAY = 0x00;
    public static final int EVERY_DAY = 0x7f;
    public static final int MONDAY_TO_FRIDAY = 0x1f;
    public static final int LEGAL_WORK_DAY = 0x80;
    private final static String POWER_ONTIME_SHUTDOWN = "power_ontime_shutdown";
    Calendar calendar = Calendar.getInstance();
    SecurityManager sm;
    Alarm.DaysOfWeek ad;
    BootShutdownSetTime  bootshutdown;


    public BootAlarmIntentService() {
        super("BootAlarmIntentService");
        // TODO Auto-generated constructor stub
    }

    protected void onHandleIntent(Intent intent) {
        // TODO Auto-generated method stub
        if (intent.getAction().equals(ACTION_SET_BOOTTIME)) {
            setBootAlarm();
        }
        if (intent.getAction().equals(ACTION_RESET_BOOTTIME)) {
            setNextBootAlarm();
        }
    }

    public void onCreate() {
        super.onCreate();
        mDataManager = DataManager.getInstance(this);
        time1 = mDataManager.getInt(POWER_ONTIME_SHUTDOWN + 3, DataManager.BOOT_TIME_DEFAULT);
        repeat1 = mDataManager.getInt(POWER_ONTIME_SHUTDOWN + 5, DataManager.BOOT_REPEAT_DEFAULT);
        bootcheck = mDataManager.getBoolean(POWER_ONTIME_SHUTDOWN + 1, false);
    }

    private void setCalendar() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, (time1 / 60));
        calendar.set(Calendar.MINUTE, (time1 % 60));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        boottime = calendar.getTimeInMillis() / DateUtils.SECOND_IN_MILLIS;
        sm = (SecurityManager) this.getSystemService(Context.SECURITY_SERVICE);
        sm.setWakeUpTime("BootAlarmIntentService", boottime);
    }
    private void bootAlarmSet(String key){
        switch (repeat1) {
            case NO_DAY:
                if(key=="First") 
                    bootNoDay();
                if(key=="Second") 
                    nextBootNoDay();
                break;
            case EVERY_DAY:
                bootEveryDay();
                break;
            case MONDAY_TO_FRIDAY:
                bootModayToFriday();
                break;
            case LEGAL_WORK_DAY:
                bootLegalWorkDay();
                break;
            default:
                bootSelfModeDay();
                break;
        }
        
        
    }
    public void setNextBootAlarm() {// 开机后或设置系统日期后重新设置闹钟
        bootshutdown= new BootShutdownSetTime(repeat1);
        if (bootcheck == true) {
            setCalendar();
            bootAlarmSet("Second");
        }
        if (bootcheck == false) {
            sm.setWakeUpTime("BootAlarmIntentService", 0);
        }
    }

    public void setBootAlarm() {
        bootshutdown= new BootShutdownSetTime(repeat1);
        if (bootcheck == true) {
            setCalendar();
            bootAlarmSet("First");
        }
        if (bootcheck == false ) {
            sm.setWakeUpTime("BootAlarmIntentService", 0);
        }
    }

    public void nextBootNoDay() {
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            mDataManager.putBoolean(POWER_ONTIME_SHUTDOWN + 1, false);// 已经开过机，则置开机按钮为false
            bootcheck = false;
        }
    }

    public void bootNoDay() {
        Log.d("BOOTDBUG","calendar before="+calendar.getTimeInMillis());
        if(bootshutdown.noDay(calendar)){
            Log.d("BOOTDBUG","calendar after="+calendar.getTimeInMillis());
            boottime = calendar.getTimeInMillis() / DateUtils.SECOND_IN_MILLIS;
            sm.setWakeUpTime("BootAlarmIntentService", boottime);
        }
    }

    public void bootEveryDay() {
        bootNoDay();
    }

    public void bootModayToFriday() {
        if(bootshutdown.modayToFriday(calendar)){
            boottime = calendar.getTimeInMillis() / DateUtils.SECOND_IN_MILLIS;
            sm.setWakeUpTime("BootAlarmIntentService", boottime);
        }
    }

    public void bootLegalWorkDay() {
        Log.d("BOOTDBUG","calendar before="+calendar.getTimeInMillis());
        if(bootshutdown.legalWorkDay(this, calendar)){
            Log.d("BOOTDBUG","calendar after="+calendar.getTimeInMillis());
            boottime = calendar.getTimeInMillis() / DateUtils.SECOND_IN_MILLIS;
            Log.d("BOOTDBUG", "boottime=" + boottime);
            sm.setWakeUpTime("BootAlarmIntentService", boottime);
        }
    }

    public void bootSelfModeDay() {
        Log.d("BOOTDBUG","calendar before="+calendar.getTimeInMillis());
        if(bootshutdown.selfModeDay(calendar)){
            Log.d("BOOTDBUG","calendar after="+calendar.getTimeInMillis());
            boottime = calendar.getTimeInMillis() / DateUtils.SECOND_IN_MILLIS;
            sm.setWakeUpTime("BootAlarmIntentService", boottime);
        }
    }
}
