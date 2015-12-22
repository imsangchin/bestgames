
package com.miui.securitycenter.cache;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.miui.securitycenter.R;
import com.miui.common.EventHandler;
import com.miui.securitycenter.cache.CacheActivity.CacheListAdapter;
import com.miui.securitycenter.event.CleanupListEvent;
import com.miui.securitycenter.event.EventType;

public class CacheActivityView extends RelativeLayout implements OnClickListener {

    private EventHandler mEventHandler;

    private ListView mCacheListView;
    private TextView mHeaderTextView;
    private Button mCleanupButton;

    public CacheActivityView(Context context) {
        this(context, null);
    }

    public CacheActivityView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CacheActivityView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mCacheListView = (ListView) findViewById(R.id.cache_list);
        mHeaderTextView = (TextView) findViewById(R.id.header_title);
        mCleanupButton = (Button) findViewById(R.id.cleanup);
        mCleanupButton.setOnClickListener(this);
    }

    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    public void setCacheListAdapter(CacheListAdapter adapter) {
        mCacheListView.setAdapter(adapter);
    }

    public void setHeaderTitle(CharSequence text) {
        mHeaderTextView.setText(text);
    }

    public void setHeaderBarShown(boolean shown) {
        findViewById(R.id.header_bar).setVisibility(shown ? View.VISIBLE : View.INVISIBLE);
    }

    public void setCleanupButtonEnabled(boolean enabled) {
        mCleanupButton.setEnabled(enabled);
    }

    public void setEmptyViewShown(boolean shown) {
        View emptyView = findViewById(R.id.empty_view);
        if (shown) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        mEventHandler.sendEventMessage(EventType.EVENT_CLEANUP_LIST_EVENT,
                CleanupListEvent.create());
    }
}
