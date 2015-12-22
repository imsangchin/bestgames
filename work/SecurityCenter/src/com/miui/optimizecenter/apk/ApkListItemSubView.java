
package com.miui.optimizecenter.apk;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.miui.common.BindableView;
import com.miui.common.EventHandler;
import com.miui.optimizecenter.event.AddToWhiteListEvent;
import com.miui.optimizecenter.event.CleanAdItemEvent;
import com.miui.optimizecenter.event.CleanApkItemEvent;
import com.miui.optimizecenter.event.EventType;
import com.miui.optimizecenter.event.InstallApkEvent;
import com.miui.optimizecenter.event.NotifyDataSetChangedEvent;
import com.miui.optimizecenter.event.ViewFileEvent;
import com.miui.securitycenter.R;

public class ApkListItemSubView extends LinearLayout implements BindableView<ApkModel>,
        OnClickListener {

    private EventHandler mEventHandler;
    private ApkModel mData;

    private TextView mViewFileButton;
    private TextView mCleanButton;
    private TextView mInstallButton;
    private TextView mAddWhiteListButton;

    private TextView mInfoView;

    public ApkListItemSubView(Context context) {
        this(context, null);
    }

    public ApkListItemSubView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setOnClickListener(this);

        mViewFileButton = (TextView) findViewById(R.id.view_file);
        mCleanButton = (TextView) findViewById(R.id.clean);
        mInstallButton = (TextView) findViewById(R.id.install);
        mAddWhiteListButton = (TextView) findViewById(R.id.add_white_list);

        mViewFileButton.setOnClickListener(this);
        mCleanButton.setOnClickListener(this);
        mInstallButton.setOnClickListener(this);
        mAddWhiteListButton.setOnClickListener(this);

        mInfoView = (TextView) findViewById(R.id.info);
    }

    @Override
    public void fillData(ApkModel data) {
        mData = data;
    }

    @Override
    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_file:
                mEventHandler.sendEventMessage(EventType.EVENT_VIEW_FILE,
                        ViewFileEvent.create(mData.getAbsolutePath()));
                break;
            case R.id.clean:
                mEventHandler.sendEventMessage(EventType.EVENT_CLEAN_APK_ITEM,
                        CleanApkItemEvent.create(mData));
                break;
            case R.id.install:
                mEventHandler.sendEventMessage(EventType.EVENT_INSTALL_APK,
                        InstallApkEvent.create(mData.getAbsolutePath()));
                break;
            case R.id.add_white_list:
                mEventHandler.sendEventMessage(EventType.EVENT_ADD_TO_WHITE_LIST,
                        AddToWhiteListEvent.create(mData));
                break;

            default:
                break;
        }
    }

}
