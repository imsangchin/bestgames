
package com.miui.optimizecenter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.miui.common.AndroidUtils;
import com.miui.common.EventHandler;
import com.miui.common.ViewAlphaAnimation;
import com.miui.optimizecenter.event.EventType;
import com.miui.optimizecenter.event.ViewDeepCleanEvent;
import com.miui.securitycenter.event.OnBackPressedEvent;
import com.miui.securitycenter.event.ViewSettingsEvent;

import com.miui.securitycenter.R;

public class MainContentFrame extends RelativeLayout implements OnClickListener {

    public enum BackgroundStatus {
        CYAN, ORANGE, RED
    }

    private EventHandler mEventHandler;

    private View mForegroundView;
    private View mBackgroundView;

    private AnimationView mAnimationView;

    private FloatTextView mNumberView;

    private TextView mUnitView;
    private TextView mUnitSuffixView;
    private TextView mTitleView;
    private TextView mSummaryView;

    public MainContentFrame(Context context) {
        super(context, null);
    }

    public MainContentFrame(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public MainContentFrame(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mForegroundView = findViewById(R.id.foreground);
        mBackgroundView = findViewById(R.id.background);

        mNumberView = (FloatTextView) findViewById(R.id.number);

        mAnimationView = (AnimationView) findViewById(R.id.animation_view);

        mUnitView = (TextView) findViewById(R.id.unit);
        mUnitSuffixView = (TextView) findViewById(R.id.unit_suffix);

        mTitleView = (TextView) findViewById(R.id.title_text);
        mSummaryView = (TextView) findViewById(R.id.summary_text);

        findViewById(R.id.view_deep).setOnClickListener(this);
        findViewById(R.id.settings).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);
    }

    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    public void setFreeMemory(long size) {
        mNumberView.updateFlip(AndroidUtils.formatFileSizeWithoutSuffix(size));
        mUnitView.setText(AndroidUtils.getFileSizeSuffix(size));
    }

    public void setGarbageSize(long size) {
        if(size == 0){
            mNumberView.updateFlip("0");
            mUnitView.setText("MB");
        }
        else{
            mNumberView.updateFlip(AndroidUtils.formatFileSizeWithoutSuffix(size));
            mUnitView.setText(AndroidUtils.getFileSizeSuffix(size));
        }
    }

    public void setTitleText(CharSequence text) {
        mTitleView.setText(text);
    }

    public void setSummaryText(CharSequence text) {
        mSummaryView.setText(text);
    }

    public void setSummaryVisibility(int visibility) {
        mSummaryView.setVisibility(visibility);
    }

    public void setUnitSuffixVisibility(int visibility) {
        mUnitSuffixView.setVisibility(visibility);
    }

    public void setDeepCleanButtonVisibility(int visibility) {
        findViewById(R.id.view_deep).setVisibility(visibility);
    }

    public void startScanAnimation() {
        // TODO
    }

    public void stopScanAnimation() {
        // TODO
    }

    private BackgroundStatus mCurrentStatus = BackgroundStatus.CYAN;
    private View mToGoneView;
    private View mToVisiableView;

    public void updateForeground(BackgroundStatus status) {
        if (status != mCurrentStatus) {
            if (mForegroundView.getAlpha() == 1) {
                mToGoneView = mForegroundView;
                mToVisiableView = mBackgroundView;
            } else {
                mToVisiableView = mForegroundView;
                mToGoneView = mBackgroundView;
            }

            switch (status) {
                case CYAN:
                    mToVisiableView.setBackgroundResource(R.drawable.main_bg_cyan);
                    break;
                case ORANGE:
                    mToVisiableView.setBackgroundResource(R.drawable.main_bg_light_orange);
                    break;
                case RED:
                    mToVisiableView.setBackgroundResource(R.drawable.main_bg_orange);
                    break;
                default:
                    break;
            }

            mCurrentStatus = status;
            ViewAlphaAnimation anim = new ViewAlphaAnimation(mToVisiableView, mToGoneView);
            anim.setStartOffset(0);
            anim.setDuration(700);
            mToVisiableView.startAnimation(anim);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settings:
                mEventHandler.sendEventMessage(EventType.EVENT_VIEW_SETTINGS,
                        ViewSettingsEvent.create());
                break;
            case R.id.view_deep:
                mEventHandler.sendEventMessage(EventType.EVENT_VIEW_DEEPCLEAN,
                        ViewDeepCleanEvent.create());
                break;
            case R.id.back:
                mEventHandler.sendEventMessage(EventType.EVENT_ON_BACK_PRESSED,
                        OnBackPressedEvent.create());
                break;
            default:
                break;
        }
    }

}
