
package com.miui.powercenter;

import android.content.Context;

import com.miui.securitycenter.R;
import com.miui.powercenter.provider.PowerData.PowerMode;

public class StringMatcher {

    /***
     * 根据字段index,字段value获得对应字符串
     * 
     * @param fieldId
     * @param value
     * @return
     */
    public static String value2String(Context context, int fieldId, int value) {
        switch (fieldId) {
            case PowerMode.INDEX_CPU_STATE:
                switch (value) {
                    case PowerMode.CPU_STATE_BALANCE:
                        return context.getString(R.string.power_mode_cpu_state_balance);
                    case PowerMode.CPU_STATE_PERFORMANCE:
                        return context.getString(R.string.power_mode_cpu_state_performance);
                    case PowerMode.CPU_STATE_SAVE:
                        return context.getString(R.string.power_mode_cpu_state_save);
                }
                break;

            case PowerMode.INDEX_AUTO_CLEAN_MEMORY:
                switch (value) {
                    case PowerMode.AUTO_CLEAN_MEMORY_NEVER:
                        return context.getString(R.string.power_mode_auto_clean_memory_never);
                    default:
                        return String.format(context
                                .getString(R.string.power_mode_auto_clean_memery_lock_screen),
                                value / 60);
                }

            case PowerMode.INDEX_BRIGHTNESS:
                if (value >= 0) {
                    return String.format(
                            context.getString(R.string.power_mode_screen_brightness_automatic),
                            value);
                }
                value = 0 - value;
                --value;
                return String.format(
                        context.getString(R.string.power_mode_screen_brightness_manual), value);

            case PowerMode.INDEX_SLEEP:
                if (value == PowerMode.SLEEP_NEVER) {
                    return context.getString(R.string.power_mode_sleep_never);
                }
                if (value < 60) {
                    return String.format(context.getString(R.string.power_mode_sleep_in_seconds),
                            value);
                }
                return String.format(context.getString(R.string.power_mode_sleep_in_minutes),
                        value / 60);

            case PowerMode.INDEX_VIBRATION:
                switch (value) {
                    case PowerMode.VIBRATION_YES:
                        return context.getString(R.string.power_mode_vibration_yes);
                    case PowerMode.VIBRATION_NO:
                        return context.getString(R.string.power_mode_vibration_no);
                    case PowerMode.VIBRATION_SLIENCE:
                        return context.getString(R.string.power_mode_vibration_silence);
                }
                break;

            case PowerMode.INDEX_AIRPLANE_MODE:
            case PowerMode.INDEX_WIFI:
            case PowerMode.INDEX_INTERNET:
            case PowerMode.INDEX_BLUETOOTH:
            case PowerMode.INDEX_SYNCHRONIZATION:
            case PowerMode.INDEX_GPS:
            case PowerMode.INDEX_TOUCH_VIBRATION:
            case PowerMode.INDEX_TOUCH_RING:
                switch (value) {
                    case PowerMode.SWITCH_ON:
                        return context.getString(R.string.power_mode_switch_on);
                    case PowerMode.SWITCH_OFF:
                        return context.getString(R.string.power_mode_switch_off);
                }
                break;
            default:
                break;
        }
        return null;
    }

    /***
     * 根据字段index，字段value获得单选框默认选中项的index 如果是屏幕亮度，则返回亮度百分比
     * 
     * @param fieldId
     * @param value
     * @return
     */
    public static int value2Index(int fieldId, int value) {
        switch (fieldId) {
            case PowerMode.INDEX_CPU_STATE:
                return value;
            case PowerMode.INDEX_AUTO_CLEAN_MEMORY:
                if (value < 0) {
                    return 3;
                }
                value = value / 60;
                switch (value) {
                    case 1:
                        return 0;
                    case 5:
                        return 1;
                    case 10:
                        return 2;
                }
                return 3;
                // //理论上不会走到这个case
                // case PowerMode.INDEX_BRIGHTNESS:
                // return value;
            case PowerMode.INDEX_SLEEP:
                switch (value) {
                    case PowerMode.SLEEP_NEVER:
                        return 6;
                    case 15:
                        return 0;
                    case 30:
                        return 1;
                    case 60:
                        return 2;
                    case 120:
                        return 3;
                    case 300:
                        return 4;
                    case 600:
                        return 5;
                }
            case PowerMode.INDEX_VIBRATION:
                return value;
            case PowerMode.INDEX_AIRPLANE_MODE:
            case PowerMode.INDEX_WIFI:
            case PowerMode.INDEX_INTERNET:
            case PowerMode.INDEX_BLUETOOTH:
            case PowerMode.INDEX_SYNCHRONIZATION:
            case PowerMode.INDEX_GPS:
            case PowerMode.INDEX_TOUCH_VIBRATION:
            case PowerMode.INDEX_TOUCH_RING:
            default:
                return value;
        }
    }
}
