
package com.miui.permcenter.permissions;

import android.content.Context;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import com.lbe.security.bean.AppPermissionConfig;
import com.miui.common.AndroidUtils;

public class AppPermissionsComparator implements Comparator<AppPermissionConfig> {

    private Collator COLLATOR = Collator.getInstance(Locale.getDefault());

    private Context mContext;

    public AppPermissionsComparator(Context context) {
        mContext = context;
    }

    @Override
    public int compare(AppPermissionConfig lhs, AppPermissionConfig rhs) {
        String lAppLabel = AndroidUtils.loadAppLabel(mContext, lhs.getPackageName()).toString();
        String rAppLabel = AndroidUtils.loadAppLabel(mContext, rhs.getPackageName()).toString();

        return COLLATOR.compare(lAppLabel, rAppLabel);
    }

}
