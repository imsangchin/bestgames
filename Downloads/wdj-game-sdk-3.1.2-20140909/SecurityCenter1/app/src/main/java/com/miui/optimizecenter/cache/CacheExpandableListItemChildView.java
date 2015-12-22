
package com.miui.optimizecenter.cache;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.miui.common.ApkIconHelper;
import com.miui.common.BaseExpandableListItemChildView;
import com.miui.common.ExpandableListView.ListItemCollapseListener;
import com.miui.common.ExpandableListView.ListItemExpandListener;
import com.miui.guardprovider.service.ProxyFileInfo;
import com.miui.optimizecenter.event.CleanCacheItemEvent;
import com.miui.optimizecenter.event.EventType;
import com.miui.optimizecenter.event.NotifyListUpdateEvent;
import com.miui.optimizecenter.event.ViewCacheDetailsEvent;
import com.miui.securitycenter.AidlProxyHelper;

import miui.text.ExtraTextUtils;

import com.miui.securitycenter.R;

public class CacheExpandableListItemChildView extends
        BaseExpandableListItemChildView<CacheModel> implements ListItemExpandListener,
        ListItemCollapseListener {

    private TextView mTitleView;
    private TextView mContentView;
    private CheckBox mCheckBox;
    private ImageView mIndicatorCloseView;
    private ImageView mIndicatorOpenView;

    private CacheModel mModel;

    public CacheExpandableListItemChildView(Context context) {
        this(context, null);
    }

    public CacheExpandableListItemChildView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CacheExpandableListItemChildView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTitleView = (TextView) findViewById(R.id.title);
        mContentView = (TextView) findViewById(R.id.content);
        mCheckBox = (CheckBox) findViewById(R.id.checkbox);
        mIndicatorCloseView = (ImageView) findViewById(R.id.indicator_close);
        mIndicatorOpenView = (ImageView) findViewById(R.id.indicator_open);

    }

    @Override
    public void fillData(CacheModel model) {
        mModel = model;
        mCheckBox.setOnCheckedChangeListener(null);
        mTitleView.setText(model.getCacheType());
        mContentView.setText(ExtraTextUtils.formatFileSize(getContext(), model.getFileSize()));
        mCheckBox.setChecked(model.adviseDelete());
        mCheckBox.setOnCheckedChangeListener(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (mModel != null && hasWindowFocus) {
            String pkg = mModel.getPackageName();
            if (pkg != ApkIconHelper.PKG_SYSTEM_CACHE && pkg != ApkIconHelper.PKG_EMPTY_FOLDER) {
                ProxyFileInfo fileInfo = AidlProxyHelper.getInstance().getProxyFileInfo(
                        mModel.getDirectoryPath());
                if (fileInfo == null || !fileInfo.exists()) {
                    mEventHandler.sendEventMessage(EventType.EVENT_CLEAN_CACHE_ITEM,
                            CleanCacheItemEvent.create(mModel));
                }
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mModel.setAdviseDelete(isChecked);
        mEventHandler.sendEventMessage(EventType.EVENT_NOTIFY_LIST_UPDATE,
                NotifyListUpdateEvent.create(true));
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
