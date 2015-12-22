
package com.miui.optimizecenter.uninstallmonitor;

import android.provider.BaseColumns;

public final class PackageColumns implements BaseColumns {

    public static final String DB_NAME = "download_apps.db";

    public static final int DB_VERSION = 1;

    public static final String TABLE_PACKAGE = "t_package";

    public static final String COLUMN_IC_LAUNCHER = "ic_launcher";
    public static final String COLUMN_APPLICATION_LABEL = "application_label";
    public static final String COLUMN_PACKAGE_NAME = "package_name";
    public static final String COLUMN_VERSION_NAME = "version_name";
    public static final String COLUMN_VERSION_CODE = "version_code";

}
