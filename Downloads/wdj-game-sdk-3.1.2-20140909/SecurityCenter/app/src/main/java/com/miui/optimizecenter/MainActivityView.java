
package com.miui.optimizecenter;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.miui.common.EventHandler;
import com.miui.optimizecenter.MainContentFrame.BackgroundStatus;
import com.miui.optimizecenter.MainHandleBar.HandleItem;
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

    public void setFreeMemory(long size) {
        mContentFrame.setFreeMemory(size);
    }

    public void setGarbageSize(long size) {
        mContentFrame.setGarbageSize(size);
    }

    public void setContentTitleText(CharSequence text) {
        mContentFrame.setTitleText(text);
    }

    public void setContentSummaryText(CharSequence text) {
        mContentFrame.setSummaryText(text);
    }

    public void setContentSummaryVisibility(int visibility) {
        mContentFrame.setSummaryVisibility(visibility);
    }

    public void setUnitSuffixVisibility(int visibility) {
        mContentFrame.setUnitSuffixVisibility(visibility);
    }

    public void setDeepCleanButtonVisibility(int visibility) {
        mContentFrame.setDeepCleanButtonVisibility(visibility);
    }

    public void startScanAnimation() {
        mContentFrame.startScanAnimation();
    }

    public void stopScanAnimation() {
        mContentFrame.stopScanAnimation();
    }

    public void setActionButtonText(CharSequence text) {
        mHandleBar.setActionButtonText(text);
    }

    public void setActionButtonEnabled(boolean enabled) {
        mHandleBar.setActionButtonEnabled(enabled);
    }

    public void setHandleItemEnabled(HandleItem item, boolean enabled) {
        mHandleBar.setHandleItemEnabled(item, enabled);
    }

    public void setHandleItemContextText(HandleItem item, CharSequence text) {
        mHandleBar.setHandleItemContextText(item, text);
    }

    public void updateForeground(BackgroundStatus status) {
        mContentFrame.updateForeground(status);
    }

    public void performActionButtonClick() {
        mHandleBar.performActionButtonClick();
    }
}
