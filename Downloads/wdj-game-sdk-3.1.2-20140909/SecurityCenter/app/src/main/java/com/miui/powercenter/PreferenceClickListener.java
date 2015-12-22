
package com.miui.powercenter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.miui.securitycenter.R;
import com.miui.powercenter.provider.PowerData.PowerMode;

public class PreferenceClickListener implements Preference.OnPreferenceClickListener {
    private static final String TAG = "PreferenceClickListener";
    private PowerMode mCustomMode;

    public PreferenceClickListener(PowerMode mode) {
        mCustomMode = mode;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        Context context = preference.getContext();
        Log.d(TAG, "preference key: " + preference.getKey());
        String key = preference.getKey();
        int index = Integer.parseInt(key);
        if (preference instanceof CheckBoxPreference) {
            CheckBoxPreference cp = (CheckBoxPreference) preference;

            mCustomMode.mDBValue[index] = (cp.isChecked() ? PowerMode.SWITCH_ON
                    : PowerMode.SWITCH_OFF);
            return true;
        }

        DialogInterface.OnClickListener listener = new DialogClickListener(context, mCustomMode,
                preference);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        CharSequence[] items;
        int value;
        switch (index) {
            case PowerMode.INDEX_CPU_STATE:
                builder.setTitle(R.string.power_dialog_cpu_state_title);
                items = context.getResources().getTextArray(R.array.power_dialog_cpu_state_items);
                value = Integer.parseInt(String.valueOf(mCustomMode.mDBValue[index]));
                builder.setSingleChoiceItems(items, StringMatcher.value2Index(index, value),
                        listener);
                break;
            case PowerMode.INDEX_AUTO_CLEAN_MEMORY:
                View titleView = (View) LayoutInflater.from(context).inflate(
                        R.layout.pc_power_dialog_auto_clean_memory_title, null);
                builder.setCustomTitle(titleView);
                items = context.getResources().getTextArray(
                        R.array.power_dialog_auto_clean_memory_items);
                value = Integer.parseInt(String.valueOf(mCustomMode.mDBValue[index]));
                builder.setSingleChoiceItems(items, StringMatcher.value2Index(index, value),
                        listener);
                break;
            case PowerMode.INDEX_BRIGHTNESS:
                builder.setTitle(R.string.power_dialog_screen_brightness_title);
                BrightnessView contentView = (BrightnessView) LayoutInflater.from(context).inflate(
                        R.layout.pc_brightness_view, null);
                builder.setView(contentView);
                listener = new DialogClickListener(context, mCustomMode, preference, contentView);
                builder.setPositiveButton(R.string.power_dialog_ok, listener);
                break;
            case PowerMode.INDEX_SLEEP:
                builder.setTitle(R.string.power_dialog_sleep_title);
                items = context.getResources().getTextArray(R.array.power_dialog_sleep_items);
                value = Integer.parseInt(String.valueOf(mCustomMode.mDBValue[index]));
                builder.setSingleChoiceItems(items, StringMatcher.value2Index(index, value),
                        listener);
                break;
            case PowerMode.INDEX_VIBRATION:
                builder.setTitle(R.string.power_dialog_vibration_title);
                items = context.getResources().getTextArray(R.array.power_dialog_vibration_items);
                value = Integer.parseInt(String.valueOf(mCustomMode.mDBValue[index]));
                builder.setSingleChoiceItems(items, StringMatcher.value2Index(index, value),
                        listener);
                break;
            default:
                break;
        }
        builder.setNegativeButton(R.string.power_dialog_cancel, listener);
        builder.show();
        return true;
    }
};
