
package com.miui.optimizecenter.apk;

import miui.app.ProgressDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.miui.common.EventHandler;
import com.miui.common.ExpandableListView;
import com.miui.optimizecenter.event.CleanupListItemsEvent;
import com.miui.optimizecenter.event.EventType;

import com.miui.securitycenter.R;

public class ApkActivityView extends LinearLayout implements OnClickListener {

    private EventHandler mEventHandler;

    private TextView mHeaderLeftView;
    private TextView mHeaderRightView;

    private ExpandableListView mApkListView;

    private ProgressDialog mLoadingDialog;

    public ApkActivityView(Context context) {
        this(context, null);
    }

    public ApkActivityView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHeaderLeftView = (TextView) findViewById(R.id.left_text);
        mHeaderRightView = (TextView) findViewById(R.id.right_text);

        mApkListView = (ExpandableListView) findViewById(R.id.apk_list);

        findViewById(R.id.cleanup).setOnClickListener(this);
    }

    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    public void setApkListAdapter(ApkListAdapter adapter) {
        mApkListView.setAdapter(adapter);
    }

    public void collapseAllItems(boolean anim) {
        mApkListView.collapseAllItem(anim);
    }

    public void performItemClick(View view, int position, int id) {
        mApkListView.performItemClick(view, position, id);
    }

    public void setHeaderLeftTitle(CharSequence text) {
        mHeaderLeftView.setText(text);
    }

    public void setHeaderRightTitle(CharSequence text) {
        mHeaderRightView.setText(text);
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
        if (!shown && mApkListView.getEmptyView() == null) {
            mApkListView.setEmptyView(findViewById(R.id.empty_view));
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
