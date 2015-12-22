
package com.miui.permcenter.autostart;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public class AutoStartComparator implements Comparator<AutoStartModel> {
    private Collator COLLATOR = Collator.getInstance(Locale.getDefault());

    @Override
    public int compare(AutoStartModel lhs, AutoStartModel rhs) {

        return COLLATOR.compare(lhs.getAppLabel(), rhs.getAppLabel());
    }

}
