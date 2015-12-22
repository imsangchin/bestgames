
package com.miui.permcenter.root;

import java.util.Comparator;

public class RootHeaderComparator implements Comparator<RootHeaderModel> {

    @Override
    public int compare(RootHeaderModel lhs, RootHeaderModel rhs) {
        RootHeaderType lHeaderType = lhs.getRootHeaderType();
        RootHeaderType rHeaderType = rhs.getRootHeaderType();

        return lHeaderType.ordinal() - rHeaderType.ordinal();
    }

}
