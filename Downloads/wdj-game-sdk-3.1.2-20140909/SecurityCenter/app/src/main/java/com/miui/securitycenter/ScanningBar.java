
package com.miui.securitycenter;

import com.miui.securitycenter.R;
import com.miui.securitycenter.event.OnActionButtonClickEvent;
import com.miui.securitycenter.event.OnCancelOptimizeButtonClickEvent;
import com.miui.securitycenter.handlebar.HandleItem;

import android.content.Context;
import android.os.Handler;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.miui.antivirus.event.EventType;
import com.miui.common.EventHandler;

import android.animation.Animator.AnimatorListener;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScanningBar extends LinearLayout implements OnClickListener {

    private Context mContext;
    private ListView mScanningTextListView;
    private List<String> mScanningTextList;
    private ArrayAdapter<String> mListAdapter;

    private TextView mScanningTitle;
    private TextView mScanningNumber;
    private View mScanningHeader;
    private Button mCancelOptimizeButton;

    private Handler mHandler = new Handler();

    private EventHandler mEventHandler;

    public ScanningBar(Context context) {
        this(context, null);
    }

    public ScanningBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScanningBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mScanningTitle = (TextView) findViewById(R.id.scanning_item_title);
        mScanningNumber = (TextView) findViewById(R.id.scanning_item_number);
        mScanningHeader = findViewById(R.id.scanning_item_header);
        mCancelOptimizeButton = (Button) findViewById(R.id.button_cancel_optimize);
        mCancelOptimizeButton.setOnClickListener(this);

        mScanningTextList = new ArrayList<String>();
        mScanningTextListView = (ListView) findViewById(R.id.scanning_system_text);
        mListAdapter = new ArrayAdapter<String>(getContext(), R.layout.m_activity_textview,
                mScanningTextList);
        mScanningTextListView.setAdapter(mListAdapter);
    }

    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    public void resetListAdapter() {
        mScanningTextList.clear();
        mListAdapter.clear();
        mListAdapter.notifyDataSetChanged();
        mScanningTextListView.invalidate();
    }

    public void setScanningHeaderVisibility(int visibility) {
        mScanningHeader.setVisibility(visibility);
    }

    public void setScanningBarAnimation(Animation anim) {
        mScanningHeader.startAnimation(anim);
    }

    public void removeScanningBarCallbacks(Runnable action) {
        mScanningHeader.removeCallbacks(action);
    }

    public void setScanningTitle(CharSequence text) {
        mScanningTitle.setText(text);
    }

    public void setScanningNumber(CharSequence text) {
        mScanningNumber.setText(text);
    }

    public void setScanningText(String text) {
        mListAdapter.add(text);
        mListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.button_cancel_optimize:
                mEventHandler.sendEventMessage(EventType.EVENT_ON_CANCEL_OPTIMIZE_BUTTON_CLICK,
                        OnCancelOptimizeButtonClickEvent.create());
                break;
            default:
                break;
        }
    }

}
