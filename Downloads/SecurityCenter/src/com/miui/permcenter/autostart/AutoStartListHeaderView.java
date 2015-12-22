
package com.miui.permcenter.autostart;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.miui.common.BindableView;
import com.miui.common.EventHandler;

import com.miui.securitycenter.R;

public class AutoStartListHeaderView extends FrameLayout implements
        BindableView<AutoStartHeaderModel> {

    private TextView mHeaderTitleView;

    public AutoStartListHeaderView(Context context) {
        this(context, null);
    }

    public AutoStartListHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoStartListHeaderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHeaderTitleView = (TextView) findViewById(R.id.header_title);
    }

    @Override
    public void fillData(AutoStartHeaderModel data) {
        mHeaderTitleView.setText(data.getHeaderTitle());
    }

    @Override
    public void setEventHandler(EventHandler handler) {
        // ignore

    }

}
