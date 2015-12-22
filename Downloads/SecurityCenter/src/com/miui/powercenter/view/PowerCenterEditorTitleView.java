package com.miui.powercenter.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.miui.securitycenter.R;


public class PowerCenterEditorTitleView extends LinearLayout{
    private Button mOk;
    private Button mCancel;
    private TextView mTitle;
    private View mTitleContainer;

    private Rect mTempRect = new Rect();

    public Button getOk() {
        return mOk;
    }

    public Button getCancel() {
        return mCancel;
    }

    public TextView getTitle() {
        return mTitle;
    }

    public PowerCenterEditorTitleView(Context context) {
        super(context);
    }

    public PowerCenterEditorTitleView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mOk = (Button) findViewById(R.id.ok);
        mCancel = (Button) findViewById(R.id.cancel);
        mTitle = (TextView) findViewById(R.id.title);
        mTitleContainer = findViewById(R.id.title_container);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mTitleContainer.getHitRect(mTempRect);
            final int dx = mTempRect.left;
            final int dy = mTempRect.top;
            mTitle.getHitRect(mTempRect);
            mTempRect.offset(dx, dy);
            if (!mTempRect.contains((int) event.getX(), (int) event.getY())) {
                return true;
            }
        }

        return super.onTouchEvent(event);
    }
}
