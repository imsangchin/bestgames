/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.miui.powercenter;

import android.content.Context;
import android.database.Cursor;
import com.miui.securitycenter.R;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.util.Log;

import java.text.DateFormatSymbols;
import java.util.Calendar;

public final class Alarm implements Parcelable {

    //////////////////////////////
    // Parcelable apis
    //////////////////////////////
    public static final Parcelable.Creator<Alarm> CREATOR
            = new Parcelable.Creator<Alarm>() {
                public Alarm createFromParcel(Parcel p) {
                    return new Alarm(p);
                }

                public Alarm[] newArray(int size) {
                    return new Alarm[size];
                }
            };

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel p, int flags) {
        p.writeInt(id);
        p.writeInt(enabled ? 1 : 0);
        p.writeInt(hour);
        p.writeInt(minutes);
        p.writeInt(seconds);
        p.writeInt(daysOfWeek.getCoded());
        p.writeLong(time);
        p.writeInt(vibrate ? 1 : 0);
        p.writeString(label);
        p.writeParcelable(alert, flags);
        p.writeInt(silent ? 1 : 0);
        p.writeInt(deleteAfterUse ? 1 : 0);
    }
    //////////////////////////////
    // end Parcelable apis
    //////////////////////////////

    public static final int ALARM_TYPE = 0;
    public static final int TIMER_TYPE = 1;
    public static final String ALARM_ALERT_SILENT="alarm_killed";

    //////////////////////////////
    // Column definitions
    //////////////////////////////
    public static class Columns implements BaseColumns {
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =
                Uri.parse("content://com.android.deskclock/alarm");

        /**
         * Hour in 24-hour localtime 0 - 23.
         * <P>Type: INTEGER</P>
         */
        public static final String HOUR = "hour";

        /**
         * Minutes in localtime 0 - 59
         * <P>Type: INTEGER</P>
         */
        public static final String MINUTES = "minutes";

        /**
         * Days of week coded as integer
         * <P>Type: INTEGER</P>
         */
        public static final String DAYS_OF_WEEK = "daysofweek";

        /**
         * Alarm time in UTC milliseconds from the epoch.
         * <P>Type: INTEGER</P>
         */
        public static final String ALARM_TIME = "alarmtime";

        /**
         * True if alarm is active
         * <P>Type: BOOLEAN</P>
         */
        public static final String ENABLED = "enabled";

        /**
         * True if alarm should vibrate
         * <P>Type: BOOLEAN</P>
         */
        public static final String VIBRATE = "vibrate";

        /**
         * Message to show when alarm triggers
         * Note: not currently used
         * <P>Type: STRING</P>
         */
        public static final String MESSAGE = "message";

        /**
         * Audio alert to play when alarm triggers
         * <P>Type: STRING</P>
         */
        public static final String ALERT = "alert";

        /**
         * Alarm's type.
         * 0: alarm; 1: timer
         * <P>Type: INTEGER</P>
         */
        public static final String TYPE = "type";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER =
                HOUR + ", " + MINUTES + " ASC";

        // Used when filtering enabled alarms.
        public static final String WHERE_ENABLED = ENABLED + "=1";

        public static final String WHERE_ENABLED_AND_NON_REPEAT = WHERE_ENABLED + " AND " + DAYS_OF_WEEK + "=0";

        /**
         * true if alarm need to be deleted after dismiss
         * <P>Type: BOOLEAN</P>
         */
        public static final String DELETE_AFTER_USE = "deleteAfterUse";

        static final String[] ALARM_QUERY_COLUMNS = {
            _ID, HOUR, MINUTES, DAYS_OF_WEEK, ALARM_TIME,
            ENABLED, VIBRATE, MESSAGE, ALERT, TYPE, DELETE_AFTER_USE};

        /**
         * These save calls to cursor.getColumnIndexOrThrow()
         * THEY MUST BE KEPT IN SYNC WITH ABOVE QUERY COLUMNS
         */
        public static final int ALARM_ID_INDEX = 0;
        public static final int ALARM_HOUR_INDEX = 1;
        public static final int ALARM_MINUTES_INDEX = 2;
        public static final int ALARM_DAYS_OF_WEEK_INDEX = 3;
        public static final int ALARM_TIME_INDEX = 4;
        public static final int ALARM_ENABLED_INDEX = 5;
        public static final int ALARM_VIBRATE_INDEX = 6;
        public static final int ALARM_MESSAGE_INDEX = 7;
        public static final int ALARM_ALERT_INDEX = 8;
        public static final int ALARM_TYPE_INDEX = 9;
        public static final int DELETE_AFTER_USE_INDEX = 10;
    }
    //////////////////////////////
    // End column definitions
    //////////////////////////////

    // Public fields
    public int        id;
    public boolean    enabled;
    public int        hour;
    public int        minutes;
    public int        seconds;
    public DaysOfWeek daysOfWeek;
    public long       time;
    public boolean    vibrate;
    public String     label;
    public Uri        alert;
    public boolean    silent;
    public boolean deleteAfterUse;

    public Alarm(Cursor c) {
        id = c.getInt(Columns.ALARM_ID_INDEX);
        enabled = c.getInt(Columns.ALARM_ENABLED_INDEX) == 1;
        hour = c.getInt(Columns.ALARM_HOUR_INDEX);
        minutes = c.getInt(Columns.ALARM_MINUTES_INDEX);
        daysOfWeek = new DaysOfWeek(c.getInt(Columns.ALARM_DAYS_OF_WEEK_INDEX));
        time = c.getLong(Columns.ALARM_TIME_INDEX);
        vibrate = c.getInt(Columns.ALARM_VIBRATE_INDEX) == 1;
        label = c.getString(Columns.ALARM_MESSAGE_INDEX);
        deleteAfterUse = c.getInt(Columns.DELETE_AFTER_USE_INDEX) == 1;
        String alertString = c.getString(Columns.ALARM_ALERT_INDEX);
        if (ALARM_ALERT_SILENT.equals(alertString)) {
            silent = true;
        } else {
            if (alertString != null && alertString.length() != 0) {
                alert = Uri.parse(alertString);
            }

            // If the database alert is null or it failed to parse, use the
            // default alert.
            if (alert == null) {
                alert = RingtoneManager.getDefaultUri(
                        RingtoneManager.TYPE_ALARM);
            }
        }
    }

    public Alarm(Parcel p) {
        id = p.readInt();
        enabled = p.readInt() == 1;
        hour = p.readInt();
        minutes = p.readInt();
        seconds = p.readInt();
        daysOfWeek = new DaysOfWeek(p.readInt());
        time = p.readLong();
        vibrate = p.readInt() == 1;
        label = p.readString();
        alert = (Uri) p.readParcelable(null);
        silent = p.readInt() == 1;
        deleteAfterUse = p.readInt() == 1;
    }

    // Creates a default alarm at the current time.
    public Alarm() {
        id = -1;
        enabled = true;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        hour = c.get(Calendar.HOUR_OF_DAY);
        minutes = c.get(Calendar.MINUTE);
        seconds = c.get(Calendar.SECOND);
        vibrate = true;
        daysOfWeek = new DaysOfWeek(0);
        alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        label = "";
        deleteAfterUse = false;
    }

    public String getLabel(Context context) {
        if (label == null || label.length() == 0) {
            return "";
        }
        return label;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Alarm)) return false;
        final Alarm other = (Alarm) o;
        return id == other.id;
    }


    /*
     * Days of week code as a single int.
     * 0x00: no day
     * 0x01: Monday
     * 0x02: Tuesday
     * 0x04: Wednesday
     * 0x08: Thursday
     * 0x10: Friday
     * 0x20: Saturday
     * 0x40: Sunday
     * 0x80: Legal workday
     */
    public static final class DaysOfWeek {
        public static final int NO_DAY = 0x00;
        public static final int EVERY_DAY = 0x7f;
        public static final int MONDAY_TO_FRIDAY = 0x1f;
        public static final int LEGAL_WORK_DAY = 0x80;

        private static int[] DAY_MAP = new int[] {
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY,
            Calendar.SUNDAY,
        };

        // Bitmask of all repeating days
        private int mDays;


        public DaysOfWeek(int days) {
            mDays = days;
        }

        public String toString(Context context, boolean showNever) {
            StringBuilder ret = new StringBuilder();

            // no days
            if (mDays == NO_DAY) {
                return showNever ?
                        context.getText(R.string.never).toString() : "";
            }

            // every day
            if (mDays == EVERY_DAY) {
                return context.getText(R.string.every_day).toString();
            }

            // legal workday
            if (mDays == LEGAL_WORK_DAY) {
                int resId = R.string.legal_workday;
                if (HolidayHelper.isHolidayDataInvalid(context)) {
                    resId = R.string.legal_workday_invalidate;
                }
                return context.getText(resId).toString();
            }

            // count selected days
            int dayCount = 0, days = mDays;
            while (days > 0) {
                if ((days & 1) == 1) dayCount++;
                days >>= 1;
            }

            // short or long form?
            DateFormatSymbols dfs = new DateFormatSymbols();
            String[] dayList = (dayCount > 1) ?
                    dfs.getShortWeekdays() :
                    dfs.getWeekdays();

            // selected days
            for (int i = 0; i < 7; i++) {
                if ((mDays & (1 << i)) != 0) {
                    ret.append(dayList[DAY_MAP[i]]);
                    dayCount -= 1;
                    if (dayCount > 0) ret.append(" ");
                }
            }
            return ret.toString();
        }

        public int getAlarmType() {
            switch (mDays) {
                case NO_DAY:
                    return RepeatPreference.ALARM_TYPE_ONLY_ONCE;
                case EVERY_DAY:
                    return RepeatPreference.ALARM_TYPE_EVERY_DAY;
                case LEGAL_WORK_DAY:
                    return RepeatPreference.ALARM_TYPE_LEGAL_WORKDAY;
                case MONDAY_TO_FRIDAY:
                    return RepeatPreference.ALARM_TYPE_MONDAY_TO_FRIDAY;

                default:
                    return RepeatPreference.ALARM_TYPE_SELF_DEFINE;
            }
        }

        private boolean isSet(int day) {
            return ((mDays & (1 << day)) > 0);
        }

        public void set(int day, boolean set) {
            if (set) {
                mDays |= (1 << day);
            } else {
                mDays &= ~(1 << day);
            }
        }

        public void set(DaysOfWeek dow) {
            mDays = dow.mDays;
        }

        public void setWorkDay(boolean set) {
            if (set) {
                mDays = 0;
                set(7, true);  // 0x80: legal workday
            } else {
                set(7, false);
            }
        }

        public int getCoded() {
            return mDays;
        }

        // Returns days of week encoded in an array of booleans.
        public boolean[] getBooleanArray() {
            boolean[] ret = new boolean[7];
            for (int i = 0; i < 7; i++) {
                ret[i] = isSet(i);
            }
            return ret;
        }

        public boolean isRepeatSet() {
            return mDays != 0;
        }

        /**
         * returns number of days from today until next alarm
         * @param c must be set to today
         */
        public int getNextAlarm(Context context, Calendar c) {
            if (mDays == 0) {
                return -1;
            }

            int day = 0;
            int dayCount = 0;

            // legal workday
            if (mDays == LEGAL_WORK_DAY) {
                Calendar calendar = (Calendar) c.clone();
                // holidays less than 10
                for(; dayCount < 10; dayCount++) {
                    if (!HolidayHelper.isHoliday(context, calendar)) {
                        return dayCount;
                    }
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                }
            }

            int today = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;
            for (; dayCount < 7; dayCount++) {
                day = (today + dayCount) % 7;
                if (isSet(day)) {
                    break;
                }
            }
            return dayCount;
        }

        /**
         * Need to have monday start at index 0 to be backwards compatible. This converts
         * Calendar.DAY_OF_WEEK constants to our internal bit structure.
         */
        private static int convertDayToBitIndex(int day) {
            return (day + 5) % 7;
        }

        /**
         * Enables or disable certain days of the week.
         *
         * @param daysOfWeek Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, etc.
         */
        public void setDaysOfWeek(boolean value, int ... daysOfWeek) {
            for (int day : daysOfWeek) {
                set(convertDayToBitIndex(day), value);
            }
        }

        public boolean isAlarmDay(Calendar calendar) {// 是不是设置闹钟日
            Calendar c = Calendar.getInstance();
            for (int i = 0; i < 7; i++) {
                if ((mDays & (1 << i)) != 0) {
                    if (i == 6)
                        calendar.set(Calendar.DAY_OF_WEEK, 1);
                    else
                        calendar.set(Calendar.DAY_OF_WEEK, i + 2);
                    if (calendar.get(Calendar.DAY_OF_WEEK) == c.get(Calendar.DAY_OF_WEEK)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public void daysOfNearestAlarm(int[] mNearestDays) {
            boolean flag = false;
            for (int i = 0; i < 7; i++) {
                for (int j = i + 1; j < 7; j++) {
                    if ((mDays & (1 << j)) != 0) {
                        mNearestDays[i] = j - i;
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    for (int j = 0; j < 7; j++) {
                        if ((mDays & (1 << j)) != 0) {
                            mNearestDays[i] = 7 - i + j;
                            break;
                        }
                    }
                }
                flag = false;
            }
        }
    }
}
