
package com.miui.antivirus;

import com.miui.antivirus.MainHandleBar.HandleItem;
import com.miui.common.EventHandler;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.miui.securitycenter.R;

public class MainActivityView extends LinearLayout {
    private MainContentFrame mContentFrame;
    private MainHandleBar mHandleBar;

    public MainActivityView(Context context) {
        this(context, null);
    }

    public MainActivityView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContentFrame = (MainContentFrame) findViewById(R.id.content_frame);
        mHandleBar = (MainHandleBar) findViewById(R.id.handle_bar);
    }

    public void setEventHandler(EventHandler handler) {
        mContentFrame.setEventHandler(handler);
        mHandleBar.setEventHandler(handler);
    }

    public void setContentTitleText(CharSequence text) {
        mContentFrame.setTitleText(text);
    }

    public void setContentSummaryText(CharSequence text) {
        mContentFrame.setSummaryText(text);
    }

    public void updateContentForeground(AntiVirusStatus status, int count) {
        mContentFrame.updateForeground(status, count);
    }

    public void setHandleItemContentText(HandleItem item, CharSequence text) {
        mHandleBar.setHandleItemContentText(item, text);
    }

    public void setHandleItemEnabled(HandleItem item, boolean enabled) {
        mHandleBar.setHandleItemEnabled(item, enabled);
    }

    public void setActionButtonText(CharSequence text) {
        mHandleBar.setActionButtonText(text);
    }

    public void startScanningAnimation() {
        mContentFrame.startScanningAnimation();
    }

    public void stopScanningAnimation() {
        mContentFrame.stopScanningAnimation();
    }
}
