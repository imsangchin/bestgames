
package com.miui.securitycenter.cache;

import miui.text.ExtraTextUtils;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.miui.common.ApkIconHelper;
import com.miui.common.EventHandler;
import com.miui.securitycenter.event.EventType;

import com.miui.securitycenter.R;

public class CacheListItemView extends LinearLayout implements OnCheckedChangeListener,
        OnClickListener {
    private EventHandler mEventHandler;
    private CacheModel mModel;

    private ImageView mIconView;
    private TextView mNameView;
    private TextView mSummaryView;
    private CheckBox mCheckView;

    public CacheListItemView(Context context) {
        this(context, null);
    }

    public CacheListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mIconView = (ImageView) findViewById(R.id.icon);
        mNameView = (TextView) findViewById(R.id.name);
        mSummaryView = (TextView) findViewById(R.id.summary);
        mCheckView = (CheckBox) findViewById(R.id.check);

        setOnClickListener(this);
    }

    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    public void fillData(CacheModel model) {
        mCheckView.setOnCheckedChangeListener(null);
        mModel = model;
        ApkIconHelper.getInstance(getContext()).loadInstalledAppLauncher(mIconView,
                model.getPackageName());
        mNameView.setText(model.getAppName());

        mSummaryView.setText(ExtraTextUtils.formatFileSize(getContext(), model.getCacheSize()));

        mCheckView.setChecked(!model.isLocked());
        mCheckView.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton button, boolean checked) {
        mEventHandler.sendEventMessage(EventType.EVENT_ON_CACHE_LIST_ITEM_CHECKED,
                CacheListItemCheckedEvent.create(checked, mModel));
    }

    @Override
    public void onClick(View v) {
        mCheckView.setChecked(!mCheckView.isChecked());
    }
}
