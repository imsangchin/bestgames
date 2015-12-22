
package com.miui.mdb;

import android.os.SystemProperties;

public class DataUpdateUtils {
    private static final String LOG_TAG = "DataUpdateUtils";
    // alpha build
    public static final boolean IS_ALPHA_BUILD = "1".equals(SystemProperties.get("ro.miui.secure",
            ""));

    private static final String FORMAL_BASE_URL = "http://api.sec.miui.com";

    // 注册data update
    public static final String DATA_UPDATE_REGISTRATION = "com.xiaomi.dataUpdate.REGISTRATION";
    // 接收data update
    public static final String DATA_UPDATE_RECEIVE = "com.xiaomi.dataUpdate.RECEIVE";

    public static final String EXTRA_WATER_MARK = "water_mark";

    public static final String EXTRA_SERVICE_NAME = "service_name";

    public static final String RECEIVER_META_DATA = "com.xiaomi.dataUpdate";

    public static final String ATTRIBUTES_NAME = "data-update";

    /**
     * 静态文件的上线机制与动态的有些区别，需要用exp_前缀标识文件来处理
     */
    public static final String HOLIDAY_URL = FORMAL_BASE_URL + "/holiday/"
            + (IS_ALPHA_BUILD ? "exp_holiday.jsp" : "holiday.jsp");
    public static final String MDB_URL = FORMAL_BASE_URL + "/mdb_pub/"
            + (IS_ALPHA_BUILD ? "exp_mdb_pub.key" : "mdb_pub.key");

    public static final String VERSION_URL = "http://api.sec.miui.com/mdb_pub/mdb_pub.version";
}
