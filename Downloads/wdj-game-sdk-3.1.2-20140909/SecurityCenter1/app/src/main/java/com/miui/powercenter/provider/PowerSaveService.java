package com.miui.powercenter.provider;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class PowerSaveService extends Service {
    private static final String TAG = "PowerSaveService";

    public static final String ACTION_LOW_BATTERY_ON = "com.miui.powercenter.LOW_BATTERY_ON";
    public static final String ACTION_LOW_BATTERY_OFF = "com.miui.powercenter.LOW_BATTERY_OFF";
    public static final String ACTION_ON_TIME_ON = "com.miui.powercenter.ON_TIME_ON";
    public static final String ACTION_ON_TIME_OFF = "com.miui.powercenter.ON_TIME_OFF";
    public static final String ACTION_LOW_BATTERY_CONFIG_CHANGE = "com.miui.powercenter.LOW_CONFIG_CHANGE";
    public static final String ACTION_ON_TIME_CONFIG_CHANGE = "com.miui.powercenter.ONTIME_CHANGE";
    public static final String ACTION_CANCEL_LOW_BATTERY = "com.miui.powercenter.CANCEL_LOW_BATTERY";

    private BatteryInfoReceiver mBatteryInfoReceiver;
    private PowerModeStateTransfer mPowerModeTransfer;
    private AlarmReceiver mAlarmReceiver;
    private PowerModeStateTransfer mTransition;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "电源中心：PowerSaveService启动了。");
        super.onCreate();
        startForeground(PowerUtils.POWER_SAVE_SERVICE_RUNNING, new Notification());

        mBatteryInfoReceiver = new BatteryInfoReceiver(this);
        mBatteryInfoReceiver.register(this);
        mPowerModeTransfer = PowerModeStateTransfer.getInstance(this);
        mAlarmReceiver = new AlarmReceiver(this);
        mTransition = PowerModeStateTransfer.getInstance(this);
        Log.d(TAG, "电源中心：注册了一个screen receiver");

        PowerUtils.triggerLowBatteryMode(this);
        PowerUtils.triggerOnTimeMode(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }
        String action = intent.getAction();
        if (action == null) {
            return super.onStartCommand(intent, flags, startId);
        }

        Log.d(TAG, "电源中心——PowerSaveService get action: " + action);
        if (action.equals(ACTION_LOW_BATTERY_ON)) {
            Log.d(TAG, "电源中心——PowerSaveService：正在注册BatteryLevelReceiver。");
            mPowerModeTransfer.register(this);
        } else if (action.equals(ACTION_LOW_BATTERY_OFF)) {
            Log.d(TAG, "电源中心——PowerSaveService：正在注销BatteryLevelReceiver。");
            mPowerModeTransfer.unregister(this);
        } else if (action.equals(ACTION_ON_TIME_ON)) {
            Log.d(TAG, "电源中心——PowerSaveService:正在注册AlarmReceiver。");
            mAlarmReceiver.register(this);
            PowerUtils.setOnTimeMission(this);
        } else if (action.equals(ACTION_ON_TIME_OFF)) {
            Log.d(TAG, "电源中心——PowerSaveService:正在注销AlarmReceiver。");
            mAlarmReceiver.unregister(this);
        } else if (action.equals(ACTION_LOW_BATTERY_CONFIG_CHANGE)) {
            //如果low battery 的状态发生了改变，那么就进行触发，在on pause 时触发
        } else if (action.equals(ACTION_ON_TIME_CONFIG_CHANGE)) {
            //如果 on time 的状态发生了改变，那么就进行触发， 在on pause 时触发
        } else if (action.equals(ACTION_CANCEL_LOW_BATTERY)) {
            Bundle bundle = intent.getExtras();
            int modeType = bundle.getInt(PowerModeStateTransfer.KEY_MODE_TYPE);
            mTransition.cancelNotify(modeType);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "电源中心：PowerSaveService结束了。");
        if (mBatteryInfoReceiver != null) {
            mBatteryInfoReceiver.unregister(this);
        }

        if (mAlarmReceiver != null) {
            mAlarmReceiver.unregister(this);
        }

        if (mPowerModeTransfer != null) {
            mPowerModeTransfer.unregister(this);
        }

        stopForeground(true);
        super.onDestroy();
    }
}
