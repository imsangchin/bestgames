
package com.miui.securitycenter.handlebar;

public enum HandleItem {
    SYSTEM(0, 0, 0), MEMORY(0, 0, 0), CACHE(0, 0, 0), MIUI_UPDATE(0, 1, 1), CLOUD_ACCOUNT(0, 2, 1), CLOUD_SERVICE(0, 2, 1),
    POWER_OPTIMIZER(0, 3, 1), DATA_FLOW(0, 4, 1), TELECOM_OPERAOTR(0, 4, 1), FLOW_VERIFY(0, 4, 1),
    SAVING_SWITCH(0, 5, 1), FLOW_NOTIFICATION(0, 6, 1), FLOW_PURCHASE(0, 7, 1),
    PERMANENT_NOTIFICATIONBAR(0, 8, 1), GARBAGE_LIB(0, 9, 1), PERMISSION_ROOT(0, 10, 1), APP_UPDATE(0, 11, 0);

    public int mScore;
    public int mWeight;
    public int mChecked;

    private HandleItem(int score, int weight, int checked) {
        mScore = score;
        mWeight = weight;
        mChecked = checked;
    }
}
