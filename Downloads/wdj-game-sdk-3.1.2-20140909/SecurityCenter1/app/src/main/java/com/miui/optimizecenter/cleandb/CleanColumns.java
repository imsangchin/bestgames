
package com.miui.optimizecenter.cleandb;

import android.provider.BaseColumns;

public abstract class CleanColumns implements BaseColumns {

    // column
    public static final String PKG_NAME = "pkg_name";
    public static final String DIR_PATH = "dir_path";
    public static final String ROOT_DIR = "root_dir";
    public static final String RES_ZH_CN = "res_zh_cn";
    public static final String RES_ZH_TW = "res_zh_tw";
    public static final String RES_DEFAULT = "res_default";
    public static final String ADVISE_DEL = "advise_del";

    // default value
    public static final int ADVISE_DEL_YES = 1;
    public static final int ADVISE_DEL_NO = 0;

    public static final String[] ZH_CN_COLUMNS = {
            PKG_NAME, DIR_PATH, RES_ZH_CN, ADVISE_DEL
    };

    public static final String[] ZH_TW_COLUMNS = {
            PKG_NAME, DIR_PATH, RES_ZH_TW, ADVISE_DEL
    };

    public static final String[] DEF_COLUMNS = {
            PKG_NAME, DIR_PATH, RES_DEFAULT, ADVISE_DEL
    };
}
