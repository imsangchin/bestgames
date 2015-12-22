
package com.miui.permcenter.permissions;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
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
import com.miui.permcenter.event.EventType;
import com.miui.permcenter.event.OnPermAppsItemClickEvent;
import com.miui.securitycenter.R;


public class PermissionAppsListItemView extends LinearLayout implements OnClickListener {
    private EventHandler mEventHandler;
    private AppPermissionConfig mData;

    private TextView mTitleView;
    private TextView mSummaryView;

    private ImageView mIconView;
    private ImageView mActionView;


    public PermissionAppsListItemView(Context context) {
        this(context, null);
    }

    public PermissionAppsListItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PermissionAppsListItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTitleView = (TextView) findViewById(R.id.title);
        mSummaryView = (TextView) findViewById(R.id.summary);

        mIconView = (ImageView) findViewById(R.id.icon);
        mActionView = (ImageView) findViewById(R.id.action);

        setOnClickListener(this);
    }

    public void fillData(long permissionId, AppPermissionConfig data,boolean isEnable) {
        mData = data;

        ApkIconHelper.getInstance(getContext()).loadInstalledAppLauncher(mIconView,
                data.getPackageName());
        if (isEnable) {
            mIconView.setAlpha(1.0f);
        } else {
            mIconView.setAlpha(0.5f);
        }

        mTitleView.setText(AndroidUtils.loadAppLabel(getContext(), data.getPackageName()));
        mSummaryView.setText(data.getDescription(permissionId));

        mTitleView.setEnabled(isEnable);
        setEnabled(isEnable);

        int action = data.getEffectivePermissionConfig(permissionId);
        switch (action) {
            case AppPermissionConfig.ACTION_ACCEPT:
                if (isEnable) {
                    mActionView.setImageResource(R.drawable.icon_action_accept);
                } else {
                    mActionView.setImageResource(R.drawable.icon_action_accept_disable);
                }
                break;
            case AppPermissionConfig.ACTION_PROMPT:
                if (isEnable) {
                    mActionView.setImageResource(R.drawable.icon_action_prompt);
                } else {
                    mActionView.setImageResource(R.drawable.icon_action_prompt_disable);
                }
                break;
            case AppPermissionConfig.ACTION_REJECT:
                if (isEnable) {
                    mActionView.setImageResource(R.drawable.icon_action_reject);
                } else {
                    mActionView.setImageResource(R.drawable.icon_action_reject_disable);
                }
                break;
            default:
                mActionView.setImageDrawable(null);
                break;
        }
    }

    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    @Override
    public void onClick(View v) {
        mEventHandler.sendEventMessage(EventType.EVENT_ON_PERM_APPS_ITEM_CLICK,
                OnPermAppsItemClickEvent.create(mData.getPackageName()));
    }
}
