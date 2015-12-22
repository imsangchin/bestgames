
package com.miui.antivirus;

import com.miui.antivirus.event.EventType;
import com.miui.antivirus.event.OnActionButtonClickEvent;
import com.miui.antivirus.event.OnVirusHandleItemClickEvent;
import com.miui.common.EventHandler;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.miui.securitycenter.R;

public class MainHandleBar extends LinearLayout implements OnClickListener {

    public enum HandleItem {
        RISK, VIRUS
    }

    public enum ActionButtonStatus {
        NORMAL, SCANNING, SCANNED, CLEANNING, CLEANNED
    }

    private EventHandler mEventHandler;

    private TextView mVirusContentView;
    private TextView mRiskContentView;

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
        findViewById(R.id.handle_item_virus).setOnClickListener(this);
        findViewById(R.id.handle_item_risk).setOnClickListener(this);
        findViewById(R.id.handle_item_risk).setEnabled(false);
        findViewById(R.id.handle_item_virus).setEnabled(false);

        mVirusContentView = (TextView) findViewById(R.id.handle_text_content_virus);
        mRiskContentView = (TextView) findViewById(R.id.handle_text_content_risk);

        mActionButton = (Button) findViewById(R.id.btn_action);
        mActionButton.setOnClickListener(this);
    }

    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    public void setHandleItemContentText(HandleItem item, CharSequence text) {
        switch (item) {
            case RISK:
                mRiskContentView.setText(text);
                break;
            case VIRUS:
                mVirusContentView.setText(text);
                break;
            default:
                break;
        }
    }

    public void setHandleItemEnabled(HandleItem item, boolean enabled) {
        switch (item) {
            case RISK:
                findViewById(R.id.handle_item_risk).setEnabled(enabled);
                break;
            case VIRUS:
                findViewById(R.id.handle_item_virus).setEnabled(enabled);
                break;
            default:
                break;
        }
    }

    public void setActionButtonText(CharSequence text) {
        mActionButton.setText(text);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_action:
                mEventHandler.sendEventMessage(EventType.EVENT_ON_ACTION_BUTTON_CLICK,
                        OnActionButtonClickEvent.create());
                break;
            case R.id.handle_item_virus:
                mEventHandler.sendEventMessage(EventType.EVENT_ON_VIRUS_HANDLE_ITEM_CLICK,
                        OnVirusHandleItemClickEvent.create(HandleItem.VIRUS));
                break;
            case R.id.handle_item_risk:
                mEventHandler.sendEventMessage(EventType.EVENT_ON_VIRUS_HANDLE_ITEM_CLICK,
                        OnVirusHandleItemClickEvent.create(HandleItem.RISK));
                break;

            default:
                break;
        }
    }

}
