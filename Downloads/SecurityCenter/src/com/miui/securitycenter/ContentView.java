
package com.miui.securitycenter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.miui.common.AnimationListenerAdapter;
import com.miui.common.EventHandler;
import com.miui.securitycenter.event.EventType;
import com.miui.securitycenter.event.StartQuickScanEvent;
import com.miui.securitycenter.event.ViewSettingsEvent;

import com.miui.securitycenter.R;

public class ContentView extends RelativeLayout implements OnClickListener {

    private EventHandler mEventHandler;

    private Dashboard mDashboard;
    private Button mActionButton;
    private TextView mStatusView;
    private View mScoreFrameView;
    private ScoreTextView mScoreTextView;
    private TextView mCheckingView;

    private View mTopScoreFrameView;
    private ScoreTextView mTopScoreTextView;
    private TextView mTopCheckingView;

    private Button mSettingsView;

    public ContentView(Context context) {
        this(context, null);
    }

    public ContentView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDashboard = (Dashboard) findViewById(R.id.dashboard);
        mActionButton = (Button) findViewById(R.id.btn_action);
        mActionButton.setOnClickListener(this);
        mSettingsView = (Button) findViewById(R.id.settings);
        mSettingsView.setOnClickListener(this);

        mStatusView = (TextView) findViewById(R.id.status_bar);
        mScoreFrameView = findViewById(R.id.score_frame);
        mCheckingView = (TextView) findViewById(R.id.checking_bar);
        mScoreTextView = (ScoreTextView) findViewById(R.id.score);
        mScoreTextView.setScore(100);

        mTopScoreFrameView = findViewById(R.id.top_score_frame);
        mTopCheckingView = (TextView) findViewById(R.id.top_checking_bar);
        mTopScoreTextView = (ScoreTextView) findViewById(R.id.top_score);

    }

    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    public void startFlashAnimation() {
        mDashboard.startFlashAnimation();
    }

    public void setScore(int score, AnimatorListener listener) {
        mScoreTextView.setScore(score);
        mTopScoreTextView.setScore(score);
        mDashboard.setScore(score, listener);
    }

    public void setStatusText(CharSequence text) {
        mStatusView.setText(text);
    }

    public void setCheckingText(CharSequence text) {
        mCheckingView.setText(text);
        mTopCheckingView.setText(text);
    }

    public void startCheckingAnimation() {
        mHandler.removeCallbacks(mHideCheckingRunnable);
        mHandler.removeCallbacks(mHideScoreRunnable);
        mHandler.removeCallbacks(mShowButtonRunnable);
        mHandler.removeCallbacks(mShowStatusRunnable);

        mHandler.postDelayed(mHideStatusRunnable, 0);
        mHandler.postDelayed(mHideButtonRunnable, 50);
        mHandler.postDelayed(mShowScoreRunnable, 180);
        mHandler.postDelayed(mShowCheckingRunnable, 230);
    }

    public void finishScanningAnimation() {
        mHandler.removeCallbacks(mHideTopCheckingRunnable);
        mHandler.removeCallbacks(mShowDashboardRunnable);
        mHandler.removeCallbacks(mShowScoreRunnable);
        mHandler.removeCallbacks(mShowCheckingRunnable);

        mHandler.postDelayed(mHideCheckingRunnable, 0);
        mHandler.postDelayed(mHideScoreRunnable, 10);
        mHandler.postDelayed(mHideDashboardRunnable, 20);
        mHandler.postDelayed(mShowTopCheckingRunnable, 40);
    }

    public void stopCheckingAnimation() {
        mHandler.removeCallbacks(mHideDashboardRunnable);
        mHandler.removeCallbacks(mHideStatusRunnable);
        mHandler.removeCallbacks(mHideButtonRunnable);
        mHandler.removeCallbacks(mShowScoreRunnable);
        mHandler.removeCallbacks(mShowTopCheckingRunnable);
        mHandler.removeCallbacks(mShowCheckingRunnable);

        mHandler.postDelayed(mHideCheckingRunnable, 0);
        mHandler.postDelayed(mHideTopCheckingRunnable, 50);
        mHandler.postDelayed(mHideScoreRunnable, 60);
        mHandler.postDelayed(mShowButtonRunnable, 180);
        mHandler.postDelayed(mShowStatusRunnable, 230);
        mHandler.postDelayed(mShowDashboardRunnable, 280);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_action) {
            mEventHandler.sendEventMessage(EventType.EVENT_START_QUICK_SCAN,
                    StartQuickScanEvent.create());
        } else if (v.getId() == R.id.settings) {
            mEventHandler.sendEventMessage(EventType.EVENT_VIEW_SETTINGS,
                    ViewSettingsEvent.create());
        }
    }

    private Handler mHandler = new Handler();

    private Runnable mHideButtonRunnable = new Runnable() {

        @Override
        public void run() {
            Animation anim = AnimationUtils.loadAnimation(getContext(),
                    R.anim.hide_from_top_to_bottom);
            anim.setAnimationListener(new AnimationListenerAdapter() {
                public void onAnimationEnd(Animation animation) {
                    mActionButton.setVisibility(View.GONE);
                };
            });

            mActionButton.startAnimation(anim);
        }
    };

    private Runnable mHideStatusRunnable = new Runnable() {

        @Override
        public void run() {
            Animation anim = AnimationUtils.loadAnimation(getContext(),
                    R.anim.hide_from_top_to_bottom);
            anim.setAnimationListener(new AnimationListenerAdapter() {

                public void onAnimationEnd(Animation animation) {
                    mStatusView.setVisibility(View.GONE);
                };
            });

            mStatusView.startAnimation(anim);

        }
    };
    private Runnable mShowScoreRunnable = new Runnable() {

        @Override
        public void run() {
            Animation anim = AnimationUtils.loadAnimation(getContext(),
                    R.anim.show_from_bottom_to_top);
            anim.setAnimationListener(new AnimationListenerAdapter() {
                public void onAnimationStart(Animation animation) {
                    mScoreFrameView.setVisibility(View.VISIBLE);
                };
            });

            mScoreFrameView.startAnimation(anim);
        }
    };
    private Runnable mShowCheckingRunnable = new Runnable() {

        @Override
        public void run() {
            Animation anim = AnimationUtils.loadAnimation(getContext(),
                    R.anim.show_from_bottom_to_top);
            anim.setAnimationListener(new AnimationListenerAdapter() {
                public void onAnimationStart(Animation animation) {
                    mCheckingView.setVisibility(View.VISIBLE);
                };
            });

            mCheckingView.startAnimation(anim);
        }
    };
    private Runnable mHideScoreRunnable = new Runnable() {

        @Override
        public void run() {
            Animation anim = AnimationUtils.loadAnimation(getContext(),
                    R.anim.hide_from_bottom_to_top);
            anim.setAnimationListener(new AnimationListenerAdapter() {

                public void onAnimationEnd(Animation animation) {
                    mScoreFrameView.setVisibility(View.GONE);
                };
            });

            mScoreFrameView.startAnimation(anim);
        }
    };

    private Runnable mHideCheckingRunnable = new Runnable() {

        @Override
        public void run() {
            Animation anim = AnimationUtils.loadAnimation(getContext(),
                    R.anim.hide_from_bottom_to_top);
            anim.setAnimationListener(new AnimationListenerAdapter() {

                public void onAnimationEnd(Animation animation) {
                    mCheckingView.setVisibility(View.GONE);
                };
            });

            mCheckingView.startAnimation(anim);
        }
    };
    private Runnable mShowButtonRunnable = new Runnable() {

        @Override
        public void run() {
            Animation anim = AnimationUtils.loadAnimation(getContext(),
                    R.anim.show_from_bottom_to_top);
            anim.setAnimationListener(new AnimationListenerAdapter() {
                public void onAnimationStart(Animation animation) {
                    mActionButton.setVisibility(View.VISIBLE);
                };
            });

            mActionButton.startAnimation(anim);
        }
    };
    private Runnable mShowStatusRunnable = new Runnable() {

        @Override
        public void run() {
            Animation anim = AnimationUtils.loadAnimation(getContext(),
                    R.anim.show_from_bottom_to_top);
            anim.setAnimationListener(new AnimationListenerAdapter() {
                public void onAnimationStart(Animation animation) {
                    mStatusView.setVisibility(View.VISIBLE);
                };
            });

            mStatusView.startAnimation(anim);
        }
    };
    private Runnable mShowDashboardRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            AlphaAnimation anim = new AlphaAnimation(0, 1);
            anim.setDuration(500);
            anim.setInterpolator(new DecelerateInterpolator());
            anim.setAnimationListener(new AnimationListenerAdapter() {
                public void onAnimationStart(Animation animation) {
                    mDashboard.setVisibility(View.VISIBLE);
                }
            });
            mDashboard.startAnimation(anim);
        }
    };
    private Runnable mShowTopCheckingRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            Animation anim = AnimationUtils.loadAnimation(getContext(),
                    R.anim.show_from_bottom_to_top);
            anim.setAnimationListener(new AnimationListenerAdapter() {
                public void onAnimationStart(Animation animation) {
                    mTopCheckingView.setVisibility(View.VISIBLE);
                    mTopScoreFrameView.setVisibility(View.VISIBLE);
                }
            });
            mTopCheckingView.startAnimation(anim);
            mTopScoreFrameView.startAnimation(anim);
        }
    };

    private Runnable mHideTopCheckingRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            AlphaAnimation anim = new AlphaAnimation(1, 0);
            anim.setDuration(180);
            anim.setInterpolator(new DecelerateInterpolator());
            anim.setAnimationListener(new AnimationListenerAdapter() {
                public void onAnimationEnd(Animation animation) {
                    mTopCheckingView.setVisibility(View.GONE);
                    mTopScoreFrameView.setVisibility(View.GONE);
                }
            });
            mTopCheckingView.startAnimation(anim);
            mTopScoreFrameView.startAnimation(anim);
        }
    };

    private Runnable mHideDashboardRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            AlphaAnimation anim = new AlphaAnimation(1, 0);
            anim.setDuration(180);
            anim.setInterpolator(new DecelerateInterpolator());
            anim.setAnimationListener(new AnimationListenerAdapter() {
                public void onAnimationStart(Animation animation) {
                    mDashboard.setVisibility(View.GONE);
                }
            });
            mDashboard.startAnimation(anim);
        }
    };

}
