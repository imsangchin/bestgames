
package com.miui.powercenter;

import miui.app.ActionBar;
import miui.app.Activity;
import miui.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;
import miui.widget.TimePicker;
import miui.widget.TimePicker.OnTimeChangedListener;

import com.miui.securitycenter.R;
import com.miui.analytics.AnalyticsUtil;
import com.miui.powercenter.provider.DataManager;
import com.miui.powercenter.provider.PowerData;
import com.miui.powercenter.provider.PowerModeDef;
import com.miui.powercenter.provider.PowerData.PowerMode;
import com.miui.powercenter.provider.PowerModeStateTransfer;
import com.miui.powercenter.provider.PowerUtils;
import com.miui.powercenter.view.PowerCenterEditorTitleView;

import miui.widget.NumberPicker;
import miui.widget.NumberPicker.OnValueChangeListener;
import miui.widget.SlidingButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class PowerSaveOnTime extends Activity {
    private static final String TAG = "PowerSaveOnTime";

    private View mSlidingButtonContainer;
    private SlidingButton mSlidingButton;
    private View mButton1;;
    private View mButton2;
    private TextView mTimeTitle1;
    private TextView mTimeTitle2;
    private TextView mTimeSummary1;
    private TextView mTimeSummary2;
    private TimePicker mTimePicker;

    // 我们进入按时模式时的模式
    private View mOptionView;
    private TextView mOptionLabel;

    // 这里是我们的按时达到后，我们的模式
    private View mOptionViewOut;
    private TextView mOptionLabelOut;

    private int mButtonSelected = 0;
    private DataManager mDataManager;
    private PowerMode[] mModeArray;

    private int mOnTimeEnterSelectedIndex;
    private int mOnTimeOutSelectedIndex;

    private int mOnTimeEnterSelectedIndexTemp;
    private int mOnTimeOutSelectedIndexTemp;

    private int mStartHour;
    private int mStartMinute;
    private int mEndHour;
    private int mEndMinute;
    private PowerModeStateTransfer mTransition;
    private boolean mInOnTimeMode = false;

    private int mOnTimeSelectedModeId = DataManager.ON_TIME_SELECTED_DEFAULT;
    private int mOnTimeRecoveryModeId = DataManager.ON_TIME_RECOVERY_DEFAULT;
    private boolean mOnTimeEnable = false;

    private View.OnClickListener mViewOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            View customView = null;
            AlertDialog.Builder builder = null;
            switch (id) {
                case R.id.sliding_button_container:
                    boolean isChecked = !mSlidingButton.isChecked();
                    mSlidingButton.setChecked(isChecked);
                    doIsChecked(isChecked);
                    AnalyticsUtil.track(PowerSaveOnTime.this,
                            AnalyticsUtil.TRACK_ID_BATTERY_CHANGE_TIMER_SAVE_MODE,
                            isChecked ? AnalyticsUtil.TRACK_VALUE_SWITCH_OPEN
                                    : AnalyticsUtil.TRACK_VALUE_SWITCH_CLOSE);
                    break;
                case R.id.button1:
                    mButtonSelected = 0;
                    selectTimeButton();
                    break;
                case R.id.button2:
                    mButtonSelected = 1;
                    selectTimeButton();
                    break;
                case R.id.option_view:
                    builder = new AlertDialog.Builder(PowerSaveOnTime.this);
                    customView = getDialogCustomView(PowerModeDef.ENTER_SELECT_MODE);
                    builder.setTitle(R.string.power_save_choose_mode);
                    builder.setView(customView);
                    builder.setPositiveButton(R.string.power_dialog_ok, mOnTimeEnterDialogListener);
                    builder.setNegativeButton(R.string.power_dialog_cancel,
                            mOnTimeEnterDialogListener);
                    builder.show();
                    break;
                case R.id.goout_option:
                    builder = new AlertDialog.Builder(PowerSaveOnTime.this);
                    customView = getDialogCustomView(PowerModeDef.GOOUT_SELECT_MODE);
                    builder.setTitle(R.string.power_save_choose_recovery_mode);
                    builder.setView(customView);
                    builder.setPositiveButton(R.string.power_dialog_ok, mOnTimeOutDialogListener);
                    builder.setNegativeButton(R.string.power_dialog_cancel,
                            mOnTimeOutDialogListener);
                    builder.show();
                    break;
                default:
                    break;
            }
        }
    };

    private DialogInterface.OnClickListener mOnTimeEnterDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {

                int oldModeId = mDataManager.getInt(DataManager.KEY_ON_TIME_SELECTED,
                        DataManager.ON_TIME_SELECTED_DEFAULT);

                mOnTimeEnterSelectedIndex = mOnTimeEnterSelectedIndexTemp;
                if (mModeArray != null && mOnTimeEnterSelectedIndex >= 0
                        && mOnTimeEnterSelectedIndex < mModeArray.length) {
                    String name = String
                            .valueOf(mModeArray[mOnTimeEnterSelectedIndex].mDBValue[PowerMode.INDEX_NAME]);
                    mOptionLabel.setText(name);
                }

                int modeId = Integer
                        .parseInt(String
                                .valueOf(mModeArray[mOnTimeEnterSelectedIndex].mDBValue[PowerMode.INDEX_ID]));
                int defaultCount = PowerData.getDefaultModeCount();
                if (mOnTimeEnterSelectedIndex >= defaultCount) {
                    modeId += defaultCount - 1;
                }

                if (oldModeId != modeId) {
                    mOnTimeSelectedModeId = modeId;
                }
            }
            dialog.dismiss();
        }
    };

    private DialogInterface.OnClickListener mOnTimeOutDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                int oldModeId = mDataManager.getInt(DataManager.KEY_ON_TIME_RECOVERY_SELECTED,
                        DataManager.ON_TIME_SELECTED_DEFAULT);

                mOnTimeOutSelectedIndex = mOnTimeOutSelectedIndexTemp;

                if (mModeArray != null && mOnTimeOutSelectedIndex >= 0
                        && mOnTimeOutSelectedIndex < mModeArray.length) {
                    String name = String
                            .valueOf(mModeArray[mOnTimeOutSelectedIndex].mDBValue[PowerMode.INDEX_NAME]);
                    mOptionLabelOut.setText(name);
                }

                int modeId = Integer.parseInt(String
                        .valueOf(mModeArray[mOnTimeOutSelectedIndex].mDBValue[PowerMode.INDEX_ID]));

                int defaultCount = PowerData.getDefaultModeCount();
                if (mOnTimeOutSelectedIndex >= defaultCount) {
                    modeId += defaultCount - 1;
                }

                if (oldModeId != modeId) {
                    // 如果改变了按时的模式，而且当前的状态在按时状态里面，那么我们需要使用新的按时模式
                    mOnTimeRecoveryModeId = modeId;
                }
            }
            dialog.dismiss();
        }
    };

    private OnTimeChangedListener mTimeChangedListener = new OnTimeChangedListener() {
        @Override
        public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
            String sTime = PowerUtils.getFormatTime(hourOfDay, minute);
            if (mButton1.isSelected()) {
                mTimeTitle1.setText(sTime);
                if (mStartHour != hourOfDay || mStartMinute != minute) {
                }

                mStartHour = hourOfDay;
                mStartMinute = minute;
            } else if (mButton2.isSelected()) {
                mTimeTitle2.setText(sTime);
                if (mEndHour != hourOfDay || minute != mEndMinute) {
                }
                mEndHour = hourOfDay;
                mEndMinute = minute;
            }
        }
    };

    private class ButtonClickListener implements View.OnClickListener {
        private Activity mActivity;

        public ButtonClickListener(Activity activity) {
            mActivity = activity;
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.cancel:
                    mActivity.finish();
                    break;
                case R.id.ok:
                    Log.d("LDEBUG", "click ok in save on time");
                    saveData();
                    mTransition.exitOnTimePage();
                    mActivity.finish();
                    break;
                default:
                    break;
            }
        }
    }

    private void setActionBar() {
        String activeModeTitle;
        activeModeTitle = this.getResources().getString(R.string.power_save_on_time_title);

        PowerCenterEditorTitleView titleView = (PowerCenterEditorTitleView)
                getLayoutInflater().inflate(R.layout.pc_editor_title_view, null);
        ButtonClickListener listener = new ButtonClickListener(this);

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pc_power_save_on_time);
        init();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void doIsChecked(boolean isChecked) {
        activateComponent(isChecked);
        mOnTimeEnable = isChecked;
    }

    private void init() {
        mSlidingButtonContainer = findViewById(R.id.sliding_button_container);
        mSlidingButton = (SlidingButton) findViewById(R.id.sliding_button);
        mButton1 = findViewById(R.id.button1);
        mButton2 = findViewById(R.id.button2);
        mTimeTitle1 = (TextView) mButton1.findViewById(R.id.title);
        mTimeTitle2 = (TextView) mButton2.findViewById(R.id.title);
        mTimeSummary1 = (TextView) mButton1.findViewById(R.id.summary);
        mTimeSummary2 = (TextView) mButton2.findViewById(R.id.summary);
        mTimePicker = (TimePicker) findViewById(R.id.time_picker);

        mOptionView = findViewById(R.id.option_view);
        mOptionViewOut = findViewById(R.id.goout_option);

        mOptionLabel = (TextView) mOptionView.findViewById(R.id.option_label);
        mOptionLabelOut = (TextView) mOptionViewOut.findViewById(R.id.option_label_1);

        mDataManager = DataManager.getInstance(this);
        mModeArray = PowerUtils.getAllAvailableModes(this);
        mTransition = PowerModeStateTransfer.getInstance(this);

        mSlidingButtonContainer.setOnClickListener(mViewOnClickListener);
        mSlidingButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                doIsChecked(isChecked);
            }
        });

        mOptionView.setOnClickListener(mViewOnClickListener);
        mOptionViewOut.setOnClickListener(mViewOnClickListener);

        mButton1.setOnClickListener(mViewOnClickListener);
        mButton2.setOnClickListener(mViewOnClickListener);

        mTimePicker.setIs24HourView(true);
        mTimePicker.setOnTimeChangedListener(mTimeChangedListener);

        initStandbyMode();
        setActionBar();
    }

    private void activateComponent(boolean isEnabled) {
        mButton1.setEnabled(isEnabled);
        mButton2.setEnabled(isEnabled);
        mTimePicker.setEnabled(isEnabled);
        mOptionView.setEnabled(isEnabled);
        mOptionViewOut.setEnabled(isEnabled);

        mButton1.setAlpha(isEnabled ? 1.0f : 0.7f);
        mButton2.setAlpha(isEnabled ? 1.0f : 0.7f);
        mTimePicker.setAlpha(isEnabled ? 1.0f : 0.7f);
        mOptionView.setAlpha(isEnabled ? 1.0f : 0.7f);
        mOptionViewOut.setAlpha(isEnabled ? 1.0f : 0.7f);

        if (isEnabled) {
            selectTimeButton();
        } else {
            mButton1.setSelected(false);
            mButton2.setSelected(false);
            mTimeTitle1.setTextColor(Color.BLACK);
            mTimeTitle2.setTextColor(Color.BLACK);
            mTimeSummary1.setTextColor(Color.BLACK);
            mTimeSummary2.setTextColor(Color.BLACK);

            String name = String
                    .valueOf(mModeArray[mOnTimeEnterSelectedIndex].mDBValue[PowerMode.INDEX_NAME]);
            mOptionLabel.setText(name);

            name = String
                    .valueOf(mModeArray[mOnTimeOutSelectedIndex].mDBValue[PowerMode.INDEX_NAME]);
            mOptionLabelOut.setText(name);
        }
    }

    private View getDialogCustomView(int mode) {
        NumberPicker picker = new NumberPicker(this);
        String[] nameArray = PowerUtils.getAllAvailableNames(mModeArray);
        picker.setDisplayedValues(nameArray);
        picker.setMinValue(0);
        picker.setMaxValue(nameArray.length - 1);
        mOnTimeEnterSelectedIndexTemp = mOnTimeEnterSelectedIndex;
        mOnTimeOutSelectedIndexTemp = mOnTimeOutSelectedIndex;

        int currentSelected = 0;
        if (mode == PowerModeDef.ENTER_SELECT_MODE) {
            currentSelected = mOnTimeEnterSelectedIndex;
        } else if (mode == PowerModeDef.GOOUT_SELECT_MODE) {
            currentSelected = mOnTimeOutSelectedIndex;
        }

        picker.setValue(currentSelected);
        picker.setWrapSelectorWheel(false);

        if (mode == PowerModeDef.ENTER_SELECT_MODE) {
            picker.setOnValueChangedListener(new OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    mOnTimeEnterSelectedIndexTemp = newVal;
                }
            });
        } else if (mode == PowerModeDef.GOOUT_SELECT_MODE) {
            picker.setOnValueChangedListener(new OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                    mOnTimeOutSelectedIndexTemp = newVal;
                }
            });
        }
        FrameLayout container = new FrameLayout(this);
        container.addView(picker, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        container.setPadding(1, 0, 1, 0);
        return container;
    }

    private void initStandbyMode() {
        mOnTimeEnable = mDataManager.getBoolean(DataManager.KEY_ON_TIME_ENABLED,
                DataManager.ON_TIME_ENABLED_DEFAULT);

        mSlidingButton.setChecked(mOnTimeEnable);

        mStartHour = mDataManager.getInt(DataManager.KEY_ON_TIME_START_HOUR,
                DataManager.ON_TIME_START_HOUR_DEFAULT);
        mStartMinute = mDataManager.getInt(DataManager.KEY_ON_TIME_START_MINUTE,
                DataManager.ON_TIME_START_MINUTE_DEFAULT);
        mTimeTitle1.setText(PowerUtils.getFormatTime(mStartHour, mStartMinute));

        mTimePicker.setOnTimeChangedListener(null);
        mTimePicker.setCurrentHour(mStartHour);
        mTimePicker.setCurrentMinute(mStartMinute);
        mTimePicker.setOnTimeChangedListener(mTimeChangedListener);

        mEndHour = mDataManager.getInt(DataManager.KEY_ON_TIME_END_HOUR,
                DataManager.ON_TIME_END_HOUR_DEFAULT);
        mEndMinute = mDataManager.getInt(DataManager.KEY_ON_TIME_END_MINUTE,
                DataManager.ON_TIME_END_MINUTE_DEFAULT);
        mTimeTitle2.setText(PowerUtils.getFormatTime(mEndHour, mEndMinute));

        int modeId = mDataManager.getInt(DataManager.KEY_ON_TIME_SELECTED,
                DataManager.ON_TIME_SELECTED_DEFAULT);

        int gooutModeId = mDataManager.getInt(DataManager.KEY_ON_TIME_RECOVERY_SELECTED,
                DataManager.ON_TIME_RECOVERY_DEFAULT);

        String name = "";
        if (modeId >= 0) {
            mOnTimeEnterSelectedIndexTemp = mOnTimeEnterSelectedIndex = getSelectedIndexByModeId(modeId);
            name = PowerUtils.getModeNameById(this, modeId);
            mOptionLabel.setText(name);
        }

        if (gooutModeId >= 0) {
            mOnTimeOutSelectedIndexTemp = mOnTimeOutSelectedIndex = getSelectedIndexByModeId(gooutModeId);
            name = PowerUtils.getModeNameById(this, gooutModeId);
            mOptionLabelOut.setText(name);
        }

        mOnTimeSelectedModeId = mDataManager.getInt(DataManager.KEY_ON_TIME_SELECTED,
                DataManager.ON_TIME_SELECTED_DEFAULT);
        mOnTimeRecoveryModeId = mDataManager.getInt(DataManager.KEY_ON_TIME_RECOVERY_SELECTED,
                DataManager.ON_TIME_RECOVERY_DEFAULT);
        activateComponent(mOnTimeEnable);
    }

    private int getSelectedIndexByModeId(int selectedId) {
        if (mModeArray == null) {
            mModeArray = PowerUtils.getAllAvailableModes(this);
        }

        for (int i = 0; i < mModeArray.length; ++i) {
            PowerMode mode = mModeArray[i];
            int modeId = Integer.parseInt(String.valueOf(mode.mDBValue[PowerMode.INDEX_ID]));
            if (i >= PowerData.getDefaultModeCount()) {
                modeId += PowerData.getDefaultModeCount() - 1;
            }
            if (selectedId == modeId) {
                return i;
            }
        }
        return DataManager.ON_TIME_SELECTED_DEFAULT;
    }

    private void saveData() {
        mDataManager.putInt(DataManager.KEY_ON_TIME_START_HOUR, mStartHour);
        mDataManager.putInt(DataManager.KEY_ON_TIME_START_MINUTE, mStartMinute);
        mDataManager.putInt(DataManager.KEY_ON_TIME_END_HOUR, mEndHour);
        mDataManager.putInt(DataManager.KEY_ON_TIME_END_MINUTE, mEndMinute);
        mDataManager.putInt(DataManager.KEY_ON_TIME_SELECTED, mOnTimeSelectedModeId);
        mDataManager.putInt(DataManager.KEY_ON_TIME_RECOVERY_SELECTED, mOnTimeRecoveryModeId);
        mDataManager.putBoolean(DataManager.KEY_ON_TIME_ENABLED, mOnTimeEnable);
        Log.d(TAG, "PONTIME--on time 数据. start hour: " + mStartHour + " start minute: "
                + mStartMinute +
                " endhour: " + mEndHour + " end minute: " + mEndMinute);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mTransition.isOnTimeStatusChanged(mOnTimeEnable, mOnTimeSelectedModeId,
                    mOnTimeRecoveryModeId,
                    mStartHour, mEndHour, mStartMinute, mEndMinute)) {
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
                        // 如果放弃，那么我们就退出
                        PowerSaveOnTime.this.finish();
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

    private void selectTimeButton() {
        mButton1.setSelected(mButtonSelected == 0 ? true : false);
        mButton2.setSelected(mButtonSelected == 1 ? true : false);

        mTimeTitle1.setTextColor(mButtonSelected == 0 ? Color.BLACK : Color.GRAY);
        mTimeTitle2.setTextColor(mButtonSelected == 1 ? Color.BLACK : Color.GRAY);
        mTimeSummary1.setTextColor(mButtonSelected == 0 ? Color.BLACK : Color.GRAY);
        mTimeSummary2.setTextColor(mButtonSelected == 1 ? Color.BLACK : Color.GRAY);

        mTimePicker.setOnTimeChangedListener(null);
        mTimePicker.setCurrentHour(mButtonSelected == 0 ? mStartHour : mEndHour);
        mTimePicker.setCurrentMinute(mButtonSelected == 0 ? mStartMinute : mEndMinute);
        mTimePicker.setOnTimeChangedListener(mTimeChangedListener);
    }
}
