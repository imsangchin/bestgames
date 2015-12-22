
package com.miui.optimizecenter.deepclean;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
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
import com.miui.guardprovider.service.ProxyFileInfo;
import com.miui.optimizecenter.ad.AdModel;
import com.miui.optimizecenter.apk.ApkModel;
import com.miui.optimizecenter.enums.ApkStatus;
import com.miui.optimizecenter.enums.SecurityStatus;
import com.miui.optimizecenter.event.AddToWhiteListEvent;
import com.miui.optimizecenter.event.BackupUninstallAppEvent;
import com.miui.optimizecenter.event.CleanAdItemEvent;
import com.miui.optimizecenter.event.CleanApkItemEvent;
import com.miui.optimizecenter.event.EventType;
import com.miui.optimizecenter.event.UninstallAppEvent;
import com.miui.optimizecenter.event.ViewAppDetailsEvent;
import com.miui.optimizecenter.event.ViewFileEvent;
import com.miui.optimizecenter.event.ViewInstalledAppDetailsEvent;
import com.miui.optimizecenter.tools.ApkUtils;
import com.miui.securitycenter.AidlProxyHelper;
import com.miui.securitycenter.DateTimeUtils;

import miui.text.ExtraTextUtils;
import com.miui.securitycenter.R;

public class InstalledAppsListItemSubView extends LinearLayout implements
        BindableView<InstalledAppModel>, OnClickListener {
    private EventHandler mEventHandler;
    private InstalledAppModel mData;

    private TextView mAppDetailsButton;
    private TextView mViewFileButton;
    private TextView mBackupUninstallButton;
    private TextView mUninstallButton;

    private TextView mInfoView;

    public InstalledAppsListItemSubView(Context context) {
        this(context, null);
    }

    public InstalledAppsListItemSubView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setOnClickListener(this);

        mAppDetailsButton = (TextView) findViewById(R.id.app_details);
        mViewFileButton = (TextView) findViewById(R.id.view_file);
        mBackupUninstallButton = (TextView) findViewById(R.id.backup_uninstall);
        mUninstallButton = (TextView) findViewById(R.id.uninstall);

        mAppDetailsButton.setOnClickListener(this);
        mViewFileButton.setOnClickListener(this);
        mBackupUninstallButton.setOnClickListener(this);
        mUninstallButton.setOnClickListener(this);

        mInfoView = (TextView) findViewById(R.id.info);
    }

    @Override
    public void fillData(InstalledAppModel data) {
        mData = data;

        String info = data.getSecurityInfo();
        if (TextUtils.isEmpty(info)) {
            mInfoView.setVisibility(View.GONE);
            mInfoView.setText(null);
        } else {
            mInfoView.setVisibility(View.VISIBLE);
            mInfoView.setText(info);
        }
    }

    @Override
    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.app_details:
                mEventHandler.sendEventMessage(EventType.EVENT_VIEW_APP_DETAILS,
                        ViewAppDetailsEvent.create(mData.getPackageInfo().packageName));
                break;
            case R.id.view_file:
                mEventHandler.sendEventMessage(EventType.EVENT_VIEW_FILE,
                        ViewFileEvent.create(mData.getPackageInfo().packageName));
                break;
            case R.id.backup_uninstall:
                mEventHandler.sendEventMessage(EventType.EVENT_BACKUP_UNINSTALL_APP,
                        BackupUninstallAppEvent.create(mData.getPackageInfo().packageName));
                break;
            case R.id.uninstall:
                mEventHandler.sendEventMessage(EventType.EVENT_UNINSTALL_APP,
                        UninstallAppEvent.create(mData.getPackageInfo().packageName, false));
                break;

            default:
                break;
        }
    }

}
