
package com.miui.powercenter;

import com.miui.powercenter.provider.DataManager;
import com.miui.powercenter.provider.PowerModeStateTransfer;
import com.miui.powercenter.provider.ShutdownAlarmIntentService;
import com.miui.powercenter.view.PowerCenterEditorTitleView;
import com.miui.securitycenter.R;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import java.util.Calendar;
import android.os.IBinder;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import miui.app.ActionBar;
import miui.app.AlertDialog;
import miui.preference.PreferenceActivity;
import miui.security.SecurityManager;
import miui.widget.TimePicker;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import miui.app.TimePickerDialog;
import com.miui.powercenter.provider.BootAlarmIntentService;
import com.miui.powercenter.provider.ShutdownAlarmIntentService;

public class PowerShutdownOnTime extends PreferenceActivity {

    private DataManager mDataManager;
    private PreferenceActivity mActivity;
    private CheckBoxPreference checkboot;
    private LabelPreference boot_time;
    private RepeatPreference boot_repeat;
    private CheckBoxPreference checkshutdown;
    private LabelPreference shutdown_time;
    private RepeatPreference shutdown_repeat;
    private TimePickerDialog tpd;
    private boolean button1;
    private boolean button2;
    private int time1;
    private int time2;
    private int repeat1;
    private int repeat2;
    private int min;
    private int hour;
    public final static String POWER_ONTIME_SHUTDOWN = "power_ontime_shutdown";
    private PowerModeStateTransfer mTransition;
    private boolean time;
    private final static String SHUTDOWN_TIME = "shutdown_time";
    private final static String SHUTDOWN_REPEAT = "shutdown_repeat";
    private final static String SHUTDOWN_CHECK = "shutdown_check";


    private class ButtonClickListener implements View.OnClickListener {
        public ButtonClickListener(PreferenceActivity context) {
            mActivity = context;
        }

        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.cancel:
                    mActivity.finish();
                    break;
                case R.id.ok:
                    saveData();
                    shutOnDown();
                    mTransition.exitOrEnterShutdownBoot();
                    mActivity.finish();
                    break;
                default:
                    break;
            }
        }
    }

    private void shutOnDown() {
        if (time1 > 0) {
            Intent intent = new Intent(BootAlarmIntentService.ACTION_SET_BOOTTIME);
            startService(intent);

        }
        if (time2 > 0) {
            Intent intent = new Intent(ShutdownAlarmIntentService.ACTION_SET_SHUTDOWN_ALARM);
            startService(intent);
        }
    }

    private void setActionBar() {
        String activeModeTitle;
        activeModeTitle = this.getResources().getString(R.string.power_center_auto_shutdown);

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

    public void saveData() {
        mDataManager.putBoolean(POWER_ONTIME_SHUTDOWN + 1, checkboot.isChecked());
        mDataManager.putBoolean(POWER_ONTIME_SHUTDOWN + 2, checkshutdown.isChecked());
        mDataManager.putInt(POWER_ONTIME_SHUTDOWN + 3, time1);
        mDataManager.putInt(POWER_ONTIME_SHUTDOWN + 4, time2);
        repeat1 = (boot_repeat.getSummary() != null) ? boot_repeat.getDaysOfWeek().getCoded() : DataManager.BOOT_REPEAT_DEFAULT;
        repeat2 = (shutdown_repeat.getSummary() != null) ? shutdown_repeat.getDaysOfWeek()
                .getCoded() : DataManager.SHUTDOWN_REPEAT_DEFAULT;
        mDataManager.putInt(POWER_ONTIME_SHUTDOWN + 5, repeat1);
        mDataManager.putInt(POWER_ONTIME_SHUTDOWN + 6, repeat2);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //如果直接返回，用户设置的重复值没有保存在repeat中
            repeat1 = (boot_repeat.getSummary() != null) ? boot_repeat.getDaysOfWeek().getCoded() : DataManager.BOOT_REPEAT_DEFAULT;
            repeat2 = (shutdown_repeat.getSummary() != null) ? shutdown_repeat.getDaysOfWeek()
                    .getCoded() :DataManager.SHUTDOWN_REPEAT_DEFAULT;
            if (mTransition.isShutdownOnStatusChanged(checkboot.isChecked(),checkshutdown.isChecked(),time1,time2,repeat1,repeat2)) {
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
                        PowerShutdownOnTime.this.finish();
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
    public TimePickerDialog.OnTimeSetListener otListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            if (!time) {
                time2 = hourOfDay * 60 + minute;
                shutdown_time.setLabel(timeTostring(time2));
            }
            else {
                time1 = hourOfDay * 60 + minute;
                boot_time.setLabel(timeTostring(time1));
            }
        }
    };

    public OnPreferenceClickListener click = new OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference preference) {
            if (preference == shutdown_time) {
                time = false;
                if (time2 > 0) {
                    tpd.updateTime(time2 / 60, time2 % 60);
                } else {
                    tpd.updateTime(0, 0);
                }
                tpd.show();
            }
            else {
                if (preference == boot_time) {
                    time = true;
                    if (time1 > 0) {
                        tpd.updateTime(time1 / 60, time1 % 60);
                    } else {
                        tpd.updateTime(0, 0);
                    }
                    tpd.show();
                }
            }
            return false;
        }
    };

    private void findview() {
        shutdown_time = (LabelPreference) findPreference("time_shutdown");
        boot_time = (LabelPreference) findPreference("time_boot");
        shutdown_time.setOnPreferenceClickListener(click);
        boot_time.setOnPreferenceClickListener(click);
        checkboot = (CheckBoxPreference) findPreference("button_boot");
        checkshutdown = (CheckBoxPreference) findPreference("button_shutdown");
        boot_repeat = (RepeatPreference) findPreference("repeat_boot");
        shutdown_repeat = (RepeatPreference) findPreference("repeat_shutdown");
    }

    private void notifyUI() {
        checkboot.setChecked(button1);
        checkshutdown.setChecked(button2);

        boot_time.setLabel(timeTostring(time1));
        shutdown_time.setLabel(timeTostring(time2));

         Alarm.DaysOfWeek dof1 = new Alarm.DaysOfWeek(repeat1);
         String repeat_summary1 = dof1.toString(this, true);
         boot_repeat.setLabel(repeat_summary1);
         boot_repeat.setDaysOfWeek(dof1);

            Alarm.DaysOfWeek dof2 = new Alarm.DaysOfWeek(repeat2);
            String repeat_summary2 = dof2.toString(this, true);
            shutdown_repeat.setLabel(repeat_summary2);
            shutdown_repeat.setDaysOfWeek(dof2);
    }

    private String timeTostring(int time) {
        return (time / 60) + ":" + minutes(time % 60);
    }

    private String minutes(int m) {
        return (m < 10) ? "0" + m : m + "";
    }

    private void init() {
        button1 = mDataManager.getBoolean(POWER_ONTIME_SHUTDOWN + 1, false);
        button2 = mDataManager.getBoolean(POWER_ONTIME_SHUTDOWN + 2, false);
        time1 = mDataManager.getInt(POWER_ONTIME_SHUTDOWN + 3, DataManager.BOOT_TIME_DEFAULT);
        time2 = mDataManager.getInt(POWER_ONTIME_SHUTDOWN + 4,DataManager.SHUTDOWN_TIME_DEFAULT );
        repeat1 = mDataManager.getInt(POWER_ONTIME_SHUTDOWN + 5, DataManager.BOOT_REPEAT_DEFAULT);
        repeat2 = mDataManager.getInt(POWER_ONTIME_SHUTDOWN + 6, DataManager.SHUTDOWN_REPEAT_DEFAULT);
        Log.d("REPEAT","time2="+time2);
        Log.d("REPEAT","repeat2="+repeat2);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.pc_power_shutdown_on_time);
        mDataManager = DataManager.getInstance(this);
        mTransition = PowerModeStateTransfer.getInstance(this);
        setActionBar();
        tpd = new TimePickerDialog(this, otListener, hour, min, true);
        findview();
        init();
        notifyUI();
    }

}
