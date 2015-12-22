
package com.miui.optimizecenter.whitelist;

import java.util.Comparator;

public class HeaderComparator implements Comparator<WhiteListHeaderModel> {

    @Override
    public int compare(WhiteListHeaderModel lhs, WhiteListHeaderModel rhs) {

        WhiteListType lType = lhs.getWhiteListType();
        WhiteListType rType = rhs.getWhiteListType();

        if (lType.ordinal() > rType.ordinal()) {
            return 1;
        } else if (lType.ordinal() < rType.ordinal()) {
            return -1;
        } else {
            return 0;
        }
    }

}
