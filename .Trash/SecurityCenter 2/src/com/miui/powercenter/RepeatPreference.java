package com.miui.powercenter;

import android.app.AlertDialog.Builder;
import com.miui.securitycenter.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.preference.ListPreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import miui.os.Build;

import java.text.DateFormatSymbols;
import java.util.Calendar;

public class RepeatPreference extends ListPreference {

    // Initial value that can be set with the values saved in the database.
    private Alarm.DaysOfWeek mDaysOfWeek = new Alarm.DaysOfWeek(0);
    // New value that will be set if a positive result comes back from the
    // dialog.
    private Alarm.DaysOfWeek mNewDaysOfWeek = new Alarm.DaysOfWeek(0);

    // The value is the order of the R.array.alarm_repeat_type
    public static final int ALARM_TYPE_ONLY_ONCE = 0;
    public static final int ALARM_TYPE_EVERY_DAY = 1;
    public static final int ALARM_TYPE_LEGAL_WORKDAY = 2;
    public static final int ALARM_TYPE_MONDAY_TO_FRIDAY = 3;
    public static final int ALARM_TYPE_SELF_DEFINE = 4;

    private String mLabel;

    public RepeatPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        mLabel = null;
        String[] weekdays = new DateFormatSymbols().getWeekdays();
        String[] values = new String[] {
            weekdays[Calendar.MONDAY],
            weekdays[Calendar.TUESDAY],
            weekdays[Calendar.WEDNESDAY],
            weekdays[Calendar.THURSDAY],
            weekdays[Calendar.FRIDAY],
            weekdays[Calendar.SATURDAY],
            weekdays[Calendar.SUNDAY],
        };
        setEntries(values);
        setEntryValues(values);
    }
    protected void onBindView(View view) {
        TextView labelView = (TextView) view.findViewById(R.id.label);
        labelView.setText(mLabel);
        super.onBindView(view);
    }

    public void setLabel(String label) {
        if (!TextUtils.equals(mLabel, label)) {
            mLabel = label;
            notifyChanged();
        }
    }

    public String getLabel() {
        return mLabel;
    }

    public void setLabel(Context context, int resId) {
        setLabel(context.getString(resId));
    }
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            mDaysOfWeek.set(mNewDaysOfWeek);
//            setSummary(mDaysOfWeek.toString(getContext(), true));
            setLabel(mDaysOfWeek.toString(getContext(), true));
            callChangeListener(mDaysOfWeek);
        } else {
            mNewDaysOfWeek.set(mDaysOfWeek);
        }
    }

    @Override
    protected void onClick() {
        String[] repeatType = null;
        if (!Build.IS_INTERNATIONAL_BUILD) {
            repeatType = getContext().getResources().getStringArray(R.array.alarm_repeat_type);
            int workdayMessage = R.string.legal_workday_message;
            if (HolidayHelper.isHolidayDataInvalid(getContext())) {
                workdayMessage = R.string.legal_workday_invalidate_message;
            }
            repeatType[ALARM_TYPE_LEGAL_WORKDAY] = repeatType[ALARM_TYPE_LEGAL_WORKDAY]
                    + getContext().getString(workdayMessage);
        } else {
            repeatType = getContext().getResources().getStringArray(R.array.alarm_repeat_type_no_workdays);
        }

        final int alarmType = mDaysOfWeek.getAlarmType();
        final int[] repeatValue = getContext().getResources().getIntArray(
                Build.IS_INTERNATIONAL_BUILD ? R.array.alarm_repeat_type_no_workdays_values : R.array.alarm_repeat_type_values);

        int checkedItem = -1; // no items are checked
        for(int i = 0; i < repeatValue.length; i++) {
            if (alarmType == repeatValue[i]) {
                checkedItem = i;
                break;
            }
        }

        new AlertDialog.Builder(getContext()).setSingleChoiceItems(repeatType, checkedItem,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (repeatValue[which]) {
                            case ALARM_TYPE_ONLY_ONCE:
                                mNewDaysOfWeek.set(new Alarm.DaysOfWeek(Alarm.DaysOfWeek.NO_DAY));
                                onDialogClosed(true);
                                break;
                            case ALARM_TYPE_EVERY_DAY:
                                mNewDaysOfWeek.set(new Alarm.DaysOfWeek(Alarm.DaysOfWeek.EVERY_DAY)); //everyday
                                onDialogClosed(true);
                                break;
                            case ALARM_TYPE_LEGAL_WORKDAY:
                                mNewDaysOfWeek.setWorkDay(true);
                                onDialogClosed(true);
                                break;
                            case ALARM_TYPE_MONDAY_TO_FRIDAY:
                                mNewDaysOfWeek.set(new Alarm.DaysOfWeek(Alarm.DaysOfWeek.MONDAY_TO_FRIDAY)); // 0x1f: monday ~ friday
                                onDialogClosed(true);
                                break;
                            case ALARM_TYPE_SELF_DEFINE:
                                RepeatPreference.super.onClick();
                                break;
                        }
                        dialog.cancel();
                    }
                }).show();
    }

    @Override
    protected void onPrepareDialogBuilder(final Builder builder) {
        CharSequence[] entries = getEntries();

        builder.setMultiChoiceItems(
                entries, mDaysOfWeek.getBooleanArray(),
                new DialogInterface.OnMultiChoiceClickListener() {
                    public void onClick(DialogInterface dialog, int which,
                            boolean isChecked) {
                        mNewDaysOfWeek.set(which, isChecked);
                    }
                });
    }

    public void setDaysOfWeek(Alarm.DaysOfWeek dow) {
        mDaysOfWeek.set(dow);
        mNewDaysOfWeek.set(dow);
        setSummary(dow.toString(getContext(), true));
    }

    public Alarm.DaysOfWeek getDaysOfWeek() {
        return mDaysOfWeek;
    }
}
