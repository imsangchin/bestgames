
package com.miui.antivirus;

import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;

import com.miui.securitycenter.R;

public class AnimationView extends View {

    private enum AnimStatus {
        TO_TOP, TO_BOTTOM
    }

    private ObjectAnimator mScanningAnimator;
    private ObjectAnimator mAlphaAnimator;

    private Bitmap mToTopBitmap;
    private Bitmap mToBottomBitmap;

    private Paint mHighLightPaint = new Paint();

    private float mHighLightViewTop;
    private int mHighLightAlpha = 255;

    private boolean mAnimForceStopped = false;

    private AnimStatus mCurrentAnimStatus = AnimStatus.TO_TOP;

    private AccelerateDecelerateInterpolator mInterpolator = new AccelerateDecelerateInterpolator();

    public AnimationView(Context context) {
        this(context, null);
    }

    public AnimationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Resources res = context.getResources();

        mToTopBitmap = BitmapFactory.decodeResource(res,
                R.drawable.virus_scanning_hight_light_to_top);
        mToBottomBitmap = BitmapFactory.decodeResource(res,
                R.drawable.virus_scanning_hight_light_to_bottom);

        mHighLightPaint.setAntiAlias(true);
        mHighLightPaint.setFilterBitmap(true);

    }

    private void setLayoutMargin(int marginTop, int marginBottom) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
        params.topMargin = marginTop;
        params.bottomMargin = marginBottom;
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestLayout();
        }
    }

    public void startScanningAnimation() {
        mAnimForceStopped = false;
        scanningToTop();
    }

    public void stopScanningAnimation() {
        mAnimForceStopped = true;
    }

    private AnimatorListenerAdapter mToTopListener = new AnimatorListenerAdapter() {
        public void onAnimationEnd(android.animation.Animator animation) {
            if (mAnimForceStopped) {
                if (mScanningAnimator != null) {
                    mScanningAnimator.cancel();
                    mScanningAnimator = null;
                }
            } else {
                startAlphaAnimation();
            }
        };
    };

    private AnimatorListenerAdapter mAlphaListener = new AnimatorListenerAdapter() {
        public void onAnimationEnd(android.animation.Animator animation) {
            if (mAnimForceStopped) {
                if (mScanningAnimator != null) {
                    mScanningAnimator.cancel();
                    mScanningAnimator = null;
                }
            } else {
                if (mCurrentAnimStatus == AnimStatus.TO_TOP) {
                    scanningToBottom();
                } else if (mCurrentAnimStatus == AnimStatus.TO_BOTTOM) {
                    scanningToTop();
                }
            }
        };
    };

    private AnimatorListenerAdapter mToBottomListener = new AnimatorListenerAdapter() {
        public void onAnimationEnd(android.animation.Animator animation) {
            if (mAnimForceStopped) {
                if (mScanningAnimator != null) {
                    mScanningAnimator.cancel();
                    mScanningAnimator = null;
                }
            } else {
                startAlphaAnimation();
            }
        };
    };

    private void startAlphaAnimation() {
        if (mAlphaAnimator != null) {
            mAlphaAnimator.cancel();
            mAlphaAnimator = null;
        }

        mAlphaAnimator = ObjectAnimator.ofInt(this, "HighLightAlpha", 255, 0);
        mAlphaAnimator.setDuration(300);
        mAlphaAnimator.addListener(mAlphaListener);
        mAlphaAnimator.start();
    }

    public void setHighLightAlpha(int alpha) {
        mHighLightAlpha = alpha;
        invalidate();
    }

    public int getHighLightAlpha() {
        return mHighLightAlpha;
    }

    private void scanningToTop() {
        if (mScanningAnimator != null) {
            mScanningAnimator.cancel();
            mScanningAnimator = null;
        }

        mCurrentAnimStatus = AnimStatus.TO_TOP;
        mHighLightAlpha = 255;

        mScanningAnimator = ObjectAnimator.ofFloat(this, "HighLightViewTop", getHeight(), 0);
        mScanningAnimator.setDuration(1000);
        mScanningAnimator.setInterpolator(mInterpolator);
        mScanningAnimator.addListener(mToTopListener);
        mScanningAnimator.start();
    }

    public void setHighLightViewTop(float top) {
        mHighLightViewTop = top;
        invalidate();
    }

    public float getHighLightViewTop() {
        return mHighLightViewTop;
    }

    private void scanningToBottom() {
        if (mScanningAnimator != null) {
            mScanningAnimator.cancel();
            mScanningAnimator = null;
        }

        mCurrentAnimStatus = AnimStatus.TO_BOTTOM;
        mHighLightAlpha = 255;

        mScanningAnimator = ObjectAnimator.ofFloat(this, "HighLightViewTop", 0, getHeight());
        mScanningAnimator.setDuration(1000);
        mScanningAnimator.setInterpolator(mInterpolator);
        mScanningAnimator.addListener(mToBottomListener);
        mScanningAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        switch (mCurrentAnimStatus) {
            case TO_TOP:
                drawToTop(canvas);
                break;
            case TO_BOTTOM:
                drawToBottom(canvas);
                break;

            default:
                super.onDraw(canvas);
                break;
        }
    }

    private void drawToTop(Canvas canvas) {
        canvas.save();

        mHighLightPaint.setAlpha(mHighLightAlpha);
        canvas.drawBitmap(mToTopBitmap, 0, mHighLightViewTop, mHighLightPaint);

        canvas.restore();
    }

    private void drawToBottom(Canvas canvas) {
        canvas.save();

        mHighLightPaint.setAlpha(mHighLightAlpha);
        canvas.drawBitmap(mToBottomBitmap, 0, mHighLightViewTop - mToBottomBitmap.getHeight(),
                mHighLightPaint);

        canvas.restore();
    }
}
