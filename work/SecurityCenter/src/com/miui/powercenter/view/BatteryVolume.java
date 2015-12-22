
package com.miui.powercenter.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.miui.common.AnimationListenerAdapter;

import java.util.ArrayList;
import java.util.List;

public class BatteryVolume extends View {

    private class Bubble {

        public static final int ERROR_VALUE = -1;

        public int width = ERROR_VALUE;
        public int height = ERROR_VALUE;
        public int quarterHeight = ERROR_VALUE;

        public float x;
        public float y;
        public float size;
        public float speed;
        public float speedIncrease;
        public float alpha;

        public Bubble(int width, int height) {
            this.width = width;
            this.height = height;
            this.quarterHeight = height / 4;
            this.x = (float) (Math.random() * width);
            this.y = height - (float) (Math.random() * quarterHeight);
            this.size = (float) (0.1 + Math.random() * 0.3);
            this.alpha = (float) (0.1 + Math.random() * 0.5);
            this.speed = 0;
            this.speedIncrease = (float) (Math.pow(this.size * 10, 2) * 0.02);
        }

        public void reset() {
            this.y = height - (float) (Math.random() * quarterHeight);
            this.alpha = (float) (0.1 + Math.random() * 0.5);
            this.speed = 0;
            this.speedIncrease = (float) (Math.pow(this.size * 10, 2) * 0.02);
        }

    }

    public BatteryVolume(Context context) {
        this(context, null);
    }

    public BatteryVolume(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BatteryVolume(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mBubblePaint.setColor(Color.WHITE);
    }

    public void handleChargingAnimation(boolean charging, boolean anim) {
        if (charging) {
            if (getVisibility() != View.VISIBLE) {
                if (anim) {
                    startVisibleAnimation();
                } else {
                    setVisibility(View.VISIBLE);
                }
            }
            mHandler.post(mRunnable);
        } else {
            if (getVisibility() != View.GONE) {
                if (anim) {
                    startGoneAnimation();
                } else {
                    setVisibility(View.GONE);
                }
            }
            mHandler.removeCallbacks(mRunnable);
        }
    }

    private void startGoneAnimation() {
        Animation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setDuration(300);
        anim.setAnimationListener(mGoneListener);
        startAnimation(anim);
    }

    private void startVisibleAnimation() {
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(300);
        anim.setAnimationListener(mVisibleListener);
        startAnimation(anim);
    }

    private AnimationListenerAdapter mGoneListener = new AnimationListenerAdapter() {
        public void onAnimationEnd(Animation animation) {
            setVisibility(View.GONE);
        };
    };

    private AnimationListenerAdapter mVisibleListener = new AnimationListenerAdapter() {
        public void onAnimationEnd(Animation animation) {
            setVisibility(View.VISIBLE);
        };
    };

    private List<Bubble> mBubbles = new ArrayList<Bubble>();
    private Paint mBubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            for (Bubble bubble : mBubbles) {
                bubble.speed += bubble.speedIncrease;
                bubble.speedIncrease *= 1.01;
                bubble.y -= bubble.speed;
                if (bubble.y < bubble.quarterHeight * 2) {
                    bubble.reset();
                } else if (bubble.y < bubble.quarterHeight * 3) {
                    if (bubble.alpha < 0) {
                        bubble.reset();
                    } else {
                        bubble.alpha *= 0.95;
                    }
                }
            }

            invalidate();

            mHandler.postDelayed(mRunnable, 18);
        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getWidth();
        int height = getHeight();
        if (width != 0 && height != 0 && mBubbles.isEmpty()) {
            for (int i = 0; i < 100; i++) {
                mBubbles.add(new Bubble(width, height));
            }
        }
    };

    @Override
    protected void onDraw(Canvas canvas) {
        int count = mBubbles.size();
        for (int i = 0; i < count; i++) {
            Bubble bubble = mBubbles.get(i);
            int alpha = (int) (bubble.alpha * 255);
            mBubblePaint.setAlpha(alpha);
            canvas.drawCircle(bubble.x, bubble.y, bubble.size * 10, mBubblePaint);
        }
    }

}
