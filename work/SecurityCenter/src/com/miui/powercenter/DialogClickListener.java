
package com.miui.powercenter;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.Log;

import com.miui.powercenter.provider.PowerData.PowerMode;

public class DialogClickListener implements DialogInterface.OnClickListener {
    private static final String TAG = "DialogClickListener";

    private Context mContext;
    private PowerMode mCustomMode;
    private OptionPreference mPreference;
    private BrightnessView mView;

    public DialogClickListener(Context context, PowerMode mode, Preference preference) {
        mContext = context;
        mCustomMode = mode;
        mPreference = (OptionPreference) preference;
    }

    public DialogClickListener(Context context, PowerMode mode, Preference preference,
            BrightnessView view) {
        this(context, mode, preference);
        mView = view;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        String key = mPreference.getKey();
        int index = Integer.parseInt(key);
        Log.d(TAG, "preference key: " + key);

        if (mView != null) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                int value = mView.getProgress();
                if (!mView.isChecked()) {
                    ++value;
                    value = 0 - value;
                }
                mCustomMode.mDBValue[index] = value;
                mPreference.setMiuiLabel(StringMatcher.value2String(mContext, index, value));
                mView.recoveryOld(false);
            } else {
                mView.recoveryOld(true);
            }

            dialog.dismiss();
            return;
        }

        if (which < 0) {
            dialog.dismiss();
            return;
        }

        if (TextUtils.equals(key, PowerMode.KEY_CPU_STATE)) {
            mCustomMode.mDBValue[PowerMode.INDEX_CPU_STATE] = which;
            mPreference.setMiuiLabel(StringMatcher.value2String(mContext,
                    PowerMode.INDEX_CPU_STATE,
                    which));
        } else if (TextUtils.equals(key, PowerMode.KEY_AUTO_CLEAN_MEMORY)) {
            int seconds = PowerMode.AUTO_CLEAN_MEMORY_NEVER;
            switch (which) {
                case 0:
                    seconds = 60;
                    break;
                case 1:
                    seconds = 300;
                    break;
                case 2:
                    seconds = 600;
                    break;
                default:
                    break;
            }
            mCustomMode.mDBValue[PowerMode.INDEX_AUTO_CLEAN_MEMORY] = seconds;
            mPreference.setMiuiLabel(StringMatcher.value2String(mContext,
                    PowerMode.INDEX_AUTO_CLEAN_MEMORY,
                    seconds));
        } else if (TextUtils.equals(key, PowerMode.KEY_BRIGHTNESS)) {

        } else if (TextUtils.equals(key, PowerMode.KEY_MODE_SLEEP)) {
            int seconds = PowerMode.SLEEP_NEVER;
            switch (which) {
                case 0:
                    seconds = 15;
                    break;
                case 1:
                    seconds = 30;
                    break;
                case 2:
                    seconds = 60;
                    break;
                case 3:
                    seconds = 120;
                    break;
                case 4:
                    seconds = 300;
                    break;
                case 5:
                    seconds = 600;
                    break;
                default:
                    break;
            }
            mCustomMode.mDBValue[PowerMode.INDEX_SLEEP] = seconds;
            mPreference
                    .setMiuiLabel(StringMatcher.value2String(mContext, PowerMode.INDEX_SLEEP,
                            seconds));
        } else if (TextUtils.equals(key, PowerMode.KEY_VIBRATION)) {
            int vibration = PowerMode.VIBRATION_NO;
            switch (which) {
                case 0:
                    vibration = PowerMode.VIBRATION_NO;
                    break;
                case 1:
                    vibration = PowerMode.VIBRATION_SLIENCE;
                    break;
                case 2:
                    vibration = PowerMode.VIBRATION_YES;
                    break;
                default:
                    break;
            }
            mCustomMode.mDBValue[PowerMode.INDEX_VIBRATION] = vibration;
            mPreference.setMiuiLabel(StringMatcher.value2String(mContext,
                    PowerMode.INDEX_VIBRATION,
                    vibration));
        }
        dialog.dismiss();
    }
}
