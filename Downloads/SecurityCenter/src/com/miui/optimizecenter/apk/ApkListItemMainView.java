
package com.miui.optimizecenter.apk;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.miui.common.ApkIconHelper;
import com.miui.common.BindableView;
import com.miui.common.EventHandler;
import com.miui.common.ExpandableListView.ListItemCollapseListener;
import com.miui.common.ExpandableListView.ListItemExpandListener;
import com.miui.guardprovider.service.ProxyFileInfo;
import com.miui.optimizecenter.enums.ApkStatus;
import com.miui.optimizecenter.enums.SecurityStatus;
import com.miui.optimizecenter.event.CleanApkItemEvent;
import com.miui.optimizecenter.event.EventType;
import com.miui.optimizecenter.event.PerformItemClickEvent;
import com.miui.optimizecenter.event.ViewApkDetailsEvent;
import com.miui.optimizecenter.tools.ApkUtils;
import com.miui.securitycenter.AidlProxyHelper;

import miui.text.ExtraTextUtils;
import com.miui.securitycenter.R;

public class ApkListItemMainView extends FrameLayout implements BindableView<ApkModel>,
        OnCheckedChangeListener, OnClickListener, ListItemExpandListener,
        ListItemCollapseListener {
    private EventHandler mEventHandler;
    private ImageView mIconView;
    private TextView mTitleView;
    private TextView mSizeView;
    private TextView mStatusView;
    private TextView mVersionView;
    private CheckBox mCheckBox;
    private ImageView mSubscriptView;
    private ImageView mIndicatorCloseView;
    private ImageView mIndicatorOpenView;

    private ApkModel mModel;

    public ApkListItemMainView(Context context) {
        this(context, null);
    }

    public ApkListItemMainView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ApkListItemMainView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mIconView = (ImageView) findViewById(R.id.icon);
        mSubscriptView = (ImageView) findViewById(R.id.subscript);
        mTitleView = (TextView) findViewById(R.id.title);
        mSizeView = (TextView) findViewById(R.id.size);
        mStatusView = (TextView) findViewById(R.id.status);
        mVersionView = (TextView) findViewById(R.id.version);
        mCheckBox = (CheckBox) findViewById(R.id.check);
        mIndicatorCloseView = (ImageView) findViewById(R.id.indicator_close);
        mIndicatorOpenView = (ImageView) findViewById(R.id.indicator_open);
        mSubscriptView.setVisibility(View.GONE);

        setOnClickListener(this);
    }

    @Override
    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    @Override
    public void fillData(ApkModel model) {
        mModel = model;

        updateUI();
    }

    protected void updateUI() {
        ApkIconHelper.getInstance(getContext()).loadFileIcon(mIconView, mModel.getAbsolutePath());

        mCheckBox.setOnCheckedChangeListener(null);
        SecurityStatus status = mModel.getSecurityStatus();

        mTitleView.setText(mModel.getApplicationLabel());
        mSizeView.setText(ExtraTextUtils.formatFileSize(getContext(), mModel.getFileSize()));
        String version = getResources().getString(R.string.hints_apk_version,
                mModel.getVersionName());
        mVersionView.setText(version);
        mStatusView.setText(getStatusCharSequence(mModel.getStatus()));

        mCheckBox.setChecked(mModel.adviseDelete());
        mCheckBox.setOnCheckedChangeListener(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (mModel != null && hasWindowFocus) {
            ProxyFileInfo fileInfo = AidlProxyHelper.getInstance().getProxyFileInfo(
                    mModel.getAbsolutePath());
            if (fileInfo != null && fileInfo.exists()) {
                ApkUtils.checkApkStatus(getContext(), mModel, mModel.getAbsolutePath(),
                        AidlProxyHelper.getInstance().getIFileProxy());
                updateUI();
            } else {
                mEventHandler.sendEventMessage(EventType.EVENT_CLEAN_APK_ITEM,
                        CleanApkItemEvent.create(mModel));
            }
        }
    }

    @Override
    public void onClick(View v) {
        final Integer position = (Integer) getTag(R.id.position);
        mEventHandler.sendEventMessage(EventType.EVENT_PERFORM_ITEM_CLICK,
                PerformItemClickEvent.create(this, position));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mModel.setAdviseDelete(isChecked);
    }

    private CharSequence getStatusCharSequence(ApkStatus status) {
        switch (status) {
            case UNINSTALLED:
                return getResources().getString(R.string.apk_status_uninstalled);
            case INSTALLED_OLD:
                return getResources().getString(R.string.apk_status_installed_old);
            case INSTALLED:
                return getResources().getString(R.string.apk_status_install);
            case DUPLICATE:
                return getResources().getString(R.string.apk_status_duplicate);
            case DAMAGED:
                return getResources().getString(R.string.apk_status_damage);
            default:
                return null;
        }
    }

    @Override
    public void willExpand(View v) {
        // TODO Auto-generated method stub

    }

    @Override
    public void didExpand(View v) {
        mIndicatorCloseView.setVisibility(View.GONE);
        mIndicatorOpenView.setVisibility(View.VISIBLE);
    }

    @Override
    public void willCollapse(View v) {
        // TODO Auto-generated method stub

    }

    @Override
    public void didCollapse(View v) {
        mIndicatorOpenView.setVisibility(View.GONE);
        mIndicatorCloseView.setVisibility(View.VISIBLE);
    }
}
