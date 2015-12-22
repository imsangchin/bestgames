package com.miui.powercenter.provider;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.os.Handler;
import com.miui.securitycenter.R;
import android.os.Process;

import com.android.internal.telephony.ITelephony;
import com.miui.powercenter.PowerShutdownOnTime;
import com.miui.powercenter.view.PowerCenterRemoteView;

import miui.content.res.IconCustomizer;

public class ShutdownOnTimeState extends BroadcastReceiver {
    private final static String ACTION_SHUTDOWN_NOTIFICATION=ShutdownAlarmIntentService.ACTION_SHUTDOWN_NOTIFICATION;
    private final static String POWER_ONTIME_SHUTDOWN = PowerShutdownOnTime.POWER_ONTIME_SHUTDOWN;
    private final static String ACTION_CANCEL_SHUTDOWN="com.miui.powercenter.provider.CANCEL_SHUTDOWN";
    private final static int MSG_SHUTDOWN=1;
    private DataManager mDataManager;
    private final static String TAG="ShutdownOnTimeState";
    private static final int FLOATING_TIME = 3000;
    public  static final int TIME_DELAYED = 20000;
    private static final int SHUTDOWN_NOTIFICATION=1;
    private ITelephony phone; 
    private Context mContext;
    private int shutdownTime;
    private String formatShutdownTime;
    private int bootTime;
    private String formatBootTime;
    private boolean mIsRegistered = false;
    NotificationManager mNotificationManager;
    private boolean shutdownFlag=true;
    private static ShutdownOnTimeState mInstance;
    private static Handler mHandler;

    public static ShutdownOnTimeState getInstance(Context context){
        if (mInstance == null) {
            synchronized (ShutdownOnTimeState.class) {
                if (mInstance == null) {
                    mInstance = new ShutdownOnTimeState(context.getApplicationContext());
                }
            }
        }
        return mInstance;
    }

    private ShutdownOnTimeState(Context context){
        mHandler = new Handler(){
            public void handleMessage(Message msg){
              if(msg.what==MSG_SHUTDOWN){
                    shutdownPhone();
                }
            }
        };
        Log.i("LDEBUG","init handler="+mHandler);
    }
    public ShutdownOnTimeState(){
        super();
    }

    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        mContext=context;
        mDataManager = DataManager.getInstance(context);
        if(intent.getAction().equals(ACTION_SHUTDOWN_NOTIFICATION)){
            Log.i("LDEBUG","onReceive handler="+mHandler);
            sendNotification(context);
        }
        if(intent.getAction().equals(ACTION_CANCEL_SHUTDOWN)){
            sendCancelShutdownMessage();
        }

    }
    private void getShutdownTime(){
        shutdownTime =
                mDataManager.getInt(POWER_ONTIME_SHUTDOWN + 4, DataManager.SHUTDOWN_TIME_DEFAULT);
        bootTime = mDataManager.getInt(POWER_ONTIME_SHUTDOWN + 3, DataManager.SHUTDOWN_TIME_DEFAULT);
        formatShutdownTime=timeTostring(shutdownTime);
        formatBootTime=timeTostring(bootTime);
    }
    private String timeTostring(int time) {
        return (time / 60) + ":" + minutes(time % 60);
    }
    private String minutes(int m) {
        return (m < 10) ? "0" + m : m + "";
    }

    private void sendNotification(Context context) {
        mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        int    smallIcon = R.drawable.ic_launcher_power_optimize;

        Notification.Builder builder = new Notification.Builder(context);

        if (smallIcon != 0) builder.setSmallIcon(smallIcon);
        android.graphics.Bitmap largeIcon = IconCustomizer.generateIconStyleDrawable(
                context.getResources().getDrawable(R.drawable.ic_launcher_power_optimize)).getBitmap();
        PowerCenterRemoteView remoteViews = new PowerCenterRemoteView(mContext);
        remoteViews.setIcon(largeIcon);
        String title = String.format(mContext.getString(R.string.power_20s_shutdown));
        getShutdownTime();
        String subTitle=formatShutdownTime+"-"+formatBootTime;
        remoteViews.setTitles(title,subTitle);

        builder.setAutoCancel(true);

        Intent openintent = new Intent(context,PowerShutdownOnTime.class);
        PendingIntent mClickPendingIntent = PendingIntent.getActivity(context, 0, openintent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (mClickPendingIntent != null) {
            builder.setContentIntent(mClickPendingIntent);
        }

        if(!checkCalling()){
            sendShutdownMessage();
            setActionButtonIntent( remoteViews);
        }
        builder.setContent(remoteViews);
        Notification notification = builder.build();
        notification.extraNotification.setEnableFloat(true);
        notification.extraNotification.setFloatTime(FLOATING_TIME);
        mNotificationManager.notify(0, notification);
    }

        private boolean checkCalling(){
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

     void sendCancelShutdownMessage(){
//        shutdownFlag=false;
//        Log.i("test","sendCancel="+Process.myPid()+"");
//        if (mHandler.hasMessages(MSG_SHUTDOWN))  Log.i("test1","has");
//        else Log.i("test1","there are not messages");
         Log.i("LDEBUG","cancel handler="+mHandler);
        mHandler.removeMessages(MSG_SHUTDOWN);
//        if (mHandler.hasMessages(MSG_SHUTDOWN))  Log.i("test2","has");
//        else Log.i("test2","there are not messages");
        if(mNotificationManager==null) 
            mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(0);
    }
    private void sendShutdownMessage(){
        Log.i("LDEBUG","shutdown="+mHandler);
        Message msg = mHandler.obtainMessage();
        msg.what = MSG_SHUTDOWN;
        mHandler.sendMessageDelayed(msg, 20*1000);
    }



    private void shutdownPhone() {
        Intent shutdownIntent = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
        shutdownIntent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
        shutdownIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(shutdownIntent);
    }
//    public void register(Context context) {
//        if (!mIsRegistered) {
//            mIsRegistered = true;
//            IntentFilter filter = new IntentFilter();
//            filter.addAction(ACTION_SHUTDOWN_NOTIFICATION);
//            context.registerReceiver(ShutdownOnTimeState.this, filter);
//        }
//    }
//    public void unregister(Context context) {
//        if (mIsRegistered) {
//            mIsRegistered = false;
//            context.unregisterReceiver(ShutdownOnTimeState.this);
//        }
//    }
    private void setActionButtonIntent(PowerCenterRemoteView view){
        Intent intent = new Intent(ACTION_CANCEL_SHUTDOWN);
        String hashCodeStr = "" + System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,
                hashCodeStr.hashCode(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
        String cancel = mContext.getResources().getString(android.R.string.cancel);
        view.setActionButton(cancel, pendingIntent);
    }
}
