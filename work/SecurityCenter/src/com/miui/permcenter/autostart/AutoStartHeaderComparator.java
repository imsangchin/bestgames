
package com.miui.permcenter.autostart;

import java.util.Comparator;

public class AutoStartHeaderComparator implements Comparator<AutoStartHeaderModel> {

    @Override
    public int compare(AutoStartHeaderModel lhs, AutoStartHeaderModel rhs) {
        AutoStartHeaderType lHeaderType = lhs.getAutoStartHeaderType();
        AutoStartHeaderType rHeaderType = rhs.getAutoStartHeaderType();

        return lHeaderType.ordinal() - rHeaderType.ordinal();
    }

}
