package com.miui.powercenter.provider;

import java.util.ArrayList;

import com.miui.securitycenter.BootReceiver;
import com.miui.securitycenter.R;
import com.miui.common.NotificationHelper;
import com.miui.common.NotificationHelper.NotificationKey;
import com.miui.powercenter.PowerCenter;
import com.miui.powercenter.provider.PowerData.PowerMode;
import com.miui.powercenter.view.PowerCenterRemoteView;

import miui.content.res.IconCustomizer;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.MiuiIntent;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.PhoneStateListener;


/**
 *  对于内部状态的统一管理， 包括了对电量变化的统一管理
 *
 */
public class PowerModeStateTransfer extends BroadcastReceiver{

    private static PowerModeStateTransfer mInstance;
    private static DataManager mManager;
    private static Context mContext;

    //这个在主线程获得
    private Handler mHandler;
    private NotificationManager mNotificationManager;


    //一些参数的传递的key
    public  static final int TIME_DELAYED = 20000;
    public  static final float BATTERY_PERCENTAGE_BOUND = 0.5f;

    public static final String KEY_MODE_TYPE       = "modeType";
    public static final String KEY_MODE_ID         = "modeId";
    public static final String KEY_REFRESH_COUNT   = "refreshCount";

    public static final int MODE_TYPE_LOWBATTERY = 1;
    public static final int MODE_TYPE_ONTIME     = 2;
    private static final int MODE_TYPE_DEFAULT   = -1;

    private static final int MSG_REFRESH_ONESECOND = 3;
    private static final int DEFAULT_NOTIFICATION_ID = -1;

    private int mEnterNotification = DEFAULT_NOTIFICATION_ID;
    private int mRecoveryNotification = DEFAULT_NOTIFICATION_ID;
    private int mWaitType = MODE_TYPE_DEFAULT;

    //这些是低电影响状态转移的一些参数
    private boolean mIsLowEnable = false;
    private int mLowSelected;
    private int mLowOutSelected;
    private int mLowEnterPercent;
    private boolean mExitLowWhenCharge = false;

    //这些是按时省电影响状态转移的一些参数
    private boolean mIsOnTimeEnable = false;
    private int     mStartHour = 0;
    private int     mEndHour = 4;
    private int     mStartMinute = 50;
    private int     mEndMinute = 50;
    private int     mOnTimeSelected = 2;
    private int     mOnTimeOutSelected = 0;

    //这里是定时开关机的一些参数
    private boolean mBootEnabled = false;
    private int mBootTime =420;
    private int mBootRepeat = 0x7f;
    private boolean mShutdownEnabled=false;
    private int mShutdownTime = 1410;
    private int mShutdownRepeat = 0x7f;


    private boolean mIsRegistered = false;
    private ArrayList<ScreenOnOffListener> mListeners;

    public static final String TAG = "PowerModeStateTransfer";
    private static final int FLOATING_TIME = 3000;
    private boolean mFirstBatteryChanged = true;
    private int mDelayedSeconds;
    private PendingIntent mClickPendingIntent = null;
    private WakeLock mWakeLock;
    private int mKey;
    private final static String POWER_ONTIME_SHUTDOWN = "power_ontime_shutdown";

    public static interface ScreenOnOffListener {
        public void onScreenOn();
        public void onScreenOff();
    }

    public static PowerModeStateTransfer getInstance(Context context) {
        if (mInstance == null) {
            synchronized (PowerModeStateTransfer.class) {
                if (mInstance == null) {
                    mInstance = new PowerModeStateTransfer(
                            context.getApplicationContext());
                }
            }
        }

        return mInstance;
    }

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == Intent.ACTION_POWER_CONNECTED){
            plugInBattery();
            //由于我们在插拔的时候，会同时接受到connected 和 batterychanged
            //信息，但是在插入的时候， batterychanged 并不会马上认定为 charging 状态，因此我们需要等等
            waitOneSecond();
        } else if (intent.getAction() == Intent.ACTION_POWER_DISCONNECTED) {
            plugOutBattery();
            waitOneSecond();
        } else if (intent.getAction() == Intent.ACTION_BATTERY_CHANGED){
            batteryChanged(intent);
        } else if (intent.getAction() == Intent.ACTION_SCREEN_ON) {
            callScreenOn();
            cancelTask();
        } else if (intent.getAction() == Intent.ACTION_SCREEN_OFF) {
            callScreenOff();
            startTask();
        }else {
            Log.e("PowerCenter", "action 错误");
        }
    }

    private void callScreenOn() {
        for (ScreenOnOffListener listener: mListeners) {
            if (listener != null) {
                listener.onScreenOn();
            }
        }
    }

    private void callScreenOff() {
        for (ScreenOnOffListener listener: mListeners) {
            if (listener != null) {
                listener.onScreenOff();
            }
        }
    }

    private PendingIntent getClickPendingIntent() {
        Intent intent = new Intent(mContext, PowerCenter.class);

        int intid = (int)System.currentTimeMillis();
        int hashRequestCode = intid * intid;

        //这里，需要进行处理，否则第二个发出去的 pending intent 会覆盖第一个
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, hashRequestCode,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }

    private void waitOneSecond() {
        try {
            Thread.currentThread().sleep(1000);
        } catch (Exception e) {

        }
    }
    public void setShutdownBootStatus(boolean bootbutton,boolean shutdownbutton,int time1,int time2,int repeat1,int repeat2){
        mBootEnabled = bootbutton;
        mBootTime = time1;
        mBootRepeat =repeat1;
        mShutdownEnabled=shutdownbutton;
        mShutdownTime =time2 ;
        mShutdownRepeat = repeat2;
    }
    public void exitOrEnterShutdownBoot(){
        boolean bootbutton = mManager.getBoolean(POWER_ONTIME_SHUTDOWN + 1, false);
        boolean shutdownbutton = mManager.getBoolean(POWER_ONTIME_SHUTDOWN + 2, false);
        int time1 = mManager.getInt(POWER_ONTIME_SHUTDOWN + 3, DataManager.BOOT_TIME_DEFAULT);
        int time2 = mManager.getInt(POWER_ONTIME_SHUTDOWN + 4, DataManager.SHUTDOWN_TIME_DEFAULT);
        int repeat1 = mManager.getInt(POWER_ONTIME_SHUTDOWN + 5, DataManager.BOOT_REPEAT_DEFAULT);
        int repeat2 = mManager.getInt(POWER_ONTIME_SHUTDOWN + 6, DataManager.SHUTDOWN_REPEAT_DEFAULT);
        setShutdownBootStatus(bootbutton,shutdownbutton,time1,time2,repeat1,repeat2);
    }
    /*当我们离开低电的配置页面的时候，  我们通过各个参数的值来判断进入哪种状态，会影响状态转移的值有以下的几个
     * 1. 低电是否打开
     * 2. 设置的低电的门限值
     * 3. 低电切换状态
     * 4. 低电恢复状态
     * 5. 是否打开了充电跳出低电状态
     */

    public void exitOrEnterLowBattery() {
        boolean isLowEnable = mManager.getBoolean(DataManager.KEY_LOW_BATTERY_ENABLED,
                DataManager.LOW_BATTERY_ENABLED_DEFAULT);
        int lowSelected = mManager.getInt(DataManager.KEY_LOW_BATTERY_SELECTED,
                DataManager.LOW_BATTERY_SELECTED_DEFAULT);
        int lowOutSelected = mManager.getInt(DataManager.KEY_LOW_BATTERY_RECOVERY_SELECTED,
                DataManager.LOW_BATTERY_RECOVERY_DEFAULT);
        int percent = mManager.getInt(DataManager.KEY_LOW_BATTERY_PERCENTAGE,
                DataManager.LOW_BATTERY_PERCENTAGE_DEFAULT);
        boolean exitWhenCharge = mManager.getBoolean(DataManager.KEY_EXIT_LOWBATTERY_WHENCHARGE, false);

        exitLowBatteryPageCore(isLowEnable, lowSelected, lowOutSelected, percent, exitWhenCharge);
        setLowStatus(isLowEnable, lowSelected, lowOutSelected, percent, exitWhenCharge);
    }

    public void exitOnTimePage() {
        exitOrEnterOnTime();
        PowerUtils.setOnTimeMission(mContext);
    }

    public void exitOrEnterOnTime() {
        boolean isOnTimeEnable = mManager.getBoolean(DataManager.KEY_ON_TIME_ENABLED,
                DataManager.ON_TIME_ENABLED_DEFAULT);
        int onTimeSelected = mManager.getInt(DataManager.KEY_ON_TIME_SELECTED,
                DataManager.ON_TIME_SELECTED_DEFAULT);
        int onTimeOutSelected = mManager.getInt(DataManager.KEY_ON_TIME_RECOVERY_SELECTED,
                DataManager.ON_TIME_RECOVERY_DEFAULT);

        int startHour = mManager.getInt(DataManager.KEY_ON_TIME_START_HOUR,
                DataManager.ON_TIME_START_HOUR_DEFAULT);
        int startMinute = mManager.getInt(DataManager.KEY_ON_TIME_START_MINUTE,
                DataManager.ON_TIME_START_MINUTE_DEFAULT);
        int endHour = mManager.getInt(DataManager.KEY_ON_TIME_END_HOUR,
                DataManager.ON_TIME_END_HOUR_DEFAULT);
        int endMinute = mManager.getInt(DataManager.KEY_ON_TIME_END_MINUTE,
                DataManager.ON_TIME_END_MINUTE_DEFAULT);

        exitOnTimePageCore(isOnTimeEnable, onTimeSelected, onTimeOutSelected, startHour, endHour,
                startMinute, endMinute);

        setOnTimeStatus(isOnTimeEnable, onTimeSelected, onTimeOutSelected, startHour, endHour,
                startMinute, endMinute);
    }

    /**
     * 这里，我们插入我们的usb 进行充电
     * @param isLowEnable
     * @param lowSelected
     * @param lowOutSelected
     * @param percent
     * @param exitWhenCharge
     */
    public void plugInBattery() {
        boolean exitWhenCharge = mManager.getBoolean(DataManager.KEY_EXIT_LOWBATTERY_WHENCHARGE,
                false);

        if (exitWhenCharge) {
            //这里的逻辑是， 当我们插上电源后，判断当前是否在 low battery 状态，如果是的话， 那么我们应该进入到恢复状态
            if (PowerUtils.isLowBatteryInUse(mContext, mManager) || (mEnterNotification != DEFAULT_NOTIFICATION_ID
                    && mWaitType == MODE_TYPE_LOWBATTERY)) {
                gooutLowAtom();
            }
        }
    }

    /**
     * 这里，我们拔出我们的 usb 充电，此时，如果用户处于低电，但是并没有使用低电的情况下， 那么我们就进入低电的恢复模式
     * @param isLowEnable
     * @param lowSelected
     * @param lowOutSelected
     * @param percent
     * @param exitWhenCharge
     */
    public void plugOutBattery() {
        boolean exitWhenCharge = mManager.getBoolean(DataManager.KEY_EXIT_LOWBATTERY_WHENCHARGE,
                false);

        if (exitWhenCharge) {
            //这里的逻辑是， 当我们拔出电源的时候， 判断是否处于低电状态，如果处于的话，那么我们进入低电状态
            boolean isInLowBatteryMode = PowerUtils.isInLowBatteryMode(mContext, mManager);
            boolean isLowBatteryInUse = PowerUtils.isLowBatteryInUse(mContext,mManager);

            if (isInLowBatteryMode
                    && !isLowBatteryInUse) {
                enterLowBatteryModeAtom();
            }
        }
    }

    //用户手工进行了操作，那么进入手工模式
    public PowerMode enterManualMode(int modeId) {
        if (modeId == -1)
            return null;

        PowerMode mode = PowerUtils.getModeById(mContext, modeId);
        if (mode != null) {
            mManager.putInt(DataManager.KEY_POWER_MODE_APPLIED,
                    modeId);
            mode.apply(mContext);

            //在进入到手工模式的时候， 我们需要取消原先的所有等待状态，如果原先是低电，那么进入低电手工状态
            cancelAllNotification();
            if (PowerUtils.isLowBatteryInUse(mContext, mManager)) {
                PowerUtils.setLowBatteryManually(mManager, true);
            }
            goOutOfBoth();
        }

        return mode;
    }

    private void goOutOfBoth() {
        mManager.putBoolean(DataManager.KEY_LOW_BATTERY_INUSE, false);
        mManager.putBoolean(DataManager.KEY_ON_TIME_INUSE, false);
    }

    /*下面这个函数是退出按时页面的核心函数*/
    private void exitOnTimePageCore(boolean isOnTimeEnable, int onTimeSelected, int onTimeOutSelected,
            int startHour, int endHour, int startMinute, int endMinute) {

        //如果处于低电使用状态
        if (PowerUtils.isLowBatteryInUse(mContext, mManager)) {
            return;
        }

        if (!isOnTimeEnable) {
            switchOffOnTime();
            return;
        }

        boolean onTimeInUse = PowerUtils.isOnTimeInUse(mContext, mManager);

        //如果处于按时状态，并且没有处于正在等待按时状态，那么进入按时等待状态
        if (PowerUtils.isInOnTimeMode(mContext, mManager) && !onTimeInUse ) {
            if (mEnterNotification == DEFAULT_NOTIFICATION_ID || mWaitType != MODE_TYPE_ONTIME) {
                enterOnTimeModeAtom();
            }
            return;
        }

        if ((onTimeInUse && !PowerUtils.isInOnTimeMode(mContext, mManager)) || (!onTimeInUse &&
                mEnterNotification != DEFAULT_NOTIFICATION_ID && mWaitType == MODE_TYPE_ONTIME)) {
            if (!PowerUtils.isInLowBatteryMode(mContext, mManager)) {
                gooutOnTimeAtom();
            } else {
                if (!mExitLowWhenCharge || (mExitLowWhenCharge && !PowerUtils.isInCharging(mContext))) {
                    enterLowBatteryModeAtom();
                }
            }
        }
    }

    /*这个函数是退出低电页面的核心函数*/
    private void exitLowBatteryPageCore(boolean isLowEnable, int lowSelected, int lowOutSelected,
            int percent, boolean exitWhenCharge) {

        //如果处于使用按时状态，或者我们有一个当前正在处理的进入状态，那么退出
        //PATCH: 这里，如果是正要处理的的状态有， 那么不能进行退出，因为这样的话，我们在刚刚开启一个等待低电的模式下， 就不可以
        //在改变了preference 的情况下退出了
        if (PowerUtils.isOnTimeInUse(mContext, mManager)) {
            return;
        }

        //如果是开关进行了关闭，那么我们去掉现在所有的notify， 并且什么都不做
        if (!isLowEnable) {
            switchOffLowBattery();
            return;
        }

        //如果是充电开关开启，并且正在充电的情况下，如果处于低电模式，那么就退出低电模式

        if (exitWhenCharge && PowerUtils.isInCharging(mContext)) {
            if (PowerUtils.isInLowBatteryMode(mContext, mManager)) {
                gooutLowAtom();
            }
            return;
        }

        //如果是开启了低电，并且已经满足低电的所有条件，并且不处于手工模式，那么我们进入低电
        //手工模式必须要先充电，后放电
        if (PowerUtils.isInLowBatteryMode(mContext, mManager) && !PowerUtils.isInLowBatteryManually(mManager)) {
            //如果已经处于低电状态，或者正在准备进入低电状态，那么直接返回
            if (PowerUtils.isLowBatteryInUse(mContext, mManager) || (mEnterNotification != DEFAULT_NOTIFICATION_ID
                              && mWaitType == MODE_TYPE_LOWBATTERY && mLowSelected == lowSelected)) {
                return;
            }

            enterLowBatteryModeAtom();
            return;
        }

        boolean isLowInUse = mManager.getBoolean(mManager.KEY_LOW_BATTERY_INUSE, false);

        //if previouse is low and now is not or
        //previouse is not but a waiting is here, we get out of it
        if ((isLowInUse && !PowerUtils.isInLowBatteryMode(mContext, mManager))
                || (!isLowInUse && mEnterNotification != DEFAULT_NOTIFICATION_ID &&
                        mWaitType == MODE_TYPE_LOWBATTERY)) {
            if (!PowerUtils.isInOnTimeMode(mContext, mManager) || (mExitLowWhenCharge &&
                    PowerUtils.isInCharging(mContext))) {
                gooutLowAtom();
            } else {
                if (mEnterNotification != DEFAULT_NOTIFICATION_ID &&
                        mWaitType == MODE_TYPE_ONTIME) return;
                enterOnTimeModeAtom();
            }
            return;
        }
    }

    //如果我们关闭了低电的开关，那么我们就调用这里
    private void switchOffLowBattery() {
        mManager.putBoolean(DataManager.KEY_LOW_BATTERY_ENABLED,false);
        mManager.putBoolean(DataManager.KEY_LOW_BATTERY_INUSE, false);

        if (PowerUtils.isInOnTimeMode(mContext, mManager)) {
            enterOnTimeModeAtom();
        } else {
            mManager.putInt(DataManager.KEY_POWER_MODE_APPLIED, DataManager.POWER_MODE_APPLIED_DEFAULT);
            PowerUtils.invokePowerModeChangedReceiver(mContext, DataManager.POWER_MODE_APPLIED_DEFAULT);
        }
        cancelAllNotification();
    }

    //如果我们关闭了按时省电，那么我们就调用这里
    private void switchOffOnTime() {
        mManager.putBoolean(DataManager.KEY_ON_TIME_ENABLED, false);
        mManager.putBoolean(DataManager.KEY_ON_TIME_INUSE, false);

        //如果我们关闭了按时模式，那么如果在低电状态，并且充电失效开关开并且不在充电， 或者充电失效开关关的情况下， 进入低电模式
        if (PowerUtils.isInLowBatteryMode(mContext, mManager) && ((PowerUtils.isExitLowWhenChargeOpen(mContext)
                && !PowerUtils.isInCharging(mContext)) || !PowerUtils.isExitLowWhenChargeOpen(mContext))) {
            enterOnTimeModeAtom();
        } else {
            if(!BootReceiver.isReboot){
                mManager.putInt(DataManager.KEY_POWER_MODE_APPLIED, DataManager.POWER_MODE_APPLIED_DEFAULT);
                PowerUtils.invokePowerModeChangedReceiver(mContext, DataManager.POWER_MODE_APPLIED_DEFAULT);
                Log.i(TAG, "switchOffOnTime-----------> modeId :" + DataManager.POWER_MODE_APPLIED_DEFAULT);
            }
        }

        cancelAllNotification();
    }

    //进入低电的原子操作，并且发出通知
    private void enterLowBatteryModeAtom() {
        int  modeId = mManager.getInt(DataManager.KEY_LOW_BATTERY_SELECTED,
                DataManager.LOW_BATTERY_SELECTED_DEFAULT);

        PowerMode mode = PowerUtils.getModeById(mContext, modeId);

        if (mode != null) {
            sendNotificationWhenSwitch(mode, MODE_TYPE_LOWBATTERY, modeId, true);
        }
    }

    //进入按时省电模式的原子操作， 并且发出通知
    public void enterOnTimeModeAtom() {
        int modeId = mManager.getInt(DataManager.KEY_ON_TIME_SELECTED,
                DataManager.ON_TIME_SELECTED_DEFAULT);

        PowerMode mode = PowerUtils.getModeById(mContext, modeId);
        if (mode != null) {
            sendNotificationWhenSwitch(mode, MODE_TYPE_ONTIME, modeId, true);
        }
    }

    //当我们的电池电量变化时，我们需要判断是否进入或者退出低电模式
    public void batteryChanged(Intent intent) {
        boolean isLowBatteryOn = mManager.getBoolean(DataManager.KEY_LOW_BATTERY_ENABLED,
                DataManager.LOW_BATTERY_ENABLED_DEFAULT);

        if (!isLowBatteryOn) {
            return;
        }

        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct  = level / (float) scale;

        float bound = BATTERY_PERCENTAGE_BOUND;

        if (PowerCenter.DEBUG) {
            Log.d(PowerCenter.DEBUG_TAG, "使用测试电量 0.3f");
            batteryPct = 0.3f;
        }

        //我们的门限最大是 50%， 如果大于 50% ，那么肯定不是低电模式，可能是换电池
        if (batteryPct >= bound) {
            //充电成功，退出低电手工模式
            mManager.putBoolean(DataManager.POWER_MODE_MANUAL_LOWBATTERY, false);
            return;
        }

        //percent 是我们设置的进入低电的门限
        int percent = mManager.getInt(DataManager.KEY_LOW_BATTERY_PERCENTAGE,
                DataManager.LOW_BATTERY_PERCENTAGE_DEFAULT);

        boolean isManually = PowerUtils.isInLowBatteryManually(mManager);
        boolean isExitLowWhenCharge = PowerUtils.isExitLowWhenChargeOpen(mContext);
        boolean isCharging = PowerUtils.isInCharging(mContext);

        //如果电量低于门限值，并且不处于手工模式 && (充电失效开关关闭 || 充电开关开并且没有在充电)
        //if there is a waiting low battery notification, we not enter it
        boolean isWaitingLowBattery = (mEnterNotification != DEFAULT_NOTIFICATION_ID &&
                mWaitType == MODE_TYPE_LOWBATTERY);

        boolean isLowBatteryInUse = PowerUtils.isLowBatteryInUse(mContext, mManager);

        if (!isLowBatteryInUse && percent > 100 * batteryPct && !isManually && (!isExitLowWhenCharge ||
                (isExitLowWhenCharge && !isCharging)) && !isWaitingLowBattery) {
            //由于我们进入到省电中心，就会调用这里一次， 因此， 第一次我们不需要判断
            if (mFirstBatteryChanged) {
                mFirstBatteryChanged = false;
                return;
            }

            //如果正在进入按时， 在充电的情况下，我们不提示进入低电。 如果正在使用按时，那么就不进入低电
            boolean isOnTimeInUse = PowerUtils.isOnTimeInUse(mContext, mManager);
            boolean isWaitingOnTime = (mEnterNotification != DEFAULT_NOTIFICATION_ID &&
                    mWaitType == MODE_TYPE_ONTIME);

            if (isOnTimeInUse || isWaitingOnTime) {
                return;
            }

            enterLowBatteryModeAtom();
            return;
        }

        //如果电量已经恢复，，那么退出低电模式， 退出手工模式
        if (percent <= 100 * batteryPct) {

            //充电成功，退出低电手工模式
            mManager.putBoolean(DataManager.POWER_MODE_MANUAL_LOWBATTERY, false);
            exitOrEnterLowBattery();
        }
    }

    //进入低电的恢复状态，选择恢复状态，并且退出低电使用状态
    private void gooutLowAtom() {
        //如果不处于低电状态，那么就没有必要退出，比如在进入低电后， 又手工的退出了
        if (!PowerUtils.isLowBatteryInUse(mContext, mManager) &&
                mWaitType != MODE_TYPE_LOWBATTERY) return;

        int modeId = mManager.getInt(DataManager.KEY_LOW_BATTERY_RECOVERY_SELECTED,
                DataManager.LOW_BATTERY_RECOVERY_DEFAULT);

        PowerMode mode = PowerUtils.getModeById(mContext, modeId);
        if (mode != null) {
            mManager.putBoolean(mManager.KEY_LOW_BATTERY_INUSE, false);
            sendNotificationWhenSwitch(mode, MODE_TYPE_LOWBATTERY, modeId, false);
        }
    }


    //退出按时省电的原子操作
    public void gooutOnTimeAtom() {

        //如果并没有使用on time，那么就没有必要退出，比如在达到时间后，我们又手工的进行了配置
        if (!PowerUtils.isOnTimeInUse(mContext, mManager) && mWaitType != MODE_TYPE_ONTIME) {
            return;
        }

        int modeId = mManager.getInt(DataManager.KEY_ON_TIME_RECOVERY_SELECTED,
                DataManager.ON_TIME_RECOVERY_DEFAULT);

        PowerMode mode = PowerUtils.getModeById(mContext, modeId);

        if (mode != null) {
            mManager.putBoolean(mManager.KEY_ON_TIME_INUSE, false);
            sendNotificationWhenSwitch(mode, MODE_TYPE_ONTIME, modeId, false);
        }
    }

    //用户点击取消时，我们放弃所有的待进入状态
    public void cancelNotify(int modeType) {
        cancelAllNotification();
        mWakeLock.release();
        //如果用户手工取消掉我们的低电， 那么进入低电手工模式，必须要先充电再放电才会再次进入低电
        if (modeType == MODE_TYPE_LOWBATTERY) {
            PowerUtils.setLowBatteryManually(mManager, true);
        }
    }

    //最后一个参数，如果是进入模式，需要notify, 如果是退出，不需要notify
    private void sendNotificationWhenSwitch(PowerMode mode, int modeType, int modeId, boolean enter) {
        int    smallIcon = R.drawable.ic_launcher_power_optimize;

        int notificationId = 0;
        String key = (String)mode.mDBValue[3] + modeType +  (enter?"1":"0") + System.currentTimeMillis();
        notificationId = key.hashCode();
        Notification.Builder builder = new Notification.Builder(mContext);

        if (smallIcon != 0) builder.setSmallIcon(smallIcon);

        android.graphics.Bitmap largeIcon = IconCustomizer.generateIconStyleDrawable(
                mContext.getResources().getDrawable(R.drawable.ic_launcher_power_optimize)).getBitmap();
        PowerCenterRemoteView remoteViews = new PowerCenterRemoteView(mContext);
        String title = setRemoteViewTitles(remoteViews, modeType, (String)mode.mDBValue[3], TIME_DELAYED/1000, enter);
        remoteViews.setIcon(largeIcon);

        cancelAllNotification();
        mWaitType = MODE_TYPE_DEFAULT;

        mClickPendingIntent = getClickPendingIntent();

        builder.setContent(remoteViews);
        builder.setAutoCancel(true);
        builder.setSmallIcon(smallIcon);
        if (mClickPendingIntent != null) {
            builder.setContentIntent(mClickPendingIntent);
        }

        Notification notification = builder.build();
        notification.extraNotification.setEnableFloat(true);
        notification.extraNotification.setFloatTime(FLOATING_TIME);

        //发出倒计时的指示，如果是进入到某种模式，那么需要保留该notification id
        if (enter) {
            if(!checkCalling(mode, modeType, modeId, enter)){
                mWakeLock.acquire();
                sendRefreshMessage(mode, modeId, modeType, TIME_DELAYED/1000);
                mEnterNotification = notificationId;
                setActionButtonIntent(notificationId, remoteViews, modeType);
                mWaitType = modeType;
            }else {
                return ;
            }
        } else {
            //在 go out 的情况下， 如果有待处理的 notification 那么就退出
            mRecoveryNotification = notificationId;
            enterMode(modeId, modeType, mode, false);
        }

        mNotificationManager.notify(notificationId, notification);
    }

    public boolean isShutdownOnStatusChanged(boolean bootbutton,boolean shutdownbutton,int time1,int time2,int repeat1,int repeat2){
        exitOrEnterShutdownBoot();
        if(bootbutton == mBootEnabled && shutdownbutton == mShutdownEnabled && time1 == mBootTime && time2 ==mShutdownTime
                && repeat1 == mBootRepeat && repeat2 == mShutdownRepeat)
            return false;
        return true;
    }
    public boolean isLowStatusChanged(boolean isLowEnable, int lowSelected, int lowOutSelected,
            int percent, boolean exitWhenCharge) {
        if (isLowEnable == mIsLowEnable && lowSelected == mLowSelected && lowOutSelected == mLowOutSelected
                && percent == mLowEnterPercent && exitWhenCharge == mExitLowWhenCharge) {
            return false;
        }

        return true;
    }

    public boolean isOnTimeStatusChanged(boolean isOnTimeEnable, int onTimeSelected, int onTimeOutSelected,
            int startHour, int endHour, int startMinute, int endMinute) {

        if (isOnTimeEnable == mIsOnTimeEnable && onTimeSelected == mOnTimeSelected &&
                onTimeOutSelected == mOnTimeOutSelected && startHour == mStartHour
                && endHour == mEndHour && startMinute == mStartMinute && endMinute == mEndMinute) return false;

        return true;
    }

    public void setLowStatus(boolean isLowEnable, int lowSelected, int lowOutSelected,
            int percent, boolean exitWhenCharge) {

        mIsLowEnable = isLowEnable;
        mLowSelected = lowSelected;
        mLowOutSelected = lowOutSelected;
        mLowEnterPercent = percent;
        mExitLowWhenCharge = exitWhenCharge;
    }

    public void setOnTimeStatus(boolean isOnTimeEnable, int onTimeSelected, int onTimeOutSelected,
            int startHour, int endHour, int startMinute, int endMinute) {
        mIsOnTimeEnable = isOnTimeEnable;
        mOnTimeSelected = onTimeSelected;
        mOnTimeOutSelected = onTimeOutSelected;
        mStartHour = startHour;
        mEndHour = endHour;
        mStartMinute = startMinute;
        mEndMinute = endMinute;
    }

    public void addScreenListener(ScreenOnOffListener listener) {
        if (listener != null) {
            mListeners.add(listener);
        }
    }

    /**
     * 私有的对象创建函数
     * @param context
     */
    private PowerModeStateTransfer(Context context) {
        mManager = DataManager.getInstance(context);
        mContext = context;

        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getCanonicalName());

        mNotificationManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mListeners = new ArrayList<ScreenOnOffListener>();

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                PowerMode mode = (PowerMode)msg.obj;
                if (mode == null) return;

                Bundle data = msg.getData();
                int modeId = data.getInt(KEY_MODE_ID);
                int modeType = data.getInt(KEY_MODE_TYPE);
                int count = data.getInt(KEY_REFRESH_COUNT);

                switch(msg.what) {
                case MSG_REFRESH_ONESECOND:
                    if (count > 0) {
                        refreshNotification(msg);
                    } else {
                        cancelAllNotification();
                        mWakeLock.release();
                        //真正进入了某一种模式
                        enterMode(modeId, modeType, mode, true);
                    }
                    break;
                default:
                    break;
                }

            }
        };
        setInitilizeValues();
    }

    private void setInitilizeValues() {
        boolean lowEnable = mManager.getBoolean(DataManager.KEY_LOW_BATTERY_ENABLED,
                DataManager.LOW_BATTERY_ENABLED_DEFAULT);
        int percent = mManager.getInt(DataManager.KEY_LOW_BATTERY_PERCENTAGE,
                DataManager.LOW_BATTERY_PERCENTAGE_DEFAULT);
        int  lowBatteryModeId = mManager.getInt(DataManager.KEY_LOW_BATTERY_SELECTED,
                DataManager.LOW_BATTERY_SELECTED_DEFAULT);
        int  lowBatteryRecoveryId = mManager.getInt(DataManager.KEY_LOW_BATTERY_RECOVERY_SELECTED,
                DataManager.LOW_BATTERY_RECOVERY_DEFAULT);
        boolean exitWhenSwitchEnable = mManager.getBoolean(DataManager.KEY_EXIT_LOWBATTERY_WHENCHARGE,false);

        boolean isOnTimeEnable = mManager.getBoolean(DataManager.KEY_ON_TIME_ENABLED,
                DataManager.ON_TIME_ENABLED_DEFAULT);
        int startHour = mManager.getInt(DataManager.KEY_ON_TIME_START_HOUR,
                DataManager.ON_TIME_START_HOUR_DEFAULT);
        int startMinute = mManager.getInt(DataManager.KEY_ON_TIME_START_MINUTE,
                DataManager.ON_TIME_START_MINUTE_DEFAULT);
        int endHour = mManager.getInt(DataManager.KEY_ON_TIME_END_HOUR,
                DataManager.ON_TIME_END_HOUR_DEFAULT);
        int endMinute = mManager.getInt(DataManager.KEY_ON_TIME_END_MINUTE,
                DataManager.ON_TIME_END_MINUTE_DEFAULT);
        int modeId = mManager.getInt(DataManager.KEY_ON_TIME_SELECTED,
                DataManager.ON_TIME_SELECTED_DEFAULT);
        int recoveryId = mManager.getInt(DataManager.KEY_ON_TIME_RECOVERY_SELECTED,
                DataManager.ON_TIME_RECOVERY_DEFAULT);

        setLowStatus(lowEnable,lowBatteryModeId,lowBatteryRecoveryId,percent,exitWhenSwitchEnable);
        setOnTimeStatus(isOnTimeEnable, modeId, recoveryId, startHour, endHour, startMinute, endMinute);
    }

    private void refreshNotification(Message msg) {
        int    smallIcon = R.drawable.ic_launcher_power_optimize;
        PowerMode mode = (PowerMode)msg.obj;
        Bundle data = msg.getData();
        int count = data.getInt(KEY_REFRESH_COUNT);
        int modeType = data.getInt(KEY_MODE_TYPE);
        int modeId = data.getInt(KEY_MODE_ID);

        Notification.Builder builder = new Notification.Builder(mContext);

        if (smallIcon != 0) builder.setSmallIcon(smallIcon);

        PowerCenterRemoteView remoteViews = new PowerCenterRemoteView(mContext);

        android.graphics.Bitmap largeIcon = IconCustomizer.generateIconStyleDrawable(
                mContext.getResources().getDrawable(R.drawable.ic_launcher_power_optimize)).getBitmap();
        setRemoteViewTitles(remoteViews, modeType, (String)mode.mDBValue[3], count, true);
        remoteViews.setIcon(largeIcon);
        setActionButtonIntent(mEnterNotification, remoteViews, modeType);

        builder.setContent(remoteViews);
        builder.setAutoCancel(true);
        if (mClickPendingIntent != null) {
            builder.setContentIntent(mClickPendingIntent);
        }

        Notification notification = builder.build();
        mNotificationManager.notify(mEnterNotification, notification);
        sendRefreshMessage((PowerMode)msg.obj, modeId, modeType,0);
    }


    /**
     * get into some mode, the mode is entering or goout, if entering, we need to set the flags
     * @param modeId
     * @param modeType
     * @param mode
     */
    private void enterMode(int modeId, int modeType, PowerMode mode, boolean enter) {
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        switch (modeType) {
        case MODE_TYPE_LOWBATTERY:
            mode.apply(mContext);
            if (pm.isScreenOn()) {
                cancelTask();
            } else {
                startTask();
            }

            if (enter) {
                mManager.putBoolean(DataManager.KEY_LOW_BATTERY_INUSE, true);
                mManager.putBoolean(DataManager.KEY_ON_TIME_INUSE, false);
            } else {
                mManager.putBoolean(DataManager.KEY_LOW_BATTERY_INUSE, false);
            }

            mManager.putInt(DataManager.KEY_POWER_MODE_APPLIED, modeId);
            Log.i(TAG, "enterMode-----------> modeId :" + modeId);
            PowerUtils.invokePowerModeChangedReceiver(mContext, modeId);
            break;

        case MODE_TYPE_ONTIME:
            mode.apply(mContext);
            if (pm.isScreenOn()) {
                cancelTask();
            } else {
                startTask();
            }
            if (enter) {
                mManager.putBoolean(DataManager.KEY_ON_TIME_INUSE, true);
                mManager.putBoolean(DataManager.KEY_LOW_BATTERY_INUSE, false);
            } else {
                mManager.putBoolean(DataManager.KEY_ON_TIME_INUSE, false);
            }

            mManager.putInt(DataManager.KEY_POWER_MODE_APPLIED, modeId);
            PowerUtils.invokePowerModeChangedReceiver(mContext, modeId);
            break;
        }
    }

    private String setRemoteViewTitles(PowerCenterRemoteView remoteView, int modeType, String modeName, int count,
            boolean enter) {

        String title = "";
        String subTitle = "";
        if (enter) {
            title = String.format(mContext.getString(R.string.power_20s_enter_mode), count , modeName);
        } else {
            title = String.format(mContext.getString(R.string.power_recovery_mode), modeName);
        }


        switch(modeType) {
        case MODE_TYPE_LOWBATTERY:
            int percent = mManager.getInt(DataManager.KEY_LOW_BATTERY_PERCENTAGE,
                    DataManager.LOW_BATTERY_PERCENTAGE_DEFAULT);
            if (enter) {
                subTitle = String.format(mContext.getString(R.string.power_notify_lowbattery), percent);

            } else {
                subTitle = mContext.getString(R.string.power_subtitle_lowbattery);
            }
            break;
        case MODE_TYPE_ONTIME:
            int startHour = mManager.getInt(DataManager.KEY_ON_TIME_START_HOUR,
                    DataManager.ON_TIME_START_HOUR_DEFAULT);
            int startMinute = mManager.getInt(
                    DataManager.KEY_ON_TIME_START_MINUTE,
                    DataManager.ON_TIME_START_MINUTE_DEFAULT);
            int endHour = mManager.getInt(DataManager.KEY_ON_TIME_END_HOUR,
                    DataManager.ON_TIME_END_HOUR_DEFAULT);
            int endMinute = mManager.getInt(DataManager.KEY_ON_TIME_END_MINUTE,
                    DataManager.ON_TIME_END_MINUTE_DEFAULT);

            if (enter) {
                String startTime = PowerUtils.getFormatTime(startHour, startMinute);
                String endTime = PowerUtils.getFormatTime(endHour, endMinute);
                subTitle = startTime + "-" + endTime;
            } else {
                subTitle = mContext.getString(R.string.power_subtitle_ontime);
            }
            break;
        }

        remoteView.setTitles(title, subTitle);
        return title;
    }

    private void cancelAllNotification() {
        mHandler.removeMessages(MSG_REFRESH_ONESECOND);
        mClickPendingIntent = null;
        if (mEnterNotification != DEFAULT_NOTIFICATION_ID) {
            mNotificationManager.cancel(mEnterNotification);
            mEnterNotification = DEFAULT_NOTIFICATION_ID;
        }

        if (mRecoveryNotification != DEFAULT_NOTIFICATION_ID) {
            mNotificationManager.cancel(mRecoveryNotification);
            mRecoveryNotification = DEFAULT_NOTIFICATION_ID;
        }
    }

    private void setActionButtonIntent(int notifyId, PowerCenterRemoteView view, int modeType) {

        Intent intent = new Intent(mContext, PowerSaveService.class);
        intent.setAction(PowerSaveService.ACTION_CANCEL_LOW_BATTERY);
        Bundle bundle = new Bundle();

        bundle.putInt(KEY_MODE_TYPE, modeType);
        intent.putExtras(bundle);
        String hashCodeStr = "" + System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getService(mContext,
                hashCodeStr.hashCode(), intent, PendingIntent.FLAG_CANCEL_CURRENT);

        String cancel = mContext.getResources().getString(android.R.string.cancel);
        view.setActionButton(cancel, pendingIntent);
    }

    private void sendRefreshMessage(PowerMode mode, int modeId, int modeType, int count) {
        Message msg = mHandler.obtainMessage();
        msg.what = MSG_REFRESH_ONESECOND;
        msg.obj = mode;
        Bundle msgBundle = new Bundle();
        msgBundle.putInt(KEY_MODE_ID, modeId);
        msgBundle.putInt(KEY_MODE_TYPE, modeType);
        msgBundle.putInt(KEY_REFRESH_COUNT, count);
        msg.setData(msgBundle);

        if (count >= 0) {
            mHandler.sendMessageDelayed(msg, count*1000);
        }
    }

    public void register(Context context) {
        if (!mIsRegistered) {
            mIsRegistered = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            filter.addAction(Intent.ACTION_POWER_CONNECTED);
            filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            context.registerReceiver(PowerModeStateTransfer.this, filter);
        }
    }

    public void unregister(Context context) {
        if (mIsRegistered) {
            mIsRegistered = false;
            context.unregisterReceiver(PowerModeStateTransfer.this);
        }
    }

    private void startTask() {
        mDelayedSeconds = mManager.getInt(DataManager.KEY_AUTO_CLEAN_MEMORY_DELAYED_TIME,
                DataManager.AUTO_CLEAN_MEMORY_DELAYED_TIME_DEFAULT);
        if(mDelayedSeconds < 0) {
            return ;
        }
        Intent intent = new Intent(MiuiIntent.ACTION_SYSTEMUI_TASK_MANAGER_CLEAR);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, NotificationHelper.getInstance(mContext).getNotificationIdByKey(NotificationKey.KEY_CLEAN_AFTER_SLEEP), intent, 0);
        AlarmManager manager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()
                + mDelayedSeconds * 1000, pendingIntent);
    }

    private void cancelTask() {
        AlarmManager manager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent("com.android.systemui.taskmanager.Clear");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, NotificationHelper.getInstance(mContext).getNotificationIdByKey(
                        NotificationKey.KEY_CLEAN_AFTER_SLEEP), intent, 0);
        manager.cancel(pendingIntent);
    }

    /**
     * 检测当前手机通话状态，如果为非空闲则去等待空闲开启省电模式
     * @return
     */
    private boolean checkCalling(final PowerMode mode,final int modeType,final int modeId,final boolean enter){
        final TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if(tm.getCallState()==TelephonyManager.CALL_STATE_IDLE){
            return false;
        }
        tm.listen( new PhoneStateListener(){
            public void onCallStateChanged(int state, String incomingNumber) {
                if(state==TelephonyManager.CALL_STATE_IDLE){
                    tm.listen(this,PhoneStateListener.LISTEN_NONE );
                    sendNotificationWhenSwitch(mode, modeType, modeId, enter);
                }
            };
        },PhoneStateListener.LISTEN_CALL_STATE);
        return true;
    }
}
