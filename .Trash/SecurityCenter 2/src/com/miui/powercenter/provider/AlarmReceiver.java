
package com.miui.powercenter.provider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.miui.powercenter.provider.PowerData.PowerMode;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";

    public static final String ACTION_POWER_SAVE_ON_TIME_START_MISSION = "action_power_save_on_time_start_mission";
    public static final String ACTION_POWER_SAVE_ON_TIME_END_MISSION = "action_power_save_on_time_end_mission";

    private boolean mIsRegistered = false;
    private DataManager mManager;
    private PowerModeStateTransfer mTransition;

    public AlarmReceiver(Context context) {
        mManager = DataManager.getInstance(context.getApplicationContext());
        mTransition = PowerModeStateTransfer.getInstance(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "PDEBUG--：AlarmReceiver正在接收消息...");
        String action = intent.getAction();

        if (action.equals(ACTION_POWER_SAVE_ON_TIME_START_MISSION)) {
            Log.d(TAG, "电源中心——AlarmReceiver接受到了启动按时的消息");
            mTransition.exitOrEnterOnTime();
        }

        if (action.equals(ACTION_POWER_SAVE_ON_TIME_END_MISSION)) {
            Log.d(TAG, "电源中心——AlarmReceiver: 接受到了离开按时的消息");
            mTransition.exitOrEnterOnTime();
            //we need to reschedule tomorrow 's alarm
            try {
                Thread.currentThread().sleep(100);
            } catch(Exception e) {

            }
            Log.d(TAG, "电源中心---我们在离开的时候，重新设置我们的时间");
            PowerUtils.setOnTimeMission(context.getApplicationContext());
        }
    }

    public void register(Context context) {
        if (!mIsRegistered) {
            mIsRegistered = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_POWER_SAVE_ON_TIME_START_MISSION);
            filter.addAction(ACTION_POWER_SAVE_ON_TIME_END_MISSION);
            context.registerReceiver(AlarmReceiver.this, filter);
            Log.d(TAG, "电源中心——AlarmReceiver：已经注册");
        }
    }

    public void unregister(Context context) {
        if (mIsRegistered) {
            mIsRegistered = false;
//            mTransition.switchOff(PowerModeStateTransition.POWER_SAVE_ON_TIME_MODE);
            context.unregisterReceiver(AlarmReceiver.this);
            Log.d(TAG, "电源中心——AlarmReceiver：已经注销");
        }
    }
}
