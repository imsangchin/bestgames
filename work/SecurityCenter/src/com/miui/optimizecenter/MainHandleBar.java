
package com.miui.optimizecenter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.miui.securitycenter.R;
import com.miui.antivirus.event.OnActionButtonClickEvent;
import com.miui.common.EventHandler;
import com.miui.optimizecenter.event.EventType;
import com.miui.optimizecenter.event.OnGarbageHandleItemClickEvent;

public class MainHandleBar extends LinearLayout implements OnClickListener {

    public enum HandleItem {
        CACHE, AD, APK, RESIDUAL
    }

    private EventHandler mEventHandler;

    private TextView mCacheContentView;
    private TextView mAdContent;
    private TextView mApkContentView;
    private TextView mResidualContentView;

    private Button mActionButton;

    public MainHandleBar(Context context) {
        this(context, null);
    }

    public MainHandleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        findViewById(R.id.handle_item_cache).setOnClickListener(this);
        findViewById(R.id.handle_item_ad).setOnClickListener(this);
        findViewById(R.id.handle_item_apk).setOnClickListener(this);
        findViewById(R.id.handle_item_residual).setOnClickListener(this);

        mCacheContentView = (TextView) findViewById(R.id.handle_text_content_cache);
        mAdContent = (TextView) findViewById(R.id.handle_text_content_ad);
        mApkContentView = (TextView) findViewById(R.id.handle_text_content_apk);
        mResidualContentView = (TextView) findViewById(R.id.handle_text_content_residual);

        mActionButton = (Button) findViewById(R.id.btn_action);
        mActionButton.setOnClickListener(this);
    }

    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    public void setActionButtonText(CharSequence text) {
        mActionButton.setText(text);
    }

    public void setActionButtonEnabled(boolean enabled) {
        mActionButton.setEnabled(enabled);
    }

    public void performActionButtonClick() {
        mActionButton.performClick();
    }

    public void setHandleItemEnabled(HandleItem item, boolean enabled) {
        switch (item) {
            case CACHE:
                findViewById(R.id.handle_item_cache).setEnabled(enabled);
                break;
            case AD:
                findViewById(R.id.handle_item_ad).setEnabled(enabled);
                break;
            case APK:
                findViewById(R.id.handle_item_apk).setEnabled(enabled);
                break;
            case RESIDUAL:
                findViewById(R.id.handle_item_residual).setEnabled(enabled);
                break;

            default:
                break;
        }
    }

    public void setHandleItemContextText(HandleItem item, CharSequence text) {
        switch (item) {
            case CACHE:
                mCacheContentView.setText(text);
                break;
            case AD:
                mAdContent.setText(text);
                break;
            case APK:
                mApkContentView.setText(text);
                break;
            case RESIDUAL:
                mResidualContentView.setText(text);
                break;

            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.handle_item_cache:
                mEventHandler.sendEventMessage(EventType.EVENT_ON_garbage_HANDLE_ITEM_CLICK,
                        OnGarbageHandleItemClickEvent.create(HandleItem.CACHE));
                break;
            case R.id.handle_item_ad:
                mEventHandler.sendEventMessage(EventType.EVENT_ON_garbage_HANDLE_ITEM_CLICK,
                        OnGarbageHandleItemClickEvent.create(HandleItem.AD));
                break;
            case R.id.handle_item_apk:
                mEventHandler.sendEventMessage(EventType.EVENT_ON_garbage_HANDLE_ITEM_CLICK,
                        OnGarbageHandleItemClickEvent.create(HandleItem.APK));
                break;
            case R.id.handle_item_residual:
                mEventHandler.sendEventMessage(EventType.EVENT_ON_garbage_HANDLE_ITEM_CLICK,
                        OnGarbageHandleItemClickEvent.create(HandleItem.RESIDUAL));
                break;
            case R.id.btn_action:
                mEventHandler.sendEventMessage(EventType.EVENT_ON_ACTION_BUTTON_CLICK,
                        OnActionButtonClickEvent.create());
                break;
            default:
                break;
        }
    }
}
