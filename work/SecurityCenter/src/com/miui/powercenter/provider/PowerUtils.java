
package com.miui.powercenter.provider;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;

import com.miui.securitycenter.R;
import com.miui.powercenter.PowerCenter;
import com.miui.powercenter.provider.PowerData.PowerMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class PowerUtils {
    public static final String TAG = "PowerUtils";
    public static final boolean DEBUG = false;

    public static final int AUTO_CLEAN_MEMORY_SERVICE_RUNNING = 6061427;
    public static final int POWER_SAVE_SERVICE_RUNNING = 6061428;

    private static final long DAY_OF_MILLISECONDS = 24 * 60 * 60 * 1000;
    private static final long HOUR_OF_MILLISECONDS = 60 * 60 * 1000;
    private static final long MINUTE_OF_MILLISECONDS = 60 * 1000;
    private static BatteryInfo sBatteryInfo;

    private static class TimeCell {
        long startTime;
        long endTime;
        boolean inOnTime;
    }

    public static String getFormatTime(int hour, int minute) {
        String sHour;
        String sMinute;
        if (hour < 10) {
            sHour = "0" + hour;
        } else {
            sHour = String.valueOf(hour);
        }
        if (minute < 10) {
            sMinute = "0" + minute;
        } else {
            sMinute = String.valueOf(minute);
        }
        String sTime = sHour + ":" + sMinute;
        return sTime;
    }

    public static PowerMode[] getAllAvailableModes(Context context) {
        ArrayList<PowerMode> availableList = new ArrayList<PowerMode>();

        // 获得默认模式，除去“添加自定义模式”
        PowerMode[] defaultArray = PowerData.getDefaultModeArray(context);
        for (int i = 0; i < defaultArray.length; ++i) {
                availableList.add(defaultArray[i]);
        }

        Log.d(TAG, "PLOW--默认的模式个数: " + availableList.size());

        // 获得数据库中的模式
        ArrayList<PowerMode> dbList = SqlUtils.getAllModes(context);
        Log.d(TAG, "PLOW--数据库的模式个数: " + dbList.size());

        availableList.addAll(dbList);
        Log.d(TAG, "PLOW--总共的模式个数: " + availableList.size());

        PowerMode[] availableArray = new PowerMode[availableList.size()];
        availableList.toArray(availableArray);

        return availableArray;
    }

    public static String[] getAllAvailableNames(PowerMode[] modeArray) {
        String[] nameArray = new String[modeArray.length];
        for (int i = 0; i < nameArray.length; ++i) {
            nameArray[i] = String.valueOf(modeArray[i].mDBValue[PowerMode.INDEX_NAME]);
        }
        return nameArray;
    }

    public static String getModeNameById(Context context, int modeId) {
        int defaultCount = PowerData.getDefaultModeCount();
        if (modeId < defaultCount) {
            PowerMode[] array = PowerData.getDefaultModeArray(context);
            return String.valueOf(array[modeId].mDBValue[PowerMode.INDEX_NAME]);
        }
        modeId -= defaultCount - 1;
        PowerMode mode = SqlUtils.getModeById(context, modeId);
        return String.valueOf(mode.mDBValue[PowerMode.INDEX_NAME]);
    }

    public static PowerMode getModeById(Context context, int modeId) {
        int defaultCount = PowerData.getDefaultModeCount();
        if (modeId < defaultCount) {
            PowerMode[] array = PowerData.getDefaultModeArray(context);
            return array[modeId];
        } else {
            modeId -= defaultCount - 1;
            return SqlUtils.getModeById(context, modeId);
        }
    }

    public static void applyModeById(Context context, int modeId) {
        PowerMode mode = getModeById(context, modeId);
        if (mode != null) {
            mode.apply(context);
        }
    }

    public static String getAvailableUserDefineName(Context context) {
        Set<String> set = getExistModeNames(context);
        String prefix = context.getString(R.string.power_customizer_user_define_name);
        String newName;
        int i = 0;
        do {
            newName = prefix + (++i);
        } while (set.contains(newName));
        return newName;
    }

    public static Set<String> getExistModeNames(Context context) {
        HashSet<String> nameSet = new HashSet<String>();

        PowerMode[] defaultArray = PowerData.getDefaultModeArray(context);
        for (PowerMode mode : defaultArray) {
            nameSet.add(String.valueOf(mode.mDBValue[PowerMode.INDEX_NAME]));
        }

        ArrayList<String> nameList = SqlUtils.getAllModeNames(context);
        if (nameList != null) {
            nameSet.addAll(nameList);
        }
        return nameSet;
    }

    public boolean isOnTimeBatteryInUse(Context context, DataManager manager) {
        boolean isOnTimeInUse = manager.getBoolean(DataManager.KEY_ON_TIME_INUSE, false);
        return isOnTimeInUse;
    }

    public static boolean isLowBatteryInUse(Context context, DataManager manager) {
        boolean isLowBatteryInUse = manager.getBoolean(DataManager.KEY_LOW_BATTERY_INUSE, false);
        return isLowBatteryInUse;
    }

    public static boolean isOnTimeInUse(Context context, DataManager manager) {
        boolean isOnTimeInUse = manager.getBoolean(DataManager.KEY_ON_TIME_INUSE, false);
        return isOnTimeInUse;
    }

    public static boolean isInLowBatteryManually(DataManager manager) {
        boolean isInLowBatteryManually = manager.getBoolean(DataManager.POWER_MODE_MANUAL_LOWBATTERY,
                false);

        return isInLowBatteryManually;
    }

    public static void setLowBatteryManually(DataManager manager, boolean manually) {
        manager.putBoolean(DataManager.POWER_MODE_MANUAL_LOWBATTERY, manually);
    }

    /**
     * 判断是否处于低电模式状态
     * @param context
     * @param manager
     * @return
     */
    public static boolean isInLowBatteryMode(Context context, DataManager manager) {

        boolean isLowBatteryOn = manager.getBoolean(DataManager.KEY_LOW_BATTERY_ENABLED,
                DataManager.LOW_BATTERY_ENABLED_DEFAULT);

        if (!isLowBatteryOn) {
            Log.d(TAG, "低电模式并没有打开: " + isLowBatteryOn);
            return false;
        }

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level / (float) scale;

        if (PowerCenter.DEBUG) {
            batteryPct = 0.3f;
        }

        float bound = PowerModeStateTransfer.BATTERY_PERCENTAGE_BOUND;

        Log.d(TAG, "PDEBUG--获得电池的上限: " + bound + " 当前的电量: " + batteryPct);

        if (batteryPct >= bound) {
            return false;
        }

        int percent = manager.getInt(DataManager.KEY_LOW_BATTERY_PERCENTAGE,
                DataManager.LOW_BATTERY_PERCENTAGE_DEFAULT);

        if (percent > 100 * batteryPct) {
            Log.d(TAG, "PDEBUG--当前的电量小于低电的门限，当前: "+ batteryPct*100 + " 门限: " + percent);
            return true;
        } else {
            //如果发现当前的电量已经大于门限了，那么我们把手工低电设置为false
            PowerUtils.setLowBatteryManually(manager, false);
        }

        return false;
    }

    public static boolean isInOnTimeMode(Context context, DataManager manager) {

        boolean isOnTimeEnable = manager.getBoolean(DataManager.KEY_ON_TIME_ENABLED,
                false);

        if (!isOnTimeEnable) return false;

        int startHour = manager.getInt(DataManager.KEY_ON_TIME_START_HOUR,
                DataManager.ON_TIME_START_HOUR_DEFAULT);
        int startMinute = manager.getInt(DataManager.KEY_ON_TIME_START_MINUTE,
                DataManager.ON_TIME_START_MINUTE_DEFAULT);
        int endHour = manager.getInt(DataManager.KEY_ON_TIME_END_HOUR,
                DataManager.ON_TIME_END_HOUR_DEFAULT);
        int endMinute = manager.getInt(DataManager.KEY_ON_TIME_END_MINUTE,
                DataManager.ON_TIME_END_MINUTE_DEFAULT);

        Calendar calStart = Calendar.getInstance();
        Calendar calEnd = Calendar.getInstance();

        long curTime = System.currentTimeMillis();
        curTime = curTime - curTime % 60000;
        calStart.setTimeInMillis(curTime);
        calStart.set(Calendar.HOUR_OF_DAY, startHour);
        calStart.set(Calendar.MINUTE, startMinute);

        calEnd.setTimeInMillis(curTime);
        calEnd.set(Calendar.HOUR_OF_DAY, endHour);
        calEnd.set(Calendar.MINUTE, endMinute);

        long startTime = calStart.getTimeInMillis();
        long endTime = calEnd.getTimeInMillis();

        TimeCell cell = getJustifyTime(startTime, endTime);
        Log.d(TAG, "电源中心——PowerUtils: cell.inOnTime" + cell.inOnTime);
        return cell.inOnTime;
    }

    /**
     * 根据我们的 startTime 和 entTime 以及当前的time 来调整开启alarm 的时间
     * x < c < y 那么开启x, 开启y             1
     * x < y < c 开启x + day, y + day        2
     * c < x < y 开启x, 开启y                 3
     * y < x < c 开启x, 开启 y + day          4
     * y < c < x 开启x, 开启 y + day          5
     * c < y < x 开启x - day, y              6
     */
    public static TimeCell getJustifyTime(long startTime, long endTime) {
        long cur = System.currentTimeMillis();

        boolean case1  = startTime < cur && cur < endTime;
        boolean case2  = startTime < endTime && endTime < cur;
        boolean case3  = cur < startTime && startTime < endTime;
        boolean case4  = endTime < startTime && startTime < cur;
        boolean case5  = endTime < cur && cur < startTime;
        boolean case6  = cur < endTime && endTime < startTime;
        boolean inOnTime = false;

        TimeCell cell = new TimeCell();
        if (case1 || case3) {
            cell.startTime = startTime;
            cell.endTime = endTime;
        } else if (case4 || case5) {
            cell.startTime = startTime;
            cell.endTime = endTime + AlarmManager.INTERVAL_DAY;
        } else if (case2) {
            Log.d(TAG, "电源中心--重新设置 start: " + startTime);
            cell.startTime = startTime + AlarmManager.INTERVAL_DAY;
            cell.endTime = endTime + AlarmManager.INTERVAL_DAY;
        } if (case6) {
            cell.startTime = startTime - AlarmManager.INTERVAL_DAY;
            cell.endTime = endTime;
        }

        if (case1 || case4 || case6) {
            inOnTime = true;
        }

        cell.inOnTime = inOnTime;
        return cell;
    }

    public static void setOnTimeMission(Context context) {

        DataManager manager = DataManager.getInstance(context);
        boolean isEnabled = manager.getBoolean(DataManager.KEY_ON_TIME_ENABLED,
                DataManager.ON_TIME_ENABLED_DEFAULT);
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);

        int startHour = manager.getInt(DataManager.KEY_ON_TIME_START_HOUR,
                DataManager.ON_TIME_START_HOUR_DEFAULT);
        int startMinute = manager.getInt(DataManager.KEY_ON_TIME_START_MINUTE,
                DataManager.ON_TIME_START_MINUTE_DEFAULT);

        long curTime = System.currentTimeMillis();
        curTime = curTime - curTime%60000;

        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(curTime);

        Log.d(TAG, "PONTIME--shour: " + startHour + " sminute: " + startMinute);
        calendar1.set(Calendar.HOUR_OF_DAY, startHour);
        calendar1.set(Calendar.MINUTE, startMinute);
        Log.d(TAG, "PONTIME--calendar millis: " + calendar1.getTimeInMillis());

        Intent intent1 = new Intent();
        intent1.setAction(AlarmReceiver.ACTION_POWER_SAVE_ON_TIME_START_MISSION);
        PendingIntent startPendingIntent = PendingIntent.getBroadcast(context,0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

        int endHour = manager.getInt(DataManager.KEY_ON_TIME_END_HOUR,
                DataManager.ON_TIME_END_HOUR_DEFAULT);
        int endMinute = manager.getInt(DataManager.KEY_ON_TIME_END_MINUTE,
                DataManager.ON_TIME_END_MINUTE_DEFAULT);

        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(curTime);
        calendar2.set(Calendar.HOUR_OF_DAY, endHour);
        calendar2.set(Calendar.MINUTE, endMinute);
        Log.d(TAG, "PONTIME--calendar millis end: " + calendar1.getTimeInMillis());

        Intent intent2 = new Intent();
        intent2.setAction(AlarmReceiver.ACTION_POWER_SAVE_ON_TIME_END_MISSION);
        PendingIntent endPendingIntent = PendingIntent.getBroadcast(context, 1, intent2,
                    PendingIntent.FLAG_UPDATE_CURRENT);


        if (isEnabled) {
            // 设置按时自动省电开始、结束闹钟
            long x = calendar1.getTimeInMillis();
            long y = calendar2.getTimeInMillis();

            Log.d(TAG, "PONTIME-- 设置新的intent 1: start: " + x + " end: " + y);
            TimeCell cell = getJustifyTime(x, y);

            // 如果开始和结束时间不相同，那么就发送alarm ，否则不发送
            if (cell.startTime != cell.endTime) {
                alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, cell.startTime,
                         AlarmManager.INTERVAL_DAY, startPendingIntent);
                alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, cell.endTime,
                         AlarmManager.INTERVAL_DAY, endPendingIntent);
//                alarmMgr.setExact(AlarmManager.RTC_WAKEUP,  cell.startTime, startPendingIntent);
//                alarmMgr.setExact(AlarmManager.RTC_WAKEUP,  cell.endTime, endPendingIntent);
            } else {
                // TODO: 弹出一个toast 的提醒
            }

            Calendar calenderTest = Calendar.getInstance();
            calenderTest.setTimeInMillis(cell.startTime);
            Log.d(TAG, "电源中心——PowerUtils: === 我们设置的新的闹钟提醒时间 new: start: " + cell.startTime +
                    " end: " + cell.endTime + " day:hour:minute " + calenderTest.get(Calendar.DAY_OF_WEEK) + ":" + startHour + ":" + startMinute
                    + "    hour:minute  " + endHour + ":" + endMinute);
        } else {
            // 取消按时自动省电闹钟
            Log.d(TAG, "PONTIME——PowerUtils：关闭按时自动省电开始闹钟、结束闹钟");
            alarmMgr.cancel(startPendingIntent);
            alarmMgr.cancel(endPendingIntent);
        }
    }

    public static void triggerLowBatteryMode(Context context) {
        Intent intent = new Intent();
        intent.setAction(PowerSaveService.ACTION_LOW_BATTERY_ON);
        context.startService(intent);
    }

    public static void triggerLowBatteryMode(Context context, String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        context.startService(intent);
    }

    public static void triggerOnTimeMode(Context context) {
        Intent intent = new Intent();
        intent.setAction(PowerSaveService.ACTION_ON_TIME_ON);
        context.startService(intent);
    }

    public static void triggerPowerSaveService(Context context) {
        Intent intent = new Intent(context, PowerSaveService.class);
        context.startService(intent);
    }

    public static int calculateDay(long milliseconds) {
        return (int) (milliseconds / DAY_OF_MILLISECONDS);
    }

    public static int calculateHour(long milliseconds) {
        return (int) (milliseconds / HOUR_OF_MILLISECONDS % 24);
    }

    public static int calculateMinute(long milliseconds) {
        return (int) (milliseconds / MINUTE_OF_MILLISECONDS % 60);
    }

    //这个是通知UI 电量模式已经改变
    public static void invokePowerModeChangedReceiver(Context context, int modeId) {
        if (modeId < 0) return;

        Intent intent = new Intent(PowerModeChangedReceiver.ACTION_POWER_MODE_CHANGED);
        Bundle bundle = new Bundle();
        bundle.putInt(PowerModeChangedReceiver.KEY_NEW_MODE_ID, modeId);
        intent.putExtras(bundle);
        context.sendBroadcast(intent);
    }

    //计算字符串程度，一个英文字符长度为1，一个中文字符长度为2
    public static int getStringLength(CharSequence c) {
        int len = 0;
        for (int i = 0; i < c.length(); i++) {
            int tmp = (int) c.charAt(i);
            if (tmp > 0 && tmp < 127) {
                ++len;
            } else {
                len += 2;
            }
        }
        return len;
    }

    public static boolean isInCharging(Context context) {
        if (sBatteryInfo == null) {
            synchronized(PowerUtils.class) {
                if (sBatteryInfo == null) {
                    sBatteryInfo = BatteryInfo.getInstance(context.getApplicationContext());
                }
            }
        }

        int status = sBatteryInfo.getBatteryState(context.getApplicationContext());
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        return isCharging;
    }

    public static boolean isExitLowWhenChargeOpen(Context context) {
         DataManager manager = DataManager.getInstance(context);
         boolean ret = manager.getBoolean(DataManager.KEY_EXIT_LOWBATTERY_WHENCHARGE,
                 false);

         return ret;
    }
}
