
package com.miui.optimizecenter.deepclean;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.miui.common.ApkIconHelper;
import com.miui.common.BindableView;
import com.miui.common.EventHandler;
import com.miui.common.ExpandableListView.ListItemCollapseListener;
import com.miui.common.ExpandableListView.ListItemExpandListener;
import com.miui.guardprovider.service.ProxyFileInfo;
import com.miui.optimizecenter.apk.ApkModel;
import com.miui.optimizecenter.enums.ApkStatus;
import com.miui.optimizecenter.enums.SecurityStatus;
import com.miui.optimizecenter.event.CleanApkItemEvent;
import com.miui.optimizecenter.event.EventType;
import com.miui.optimizecenter.event.PerformItemClickEvent;
import com.miui.optimizecenter.event.UninstallAppEvent;
import com.miui.optimizecenter.event.ViewInstalledAppDetailsEvent;
import com.miui.optimizecenter.tools.ApkUtils;
import com.miui.securitycenter.AidlProxyHelper;
import com.miui.securitycenter.DateTimeUtils;

import miui.text.ExtraTextUtils;
import com.miui.securitycenter.R;

public class InstalledAppsListItemMainView extends LinearLayout implements
        BindableView<InstalledAppModel>,
        OnCheckedChangeListener, OnClickListener, ListItemExpandListener,
        ListItemCollapseListener {
    private EventHandler mEventHandler;

    private ImageView mIconView;
    private TextView mNameView;
    private TextView mSummaryView;
    private TextView mContentView;
    private CheckBox mCheckBox;
    private ImageView mIndicatorCloseView;
    private ImageView mIndicatorOpenView;

    private ImageView mSubscriptView;

    private InstalledAppModel mModel;

    public InstalledAppsListItemMainView(Context context) {
        this(context, null);
    }

    public InstalledAppsListItemMainView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InstalledAppsListItemMainView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mIconView = (ImageView) findViewById(R.id.icon);
        mNameView = (TextView) findViewById(R.id.name);
        mSummaryView = (TextView) findViewById(R.id.summary);
        mContentView = (TextView) findViewById(R.id.content);
        mCheckBox = (CheckBox) findViewById(R.id.checkbox);
        mCheckBox.setOnCheckedChangeListener(this);
        mSubscriptView = (ImageView) findViewById(R.id.subscript);
        mIndicatorCloseView = (ImageView) findViewById(R.id.indicator_close);
        mIndicatorOpenView = (ImageView) findViewById(R.id.indicator_open);

        setOnClickListener(this);
    }

    @Override
    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    @Override
    public void fillData(InstalledAppModel model) {
        mModel = model;

        ApkIconHelper.getInstance(getContext()).loadInstalledAppLauncher(mIconView,
                model.getPackageInfo().packageName);

        mSummaryView.setVisibility(View.VISIBLE);
        SecurityStatus status = model.getSecurityStatus();
        if (SecurityStatus.SAFE == status) {
            mSubscriptView.setVisibility(View.GONE);
            mNameView.setTextColor(getResources().getColor(R.color.ListItemMainViewTextPrimary));
            mSummaryView.setTextColor(getResources()
                    .getColor(R.color.ListItemMainViewTextSecondary));
        } else {
            mSubscriptView.setVisibility(View.VISIBLE);
            mNameView.setTextColor(getResources().getColor(R.color.ListItemMainViewTextRisk));
            mSummaryView.setTextColor(getResources().getColor(R.color.ListItemMainViewTextRisk));
        }

        mNameView.setText(model.getPackageInfo().applicationInfo.loadLabel(getContext()
                .getPackageManager()));

        long appSize = model.getSizeStats().internalSize + model.getSizeStats().externalSize;
        mContentView.setText(ExtraTextUtils.formatFileSize(getContext(), appSize));

        if (SecurityStatus.VIRUS == status) {
            mSummaryView.setText(getContext().getString(
                    R.string.hints_install_apps_virus));
        } else if (SecurityStatus.RISK == status) {
            mSummaryView.setText(getContext().getString(
                    R.string.hints_install_apps_risk));
        } else {
            if (model.getLastLunchTime() == InstalledAppModel.TIME_INVALID) {
                mSummaryView.setText(getContext().getString(
                        R.string.hints_install_apps_new));
            } else {
                int interval = DateTimeUtils.getFromNowDayInterval(model.getLastLunchTime());
                if (interval < 0) {
                    mSummaryView.setText(null);
                    mSummaryView.setVisibility(View.GONE);
                } else if (interval == 0) {
                    mSummaryView.setText(getContext().getString(
                            R.string.hints_install_apps_today));
                } else {
                    mSummaryView.setText(getContext().getString(
                            R.string.hints_install_apps_interval, interval));
                }
            }
        }

        mCheckBox.setChecked(model.adviseDelete());
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            try {
                PackageManager pm = getContext().getPackageManager();
                PackageInfo info = pm.getPackageInfo(mModel.getPackageInfo().packageName, 0);
                if (info == null) {
                    mEventHandler.sendEventMessage(EventType.EVENT_UNINSTALL_APP,
                            UninstallAppEvent.create(mModel.getPackageInfo().packageName,
                                    true));
                }
            } catch (Exception e) {
                // ignore
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
