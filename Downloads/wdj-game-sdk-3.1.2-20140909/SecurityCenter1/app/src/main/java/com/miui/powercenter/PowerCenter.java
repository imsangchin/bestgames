
package com.miui.powercenter;

import miui.app.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.MiuiIntent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.BatteryManager;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.TextView;

import com.miui.securitycenter.ExtraIntent;
import com.miui.securitycenter.R;
import com.miui.analytics.AnalyticsUtil;
import com.miui.common.AndroidUtils;
import com.miui.common.ViewAlphaAnimation;
import com.miui.powercenter.provider.BatteryInfo;
import com.miui.powercenter.provider.DataManager;
import com.miui.powercenter.provider.PowerModeChangedReceiver;
import com.miui.powercenter.provider.PowerData.PowerMode;
import com.miui.powercenter.provider.PowerModeChangedReceiver.UIPowerModeChangeListener;
import com.miui.powercenter.provider.PowerUtils;
import com.miui.powercenter.view.BatteryVolume;
import com.miui.securitycenter.ScoreTextView;

public class PowerCenter extends Activity {

    public enum BackgroundStatus {
        GREEN, RED
    }

    private static final String TAG = "PowerCenter";
    private TextView mLabel1;
    private TextView mLabel2;
    private TextView mLabel3;
    private TextView mLabel4;
    private TextView mSummary1;
    private TextView mSummary2;
    private TextView mSummary3;
    private TextView mBatteryTime;
    private DataManager mDataManager;
    private BatteryVolume mBatteryVolume;
    private BatteryInfo mBatteryInfo;
    private PowerModeChangedReceiver mModeChangedReceiver;
    private static int sOrange = 0xfffb6003;

    private View mForegroundView;
    private View mBackgroundView;
    private ScoreTextView mBatteryPercentView;

    public static final String DEBUG_TAG = "POWER_TEST";
    private final static String POWER_ONTIME_SHUTDOWN = "power_ontime_shutdown";

    // TODO: remove this
    public static final boolean DEBUG = false;

    private OnSharedPreferenceChangeListener mBatteryInfoListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (TextUtils.equals(key, BatteryInfo.BATTERY_INFO_STATE)
                    || TextUtils.equals(key, BatteryInfo.BATTERY_INFO_CHARGE_TIME)
                    || TextUtils.equals(key, BatteryInfo.BATTERY_INFO_STANDBY_TIME)) {
                Log.d(TAG, "PowerCenter.BatteryInfo: state, chargeTime, or standbyTime is changed.");
                int status = mBatteryInfo.getBatteryState(PowerCenter.this);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL;
                refreshBatteryTime(isCharging);
                mBatteryVolume.handleChargingAnimation(isCharging, true);
            }
        }
    };

    private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateBatteryStatus(intent);
        }
    };

    private void updateBatteryStatus(Intent intent) {
        if (intent == null) {
            return;
        }

        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int percent = (int) (level * 100 / (float) scale);

        if (percent <= 10) {
            updateForeground(BackgroundStatus.RED);
        } else {
            updateForeground(BackgroundStatus.GREEN);
        }
        mBatteryPercentView.setNumber(percent);
    }

    private UIPowerModeChangeListener mModeChangedListener = new UIPowerModeChangeListener() {
        @Override
        public void onPowerModeChanged(int modeId) {
            mSummary1.setText(R.string.power_center_custom_summary_default);
            if (modeId >= 0) {
                String modeName = PowerUtils.getModeNameById(PowerCenter.this, modeId);
//                mDataManager.putInt(DataManager.KEY_MODE_WHICH_USER_CHOOSE , modeId);
                mLabel1.setText(modeName);
            }
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.back) {
                finish();
                return;
            }

            Intent intent = null;
            switch (v.getId()) {
                case R.id.power_center_custom:
                    intent = new Intent(ExtraIntent.ACTION_POWER_MODE_CHOOSER);
                    break;
                case R.id.power_center_low_battery:
                    intent = new Intent("com.miui.powercenter.PowerSaveLowBattery");
                    break;
                case R.id.power_center_on_time:
                    intent = new Intent("com.miui.powercenter.PowerSaveOnTime");
                    break;
                case R.id.power_center_consume_rank:
                    intent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
                    break;
                case R.id.battery_time:
                    intent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
                    break;
                case R.id.settings:
                    intent = new Intent(MiuiIntent.ACTION_POWER_SETTINGS);
                    break;
                case R.id.power_center_auto_shutdown:
                    intent = new Intent("com.miui.powercenter.PowerShutdownOnTime");
                    break;
                default:
                    break;
            }
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pc_power_center);
        AnalyticsUtil.track(this, AnalyticsUtil.TRACK_ID_ACTIVE_BATTERY);
        AnalyticsUtil.track(this, AnalyticsUtil.TRACK_ID_ACTIVE_MAIN);
        setupClick();
        init();
        PowerUtils.triggerPowerSaveService(this);
        Log.d(TAG, "=======================PDEBUG--center 开启 test3");
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUI();
    }

    @Override
    protected void onDestroy() {
        mBatteryInfo.unregisterOnBatteryInfoChanged(mBatteryInfoListener);
        mModeChangedReceiver.unregister(this);
        unregisterReceiver(mBatteryReceiver);
        mBatteryVolume.handleChargingAnimation(false, false);
        super.onDestroy();
    }

    private void init() {
        mForegroundView = findViewById(R.id.foreground);
        mBackgroundView = findViewById(R.id.background);
        mBatteryPercentView = (ScoreTextView) findViewById(R.id.number);

        mLabel1 = (TextView) findViewById(R.id.power_center_custom_label);
        mLabel2 = (TextView) findViewById(R.id.power_center_low_battery_label);
        mLabel3 = (TextView) findViewById(R.id.power_center_on_time_label);
        mLabel4 = (TextView) findViewById(R.id.power_center_auto_shutdown_label);
        mSummary1 = (TextView) findViewById(R.id.power_center_custom_summary);
        mSummary2 = (TextView) findViewById(R.id.power_center_low_battery_summary);
        mSummary3 = (TextView) findViewById(R.id.power_center_on_time_summary);
        mBatteryTime = (TextView) findViewById(R.id.battery_time);
        mBatteryVolume = (BatteryVolume) findViewById(R.id.battery_volume);
        mBatteryVolume.setFocusable(true);
        mBatteryVolume.setFocusableInTouchMode(true);
        mBatteryInfo = BatteryInfo.getInstance(this);
        mBatteryInfo.registerOnBatteryInfoChanged(mBatteryInfoListener);
        int status = mBatteryInfo.getBatteryState(this);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        refreshBatteryTime(isCharging);
        mBatteryVolume.handleChargingAnimation(isCharging, false);
        mDataManager = DataManager.getInstance(this.getApplicationContext());
        mModeChangedReceiver = new PowerModeChangedReceiver(this);
        mModeChangedReceiver.register(this);
        mModeChangedReceiver.addListener(mModeChangedListener);

        Intent intent = registerReceiver(mBatteryReceiver, new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED));
        updateBatteryStatus(intent);
    }

    private void refreshUI() {
        boolean isLowBattery = mDataManager.getBoolean(DataManager.KEY_LOW_BATTERY_ENABLED,
                DataManager.LOW_BATTERY_ENABLED_DEFAULT);
        int mode_id = mDataManager.getInt(DataManager.KEY_POWER_MODE_APPLIED, -1);
        if (mode_id<0) {mLabel1.setText(R.string.power_center_state_off);}
        else {
            PowerMode mode = PowerUtils.getModeById(this, mode_id);
            String userchoose = (String)mode.mDBValue[PowerMode.INDEX_NAME];
            mLabel1.setText(userchoose);
        }
        mLabel2.setText(isLowBattery ? R.string.power_center_state_on
                : R.string.power_center_state_off);
        boolean isOnTime = mDataManager.getBoolean(DataManager.KEY_ON_TIME_ENABLED,
                DataManager.ON_TIME_ENABLED_DEFAULT);
        mLabel3.setText(isOnTime ? R.string.power_center_state_on : R.string.power_center_state_off);

//        String Shutdown_time = getShutdownString();
        boolean isCheckBoot = mDataManager.getBoolean(POWER_ONTIME_SHUTDOWN+1, false);
        boolean isCheckShutdown = mDataManager.getBoolean(POWER_ONTIME_SHUTDOWN+2, false);
        int time1 = mDataManager.getInt(POWER_ONTIME_SHUTDOWN + 3, DataManager.BOOT_TIME_DEFAULT);
        int time2 = mDataManager.getInt(POWER_ONTIME_SHUTDOWN + 4, DataManager.SHUTDOWN_TIME_DEFAULT);
        int repeat1 = mDataManager.getInt(POWER_ONTIME_SHUTDOWN + 5, DataManager.BOOT_REPEAT_DEFAULT);
        int repeat2 = mDataManager.getInt(POWER_ONTIME_SHUTDOWN + 6, DataManager.SHUTDOWN_REPEAT_DEFAULT);
        if ((isCheckBoot && time1>0 && repeat1!=-1)|| (isCheckShutdown && time2>0 && repeat2!=-1)) {
            mLabel4.setText(R.string.power_center_state_on);
        } else {
            mLabel4.setText(R.string.power_center_state_off);
        }
        int modeId = mDataManager.getInt(DataManager.KEY_POWER_MODE_APPLIED,
                -1);

            mSummary1.setText(R.string.power_center_custom_summary_default);

        if (isLowBattery) {
            int percent = mDataManager.getInt(DataManager.KEY_LOW_BATTERY_PERCENTAGE,
                    DataManager.LOW_BATTERY_PERCENTAGE_DEFAULT);
            modeId = mDataManager.getInt(DataManager.KEY_LOW_BATTERY_SELECTED,
                    DataManager.LOW_BATTERY_SELECTED_DEFAULT);
            if (modeId >= 0) {
                String modeName = PowerUtils.getModeNameById(this, modeId);
                String value = String
                        .format(getString(R.string.power_center_low_battery_summary_dynamic),
                                percent, modeName);
                SpannableString ss = AndroidUtils.getHighLightString(value, sOrange, percent + "%");
                mSummary2.setText(ss);
            }
        } else {
            mSummary2.setText(R.string.power_center_low_battery_summary_default);
        }

        if (isOnTime) {
            Log.d(TAG, "PDEBUG--刷新ui， 并且onTime 模式是打开的");
            int startHour = mDataManager.getInt(DataManager.KEY_ON_TIME_START_HOUR,
                    DataManager.ON_TIME_START_HOUR_DEFAULT);
            int startMinute = mDataManager.getInt(DataManager.KEY_ON_TIME_START_MINUTE,
                    DataManager.ON_TIME_START_MINUTE_DEFAULT);
            int endHour = mDataManager.getInt(DataManager.KEY_ON_TIME_END_HOUR,
                    DataManager.ON_TIME_END_HOUR_DEFAULT);
            int endMinute = mDataManager.getInt(DataManager.KEY_ON_TIME_END_MINUTE,
                    DataManager.ON_TIME_END_MINUTE_DEFAULT);
            modeId = mDataManager.getInt(DataManager.KEY_ON_TIME_SELECTED,
                    DataManager.ON_TIME_SELECTED_DEFAULT);

            if (modeId >= 0) {
                String modeName = PowerUtils.getModeNameById(this, modeId);
                String startTime = PowerUtils.getFormatTime(startHour, startMinute);
                String endTime = PowerUtils.getFormatTime(endHour, endMinute);
                String value = String.format(
                        getString(R.string.power_center_on_time_summary_dynamic),
                        startTime, endTime, modeName);
                SpannableString ss = AndroidUtils.getHighLightString(value, sOrange, startTime,endTime);
                mSummary3.setText(ss);
            }
        } else {
            mSummary3.setText(R.string.power_center_on_time_summary_default);
        }
    }

    private String getShutdownString() {
        return null;
    }

    private void setupClick(int id) {
        View view = findViewById(id);
        view.setOnClickListener(mOnClickListener);
    }

    private void setupClick() {
        setupClick(R.id.power_center_custom);
        setupClick(R.id.power_center_low_battery);
        setupClick(R.id.power_center_on_time);
        setupClick(R.id.power_center_consume_rank);
        setupClick(R.id.back);
        setupClick(R.id.settings);
        setupClick(R.id.power_center_auto_shutdown);
        setupClick(R.id.battery_time);
    }

    private void refreshBatteryTime(boolean isCharging) {
        int stringId = isCharging ? R.string.power_center_charge_time
                : R.string.power_center_consume_time;
        String format = getString(stringId);
        long milliseconds = isCharging ? mBatteryInfo.getBatteryChargeTime() : mBatteryInfo
                .getBatteryStandbyTime();
        int percent = isCharging ? 100 - mBatteryInfo.getBatteryPercent(this) : mBatteryInfo
                .getBatteryPercent(this);
        if (percent == 0) {
            stringId = R.string.power_center_battery_full;
            mBatteryTime.setText(stringId);
            return;
        }
        milliseconds = milliseconds * percent / 100;
        String value = getTimeString(milliseconds);
        String time = String.format(format, value);
        Log.d(TAG, time);
        mBatteryTime.setText(time);
    }

    private String getTimeString(long milliseconds) {
        String value = "";
        int day = PowerUtils.calculateDay(milliseconds);
        int hour = PowerUtils.calculateHour(milliseconds);
        int minute = PowerUtils.calculateMinute(milliseconds);
        if (day > 0) {
            value += getResources().getQuantityString(R.plurals.power_center_battery_day, day, day);
        }
        if (hour > 0) {
            value += getResources().getQuantityString(R.plurals.power_center_battery_hour, hour,
                    hour);
        }
        if (minute > 0  && day == 0) {
            value += getResources().getQuantityString(R.plurals.power_center_battery_minute, minute,minute);
        }
        return value;
    }

    private BackgroundStatus mCurrentStatus = BackgroundStatus.GREEN;
    private View mToGoneView;
    private View mToVisiableView;

    private void updateForeground(BackgroundStatus status) {
        if (status != mCurrentStatus) {
            if (mForegroundView.getAlpha() == 1) {
                mToGoneView = mForegroundView;
                mToVisiableView = mBackgroundView;
            } else {
                mToVisiableView = mForegroundView;
                mToGoneView = mBackgroundView;
            }

            switch (status) {
                case GREEN:
                    mToVisiableView.setBackgroundResource(R.drawable.main_bg_green);
                    break;
                case RED:
                    mToVisiableView.setBackgroundResource(R.drawable.main_bg_orange);
                    break;
                default:
                    break;
            }

            mCurrentStatus = status;
            ViewAlphaAnimation anim = new ViewAlphaAnimation(mToVisiableView, mToGoneView);
            anim.setStartOffset(0);
            anim.setDuration(700);
            mToVisiableView.startAnimation(anim);
        }
    }
}
