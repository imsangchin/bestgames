
package com.miui.powercenter.provider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

public class PowerModeChangedReceiver extends BroadcastReceiver {
    private static final String TAG = "PowerModeChangedReceiver";

    public static final String ACTION_POWER_MODE_CHANGED = "action_power_mode_changed";
    public static final String KEY_NEW_MODE_ID = "com.miui.powercenter.provider.NEW_MODE_ID";

    private boolean mIsRegistered;
    private UIPowerModeChangeListener mListener;

    //这个接口只用于 ui 的改变
    public interface UIPowerModeChangeListener {
        public void onPowerModeChanged(int modeId);
    }

    public PowerModeChangedReceiver(Context context) {
    }

    public void addListener(UIPowerModeChangeListener l) {
        mListener = l;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }

        Bundle data = intent.getExtras();
        if (data == null) {
            return;
        }

        int modeId = data.getInt(KEY_NEW_MODE_ID, -1);
        if (mListener != null) {
            Log.d(TAG, "PDEBUG--省电模式改变成了: " + modeId);
            mListener.onPowerModeChanged(modeId);
        }
    }

    public void register(Context context) {
        if (!mIsRegistered) {
            Log.d(TAG, "PowerModeChangedListener.register()");
            mIsRegistered = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_POWER_MODE_CHANGED);
            context.registerReceiver(PowerModeChangedReceiver.this, filter);
        }
    }

    public void unregister(Context context) {
        if (mIsRegistered) {
            Log.d(TAG, "PowerModeChangedListener.unregister()");
            mIsRegistered = false;
            context.unregisterReceiver(PowerModeChangedReceiver.this);
        }
    }
}
