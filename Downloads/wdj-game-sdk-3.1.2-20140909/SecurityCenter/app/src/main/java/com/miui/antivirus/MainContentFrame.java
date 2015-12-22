
package com.miui.antivirus;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.miui.common.EventHandler;
import com.miui.common.ViewAlphaAnimation;
import com.miui.securitycenter.ScoreTextView;
import com.miui.securitycenter.event.EventType;
import com.miui.securitycenter.event.OnBackPressedEvent;
import com.miui.securitycenter.event.ViewSettingsEvent;

import com.miui.securitycenter.R;;

public class MainContentFrame extends RelativeLayout implements OnClickListener {

    private EventHandler mEventHandler;

    private View mForegroundView;
    private View mBackgroundView;
    private ScoreTextView mNumberView;

    private TextView mTitleView;
    private TextView mSummaryView;

    private AnimationView mAnimationView;

    public MainContentFrame(Context context) {
        this(context, null);
    }

    public MainContentFrame(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainContentFrame(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mForegroundView = findViewById(R.id.foreground);
        mBackgroundView = findViewById(R.id.background);

        mNumberView = (ScoreTextView) findViewById(R.id.number);
        mNumberView.setNumber(0);

        mTitleView = (TextView) findViewById(R.id.title_text);
        mSummaryView = (TextView) findViewById(R.id.summary_text);

        mAnimationView = (AnimationView) findViewById(R.id.animation_view);

        findViewById(R.id.settings).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);
    }

    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    public void setTitleText(CharSequence text) {
        mTitleView.setText(text);
    }

    public void setSummaryText(CharSequence text) {
        mSummaryView.setText(text);
    }

    public void startScanningAnimation() {
        mAnimationView.startScanningAnimation();
    }

    public void stopScanningAnimation() {
        mAnimationView.stopScanningAnimation();
    }

    private AntiVirusStatus mCurrentStatus = AntiVirusStatus.SAVE;
    private View mToGoneView;
    private View mToVisiableView;

    public void updateForeground(AntiVirusStatus status, int count) {
        mNumberView.setNumber(count);

        if (status != mCurrentStatus) {
            if (mForegroundView.getAlpha() == 1) {
                mToGoneView = mForegroundView;
                mToVisiableView = mBackgroundView;
            } else {
                mToVisiableView = mForegroundView;
                mToGoneView = mBackgroundView;
            }

            switch (status) {
                case SAVE:
                    mToVisiableView.setBackgroundResource(R.drawable.main_bg_green);
                    break;
                case RISK:
                    mToVisiableView.setBackgroundResource(R.drawable.main_bg_light_orange);
                    break;
                case VIRUS:
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
        if (v.getId() == R.id.settings) {
            mEventHandler.sendEventMessage(EventType.EVENT_VIEW_SETTINGS,
                    ViewSettingsEvent.create());
        } else if (v.getId() == R.id.back) {
            mEventHandler.sendEventMessage(EventType.EVENT_ON_BACK_PRESSED,
                    OnBackPressedEvent.create());
        }
    }

}
