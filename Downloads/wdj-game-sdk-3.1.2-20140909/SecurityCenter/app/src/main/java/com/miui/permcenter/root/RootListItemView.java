
package com.miui.permcenter.root;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.miui.common.ApkIconHelper;
import com.miui.common.BindableView;
import com.miui.common.EventHandler;
import com.miui.permcenter.event.EnableAppGetRootEvent;
import com.miui.permcenter.event.EventType;

import miui.widget.SlidingButton;

import com.miui.securitycenter.R;

public class RootListItemView extends LinearLayout implements
        BindableView<RootModel>, OnCheckedChangeListener {

    private EventHandler mEventHandler;

    private ImageView mIconView;
    private TextView mTitleView;

    private SlidingButton mSlidingButton;

    private RootModel mModel;

    public RootListItemView(Context context) {
        this(context, null);
    }

    public RootListItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RootListItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mIconView = (ImageView) findViewById(R.id.icon);
        mTitleView = (TextView) findViewById(R.id.title);
        mSlidingButton = (SlidingButton) findViewById(R.id.sliding_button);
    }

    @Override
    public void fillData(RootModel data) {
        mSlidingButton.setOnCheckedChangeListener(null);
        mModel = data;

        ApkIconHelper.getInstance(getContext()).loadInstalledAppLauncher(mIconView,
                data.getPkgName());
        mTitleView.setText(data.getAppLabel());

        mSlidingButton.setChecked(data.isRootEnabled());
        mSlidingButton.setOnCheckedChangeListener(this);
    }

    @Override
    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mEventHandler.sendEventMessage(EventType.EVENT_ENABLE_APP_GET_ROOT,
                EnableAppGetRootEvent.create(mModel.getPkgName(), isChecked));
    }
}
