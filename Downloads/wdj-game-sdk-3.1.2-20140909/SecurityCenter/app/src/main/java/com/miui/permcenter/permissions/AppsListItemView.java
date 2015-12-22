
package com.miui.permcenter.permissions;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lbe.security.bean.AppPermissionConfig;
import com.miui.common.AndroidUtils;
import com.miui.common.ApkIconHelper;
import com.miui.common.BindableView;
import com.miui.common.EventHandler;
import com.miui.permcenter.PermissionUtils;
import com.miui.permcenter.event.EventType;
import com.miui.permcenter.event.OnPermAppsItemClickEvent;

import com.miui.securitycenter.R;

public class AppsListItemView extends LinearLayout implements BindableView<AppPermissionConfig>,
        OnClickListener {

    private EventHandler mEventHandler;
    private AppPermissionConfig mModel;

    private ImageView mIconView;
    private TextView mTitleView;
    private TextView mSummaryView;

    public AppsListItemView(Context context) {
        this(context, null);
    }

    public AppsListItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppsListItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mIconView = (ImageView) findViewById(R.id.icon);
        mTitleView = (TextView) findViewById(R.id.title);
        mSummaryView = (TextView) findViewById(R.id.summary);

        setOnClickListener(this);
    }

    @Override
    public void fillData(AppPermissionConfig data) {
        mModel = data;

        ApkIconHelper.getInstance(getContext()).loadInstalledAppLauncher(mIconView,
                data.getPackageName());
        mTitleView.setText(AndroidUtils.loadAppLabel(getContext(), data.getPackageName()));

        long[] permissionIds = data.getRequestedPermissionList();
        int count = PermissionUtils.getEffectivePermissionCount(permissionIds);
        mSummaryView.setText(getResources().getString(R.string.hints_apps_perm_count,
                count));
    }

    @Override
    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    @Override
    public void onClick(View v) {
        mEventHandler.sendEventMessage(EventType.EVENT_ON_PERM_APPS_ITEM_CLICK,
                OnPermAppsItemClickEvent.create(mModel.getPackageName()));
    }
}
