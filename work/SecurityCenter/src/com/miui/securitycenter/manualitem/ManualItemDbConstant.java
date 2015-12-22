
package com.miui.securitycenter.manualitem;

import android.provider.BaseColumns;

public class ManualItemDbConstant {

    public static final String DB_NAME = "manual_list";

    public static final int DB_VERSION = 1;

    public static final String ITEM_LIST_TABLE_NAME = "item_list";
    public static final String WHITE_LIST_TABLE_NAME = "white_list";

    public class ItemList implements BaseColumns {
        public static final String COLUMN_ITEM = "item";
        public static final String COLUMN_SCORE = "score";
        public static final String COLUMN_WEIGHT = "weight";
        public static final String COLUMN_CHECKED = "checked";
    }

    public class WhiteList implements BaseColumns {
        public static final String COLUMN_ITEM = "item";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_SUMMARY = "summary";
        public static final String COLUMN_WEIGHT = "weight";
    }

}
