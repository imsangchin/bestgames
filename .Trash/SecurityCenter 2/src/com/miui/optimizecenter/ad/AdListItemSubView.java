
package com.miui.optimizecenter.ad;

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
import com.miui.optimizecenter.event.EventType;
import com.miui.optimizecenter.event.NotifyDataSetChangedEvent;
import com.miui.optimizecenter.event.ViewFileEvent;
import com.miui.securitycenter.R;

public class AdListItemSubView extends LinearLayout implements BindableView<AdModel>,
        OnClickListener {

    private EventHandler mEventHandler;
    private AdModel mData;

    private TextView mViewFileButton;
    private TextView mCleanButton;
    private TextView mAddWhiteListButton;

    private TextView mInfoView;

    public AdListItemSubView(Context context) {
        this(context, null);
    }

    public AdListItemSubView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setOnClickListener(this);

        mViewFileButton = (TextView) findViewById(R.id.view_file);
        mCleanButton = (TextView) findViewById(R.id.clean);
        mAddWhiteListButton = (TextView) findViewById(R.id.add_white_list);

        mViewFileButton.setOnClickListener(this);
        mCleanButton.setOnClickListener(this);
        mAddWhiteListButton.setOnClickListener(this);

        mInfoView = (TextView) findViewById(R.id.info);
    }

    @Override
    public void fillData(AdModel data) {
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
                        ViewFileEvent.create(mData.getDirectoryPath()));
                break;
            case R.id.clean:
                mEventHandler.sendEventMessage(EventType.EVENT_CLEAN_AD_ITEM,
                        CleanAdItemEvent.create(mData));
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
