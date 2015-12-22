
package com.miui.securitycenter;

import android.content.MiuiIntent;

public class ScoreConstants {

    public static final int LOCK_STATE_LOCK = MiuiIntent.STATUS_LOCK;
    public static final int LOCK_STATE_UNLOCK = MiuiIntent.STATUS_UNLOCK;
    public static final int LOCK_STATE_ERROR = MiuiIntent.STATUS_ERROR;

    // 基数分数
    public static final int BASE_SCORE = 35;

    // 系统防护
    public static final int SYSTEM_TOTAL_ITEMS_SCORE = 25;
    public static final int SYSTEM_ITEM_PERMISSION_SCORE = 6;
    public static final int SYSTEM_ITEM_USB_DEBUG_SCORE = 5;
    public static final int SYSTEM_ITEM_DEV_OFF_SCORE = 2;
    public static final int SYSTEM_ITEM_VIRUS_AUTO_UPDATE_SCORE = 3;
    public static final int SYSTEM_ITEM_INSTALL_MONITOR_SCORE = 3;
    public static final int SYSTEM_ITEM_MMS_SCORE = 6;

    // 占用内存
    public static final int SCORE_MEMORY_ONE = 20;
    public static final int SCORE_MEMORY_TWO = 15;
    public static final int SCORE_MEMORY_THREE = 10;
    public static final int SCORE_MEMORY_FOUR = 7;
    public static final int SCORE_MEMORY_FIVE = 1;

    // 缓存垃圾
    public static final int SCORE_CACHE_ONE = 20;
    public static final int SCORE_CACHE_TWO = 16;
    public static final int SCORE_CACHE_THREE = 13;
    public static final int SCORE_CACHE_FOUR = 10;
    public static final int SCORE_CACHE_FIVE = 5;
    public static final int SCORE_CACHE_SIX = 0;

}
