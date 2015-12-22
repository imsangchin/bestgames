
package com.miui.permcenter.permissions;

import android.content.Context;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import com.lbe.security.bean.AppPermissionConfig;
import com.miui.common.AndroidUtils;
import com.miui.permcenter.PermissionUtils;

public class PermissionAppsComparator implements Comparator<AppPermissionConfig> {

    private Collator COLLATOR = Collator.getInstance(Locale.getDefault());

    private Context mContext;
    private long mPermissionId;

    public PermissionAppsComparator(Context context, long permissionId) {
        mContext = context;
        mPermissionId = permissionId;
    }

    @Override
    public int compare(AppPermissionConfig lhs, AppPermissionConfig rhs) {
        int lPermAction = PermissionUtils.getPermissionAction(lhs, mPermissionId);
        int rPermAction = PermissionUtils.getPermissionAction(rhs, mPermissionId);

        if (lPermAction == rPermAction) {
            String lAppLabel = AndroidUtils.loadAppLabel(mContext, lhs.getPackageName()).toString();
            String rAppLabel = AndroidUtils.loadAppLabel(mContext, rhs.getPackageName()).toString();

            return COLLATOR.compare(lAppLabel, rAppLabel);
        } else {
            return rPermAction - lPermAction;
        }
    }

}
