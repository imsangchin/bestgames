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
  // private final static String
  // BOOT_SHUTDOWN_ALARM_ACTION="com.miui.powercenter.BOOT_SHUTDOWN_ALARM";
  public final static String ACTION_SHUTDOWN_ORNOT = "com.miui.powercenter.SHUTDOWN_ORNOT";
  public final static String ACTION_SET_SHUTDOWN_ALARM = "com.miui.powercenter.SET_SHUTDOWN_ALARM";
  public final static String ACTION_RESET_SHUTDOWNTIME =
      "com.miui.powercenter.provider.RESET_SHUTDOWNTIME";
  private final static long ONE_DAY = 24 * 60 * 60 * 1000L;
  private DataManager mDataManager;
  private long savedAlarmTime;

  private ITelephony phone;
  Calendar calendar = Calendar.getInstance();
  AlarmManager am;

  public ShutdownAlarmIntentService() {
    super("ShutdownAlarmIntentService1");
  }

  protected void onHandleIntent(Intent intent) {

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

    if (intent.getAction().equals(ACTION_SET_SHUTDOWN_ALARM)) {// 点确定后设置关机闹钟
      setShutdownAlarm();
    } else if (intent.getAction().equals(ACTION_RESET_SHUTDOWNTIME)) {
      // TODO is need?
      if (checkAlarm()) {
        setShutdownAlarm();
      }
    } else if (intent.getAction().equals(ACTION_SHUTDOWN_ORNOT)) {
      savedAlarmTime = mDataManager.getLong(SHUTDOWN_MILLISECOND_KEY, -1);
      if (Math.abs(savedAlarmTime - System.currentTimeMillis()) < 60000) {
        // 由于闹钟时间小于系统时间时都会触发，所以手动调整系统时间会导致闹钟被触发，所以限制闹钟触发时间
        Log.d("LDEBUG", "start shutdown");
        boolean isValid = isAlarmValid(calendar);
        boolean phoneState = checkPhoneInUse();
        if (!phoneState && isValid) {
          Log.d("LDEBUG", "phone isnot in use");
          shutdownPhone();
        }
      }
    }
  }

  private void shutdownPhone() {
    Intent shutdownIntent = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
    shutdownIntent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
    shutdownIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(shutdownIntent);
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


  private void setShutdownAlarm() {
    if (shutdownAlarmEnable) {
      am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
      Intent intent = new Intent(ACTION_SHUTDOWN_ORNOT);
      PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent,
          PendingIntent.FLAG_CANCEL_CURRENT);
      calendar.setTimeInMillis(System.currentTimeMillis());
      calendar.set(Calendar.HOUR_OF_DAY, (shutdownTime / 60));
      calendar.set(Calendar.MINUTE, (shutdownTime % 60));
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);
      mDataManager.putLong(SHUTDOWN_MILLISECOND_KEY, calendar.getTimeInMillis()); // 保存仅一次时的关机闹钟时间，防止用户在关机时间到之前自己关机
      am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), ONE_DAY, pendingIntent);
    } else {
      cancelAlarm();
    }
  }

  private void cancelAlarm() {

    AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(
        Context.ALARM_SERVICE);
    Intent intent = new Intent(ACTION_SHUTDOWN_ORNOT);
    PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT);
    am.cancel(pendingIntent);

    // disable the alarm.
    mDataManager.putBoolean(POWER_ONTIME_SHUTDOWN + 2, false);
  }

  /* 关机后重启发现关机闹钟时间小于系统时间，说明已经关过机就不用再关机了 */
  private boolean checkAlarm() {
    savedAlarmTime = mDataManager.getLong(SHUTDOWN_MILLISECOND_KEY, -1);
    if (savedAlarmTime < System.currentTimeMillis()) {
      mDataManager.putBoolean(PowerShutdownOnTime.POWER_ONTIME_SHUTDOWN + 2, false);// 已经关过机，则置关机按钮为false
      shutdownAlarmEnable = false; // 取消闹钟
      return false;
    }
    return true;
  }

  private boolean isAlarmValid(Calendar calendar) {
    switch (shutdownRepeatType) {
      case Alarm.DaysOfWeek.NO_DAY:
        cancelAlarm();
        return true;
      case Alarm.DaysOfWeek.EVERY_DAY:
        return true;
      case Alarm.DaysOfWeek.MONDAY_TO_FRIDAY:
        return !HolidayHelper.isWeekEnd(calendar);
      case Alarm.DaysOfWeek.LEGAL_WORK_DAY:
        return !HolidayHelper.isHoliday(this, calendar);
      default:
        Alarm.DaysOfWeek ad = new Alarm.DaysOfWeek(shutdownRepeatType);
        return ad.isAlarmDay(calendar);
    }
  }
}
