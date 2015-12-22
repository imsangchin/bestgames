
package com.miui.powercenter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.miui.securitycenter.R;

import miui.widget.SlidingButton;

/***
 * 1.如何注销观察者？ 2.restore old state? 3.onSaveInstanceState?
 * 4.onRestoreInstanceState
 */
public class BrightnessView extends LinearLayout {
    private static final String TAG = "ScreenBrightnessView";

    private SeekBar mSeekBar;
    private SlidingButton mButton;
    private TextView mSummaryText;

    // brightness 的值都是以seekbar 的百分比换算的整数来计算的，而在设置的时候，也应该以 seekbar 的百分比换算的整数进行设置
    private int mOldMode;
    private int mOldBrightness;

    private int mNewMode;
    private int mNewBrightness;

    private boolean mAutomaticAvailable;

    // Backlight range is from 0 - 255. Need to make sure that user
    // doesn't set the backlight to 0 and get stuck
    private int mScreenBrightnessDim;
    private static final int MAXIMUM_BACKLIGHT = android.os.PowerManager.BRIGHTNESS_ON;

    private static final int SEEK_BAR_RANGE = 100;

    private ContentObserver mBrightnessObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            mSeekBar.setProgress(getSystemBrightness());
        }
    };

    private ContentObserver mBrightnessModeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            boolean checked = getSystemBrightnessMode()
                    == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
            mButton.setChecked(checked);
            mSeekBar.setProgress(getSystemBrightness());
        }
    };

    private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Log.d(TAG, "progress: " + progress);
            setSystemBrightness(progress, false);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            setSystemBrightness(mSeekBar.getProgress(), false);
            mNewBrightness = mSeekBar.getProgress();
            Log.d(TAG, "stop progress: " + mNewBrightness);
        }
    }

    private class CheckBoxListener implements CheckBox.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int newMode = isChecked ? Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                    : Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
            int brightNess = getSystemBrightness();

            setSystemBrightnessMode(newMode);
            mSeekBar.setProgress(brightNess);
            setSystemBrightness(mSeekBar.getProgress(), false);

            mNewMode = newMode;
            if (mNewMode == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) {
                mNewBrightness = mSeekBar.getProgress();
                Log.d(TAG, "切换到了人工模式， 当前的进度: " + mNewBrightness);
            }

            mSummaryText.setText(isChecked ?
                    R.string.brightness_auto_adjust_on :
                    R.string.brightness_auto_adjust_off);
        }
    }

    public BrightnessView(Context context) {
        super(context);
    }

    public BrightnessView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BrightnessView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public int getSystemBrightnessMode() {
        int mode = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
        try {
            mode = Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (SettingNotFoundException snfe) {
        }
        return mode;
    }

    public void setSystemBrightnessMode(int mode) {
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
    }

    private int getSystemBrightness() {
        int mode = getSystemBrightnessMode();
        float brightness = 0;
        if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            brightness = Settings.System.getFloat(mContext.getContentResolver(),
                    Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, 0);
            brightness = (brightness + 1) / 2;
        } else {
            brightness = Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, 100);
            brightness = (brightness - mScreenBrightnessDim)
                    / (MAXIMUM_BACKLIGHT - mScreenBrightnessDim);
        }
        return (int) (brightness * SEEK_BAR_RANGE);
    }

    private void setSystemBrightness(int brightness, boolean write) {
        int mode = getSystemBrightnessMode();
        if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            float valf = (((float) brightness * 2) / SEEK_BAR_RANGE) - 1.0f;
            IPowerManager power = IPowerManager.Stub.asInterface(ServiceManager
                    .getService("power"));
            if (power != null) {
                PowerAdapterUtils.setTemporaryScreenAutoBrightnessAdjustmentSettingOverride(power,
                        valf);
            }
            if (write) {
                final ContentResolver resolver = mContext.getContentResolver();
                Settings.System.putFloat(resolver,
                        Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ, valf);
            }
        } else {
            int range = (MAXIMUM_BACKLIGHT - mScreenBrightnessDim);
            brightness = (int) ((brightness * range) * 1.0 / SEEK_BAR_RANGE + 0.5 + mScreenBrightnessDim);
            PowerManager powerManager = (PowerManager) getContext().getSystemService(
                    Context.POWER_SERVICE);
            if (powerManager != null) {
                powerManager.setBacklightBrightness(brightness);
            }
            if (write) {
                final ContentResolver resolver = mContext.getContentResolver();
                Settings.System.putInt(resolver,
                        Settings.System.SCREEN_BRIGHTNESS, brightness);
            }
        }
    }

    public boolean isChecked() {
        return mButton.isChecked();
    }

    public int getProgress() {
        Log.d(TAG, "SeekBar grogress: " + mSeekBar.getProgress());
        return mSeekBar.getProgress();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {
        mAutomaticAvailable = getResources().getBoolean(
                android.miui.R.bool.android_config_automatic_brightness_available);
        mScreenBrightnessDim = getResources().getInteger(
                android.miui.R.integer.android_config_screenBrightnessDim);

        initUI();

        mContext.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_AUTO_BRIGHTNESS_ADJ), true,
                mBrightnessObserver);

        mContext.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS), true,
                mBrightnessObserver);

        mContext.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE), true,
                mBrightnessModeObserver);
    }

    private void initUI() {
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        mSeekBar.setMax(SEEK_BAR_RANGE);

        mOldMode = mNewMode = getSystemBrightnessMode();
        mOldBrightness = mNewBrightness = getSystemBrightness();

        Log.d(TAG, "初始化的值 mode: " + mOldMode + " 值: " + mOldBrightness);

        mSeekBar.setProgress(mOldBrightness);

        mButton = (SlidingButton) findViewById(R.id.automatic_mode);
        mSummaryText = (TextView) findViewById(R.id.adjust_summary);

        if (mAutomaticAvailable) {
            mButton.setOnCheckedChangeListener(new CheckBoxListener());
            mButton.setChecked(mOldMode != 0);
        } else {
            mButton.setEnabled(false);
        }
        mSeekBar.setOnSeekBarChangeListener(new SeekBarListener());
    }

    public void recoveryOld(boolean useOld) {
        if (useOld) {
            Log.d(TAG, "恢复到旧的模式, mOldMode:  " + mOldMode + " mOldBrightness: " + mOldBrightness);
            setSystemBrightnessMode(mOldMode);
            setSystemBrightness(mOldBrightness, true);
        } else {
            Log.d(TAG, "恢复到新的模式, mNewMode:  " + mNewMode + " mNewBrightness: " + mNewBrightness);
            setSystemBrightnessMode(mNewMode);
            setSystemBrightness(mNewBrightness, true);
        }
    }

}
