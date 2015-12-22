
package com.miui.optimizecenter.apk;

import java.util.Comparator;

public class ApkComparator implements Comparator<ApkModel> {

    @Override
    public int compare(ApkModel lhs, ApkModel rhs) {
        if (lhs.adviseDelete() && !rhs.adviseDelete()) {
            return -1;
        } else if (!lhs.adviseDelete() && rhs.adviseDelete()) {
            return 1;
        } else {
            return 0;
        }
    }

}
