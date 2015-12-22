
package com.miui.antivirus;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.miui.antivirus.VirusCheckManager.ScanItemType;
import com.miui.antivirus.event.EventType;
import com.miui.antivirus.event.OnRiskListItemClickEvent;
import com.miui.common.ApkIconHelper;
import com.miui.common.EventHandler;

import com.miui.securitycenter.R;

public class RiskListItemView extends LinearLayout implements OnClickListener {

    private EventHandler mEventHandler;

    private ImageView mIconView;
    private TextView mTitleView;
    private TextView mSummaryView;

    private VirusModel mData;

    public RiskListItemView(Context context) {
        this(context, null);
    }

    public RiskListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mIconView = (ImageView) findViewById(R.id.icon);
        mTitleView = (TextView) findViewById(R.id.title);
        mSummaryView = (TextView) findViewById(R.id.summary);

        setOnClickListener(this);
    }

    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    public void fillData(VirusModel data) {
        mData = data;

        mTitleView.setText(data.getAppLabel());
        ScanItemType itemType = data.getScanItemType();
        switch (itemType) {
            case INSTALLED_APP:
                mSummaryView.setText(R.string.hints_risk_app_list_item_summary);
                ApkIconHelper.getInstance(getContext()).loadInstalledAppLauncher(mIconView,
                        data.getPkgName());
                break;
            case UNINSTALLED_APK:
                mSummaryView.setText(R.string.hints_risk_apk_list_item_summary);
                ApkIconHelper.getInstance(getContext())
                        .loadFileIcon(mIconView, data.getSourceDir());
                break;

            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        mEventHandler.sendEventMessage(EventType.EVENT_ON_RISK_LIST_ITEM_CLICK,
                OnRiskListItemClickEvent.create(mData));
    }
}
