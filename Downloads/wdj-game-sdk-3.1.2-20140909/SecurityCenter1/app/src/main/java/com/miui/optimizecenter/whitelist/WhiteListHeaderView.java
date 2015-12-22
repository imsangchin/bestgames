
package com.miui.optimizecenter.whitelist;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.miui.common.EventHandler;

import com.miui.securitycenter.R;

public class WhiteListHeaderView extends FrameLayout implements OnClickListener {

    private TextView mTitleView;

    public WhiteListHeaderView(Context context) {
        this(context, null);
    }

    public WhiteListHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WhiteListHeaderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTitleView = (TextView) findViewById(R.id.header_title);
        setOnClickListener(this);
    }

    public void setEventHandler(EventHandler handler) {
        // ignore
    }

    public void fillData(WhiteListHeaderModel data) {
        mTitleView.setText(data.getHeaderTitle());
    }

    @Override
    public void onClick(View v) {
        // ignore
    }
}
