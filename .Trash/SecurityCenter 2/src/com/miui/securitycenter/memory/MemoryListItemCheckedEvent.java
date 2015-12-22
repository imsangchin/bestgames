
package com.miui.securitycenter.memory;



public class MemoryListItemCheckedEvent {
    private boolean mChecked;
    private MemoryModel mProcessModel;

    private MemoryListItemCheckedEvent() {

    }

    public static MemoryListItemCheckedEvent create(boolean checked, MemoryModel model) {
        MemoryListItemCheckedEvent res = new MemoryListItemCheckedEvent();
        res.mChecked = checked;
        res.mProcessModel = model;

        return res;
    }

    public boolean isChecked() {
        return mChecked;
    }

    public MemoryModel getProcessModel() {
        return mProcessModel;
    }
}
