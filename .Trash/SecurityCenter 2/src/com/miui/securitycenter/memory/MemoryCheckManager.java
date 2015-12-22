
package com.miui.securitycenter.memory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.MiuiIntent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.miui.securitycenter.memory.IMemoryCheck;

public class MemoryCheckManager {
    public interface OnServiceConnectedListener {
        void onServiceConnected(IMemoryCheck memoryCheck);
    }

    private static MemoryCheckManager INST;
    private Context mContext;
    private IMemoryCheck mIMemoryCheck;
    private SharedPreferences mPrefs;

    private OnServiceConnectedListener mOnServiceConnectedListener = null;

    private MemoryCheckManager(Context context) {
        mContext = context;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static MemoryCheckManager getInstance(Context context) {
        if (INST == null) {
            INST = new MemoryCheckManager(context.getApplicationContext());
        }
        return INST;
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIMemoryCheck = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIMemoryCheck = IMemoryCheck.Stub.asInterface(service);
            if (mOnServiceConnectedListener != null) {
                mOnServiceConnectedListener.onServiceConnected(mIMemoryCheck);
            }
        }
    };

    public void bindMemoryCheckService(OnServiceConnectedListener listener) {
        mOnServiceConnectedListener = listener;
        if(mIMemoryCheck == null){
            Intent intent = new Intent(MiuiIntent.ACTION_MEMORY_CHECK);
            mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }else{
            if (mOnServiceConnectedListener != null) {
                mOnServiceConnectedListener.onServiceConnected(mIMemoryCheck);
            }
        }
    }

    public IMemoryCheck getMemoryCheck() {
        return mIMemoryCheck;
    }

    public boolean isServiceDisconnected() {
        return mIMemoryCheck == null;
    }
}
