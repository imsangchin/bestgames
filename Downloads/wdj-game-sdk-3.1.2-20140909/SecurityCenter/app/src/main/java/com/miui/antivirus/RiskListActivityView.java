
package com.miui.antivirus;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.miui.antivirus.RiskListActivity.RiskListAdapter;
import com.miui.antivirus.event.EventType;
import com.miui.common.EventHandler;
import com.miui.securitycenter.event.CleanupListEvent;

import com.miui.securitycenter.R;

public class RiskListActivityView extends RelativeLayout implements OnClickListener {
    private EventHandler mEventHandler;

    private TextView mHeaderTitleView;

    private Button mCleanupButton;

    private ListView mVirusListView;

    public RiskListActivityView(Context context) {
        this(context, null);
    }

    public RiskListActivityView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RiskListActivityView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHeaderTitleView = (TextView) findViewById(R.id.header_title);
        mCleanupButton = (Button) findViewById(R.id.cleanup);
        mCleanupButton.setOnClickListener(this);

        mVirusListView = (ListView) findViewById(R.id.risk_list);
    }

    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    public void setHeaderTitle(CharSequence text) {
        mHeaderTitleView.setText(text);
    }

    public void setCleanupButtonEnabled(boolean enabled) {
        mCleanupButton.setEnabled(enabled);
    }

    public void setRiskListAdapter(RiskListAdapter adapter) {
        mVirusListView.setAdapter(adapter);
    }

    public void setHeaderBarShown(boolean shown){
        findViewById(R.id.header_bar).setVisibility(shown ? View.VISIBLE : View.INVISIBLE);
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
