
package com.miui.securitycenter.manualitem;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import miui.app.ProgressDialog;

import com.miui.common.AndroidUtils;
import com.miui.common.EventHandler;
import com.miui.common.PinnedHeaderListView;
import com.miui.securitycenter.R;
import com.miui.securitycenter.event.CleanupListItemsEvent;
import com.miui.securitycenter.event.EventType;

public class WhiteListActivityView extends RelativeLayout implements OnClickListener {

    private ListView mWhiteListView;
    private EventHandler mEventHandler;
    private TextView mEmptyView;
    private TextView mHeaderTitle;
    private ProgressDialog mLoadingDialog;
    private Button mCleanupButton;

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
        // TODO Auto-generated method stub
        super.onFinishInflate();
        mWhiteListView = (ListView) findViewById(R.id.white_list);
        mEmptyView = (TextView) findViewById(R.id.empty_view);
        mHeaderTitle = (TextView) findViewById(R.id.header_title);
        mCleanupButton = (Button) findViewById(R.id.cleanup_btn);
        mCleanupButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.cleanup_btn:
                mEventHandler.sendEventMessage(EventType.EVENT_CLEANUP_LIST_ITEMS,
                        CleanupListItemsEvent.create());
                break;
            default:
                break;
        }
    }

    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    public void setListAdapter(ListAdapter adapter) {
        mWhiteListView.setAdapter(adapter);
    }

    public void setHeaderVisibility(boolean shown) {
        if (shown) {
            mHeaderTitle.setVisibility(View.VISIBLE);
        }
        else {
            mHeaderTitle.setVisibility(View.GONE);
        }
    }

    public void setHeaderTitle(String text, String target) {
        int color = getResources().getColor(R.color.high_light_green);
        mHeaderTitle.setText(AndroidUtils.getHighLightString(text, color, target));
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

    public void setCleanupButtonEnabled(boolean enabled) {
        // TODO Auto-generated method stub
        mCleanupButton.setEnabled(enabled);
    }
}
