package com.miui.powercenter.provider;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.util.Log;

import com.miui.powercenter.Alarm;
import com.miui.powercenter.HolidayHelper;

import java.util.Calendar;

public class BootShutdownSetTime {
    private int mDays;
    private static int mNearestDays[] = new int[7];
    private int today;
    private int holidaycount;
    public static final int NO_DAY = 0x00;
    public static final int EVERY_DAY = 0x7f;
    public static final int MONDAY_TO_FRIDAY = 0x1f;
    public static final int LEGAL_WORK_DAY = 0x80;
    private DataManager mDataManager;
    private final static String POWER_ONTIME_SHUTDOWN = "power_ontime_shutdown";
    BootShutdownSetTime(int repeat){
        mDays=repeat;
    }
    public boolean noDay(Calendar calendar) {
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()){
            calendar.add(Calendar.DAY_OF_WEEK, 1);
            Log.d("BOOTDBUG", "add 1");
            return true;
        }
        return false;
    }

    public void everyDay(Calendar calendar) {
        noDay(calendar);
    }

    public boolean modayToFriday(Calendar calendar) {
        Log.d("BOOTDBUG", "workday");
        if (calendar.getTimeInMillis() < System.currentTimeMillis() || HolidayHelper.isWeekEnd(calendar)) { // 如果设置的闹钟小于系统时间或周末设置关机且一直没关机，则从下一个工作日开始关机
            if (HolidayHelper.isWeekEnd(calendar))
                calendar.set(Calendar.DAY_OF_WEEK, 2);
            else
                calendar.add(Calendar.DAY_OF_WEEK, 1);
            return true;
        }
        return false;
    }

    public boolean legalWorkDay(Context context,Calendar calendar) {
        Log.d("LDEDBUG", "Legalday");
        int dayCount = 0;
        if (calendar.getTimeInMillis() < System.currentTimeMillis() || HolidayHelper.isHoliday(context, calendar)) {// 如果设置的时间小于系统时间，则从下一个法定工作日开始开机
            for (; dayCount < 10; dayCount++) {
                calendar.add(Calendar.DAY_OF_WEEK, 1);
                if (!HolidayHelper.isHoliday(context, calendar)) {
                    break;
                }
            }
            return true;
        }
        return false;
    }

    public boolean selfModeDay(Calendar calendar) {
        Log.d("BOOTDBUG", "SelfMode");
        Alarm.DaysOfWeek ad = new Alarm.DaysOfWeek(mDays);
        initArray();
        ad.daysOfNearestAlarm(mNearestDays);
        for (int i = 0; i < 7; i++)
            Log.d("BOOTDBUG", "mNearestDays=" + mNearestDays[i]);
        Calendar c = Calendar.getInstance();
        if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
            today = 6;// 星期天单独处理
        else
            today = c.get(Calendar.DAY_OF_WEEK) - 2;
        holidaycount = mNearestDays[today];
            if (calendar.getTimeInMillis() < System.currentTimeMillis() || !ad.isAlarmDay(calendar)) {
                calendar.add(Calendar.DAY_OF_WEEK, holidaycount);
                return true;
            }
            return false;
    }

    public void initArray() {
        for (int i = 0; i < 7; i++)
            mNearestDays[i] = 1;
    }

}
