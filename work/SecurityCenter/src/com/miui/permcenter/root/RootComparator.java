
package com.miui.permcenter.root;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public class RootComparator implements Comparator<RootModel> {

    private Collator COLLATOR = Collator.getInstance(Locale.getDefault());

    @Override
    public int compare(RootModel lhs, RootModel rhs) {
        return COLLATOR.compare(lhs.getPkgName(), rhs.getPkgName());
    }

}
