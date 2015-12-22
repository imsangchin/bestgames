
package com.miui.powercenter.provider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.SystemClock;
import android.util.Log;

public class BatteryInfoReceiver extends BroadcastReceiver {
    private static final String TAG = "BatteryInfoReceiver";

    private boolean mIsRegistered = false;

    BatteryInfo mInfo;
    private long mOldTime;
    private int mOldPercent;
    private int mOldStatus;

    public BatteryInfoReceiver(Context context) {
        mInfo = BatteryInfo.getInstance(context);
        init(context);
    }

    private void init(Context context) {
        mOldTime = SystemClock.elapsedRealtime();
        mOldPercent = mInfo.getBatteryPercent(context);
        mOldStatus = mInfo.getBatteryState(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int newStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        if (mOldStatus != newStatus) {
            init(context);
            mInfo.putBatteryState(newStatus);
        }

        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int newPercent = level * 100 / scale;

        Log.d(TAG, "现有电量：" + newPercent);

        if (mOldPercent != newPercent) {
            int deltaPercent = newPercent - mOldPercent;
            mOldPercent = newPercent;
            mInfo.putBatteryPercent(newPercent);
            long endTime = SystemClock.elapsedRealtime();
            long elapsedTime = endTime - mOldTime;
            mOldTime = endTime;
            boolean isCharging = newStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
                    newStatus == BatteryManager.BATTERY_STATUS_FULL;
            updateBatteryInfo(isCharging, deltaPercent, elapsedTime);
        }
    }

    private void updateBatteryInfo(boolean isCharging, int deltaPercent, long elapsedTime) {
        Log.d(TAG, "BatteryInfoReceiver.updateBatteryInfo(...).");
        long theoryTime = isCharging ? mInfo.getBatteryChargeTime() : mInfo.getBatteryStandbyTime();
        long newtime = (long) (theoryTime * (1.0f - deltaPercent / 100.0f)) + elapsedTime;
        if (isCharging) {
            mInfo.putBatteryChargeTime(newtime);
        } else {
            mInfo.putBatteryStandbyTime(newtime);
        }
    }

    public void register(Context context) {
        if (!mIsRegistered) {
            mIsRegistered = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            context.registerReceiver(this, filter);
        }
    }

    public void unregister(Context context) {
        if (mIsRegistered) {
            mIsRegistered = false;
            context.unregisterReceiver(this);
        }
    }
}
