
package com.miui.powercenter.provider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.preference.CheckBoxPreference;
import android.util.Log;

import com.miui.powercenter.PowerCenter;
/**
 * 这个类已经作废，为了避免修改settings ，所以保留
 *
 */
public class BatteryLevelReceiver extends BroadcastReceiver {
    private static final String TAG = "BatteryLevelReceiver";

    public  static final float BATTERY_PERCENTAGE_BOUND = 0.5f;

    private DataManager mManager;
    private boolean mIsRegistered = false;
    private PowerModeStateTransfer mTransition;
    private Context mContext;
    private float mBatteryPct;

    public BatteryLevelReceiver(Context context) {
        mManager = DataManager.getInstance(context.getApplicationContext());
        mTransition = PowerModeStateTransfer.getInstance(context);
        mContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(PowerCenter.DEBUG_TAG, "接受到的intent 的action: " + intent.getAction());

//        if (intent.getAction() == Intent.ACTION_POWER_CONNECTED){
//            Log.d("LIUWEI", "插入了充电设备");
//            mTransition.plugInBattery();
//        } else if (intent.getAction() == Intent.ACTION_POWER_DISCONNECTED) {
//            mTransition.plugOutBattery();
//        } else {
//            switchPowerMode(context, intent);
//        }
    }

    //在这里，可能会接受到系统的电量变化信息，也可能接受到我们自己发出的percentage 变化信息，
    //如果是第二种，那么我们直接可以从 KEY_LOW_BATTERY_PERCENTAGE 里面读取出来
    public void checkForSwitchPowerMode(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        switchPowerMode(context, batteryStatus);
    }

    private void switchPowerMode(Context context, Intent intent) {

        //如果没有开启低电功能， 那么我们忽略这个电量信号，我们既不进入低电模式， 也不退出低电模式，
        //保持现在的模式
        boolean isLowBatteryOn = mManager.getBoolean(DataManager.KEY_LOW_BATTERY_ENABLED,
                DataManager.LOW_BATTERY_ENABLED_DEFAULT);

        if (!isLowBatteryOn) {
            Log.d(PowerCenter.DEBUG_TAG, "PDEBUG--没有开启低电功能");
            return;
        }

        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        mBatteryPct = level / (float) scale;

        applyNewModeCore(mBatteryPct);
    }

    private void applyNewModeCore(float batteryPct) {
        float bound = BATTERY_PERCENTAGE_BOUND;

        if (PowerCenter.DEBUG) {
            Log.d(PowerCenter.DEBUG_TAG, "使用测试电量 0.3f");
            batteryPct = 0.3f;
        }

        //如果是手工模式，那么我们不进入低电模式， 除非用户充满电或者换电池
        boolean isManually = mManager.getBoolean(DataManager.POWER_MODE_MANUAL_LOWBATTERY, false);

        //我们的门限最大是 50%， 如果大于 50% ，那么肯定不是低电模式
        if (batteryPct >= bound) {
            //如果可以，跳出低电模式，这里，说明充好电， 手工模式退出
            mManager.putBoolean(DataManager.POWER_MODE_MANUAL_LOWBATTERY, false);
//            mTransition.goOutOfLowBatteryMode();
            return;
        }

        //percent 是我们设置的进入低电的门限
        int percent = mManager.getInt(DataManager.KEY_LOW_BATTERY_PERCENTAGE,
                DataManager.LOW_BATTERY_PERCENTAGE_DEFAULT);

        boolean isExitLowWhenCharge = mManager.getBoolean(DataManager.KEY_EXIT_LOWBATTERY_WHENCHARGE, false);
        boolean isCharging = PowerUtils.isInCharging(mContext);

        if (percent > 100 * batteryPct && !isManually) {
            Log.d(PowerCenter.DEBUG_TAG, "进入低电模式: " + batteryPct);
            if (isExitLowWhenCharge && isCharging) return;

//            mTransition.enterLowBatteryMode(false);
            return;
        }

        if (percent <= 100 * batteryPct) {
            Log.d(PowerCenter.DEBUG_TAG, "电量已经恢复，当前电量: " + batteryPct);
            //手工模式退出，用户再进入低电的时候，就需要启动低电模式了
            mManager.putBoolean(DataManager.POWER_MODE_MANUAL_LOWBATTERY, false);
//            mTransition.goOutOfLowBatteryMode();
        }
    }

    public void register(Context context) {
        if (!mIsRegistered) {
//            mIsRegistered = true;
//            IntentFilter filter = new IntentFilter();
//            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
//            filter.addAction(Intent.ACTION_POWER_CONNECTED);
//            filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
//            context.registerReceiver(BatteryLevelReceiver.this, filter);
        }
    }

    public void unregister(Context context) {
//        if (mIsRegistered) {
//            mIsRegistered = false;
//            context.unregisterReceiver(BatteryLevelReceiver.this);
//        }
    }
}
