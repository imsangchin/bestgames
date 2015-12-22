
package com.miui.optimizecenter.whitelist;

import miui.app.ProgressDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.miui.common.EventHandler;
import com.miui.common.PinnedHeaderListView;
import com.miui.optimizecenter.event.CleanupListItemsEvent;
import com.miui.optimizecenter.event.EventType;

import com.miui.securitycenter.R;

public class WhiteListActivityView extends RelativeLayout implements View.OnClickListener {

    private EventHandler mEventHandler;

    private Button mCleanupButton;

    private TextView mEmptyView;

    private PinnedHeaderListView mWhiteListView;

    private ProgressDialog mLoadingDialog;

    public WhiteListActivityView(Context context) {
        this(context, null);
    }

    public WhiteListActivityView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WhiteListActivityView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCleanupButton = (Button) findViewById(R.id.cleanup_btn);
        mCleanupButton.setOnClickListener(this);

        mEmptyView = (TextView) findViewById(R.id.empty_view);

        mWhiteListView = (PinnedHeaderListView) findViewById(R.id.white_list);
    }

    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    public void setListAdapter(ListAdapter adapter) {
        mWhiteListView.setAdapter(adapter);
    }

    public void setCleanupButtonEnabled(boolean enabled) {
        mCleanupButton.setEnabled(enabled);
    }

    public void setLoadingShown(boolean shown) {
        if (shown) {
            if (mLoadingDialog == null) {
                mLoadingDialog = ProgressDialog.show(getContext(), null,
                        getResources().getString(R.string.hints_loading_text), true, false);
            }
        } else {
            if (mLoadingDialog != null) {
                mLoadingDialog.cancel();
                mLoadingDialog = null;
            }
        }

        if (!shown && mWhiteListView.getEmptyView() == null) {
            mWhiteListView.setEmptyView(mEmptyView);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cleanup_btn:
                mEventHandler.sendEventMessage(EventType.EVENT_CLEANUP_LIST_ITEMS,
                        CleanupListItemsEvent.create());
                break;
            default:
                break;
        }
    }
}
