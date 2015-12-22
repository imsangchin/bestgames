
package com.miui.optimizecenter.cache;

import miui.app.ProgressDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.miui.common.EventHandler;
import com.miui.common.MultiExpandableListView;
import com.miui.optimizecenter.event.CleanupListItemsEvent;
import com.miui.optimizecenter.event.EventType;

import com.miui.securitycenter.R;

public class CacheActivityView extends LinearLayout implements OnClickListener {

    private EventHandler mEventHandler;

    private TextView mHeaderLeftTextView;
    private TextView mHeaderRightTextView;

    private MultiExpandableListView mCacheListView;

    private View mEmptyView;

    private ProgressDialog mLoadingDialog;

    public CacheActivityView(Context context) {
        this(context, null);
    }

    public CacheActivityView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHeaderLeftTextView = (TextView) findViewById(R.id.left_text);
        mHeaderRightTextView = (TextView) findViewById(R.id.right_text);

        mCacheListView = (MultiExpandableListView) findViewById(R.id.cache_list);
        mEmptyView = findViewById(R.id.empty_view);

        findViewById(R.id.cleanup).setOnClickListener(this);
    }

    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    public void setCacheListAdapter(CacheExpandableListAdater adapter) {
        mCacheListView.setAdapter(adapter);
    }

    public void collapseAllItem(boolean anim) {
        mCacheListView.collapseAllItem(anim);
    }

    public void expandListAllGroups(int groupsCount) {
        try {
            for (int i = 0; i < groupsCount; i++) {
                mCacheListView.expandGroup(i);
            }
        } catch (Exception e) {
            // ignore
        }
    }

    public void expandListGroup(int groupPos) {
        mCacheListView.expandGroup(groupPos, true);
    }

    public void collapseListGroup(int groupPos) {
        mCacheListView.collapseGroup(groupPos);
    }

    public void setHeaderLeftText(CharSequence leftText) {
        mHeaderLeftTextView.setText(leftText);
    }

    public void setHeaderRightText(CharSequence rightText) {
        mHeaderRightTextView.setText(rightText);
    }

    public void setHeaderBarShown(boolean shown) {
        findViewById(R.id.header_bar).setVisibility(shown ? View.VISIBLE : View.INVISIBLE);
    }

    public void setCleanupButtonEnabled(boolean enabled) {
        findViewById(R.id.cleanup).setEnabled(enabled);
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
        if (!shown && mCacheListView.getEmptyView() == null) {
            mCacheListView.setEmptyView(mEmptyView);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.cleanup) {
            mEventHandler.sendEventMessage(EventType.EVENT_CLEANUP_LIST_ITEMS,
                    CleanupListItemsEvent.create());
        }
    }
}
