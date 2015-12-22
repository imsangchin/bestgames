
package com.miui.securitycenter;

import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateUtils;
import android.text.format.Time;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.miui.securitycenter.R;
import com.miui.antivirus.AntiVirusStatus;

public class DateTimeUtils {

    public static final SimpleDateFormat DATE_FORMAT_ALL = new SimpleDateFormat(
            "yyyy/MM/dd HH:mm:ss");

    public static final SimpleDateFormat DATE_FORMAT_DAY = new SimpleDateFormat(
            "yyyy/MM/dd");

    public static final SimpleDateFormat DATE_FORMAT_NEAR = new SimpleDateFormat(
            "HH:mm");
    public static final SimpleDateFormat DATE_FORMAT_MEDIUM = new SimpleDateFormat(
            "MM/dd HH:mm");
    public static final SimpleDateFormat DATE_FORMAT_FAR = new SimpleDateFormat(
            "yyyy/MM/dd");

    private static Date sDate = new Date();

    public static final CharSequence timestampToRelativeSpan(Context context,
            long timestamp) {
        return DateUtils.getRelativeDateTimeString(context, timestamp,
                DateUtils.SECOND_IN_MILLIS, DateUtils.DAY_IN_MILLIS, 0);
    }

    private static int getDayInterval(long time, long base, TimeZone timeZone) {
        int gmtoff = timeZone.getRawOffset() / 1000;
        return Time.getJulianDay(time, gmtoff)
                - Time.getJulianDay(base, gmtoff);
    }

    public static int getFromNowDayInterval(long time) {
        return getDayInterval(System.currentTimeMillis(), time, TimeZone.getDefault());
    }

    public static String formatCheckedTime(Context context, long time) {
        final Resources res = context.getResources();
        TimeZone timeZone = TimeZone.getDefault();
        long base = System.currentTimeMillis();

        String prefix;
        int dayInterval = getDayInterval(time, base, timeZone);

        if (dayInterval > 0) {
            return res.getString(R.string.last_check_canceled);
        }

        switch (dayInterval) {
            case 0:
                // today
                sDate.setTime(time);
                return prefix = res.getString(R.string.last_check_date_today)
                        + DATE_FORMAT_NEAR.format(sDate);
            case -1:
                // yesterday
                return res.getString(R.string.last_check_date_yesterday);
            default:
                String message = res.getString(R.string.last_checked_date2);
                int days = Math.abs(dayInterval);
                return String.format(message, days);
        }
    }

    public static String formatVirusScanTime(AntiVirusStatus status, Context context, long time) {
        final Resources res = context.getResources();
        TimeZone timeZone = TimeZone.getDefault();
        long base = System.currentTimeMillis();

        String prefix;
        int dayInterval = getDayInterval(time, base, timeZone);

        if (dayInterval > 0) {
            return res.getString(R.string.last_check_canceled);
        }

        switch (dayInterval) {
            case 0:
                // today
                sDate.setTime(time);
                switch (status) {
                    case SAVE:
                        return prefix = res.getString(R.string.hints_last_virus_scan_today_safe);
                    case RISK:
                        return prefix = res.getString(R.string.hints_last_virus_scan_today_risk);
                    case VIRUS:
                        return prefix = res.getString(R.string.hints_last_virus_scan_today_virus);
                    default:
                        return null;
                }
            case -1:
                // yesterday
                switch (status) {
                    case SAVE:
                        return prefix = res
                                .getString(R.string.hints_last_virus_scan_yesterday_safe);
                    case RISK:
                        return prefix = res
                                .getString(R.string.hints_last_virus_scan_yesterday_risk);
                    case VIRUS:
                        return prefix = res
                                .getString(R.string.hints_last_virus_scan_yesterday_virus);
                    default:
                        return null;
                }
            default:
                int days = Math.abs(dayInterval);
                switch (status) {
                    case SAVE:
                        return prefix = res.getString(R.string.hints_last_virus_scan_safe, days);
                    case RISK:
                        return prefix = res.getString(R.string.hints_last_virus_scan_risk, days);
                    case VIRUS:
                        return prefix = res.getString(R.string.hints_last_virus_scan_virus, days);
                    default:
                        return null;
                }
        }
    }

    public static String formatGarbageCleanupTime(Context context, long time) {
        final Resources res = context.getResources();
        TimeZone timeZone = TimeZone.getDefault();
        long base = System.currentTimeMillis();

        String prefix;
        int dayInterval = getDayInterval(time, base, timeZone);

        if (dayInterval > 0) {
            return res.getString(R.string.last_check_canceled);
        }

        switch (dayInterval) {
            case 0:
                // today
                return prefix = res.getString(R.string.hints_latest_garbage_cleanup_today);
            case -1:
                // yesterday
                return res.getString(R.string.hints_latest_garbage_cleanup_yesterday);
            default:
                String message = res.getString(R.string.hints_latest_garbage_cleanup_date);
                int days = Math.abs(dayInterval);
                return String.format(message, days);
        }
    }

    public static String formatDataTime(long time, SimpleDateFormat formater) {
        return formater.format(new Date(time));
    }

}
