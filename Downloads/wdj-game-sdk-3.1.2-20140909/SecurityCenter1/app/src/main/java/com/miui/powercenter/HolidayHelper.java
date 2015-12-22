package com.miui.powercenter;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;

import java.util.Calendar;

import miui.provider.ExtraCalendarContracts.HolidayContracts;
import miui.provider.ExtraCalendarContracts.HolidayContracts.HolidayType;

public class HolidayHelper {

    /**
     * 判断工作日数据是否过期，根据CalendarProvider返回的值。
     * 返回true，如果返回值等于:{@link HolidayType#INVALIDATE_DAY}
     */
    public static boolean isHolidayDataInvalid(Context context) {
        Cursor c = context.getContentResolver().query(
                ContentUris.withAppendedId(HolidayContracts.HOLIDAY_CONTENT_URI,
                        Calendar.getInstance().getTimeInMillis()), null, null, null, null);
        if (c != null) {
            try {
                if (c.moveToFirst()
                        && c.getInt(HolidayContracts.HOLIDAY_COLUMN_TYPE_INDEX) == HolidayType.INVALIDATE_DAY) {
                    return true;
                }
            } finally {
                c.close();
            }
        }
        return false;
    }

    /**
     * 判断是否是法定节假日或不调休的周末
     */
    public static boolean isHoliday(Context context, Calendar calendar) {
        Cursor c = context.getContentResolver().query(
                ContentUris.withAppendedId(HolidayContracts.HOLIDAY_CONTENT_URI,
                        calendar.getTimeInMillis()), null, null, null, null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    int holidayType = c.getInt(HolidayContracts.HOLIDAY_COLUMN_TYPE_INDEX);
                    return (HolidayType.FREE_DAY == holidayType)
                            || (HolidayType.WORK_DAY != holidayType && isWeekEnd(calendar));
                }
            } finally {
                c.close();
            }
        }
        return false;
    }

    public static boolean isWeekEnd(Calendar calendar) {
        return calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
                || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
    }
}
