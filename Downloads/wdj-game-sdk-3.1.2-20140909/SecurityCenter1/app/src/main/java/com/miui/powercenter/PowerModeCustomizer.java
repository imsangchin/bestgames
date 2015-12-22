
package com.miui.powercenter;

import miui.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import miui.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import com.miui.securitycenter.R;
import com.miui.powercenter.provider.DataManager;
import com.miui.powercenter.provider.PowerData;
import com.miui.powercenter.provider.PowerData.PowerMode;
import com.miui.powercenter.provider.PowerUtils;
import com.miui.powercenter.provider.SqlUtils;
import com.miui.powercenter.view.PowerCenterEditorTitleView;


public class PowerModeCustomizer extends PreferenceActivity {
    private static final String TAG = "PowerModeCustomizer";

    private static final int FIELD_START = PowerData.getFiledStartIndex();

    static final int MODE_TYPE_DEFAULT = 0;
    static final int MODE_TYPE_ADD = 1;
    static final int MODE_TYPE_CUSTOM = 2;

    private PowerMode mActiveMode;
    private PowerMode mOldMode;
    private int mModeType;
    private int mModeID;
    private int mModeId_using;
    private EditText mEditor;

    private Preference.OnPreferenceClickListener mPreferenceClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pc_power_mode_customizer);

        init();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void init() {
        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        int modeId = b.getInt(PowerMode.KEY_POWER_MODE_ID);
        mModeId_using = b.getInt(PowerMode.KEY_POWER_MODE_ID_USING);
        mModeID = modeId;
        mModeType = MODE_TYPE_DEFAULT;

        if (modeId == -1) {
            mModeType = MODE_TYPE_ADD;
        } else if(modeId >= PowerData.getDefaultModeCount()) {
            mModeType = MODE_TYPE_CUSTOM;
        }

        if (modeId != -1) {
            mActiveMode = PowerUtils.getModeById(this, modeId);
            mOldMode = PowerUtils.getModeById(this, modeId);
        } else {
            mActiveMode = new PowerMode();
            mActiveMode.retrieve(this);
            mActiveMode.mDBValue[2] = this.getResources().getString(R.string.power_chooser_user_define_summary);
            mOldMode = null;
        }

        if (mActiveMode == null) {
            Log.e(TAG, "error: cannot get active mode from DB.");
            return;
        }

        translatePowerModeToPreference();
        setActionBar(modeId);
    }

    private void translatePowerModeToPreference() {
        PreferenceScreen parent = getPreferenceScreen();
        if (parent == null) {
            return;
        }
        parent.removeAll();

        boolean isEditable = mModeType == MODE_TYPE_ADD || mModeType == MODE_TYPE_CUSTOM;
//        addTitlePreference(parent, isEditable);

        PreferenceCategory category = new PreferenceCategory(this);
        category.setTitle(R.string.activity_title_settings);
        parent.addPreference(category);

        if (mModeType == MODE_TYPE_ADD) {
            mActiveMode.retrieve(this);
        }

        if (isEditable) {
            mPreferenceClickListener = new PreferenceClickListener(mActiveMode);
        }

        Preference[] array = new Preference[PowerMode.size() - FIELD_START];

        if (isEditable) {
            for (int i = 1; i < 4; ++i) {
                array[i] = getOptionPreference(isEditable, FIELD_START + i);
            }
            if ((Integer)mActiveMode.mDBValue[4]>=0) {
                array[4] = getOptionPreference(isEditable, FIELD_START + 4);
            } else {
                array[4] = null;
            }
            for (int i = 5; i < 8; ++i) {
                array[i] = getCheckBoxPreference(isEditable, FIELD_START + i);
            }
            array[8] = getOptionPreference(isEditable, FIELD_START + 8);
            for (int i = 9; i < 14; ++i) {
                array[i] = getCheckBoxPreference(isEditable, FIELD_START + i);
            }
        } else {
            for (int i = 1; i < array.length; ++i) {
                array[i] = getOptionPreference(isEditable, FIELD_START + i);
            }
        }

        for (int i = 1; i < array.length; ++i) {
            Preference pref = array[i];
            if (pref != null) category.addPreference(pref);
        }

        setTitleView(isEditable, FIELD_START);
    }

    private void setActionBar(int modeId) {
        if (mModeType == MODE_TYPE_DEFAULT) {
            getActionBar().setTitle(PowerUtils.getModeNameById(this, modeId));
            return;
        }

        String activeModeTitle;
        if (modeId != -1) {
            activeModeTitle = String
                    .valueOf(mActiveMode.mDBValue[PowerMode.INDEX_TITLE]);
        } else {
            activeModeTitle = this.getResources().getString(
                    R.string.power_chooser_user_define_title);
        }

        PowerCenterEditorTitleView titleView = (PowerCenterEditorTitleView)
                getLayoutInflater().inflate(R.layout.pc_editor_title_view, null);

        ButtonClickListener listener = new ButtonClickListener(this, mActiveMode, mModeType);
        titleView.getOk().setText(android.R.string.ok);
        if (mModeType == MODE_TYPE_CUSTOM) {
            PowerCenterDeleteButton button = (PowerCenterDeleteButton)getLayoutInflater().inflate(R.layout.pc_power_deletebutton, null);
            button.setButtonListener(listener);
            setListFooter(button);
        }

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

    private void setTitleView(boolean isEditable, int fieldId) {
        if (!isEditable) {
            return;
        }

        View headView = View.inflate(this, R.layout.pc_label_editor, null);
        getListView().addHeaderView(headView);
        mEditor = (EditText) headView.findViewById(R.id.label);

        if (mModeType == MODE_TYPE_ADD) {
            String name = PowerUtils.getAvailableUserDefineName(this);
            mEditor.setHint(name);
            mActiveMode.mDBValue[PowerMode.INDEX_TITLE] = name;
            mActiveMode.mDBValue[PowerMode.INDEX_NAME] = name;
        } else if (mModeType == MODE_TYPE_CUSTOM) {
            mEditor.setText(String.valueOf(mActiveMode.mDBValue[PowerMode.INDEX_NAME]));
        }

        if (mEditor != null) {
            mEditor.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    int editStart = mEditor.getSelectionStart();
                    int editEnd = mEditor.getSelectionEnd();


                    mEditor.removeTextChangedListener(this);

                    while (PowerUtils.getStringLength(s.toString()) > 20) {
                        s.delete(editStart - 1, editEnd);
                        editStart--;
                        editEnd--;
                    }

                    mEditor.setText(s);
                    mEditor.setSelection(editStart);

                    mEditor.addTextChangedListener(this);

                    String modeName = s.toString();
                    mActiveMode.mDBValue[PowerMode.INDEX_TITLE] = modeName;
                    mActiveMode.mDBValue[PowerMode.INDEX_NAME] = modeName;
                }

            });
        }
    }

    private void addTitlePreference(PreferenceScreen parent, boolean isEditable) {
        if (!isEditable) {
            return;
        }

        TitlePreference prefer = new TitlePreference(this, mActiveMode, mModeType);
        parent.addPreference(prefer);
    }

    private Preference getOptionPreference(boolean isEditable, int fieldId) {
        OptionPreference op = new OptionPreference(this);
        op.setLayoutResource(R.layout.pc_option_preference);
        op.setKey(PowerMode.PreferenceKey[fieldId]);

        op.setTitle(PowerMode.PreferenceTitleId[fieldId]);
        op.setMiuiLabel(StringMatcher.value2String(this, fieldId,
                Integer.parseInt(String.valueOf(mActiveMode.mDBValue[fieldId]))));
        op.setMiuiClickable(isEditable);

        if (isEditable) {
            op.setOnPreferenceClickListener(mPreferenceClickListener);
        }
        return op;
    }


    private Preference getCheckBoxPreference(boolean isEditable, int fieldId) {
        CheckBoxPreference cp = new CheckBoxPreference(this);
        cp.setKey(PowerMode.PreferenceKey[fieldId]);
        cp.setTitle(getString(PowerMode.PreferenceTitleId[fieldId]));
        int state = Integer.parseInt(String.valueOf(mActiveMode.mDBValue[fieldId]));
        if (state == 0) {
            cp.setChecked(false);
        } else {
            cp.setChecked(true);
        }
        cp.setPersistent(false);
        cp.setOnPreferenceClickListener(mPreferenceClickListener);
        return cp;
    }

    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mOldMode == null) return super.onKeyDown(keyCode,event);
            else {
                //如果是customize 模式， 有修改，那么我们需要弹出对话框
                if (!PowerMode.isModeEqual(mOldMode, mActiveMode)) {
                    showChangeDialog();
                }
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
                        PowerModeCustomizer.this.finish();
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

    private class ButtonClickListener implements View.OnClickListener {
        private Activity mActivity;
        private int mCustomModeId;
        private boolean isLowBatteryMode = false;
        private boolean isOnTimeMode = false;
        private boolean isLowBatteryOutMode = false;
        private boolean isOnTimeOutMode = false;
        private boolean isMyMode = false;
        private int mModeType;

        private DialogInterface.OnClickListener mDialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        SqlUtils.deleteModeById(mActivity, mCustomModeId);
                        mActivity.finish();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                    default:
                        break;
                }
                dialog.dismiss();
            }
        };

        public ButtonClickListener(Activity activity, PowerMode customMode, int modeType) {
            mActivity = activity;
            mCustomModeId = Integer.parseInt(String
                    .valueOf(customMode.mDBValue[PowerMode.INDEX_ID]));
            mModeType = modeType;
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.button_delete:
                    showDeleteHintDialog();
                    break;
                case R.id.cancel:
                    mActivity.finish();
                    break;
                case R.id.ok:
                    if (mModeType == MODE_TYPE_ADD) {
                        SqlUtils.insertMode(mActivity, mActiveMode);
                    } else if (mModeType == MODE_TYPE_CUSTOM) {
                        SqlUtils.updateMode(mActivity, mActiveMode);
                    }
                    mActivity.finish();
                    break;
                default:
                    mActivity.finish();
                    break;
            }
        }

        private boolean initModeDeletable() {
            int defaultCount = PowerData.getDefaultModeCount();
            DataManager manager = DataManager.getInstance(mActivity.getApplicationContext());

            int myModeid = mModeID ;
            int modeid = mModeId_using;
            isMyMode = (myModeid == modeid);

            int lowBatteryId = manager.getInt(DataManager.KEY_LOW_BATTERY_SELECTED,
                    DataManager.LOW_BATTERY_SELECTED_DEFAULT);
            lowBatteryId -= defaultCount - 1;
            isLowBatteryMode = (mCustomModeId == lowBatteryId);

            int lowBatteryOutId = manager.getInt(DataManager.KEY_LOW_BATTERY_RECOVERY_SELECTED,
                    DataManager.LOW_BATTERY_SELECTED_DEFAULT);
            lowBatteryOutId -= defaultCount -1;
            isLowBatteryOutMode = (lowBatteryOutId == mCustomModeId);

            int onTimeId = manager.getInt(DataManager.KEY_ON_TIME_SELECTED,
                    DataManager.ON_TIME_SELECTED_DEFAULT);
            onTimeId -= defaultCount - 1;
            isOnTimeMode = (mCustomModeId == onTimeId);

            int onTimeOutId = manager.getInt(DataManager.KEY_ON_TIME_RECOVERY_SELECTED,
                    DataManager.ON_TIME_SELECTED_DEFAULT);

            onTimeOutId -= defaultCount -1;
            isOnTimeOutMode = (mCustomModeId == onTimeOutId);

            return !isMyMode&&!isLowBatteryMode && !isOnTimeMode && !isLowBatteryOutMode && !isOnTimeOutMode;
        }

        private void showDeleteHintDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(R.string.power_dialog_delete_dialog_title);
            initModeDeletable();

            int messageId = R.string.power_dialog_delete_dialog_delete;
            if (isMyMode) {
                messageId = R.string.power_dialog_delete_dialog_cannot_delete_is_my_mode;
                builder.setMessage(messageId);
                builder.setNegativeButton(R.string.power_dialog_ok, mDialogClickListener);
            }
             else if (isLowBatteryMode) {
                messageId = R.string.power_dialog_delete_dialog_cannot_delete_low_battery;
                builder.setMessage(messageId);
                builder.setNegativeButton(R.string.power_dialog_ok, mDialogClickListener);
            } else if (isOnTimeMode) {
                messageId = R.string.power_dialog_delete_dialog_cannot_delete_on_time;
                builder.setMessage(messageId);
                builder.setNegativeButton(R.string.power_dialog_ok, mDialogClickListener);
            } else if (isOnTimeOutMode) {
                messageId = R.string.power_dialog_delete_dialog_cannot_delete_on_out_time;
                builder.setMessage(messageId);
                builder.setNegativeButton(R.string.power_dialog_ok, mDialogClickListener);
            } else if (isLowBatteryOutMode) {
                messageId = R.string.power_dialog_delete_dialog_cannot_delete_low_out_battery;
                builder.setMessage(messageId);
                builder.setNegativeButton(R.string.power_dialog_ok, mDialogClickListener);
            } else {
                builder.setMessage(messageId);
                builder.setPositiveButton(R.string.power_dialog_ok, mDialogClickListener);
                builder.setNegativeButton(R.string.power_dialog_cancel, mDialogClickListener);
            }
            builder.show();
        }
    }
}
