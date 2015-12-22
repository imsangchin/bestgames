
package com.miui.powercenter;

import android.app.ListFragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.util.Log;

import com.miui.securitycenter.R;
import com.miui.powercenter.PowerDetailActivity.DrainType;
import com.miui.powercenter.PowerRankHelper.BatterySipperListener;

import java.util.List;

public abstract class PowerRankFragment extends ListFragment implements BatterySipperListener,
        OnItemClickListener {

    public static final String TAG = "PowerRankFragement";

    protected PowerRankHelper mRankHelper;
    private PowerRankAdapter mAdapter;
    private TextView mTitle;

    private String mRankTitle;
    private List<BatterySipper> mUsageList;
    static final int MSG_REPORT_FULLY_DRAWN = 2;
    static final int MSG_RANKHELPER_FULLY_DRAWN = 3;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REPORT_FULLY_DRAWN: {
                    mTitle.setText(mRankTitle);
                    mRankTitle = getString(getTitleRes(), getConsumePercent());
                    mUsageList = getUsageList();
                    refreshStats(mUsageList);
                }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRankHelper = new PowerRankHelper(getActivity(),mHandler);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        View v = inflater.inflate(R.layout.pc_power_consume_rank_fragment, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mAdapter = new PowerRankAdapter();
        getListView().setAdapter(mAdapter);
        getListView().setOnItemClickListener(this);
        mRankHelper.addBatterySipperListener(this);

        mTitle = (TextView) view.findViewById(R.id.power_consume_total);
    }
    @Override
    public void onResume() {
        mRankHelper.onResume(getActivity());
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRankHelper.onPause();
    }

    private void refreshStats(List<BatterySipper> usageList) {
        mAdapter.removeAll();
        BatterySipper other = null;
        for (BatterySipper sipper : usageList) {
            sipper.percent = ((sipper.getSortValue() / getTotalConsume()) * 100);
            if (this instanceof SoftwareRankFragment && (int) Math.round(sipper.percent) < 1) {
                if (other == null) {
                    other = new BatterySipper(getActivity(), null, null,
                            getString(R.string.power_consume_other), DrainType.OTHER,
                            R.drawable.power_consume_other, null, null);
                    other.iconId = R.drawable.power_consume_other;
                    other.percent = 0;
                }
                other.percent += sipper.percent;
            } else {
                mAdapter.add(sipper);
            }
        }
        if (other != null) {
            mAdapter.add(other);
        }
        mAdapter.notifyDataSetChanged();
    }

    protected double getTotalConsume() {
        return mRankHelper.getUsageTotle();
    }

    @Override
    public void OnUpdate() {
        if (isResumed()) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         BatterySipper sipper = (BatterySipper) mAdapter.getItem(position);
         mRankHelper.startPowerDetail(getActivity(), sipper);
    }

    /**
     * for subclass to override
     */
    abstract protected int getConsumePercent();

    abstract protected int getTitleRes();

    abstract protected List<BatterySipper> getUsageList();
}
