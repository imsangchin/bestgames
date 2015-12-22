
package com.miui.powercenter;

import miui.app.ActionBar;
import miui.app.Activity;
import miui.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import miui.preference.PreferenceActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.miui.securitycenter.R;
import com.miui.analytics.AnalyticsUtil;
import com.miui.powercenter.provider.DataManager;
import com.miui.powercenter.provider.PowerData;
import com.miui.powercenter.provider.PowerModeDef;
import com.miui.powercenter.provider.SqlUtils;
import com.miui.powercenter.provider.PowerData.PowerMode;
//import com.miui.powercenter.provider.PowerModeStateTransition;
import com.miui.powercenter.provider.PowerModeStateTransfer;
import com.miui.powercenter.provider.PowerSaveService;
import com.miui.powercenter.provider.PowerUtils;
import com.miui.powercenter.view.PowerCenterEditorTitleView;

import miui.widget.NumberPicker;
import miui.widget.NumberPicker.OnValueChangeListener;;

/**
 * 在原先的代码逻辑上进行了重写， 由于需求在变， 这个代码可以继续重构， 增强 transition 的功能
 * 需要把状态重构一下
 *
 */
public class PowerSaveLowBattery extends PreferenceActivity implements SeekBarPreference.SeekBarListener{
    private static final String TAG = "PowerSaveLowBattery";

    private static final String KEY_AUTO_SAVE_ENABLED = "power_save_low_battery_auto_save";
    private static final String KEY_EXIT_WHEN_CHARGE  = "power_exit_lowbattery_whencharge";

    //选择进入低电的模式
    private static final String KEY_OPTION_PREFERENCE = "power_save_low_battery_option";

    //选择恢复后的模式
    private static final String KEY_OPTION_RECOVERY_PREFERENCE = "power_save_low_battery_option_recovery";

    private CheckBoxPreference mLowBattery;
    private CheckBoxPreference mExitLowBatteryWhenCharge;
    private OptionPreference mOptionPreference;
    private OptionPreference mRecoveryOptionPreference;
    private DataManager mDataManager;
    private PowerMode[] mModeArray;

    //这里是我们选择进入低电的状态
    private int mEnterSelectedIndex;

    //这里是我们选择跳出低电的状态
    private int mRecoverySelectedIndex;

    //这个是用在当我的picker 滑动的时候， 保存暂态的
    private int mTempEnterSelectedIndex;

    //这个是用在当picker 滑动的时候， 保存recovery 的暂态的
    private int mTempRecoverySelectedIndex;

    private PowerModeStateTransfer mTransition;

    private SeekBarPreference mSeekPreference;
    private int mProgress;

    private boolean mLowBatteryEnable         = false;
    private boolean mExitLowWhenSwitchEnable  = false;
    private int     mLowBatteryModeId         = DataManager.LOW_BATTERY_SELECTED_DEFAULT;
    private int     mLowBatteryRecoveryId     = DataManager.LOW_BATTERY_RECOVERY_DEFAULT;

    private DialogInterface.OnClickListener mEnterDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {

                int oldLowModeId = mDataManager.getInt(DataManager.KEY_LOW_BATTERY_SELECTED,
                        DataManager.LOW_BATTERY_SELECTED_DEFAULT);

                mEnterSelectedIndex = mTempEnterSelectedIndex;

                //关闭开关后，会成为-1，此时，如果直接确定，那么会outofbound
                if (mEnterSelectedIndex < 0 || mEnterSelectedIndex >= mModeArray.length)
                    mEnterSelectedIndex = DataManager.LOW_BATTERY_SELECTED_DEFAULT;

                if (mModeArray != null) {
                    String name = String
                            .valueOf(mModeArray[mEnterSelectedIndex].mDBValue[PowerMode.INDEX_NAME]);
                    mOptionPreference.setMiuiLabel(name);
                }

                int modeId = Integer.parseInt(String
                        .valueOf(mModeArray[mEnterSelectedIndex].mDBValue[PowerMode.INDEX_ID]));
                int defaultCount = PowerData.getDefaultModeCount();
                if (mEnterSelectedIndex >= defaultCount) {
                    modeId += defaultCount - 1;
                }

                //如果改变了低电的模式，那么我们使用新的模式
                if (modeId != oldLowModeId) {
                    mLowBatteryModeId = modeId;
                }
            }
            dialog.dismiss();
        }
    };

    private DialogInterface.OnClickListener mRecoveryDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {

                int oldRecoveryModeId = mDataManager.getInt(DataManager.KEY_LOW_BATTERY_RECOVERY_SELECTED,
                        DataManager.LOW_BATTERY_RECOVERY_DEFAULT);

                mRecoverySelectedIndex = mTempRecoverySelectedIndex;

                //关闭开关后，会成为-1，此时，如果直接确定，那么会outofbound
                if (mRecoverySelectedIndex < 0 || mRecoverySelectedIndex >= mModeArray.length)
                    mRecoverySelectedIndex = DataManager.LOW_BATTERY_RECOVERY_DEFAULT;

                if (mModeArray != null) {
                    String name = String
                            .valueOf(mModeArray[mRecoverySelectedIndex].mDBValue[PowerMode.INDEX_NAME]);
                    mRecoveryOptionPreference.setMiuiLabel(name);
                }

                int modeId = Integer.parseInt(String
                        .valueOf(mModeArray[mRecoverySelectedIndex].mDBValue[PowerMode.INDEX_ID]));

                int defaultCount = PowerData.getDefaultModeCount();

                if (mRecoverySelectedIndex >= defaultCount) {
                    modeId += defaultCount - 1;
                }

                if (oldRecoveryModeId != modeId) {
                    mLowBatteryRecoveryId = modeId;
                }
            }
            dialog.dismiss();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pc_power_save_low_battery);

        mSeekPreference = (SeekBarPreference) findPreference("power_save_low_battery_seekbar");
        mSeekPreference.addListener(this);

        init();
        setActionBar();
        mProgress = mDataManager.getInt(DataManager.KEY_LOW_BATTERY_PERCENTAGE,
                DataManager.LOW_BATTERY_PERCENTAGE_DEFAULT) - 10;
        Log.d("PDEBUG", "init value: " + mProgress);
        mLowBatteryEnable = mDataManager.getBoolean(DataManager.KEY_LOW_BATTERY_ENABLED, false);
    }

    private void setActionBar() {
        String activeModeTitle;
        activeModeTitle = this.getResources().getString(R.string.power_save_low_battery_title);

        PowerCenterEditorTitleView titleView = (PowerCenterEditorTitleView)
                getLayoutInflater().inflate(R.layout.pc_editor_title_view, null);
        ButtonClickListener listener = new ButtonClickListener();

        titleView.getOk().setText(android.R.string.ok);
        titleView.getOk().setOnClickListener(listener);
        titleView.getCancel().setOnClickListener(listener);
        titleView.getTitle().setText(activeModeTitle);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_TITLE
                            | ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_HOME_AS_UP);
            actionBar.setCustomView(titleView, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT));
        }
    }

    private class ButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.cancel:
                    finish();
                    break;
                case R.id.ok:
                    Log.d("LDEBUG",  "click ok in save low battery");
                    saveData();
                    mTransition.exitOrEnterLowBattery();
                    finish();
                    break;
                default:
                    break;
            }
        }
    }

    private void saveData() {
        mDataManager.putInt(DataManager.KEY_LOW_BATTERY_PERCENTAGE, mProgress + 10);
        mDataManager.putBoolean(DataManager.KEY_LOW_BATTERY_ENABLED, mLowBatteryEnable);

        if (!mLowBatteryEnable) {
            PowerUtils.setLowBatteryManually(mDataManager, false);
        }

        mDataManager.putBoolean(DataManager.KEY_EXIT_LOWBATTERY_WHENCHARGE, mExitLowWhenSwitchEnable);
        mDataManager.putInt(DataManager.KEY_LOW_BATTERY_SELECTED, mLowBatteryModeId);
        mDataManager.putInt(DataManager.KEY_LOW_BATTERY_RECOVERY_SELECTED, mLowBatteryRecoveryId);
    }

    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mTransition.isLowStatusChanged(mLowBatteryEnable, mLowBatteryModeId, mLowBatteryRecoveryId,
                    mProgress + 10, mExitLowWhenSwitchEnable)) {
                showChangeDialog();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void showChangeDialog() {
        DialogInterface.OnClickListener changeDialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //如果放弃，那么我们就退出
                        PowerSaveLowBattery.this.finish();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                    default:
                        break;
                }
                dialog.dismiss();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.power_customize_giveup_change);
        builder.setPositiveButton(R.string.power_dialog_ok, changeDialogListener);
        builder.setNegativeButton(R.string.power_dialog_cancel, changeDialogListener);
        builder.show();
    }

    @Override
    public void onSetCurProgress(int progress) {
        mProgress = progress;
        Log.d("LDEBUG", "set progress: " + mProgress);
    }

    @Override
    public void onProgressChanged(int progress) {
        mProgress = progress;
        Log.d("LDEBUG", "changed progress: " + mProgress);
    }

    @Override
    public int getCurrentProgress() {
        Log.d("LDEBUG", "get progress: " + mProgress);
        return mProgress;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void init() {
        mLowBattery = (CheckBoxPreference) findPreference(KEY_AUTO_SAVE_ENABLED);
        mExitLowBatteryWhenCharge = (CheckBoxPreference) findPreference(KEY_EXIT_WHEN_CHARGE);
        mOptionPreference = (OptionPreference) findPreference(KEY_OPTION_PREFERENCE);
        mRecoveryOptionPreference = (OptionPreference) findPreference(KEY_OPTION_RECOVERY_PREFERENCE);

        mDataManager = DataManager.getInstance(this);
        mModeArray = PowerUtils.getAllAvailableModes(this);
        mTransition = PowerModeStateTransfer.getInstance(this);

        mLowBattery.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean isChecked = mLowBattery.isChecked();

                if (isChecked) {
                    //这里返回的是当前正在使用的mode
                    mLowBatteryEnable = true;
                    refreshOptionPreference();
                }

                if (!isChecked) {
                    //关闭低电的时候， 我们把低电设置为默认，由于现在是联动的，所以可以不把applied id 设置成-1
                    mLowBatteryEnable = false;
                    mModeArray = PowerUtils.getAllAvailableModes(PowerSaveLowBattery.this);
                    Log.d(TAG, "PLOW--mModeArray length: " + mModeArray.length + " mSelectetIndex: "
                            + mEnterSelectedIndex);

                    refreshOptionPreference();
                }

                AnalyticsUtil.track(PowerSaveLowBattery.this,
                        AnalyticsUtil.TRACK_ID_BATTERY_CHANGE_POWER_LOW_SAVE_MODE,
                        isChecked ? AnalyticsUtil.TRACK_VALUE_SWITCH_OPEN
                                : AnalyticsUtil.TRACK_VALUE_SWITCH_CLOSE);
                return true;
            }
        });

        mExitLowBatteryWhenCharge.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean isChecked = mExitLowBatteryWhenCharge.isChecked();

                if (isChecked) {
                    mExitLowWhenSwitchEnable = true;
                } else {
                    mExitLowWhenSwitchEnable = false;
                }

                return true;
            }
        });

        mOptionPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PowerSaveLowBattery.this);
                View customView = getDialogCustomView(PowerModeDef.ENTER_SELECT_MODE);
                builder.setTitle(R.string.power_save_choose_mode);
                builder.setView(customView);
                builder.setPositiveButton(R.string.power_dialog_ok, mEnterDialogListener);
                builder.setNegativeButton(R.string.power_dialog_cancel, mEnterDialogListener);
                builder.show();
                return true;
            }
        });

        mRecoveryOptionPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PowerSaveLowBattery.this);
                View customView = getDialogCustomView(PowerModeDef.GOOUT_SELECT_MODE);
                builder.setTitle(R.string.power_save_choose_recovery_mode);
                builder.setView(customView);
                builder.setPositiveButton(R.string.power_dialog_ok, mRecoveryDialogListener);
                builder.setNegativeButton(R.string.power_dialog_cancel, mRecoveryDialogListener);
                builder.show();
                return true;
            }
        });

        initStandbyMode();
    }

    private View getDialogCustomView(int mode) {
        NumberPicker picker = new NumberPicker(this);
        String[] nameArray = PowerUtils.getAllAvailableNames(mModeArray);
        picker.setDisplayedValues(nameArray);
        picker.setMinValue(0);
        picker.setMaxValue(nameArray.length - 1);

        int tempSelectedIndex = 0;

        if (mode == PowerModeDef.ENTER_SELECT_MODE) {

            if (mEnterSelectedIndex < 0 || mEnterSelectedIndex >= nameArray.length)
                mEnterSelectedIndex =  DataManager.LOW_BATTERY_SELECTED_DEFAULT;

            mTempEnterSelectedIndex = mEnterSelectedIndex;
            tempSelectedIndex = mEnterSelectedIndex;

        } else if(mode == PowerModeDef.GOOUT_SELECT_MODE) {

            if (mRecoverySelectedIndex < 0 || mRecoverySelectedIndex >= nameArray.length)
                mRecoverySelectedIndex =  DataManager.LOW_BATTERY_SELECTED_DEFAULT;

            mTempRecoverySelectedIndex = mRecoverySelectedIndex;
            tempSelectedIndex = mRecoverySelectedIndex;

        }

        picker.setValue(tempSelectedIndex);
        picker.setWrapSelectorWheel(false);

        if (mode == PowerModeDef.ENTER_SELECT_MODE) {
            picker.setOnValueChangedListener(new OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                    mTempEnterSelectedIndex = newVal;
                }
            });
        } else if (mode == PowerModeDef.GOOUT_SELECT_MODE) {
            picker.setOnValueChangedListener(new OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                    mTempRecoverySelectedIndex = newVal;
                }
            });
        }

        FrameLayout container = new FrameLayout(this);
        container.addView(picker, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        container.setPadding(1, 0, 1, 0);
        return container;
    }

    private void initStandbyMode() {
        boolean isChecked = mDataManager.getBoolean(DataManager.KEY_LOW_BATTERY_ENABLED,
                DataManager.LOW_BATTERY_ENABLED_DEFAULT);
        boolean isExitWhenSwitch = mDataManager.getBoolean(DataManager.KEY_EXIT_LOWBATTERY_WHENCHARGE,
                false);

        mLowBattery.setChecked(isChecked);
        mExitLowBatteryWhenCharge.setChecked(isExitWhenSwitch);

        mLowBatteryModeId = mDataManager.getInt(DataManager.KEY_LOW_BATTERY_SELECTED,
                DataManager.LOW_BATTERY_SELECTED_DEFAULT);

        mLowBatteryRecoveryId = mDataManager.getInt(DataManager.KEY_LOW_BATTERY_RECOVERY_SELECTED,
                DataManager.LOW_BATTERY_RECOVERY_DEFAULT);

        mExitLowWhenSwitchEnable = mDataManager.getBoolean(DataManager.KEY_EXIT_LOWBATTERY_WHENCHARGE,false);

        refreshOptionPreference();
    }

    private void refreshOptionPreference() {
        int modeId = mDataManager.getInt(DataManager.KEY_LOW_BATTERY_SELECTED,
                DataManager.LOW_BATTERY_SELECTED_DEFAULT);

        int recoveryModeId = mDataManager.getInt(DataManager.KEY_LOW_BATTERY_RECOVERY_SELECTED,
                DataManager.LOW_BATTERY_RECOVERY_DEFAULT);

        if (modeId >= 0) {
            mEnterSelectedIndex = getSelectedIndexByModeId(modeId);
            String name = PowerUtils.getModeNameById(this, modeId);
            mOptionPreference.setMiuiLabel(name);
        }

        if (recoveryModeId >= 0) {
            mRecoverySelectedIndex = getSelectedIndexByModeId(recoveryModeId);
            String name = PowerUtils.getModeNameById(this, recoveryModeId);
            mRecoveryOptionPreference.setMiuiLabel(name);
        }
    }

    private int getSelectedIndexByModeId(int selectedId) {
        if (mModeArray == null) {
            mModeArray = PowerUtils.getAllAvailableModes(this);
        }

        //这里，我们获得的 mModeArray 可能是从 default 里面得到的，也可能是从content provider 里面得到的
        //从content provider 里面得到的 modeId， 是从1 开始的。 所以需要mode id 进行变换
        for (int i = 0; i < mModeArray.length; ++i) {
            PowerMode mode = mModeArray[i];
            int modeId = Integer.parseInt(String.valueOf(mode.mDBValue[PowerMode.INDEX_ID]));
            if(i >= PowerData.getDefaultModeCount()) {
                modeId +=  PowerData.getDefaultModeCount() - 1;
            }

            if(selectedId == modeId) {
                return i;
            }
        }
        return DataManager.LOW_BATTERY_SELECTED_DEFAULT;
    }

}
