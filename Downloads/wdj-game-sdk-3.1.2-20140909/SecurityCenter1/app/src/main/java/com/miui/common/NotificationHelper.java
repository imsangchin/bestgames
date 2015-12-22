
package com.miui.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class NotificationHelper {

    private static NotificationHelper INST;

    private NotificationPrefs mNotifyPrefs;

    private NotificationHelper(Context context) {
        mNotifyPrefs = new NotificationPrefs(context);
    }

    public static NotificationHelper getInstance(Context context) {
        if (INST == null) {
            INST = new NotificationHelper(context.getApplicationContext());
        }
        return INST;
    }

    public int getNotificationIdByKey(String key) {
        if (TextUtils.equals(key, NotificationKey.KEY_PERM_NOTIFICATION_BAR)) {
            return NotificationPrefs.PERM_NOTIFICATION_BAR_ID;
        }
        return mNotifyPrefs.getNotificationId(key);
    }

    public class NotificationKey {
        // 常驻通知栏
        public static final String KEY_PERM_NOTIFICATION_BAR = "key_perm_notification_bar";

        // 系统空间不足时
        public static final String KEY_INTERNAL_STORAGELOW_MEMORY = "key_internal_storagelow_memory";

        // 定时扫描垃圾提醒
        public static final String KEY_TIME_GARBAGE_CLEANUP = "key_time_garbage_cleanup";

        // 系统空间不足
        public static final String KEY_LOW_SYSTEM_MEMORY = "key_low_system_memory";

        // 病毒库自动更新
        public static final String KEY_VIRUS_LIB_AUTO_UPDATE = "key_virus_lib_auto_update";

        // 清楚病毒库白名单
        public static final String KEY_CLEAR_VIRUS_WHITE_LIST = "key_clear_virus_white_list";

        // 休眠后清理
        public static final String KEY_CLEAN_AFTER_SLEEP = "key_clean_after_sleep";
    }
}

final class NotificationPrefs {

    public static final int PERM_NOTIFICATION_BAR_ID = 1000;

    private static final String PREF_NOTIFICATION_IDS = "notification_ids";
    private static final String KEY_MAX_NOTIFICATION_ID = "key_max_notification_id";
    private static final int INVALIDE_ID = -1;

    private SharedPreferences mPefs;

    public NotificationPrefs(Context context) {
        mPefs = context.getSharedPreferences(PREF_NOTIFICATION_IDS, Context.MODE_PRIVATE);
    }

    private void setMaxNotificationId(int maxId) {
        mPefs.edit().putInt(KEY_MAX_NOTIFICATION_ID, maxId).commit();
    }

    private int getMaxNotificationId() {
        return mPefs.getInt(KEY_MAX_NOTIFICATION_ID, PERM_NOTIFICATION_BAR_ID + 1);
    }

    public int getNotificationId(String key) {
        int id = mPefs.getInt(key, INVALIDE_ID);
        if (id != INVALIDE_ID) {
            return id;
        } else {
            int maxId = getMaxNotificationId();
            ++maxId;
            setMaxNotificationId(maxId);
            setNotificationId(key, maxId);
            return maxId;
        }
    }

    private void setNotificationId(String key, int id) {
        mPefs.edit().putInt(key, id).commit();
    }
}
