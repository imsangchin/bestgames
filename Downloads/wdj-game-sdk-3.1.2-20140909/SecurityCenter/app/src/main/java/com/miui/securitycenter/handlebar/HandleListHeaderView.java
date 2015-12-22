
package com.miui.securitycenter.handlebar;

import com.miui.securitycenter.R;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

public class HandleListHeaderView extends FrameLayout {

    private TextView mTitleView;

    public HandleListHeaderView(Context context) {
        this(context, null);
        // TODO Auto-generated constructor stub
    }

    public HandleListHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // TODO Auto-generated constructor stub
    }

    public HandleListHeaderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        // TODO Auto-generated method stub
        super.onFinishInflate();
        mTitleView = (TextView) findViewById(R.id.header_title);
    }

    public void fillData(HandleHeaderType type) {
        switch (type) {
            case Auto:
                mTitleView.setText(getContext().getString(R.string.auto_handle_title));
                break;
            case Manual:
                mTitleView.setText(getContext().getString(R.string.manual_handle_title));
            default:
                break;
        }
    }
}
