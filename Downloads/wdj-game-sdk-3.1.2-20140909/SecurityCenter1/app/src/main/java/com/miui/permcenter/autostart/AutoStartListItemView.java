
package com.miui.permcenter.autostart;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.miui.common.ApkIconHelper;
import com.miui.common.BindableView;
import com.miui.common.EventHandler;
import com.miui.permcenter.event.EnableAppAutoStartEvent;
import com.miui.permcenter.event.EventType;

import miui.widget.SlidingButton;

import com.miui.securitycenter.R;

public class AutoStartListItemView extends LinearLayout implements
        BindableView<AutoStartModel>, OnCheckedChangeListener {

    private EventHandler mEventHandler;

    private ImageView mIconView;
    private TextView mTitleView;
    private TextView mSummaryView;

    private SlidingButton mSlidingButton;

    private AutoStartModel mModel;

    public AutoStartListItemView(Context context) {
        this(context, null);
    }

    public AutoStartListItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoStartListItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mIconView = (ImageView) findViewById(R.id.icon);
        mTitleView = (TextView) findViewById(R.id.title);
        mSummaryView = (TextView) findViewById(R.id.summary);
        mSlidingButton = (SlidingButton) findViewById(R.id.sliding_button);
    }

    @Override
    public void fillData(AutoStartModel data) {
        mSlidingButton.setOnCheckedChangeListener(null);
        mModel = data;

        ApkIconHelper.getInstance(getContext()).loadInstalledAppLauncher(mIconView,
                data.getPkgName());
        mTitleView.setText(data.getAppLabel());
        mSummaryView.setText(data.getWarningInfo());

        mSlidingButton.setChecked(data.isAutoStartEnabled());
        mSlidingButton.setOnCheckedChangeListener(this);
    }

    @Override
    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mEventHandler.sendEventMessage(EventType.EVENT_ENABLE_APP_AUTO_START,
                EnableAppAutoStartEvent.create(mModel.getPkgName(), isChecked));
    }
}
