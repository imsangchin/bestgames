
package com.miui.optimizecenter.whitelist;

import android.provider.BaseColumns;

public class WhiteListColumns {

    public static final String DB_NAME = "clean_master";

    public static final int DB_VERSION = 3;

    // version 2
    public static final String TABLE_CACHE_WHITE_LIST = "t_cache_white_list";

    // version 2
    public static final String TABLE_AD_WHITE_LIST = "t_ad_white_list";

    // version 2
    public static final String TABLE_APK_WHITE_LIST = "t_apk_white_list";

    // version 2
    public static final String TABLE_RESIDUAL_WHITE_LIST = "t_residual_white_list";

    // version 2
    public static final String TABLE_LARGE_FILE_WHITE_LIST = "t_large_file_white_list";

    public class Cache implements BaseColumns {

        public static final String COLUMN_CACHE_TYPE = "cache_type";

        public static final String COLUMN_DIR_PATH = "dir_path";

        public static final String COLUMN_PKG_NAME = "pkg_name";

        public static final String COLUMN_ALERT_INFO = "alert_info";

        public static final String COLUMN_DESC = "desc";
    }

    public class Ad implements BaseColumns {

        public static final String COLUMN_NAME = "name";

        public static final String COLUMN_DIR_PATH = "dir_path";
    }

    public class Residual implements BaseColumns {

        public static final String COLUMN_DESC_NAME = "desc_name";

        public static final String COLUMN_DIR_PATH = "dir_path";

        public static final String COLUMN_ALERT_INFO = "alert_info";
    }

    public class Apk implements BaseColumns {

        public static final String COLUMN_DIR_PATH = "dir_path";

        public static final String COLUMN_APP_NAME = "app_name";

    }

    public class LargeFile implements BaseColumns {

        public static final String COLUMN_DIR_PATH = "dir_path";

    }
}
