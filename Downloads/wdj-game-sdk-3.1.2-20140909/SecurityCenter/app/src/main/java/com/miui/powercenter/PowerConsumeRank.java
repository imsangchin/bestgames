
package com.miui.powercenter;

import miui.app.ActionBar;
import miui.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.widget.Button;
import android.content.Context;


import com.miui.securitycenter.R;

public class PowerConsumeRank extends Activity{

    private static final String TAG = "PowerConsumeRank";
    private static final String INTENT_START_HISTORY="com.android.settings.action.BATTERY_HISTORY_DETAIL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createActionBar();
        customizeActionBar();
    }

    private void createActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setFragmentViewPagerMode(this, getFragmentManager());

        // Set up tabs
        actionBar.addFragmentTab(SoftwareRankFragment.TAG, actionBar.newTab().setText(
                R.string.power_consume_rank_software), SoftwareRankFragment.class, null, false);
        actionBar.addFragmentTab(HardwareRankFragment.TAG, actionBar.newTab().setText(
                R.string.power_consume_rank_hardware), HardwareRankFragment.class, null, false);
    }
    private void customizeActionBar() {
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
        final Button settingButton = new Button(this);
        settingButton.setBackgroundResource(R.drawable.history_button_selector);
        settingButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(INTENT_START_HISTORY);
                v.getContext().startActivity(intent);
            }
        });
        ActionBar.LayoutParams alp = new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        actionBar.setCustomView(settingButton, alp);
    }
}
