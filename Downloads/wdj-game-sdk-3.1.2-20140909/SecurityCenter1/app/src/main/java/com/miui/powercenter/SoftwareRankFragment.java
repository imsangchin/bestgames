
package com.miui.powercenter;

import com.miui.securitycenter.R;

import java.util.List;

public class SoftwareRankFragment extends PowerRankFragment {

    public static final String TAG = "SoftwareRankFragement";

    @Override
    protected int getTitleRes() {
        return R.string.power_consume_total_software;
    }

    @Override
    protected List<BatterySipper> getUsageList() {
        return mRankHelper.getAppUsageList();
    }

    @Override
    protected int getConsumePercent() {
        return 100 - (int) Math.round(mRankHelper.getMiscUsageTotle() / mRankHelper.getUsageTotle() * 100);
    }

}
