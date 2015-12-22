
package com.miui.powercenter;

import com.miui.securitycenter.R;

import java.util.List;

public class HardwareRankFragment extends PowerRankFragment {

    public static final String TAG = "HardwareRankFragment";

    @Override
    protected int getTitleRes() {
        return R.string.power_consume_total_hardware;
    }

    @Override
    protected List<BatterySipper> getUsageList() {
        return mRankHelper.getMiscUsageList();
    }

    @Override
    protected int getConsumePercent() {
        return (int) Math
                .round(mRankHelper.getMiscUsageTotle() / mRankHelper.getUsageTotle() * 100);
    }
}
