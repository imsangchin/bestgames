package com.miui.powercenter.provider;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.miui.powercenter.Alarm;
import com.miui.powercenter.HolidayHelper;
import com.miui.powercenter.PowerShutdownOnTime;

import java.util.Calendar;

import miui.security.SecurityManager;

public class BootAlarmIntentService extends IntentService {

  public final static String ACTION_SET_BOOTTIME = "com.miui.powercenter.SET_BOOTTIME";
  public final static String ACTION_PHONE_START = "com.miui.powercenter.provider.PHONE_START";

  private static final String BOOT_ALARM_INTENT_SERVICE = "BootAlarmIntentService";

  private DataManager mDataManager;
  private int formatBootTime;
  private boolean bootEnabled;
  private int bootRepeatType;

  private final static String POWER_ONTIME_SHUTDOWN = PowerShutdownOnTime.POWER_ONTIME_SHUTDOWN;
  Calendar calendar = Calendar.getInstance();
  SecurityManager sm;

  public BootAlarmIntentService() {
    super("BootAlarmIntentService");
  }

  protected void onHandleIntent(Intent intent) {

    // maybe some error condition start the service, just ignore.
    if (intent == null || TextUtils.isEmpty(intent.getAction())) {
      return;
    }

    if (intent.getAction().equals(ACTION_SET_BOOTTIME)) {
      setBootAlarm();
    } else if (intent.getAction().equals(ACTION_PHONE_START)) {
      resetBootAlarmAfterPhoneStart();
    }
  }

  public void onCreate() {
    super.onCreate();
    mDataManager = DataManager.getInstance(this);
    formatBootTime = mDataManager.getInt(POWER_ONTIME_SHUTDOWN + 3, DataManager.BOOT_TIME_DEFAULT);
    bootRepeatType =
        mDataManager.getInt(POWER_ONTIME_SHUTDOWN + 5, DataManager.BOOT_REPEAT_DEFAULT);
    bootEnabled = mDataManager.getBoolean(POWER_ONTIME_SHUTDOWN + 1, false);
    sm = (SecurityManager) this.getSystemService(Context.SECURITY_SERVICE);
    initCalendar();
  }

  private void initCalendar() {
    calendar.setTimeInMillis(System.currentTimeMillis());
    calendar.set(Calendar.HOUR_OF_DAY, (formatBootTime / 60));
    calendar.set(Calendar.MINUTE, (formatBootTime % 60));
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
  }

  private void bootAlarm() {
    switch (bootRepeatType) {
      case Alarm.DaysOfWeek.NO_DAY:
        bootNoDay();
        break;
      case Alarm.DaysOfWeek.EVERY_DAY:
        bootEveryDay();
        break;
      case Alarm.DaysOfWeek.MONDAY_TO_FRIDAY:
        bootMondayToFriday();
        break;
      case Alarm.DaysOfWeek.LEGAL_WORK_DAY:
        bootLegalWorkDay();
        break;
      default:
        bootSelfModeDay();
        break;
    }
  }

  private void resetBootAlarmAfterPhoneStart() {// 开机后或设置系统日期后重新设置闹钟
    if (bootRepeatType == Alarm.DaysOfWeek.NO_DAY
        && calendar.getTimeInMillis() <= System.currentTimeMillis()) {
      mDataManager.putBoolean(POWER_ONTIME_SHUTDOWN + 1, false);// 已经开过机，则置开机按钮为false
      bootEnabled = false;
      setWakeUpTime(0);
    } else {
      bootAlarm();
    }
  }

  private void setBootAlarm() {
    if (bootEnabled) {
      bootAlarm();
    } else {
      setWakeUpTime(0);
    }
  }

  private void bootNoDay() {
    Log.d("BOOTDBUG", "calendar before=" + calendar.getTimeInMillis());
    if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
      calendar.add(Calendar.DAY_OF_WEEK, 1);
    }
    Log.d("BOOTDBUG", "calendar after=" + calendar.getTimeInMillis());
    long bootTimeMinute = calendar.getTimeInMillis() / DateUtils.SECOND_IN_MILLIS;
    setWakeUpTime(bootTimeMinute);
  }

  private void setWakeUpTime(long time) {
    if (sm == null) {
      sm = (SecurityManager) this.getSystemService(Context.SECURITY_SERVICE);
    }
    sm.setWakeUpTime(BOOT_ALARM_INTENT_SERVICE, time);
  }

  private void bootEveryDay() {
    bootNoDay();
  }

  private void bootMondayToFriday() {
    if (calendar.getTimeInMillis() < System.currentTimeMillis()
        || HolidayHelper.isWeekEnd(calendar)) { // 如果设置的闹钟小于系统时间或周末设置关机且一直没关机，则从下一个工作日开始关机
      if (HolidayHelper.isWeekEnd(calendar))
        calendar.set(Calendar.DAY_OF_WEEK, 2);
      else
        calendar.add(Calendar.DAY_OF_WEEK, 1);
    }
    long bootTimeMinute = calendar.getTimeInMillis() / DateUtils.SECOND_IN_MILLIS;
    setWakeUpTime(bootTimeMinute);
  }

  private void bootLegalWorkDay() {
    Log.d("BOOTDBUG", "calendar before=" + calendar.getTimeInMillis());
    int dayCount = 0;
    if (calendar.getTimeInMillis() < System.currentTimeMillis()
        || HolidayHelper.isHoliday(this, calendar)) {// 如果设置的时间小于系统时间，则从下一个法定工作日开始开机
      for (; dayCount < 10; dayCount++) {
        calendar.add(Calendar.DAY_OF_WEEK, 1);
        if (!HolidayHelper.isHoliday(this, calendar)) {
          break;
        }
      }
    }
    Log.d("BOOTDBUG", "calendar after=" + calendar.getTimeInMillis());
    long bootTimeMinute = calendar.getTimeInMillis() / DateUtils.SECOND_IN_MILLIS;
    Log.d("BOOTDBUG", "bootTimeMinute=" + bootTimeMinute);
    setWakeUpTime(bootTimeMinute);
  }

  private void bootSelfModeDay() {
    Log.d("BOOTDBUG", "calendar before=" + calendar.getTimeInMillis());
    if (BootTimeHelper.selfModeDay(calendar, bootRepeatType)) {
      Log.d("BOOTDBUG", "calendar after=" + calendar.getTimeInMillis());
      long bootTimeMinute = calendar.getTimeInMillis() / DateUtils.SECOND_IN_MILLIS;
      setWakeUpTime(bootTimeMinute);
    }
  }
}
