
package com.miui.permcenter.permissions;

import java.util.Comparator;

public class PermissionGroupComparator implements Comparator<Long> {

    @Override
    public int compare(Long arg0, Long arg1) {
        return (int) (arg0 - arg1);
    }

}
