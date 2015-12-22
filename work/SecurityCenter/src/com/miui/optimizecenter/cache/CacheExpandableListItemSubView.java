
package com.miui.optimizecenter.cache;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.miui.common.ApkIconHelper;
import com.miui.common.BindableView;
import com.miui.common.EventHandler;
import com.miui.optimizecenter.event.AddToWhiteListEvent;
import com.miui.optimizecenter.event.CleanAdItemEvent;
import com.miui.optimizecenter.event.CleanCacheItemEvent;
import com.miui.optimizecenter.event.EventType;
import com.miui.optimizecenter.event.NotifyDataSetChangedEvent;
import com.miui.optimizecenter.event.ViewFileEvent;
import com.miui.securitycenter.R;

public class CacheExpandableListItemSubView extends FrameLayout implements
        BindableView<CacheModel>, OnClickListener {

    private EventHandler mEventHandler;
    private CacheModel mData;

    private TextView mViewFileButton;
    private TextView mCleanButton;
    private TextView mAddWhiteListButton;

    private TextView mInfoView;

    public CacheExpandableListItemSubView(Context context) {
        this(context, null);
    }

    public CacheExpandableListItemSubView(Context context, AttributeSet attrs) {
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
    public void fillData(CacheModel data) {
        mData = data;

        String pkg = data.getPackageName();
        mViewFileButton.setEnabled(true);
        mAddWhiteListButton.setEnabled(true);
        if (pkg == ApkIconHelper.PKG_SYSTEM_CACHE || pkg == ApkIconHelper.PKG_EMPTY_FOLDER) {
            mViewFileButton.setEnabled(false);
            mAddWhiteListButton.setEnabled(false);
        }

        String info = data.getDescription();
        if (TextUtils.isEmpty(info)) {
            mInfoView.setVisibility(View.GONE);
            mInfoView.setText(null);
        } else {
            mInfoView.setVisibility(View.VISIBLE);
            mInfoView.setText(Html.fromHtml(info));
        }
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
                mEventHandler.sendEventMessage(EventType.EVENT_CLEAN_CACHE_ITEM,
                        CleanCacheItemEvent.create(mData));
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
