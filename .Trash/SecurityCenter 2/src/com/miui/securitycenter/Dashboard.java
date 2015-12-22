
package com.miui.securitycenter;

import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.miui.securitycenter.R;

public class Dashboard extends View {

    public enum AnimStatus {
        FLASH, SCORE, NORMAL
    }

    private static final int SHORT_ANIM_DURATION = 650;

    private static final int LONG_ANIM_DURATION = 2000;

    private static final int FULL_ALPHA = 255;
    private static final float FLASH_STEP_ALPHA = FULL_ALPHA / 30;

    private static final int MAX_ANGLE = 270;

    private static final int TOTAL_PIECE_COUNT = 150;
    private static final int FLASH_PIECE_COUNT = 30;
    private static final float PIECE_ANGLE = 1.8f;

    private static final DecelerateInterpolator mDecelerateInterpolator = new DecelerateInterpolator(
            4.5f);

    private Paint mFlashPaint = new Paint();
    private Paint mGrayPaint = new Paint();
    private Paint mPointerPaint = new Paint();

    private Bitmap mArcBitmap;
    private Bitmap mPointerBitmap;
    private Bitmap mGrayBitmap;
    private Bitmap mFlashBitmap;

    private int mCenterX, mCenterY;

    private ObjectAnimator mFlashAnimator;
    private float mFlashAngle;

    private ObjectAnimator mScoreAnimator;
    private float mScoreAngle = 0;
    private float mOldScoreAngle = 0;

    private AnimStatus mCurrentAnimStatus = AnimStatus.NORMAL;

    private boolean mInScoreFlash = false;

    public Dashboard(Context context) {
        this(context, null);
    }

    public Dashboard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Dashboard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        Resources res = context.getResources();

        mArcBitmap = BitmapFactory.decodeResource(res, R.drawable.dashboard_arc);
        mPointerBitmap = BitmapFactory.decodeResource(res, R.drawable.dashboard_pointer);
        mGrayBitmap = BitmapFactory.decodeResource(res, R.drawable.dashboard_piece_gray);
        mFlashBitmap = BitmapFactory.decodeResource(res, R.drawable.dashboard_piece_flash);

        mCenterX = mArcBitmap.getWidth() / 2;
        mCenterY = mArcBitmap.getHeight() / 2;

        mFlashPaint.setAntiAlias(true);
        mFlashPaint.setStyle(Style.STROKE);
        mFlashPaint.setStrokeWidth(38);

        mPointerPaint.setAntiAlias(true);
        mPointerPaint.setFilterBitmap(true);

        mGrayPaint.setAntiAlias(true);
        mGrayPaint.setFilterBitmap(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mArcBitmap == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            setMeasuredDimension(mArcBitmap.getWidth(), mArcBitmap.getHeight());
        }
    }

    public void setScore(int score) {
        setScore(score, null);
    }

    public void setScore(int score, AnimatorListener listener) {
        score = Math.min(Math.max(score, 0), 100);
        float angle = (float) score / 100.0f * MAX_ANGLE;

        if (mOldScoreAngle == angle && score != 100) {
            return;
        }
        mOldScoreAngle = angle;

        if (mFlashAnimator != null) {
            mFlashAnimator.cancel();
            mFlashAnimator = null;
        }

        if (mScoreAnimator != null) {
            mScoreAnimator.cancel();
            mScoreAnimator = null;
        }

        mCurrentAnimStatus = AnimStatus.SCORE;

        mScoreAnimator = ObjectAnimator.ofFloat(this, "ScoreAngle", mScoreAngle, angle);
        mScoreAnimator.setDuration(SHORT_ANIM_DURATION);
        if (listener != null) {
            mScoreAnimator.addListener(listener);
        }
        mScoreAnimator.start();
    }

    public void setScoreAngle(float angle) {
        mScoreAngle = angle;
        invalidate();
    }

    public float getScoreAngle() {
        return mScoreAngle;
    }

    private AnimatorListener mFlashListener = new AnimatorListenerAdapter() {
        public void onAnimationEnd(android.animation.Animator animation) {
            mFlashAngle = 0;
            invalidate();
        };
    };

    public void startFlashAnimation() {
        if (mFlashAnimator != null) {
            mFlashAnimator.cancel();
            mFlashAnimator = null;
        }

        mCurrentAnimStatus = AnimStatus.FLASH;
        mFlashAngle = 0;

        mFlashAnimator = ObjectAnimator.ofFloat(this, "FlashAngle", 0, 360);
        mFlashAnimator.setDuration(5500);
        mFlashAnimator.setInterpolator(new DecelerateInterpolator(3.5f));
        mFlashAnimator.addListener(mFlashListener);
        mFlashAnimator.start();
    }

    public void setFlashAngle(float angle) {
        mFlashAngle = angle;
        invalidate();
    }

    public float getFlashAngle() {
        return mFlashAngle;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));

        switch (mCurrentAnimStatus) {
            case FLASH:
                drawFlash(canvas);
                break;
            case SCORE:
                drawScore(canvas);
                break;
            default:
                drawNormal(canvas);
                break;
        }
    }

    private void drawNormal(Canvas canvas) {
        // draw arc
        canvas.drawBitmap(mArcBitmap, 0, 0, null);

        // draw piece gray
        for (int i = 0; i <= TOTAL_PIECE_COUNT; i++) {
            canvas.save();
            canvas.rotate(-135 + i * PIECE_ANGLE, mCenterX, mCenterY);
            canvas.drawBitmap(mGrayBitmap, mCenterX - mGrayBitmap.getWidth() / 2, mCenterY
                    - mGrayBitmap.getHeight(), mGrayPaint);
            canvas.restore();
        }

        // draw pointer
        canvas.save();
        canvas.rotate(-225 + mScoreAngle, mCenterX, mCenterY);
        canvas.drawBitmap(mPointerBitmap, mCenterX - mPointerBitmap.getHeight() / 2,
                mCenterY - mPointerBitmap.getHeight() / 2, mPointerPaint);
        canvas.restore();
    }

    private void drawScore(Canvas canvas) {
        // draw arc
        canvas.drawBitmap(mArcBitmap, 0, 0, null);

        // draw piece gray
        for (int i = 0; i <= TOTAL_PIECE_COUNT; i++) {
            canvas.save();
            canvas.rotate(-135 + i * PIECE_ANGLE, mCenterX, mCenterY);
            canvas.drawBitmap(mGrayBitmap, mCenterX - mGrayBitmap.getWidth() / 2, mCenterY
                    - mGrayBitmap.getHeight(), mGrayPaint);
            canvas.restore();
        }

        // draw piece flash
        mFlashPaint.setAlpha(FULL_ALPHA);
        int flashCount = (int) (mScoreAngle / PIECE_ANGLE);
        for (int i = 0; i <= flashCount; i++) {
            canvas.save();
            canvas.rotate(-135 + i * PIECE_ANGLE, mCenterX, mCenterY);
            canvas.drawBitmap(mFlashBitmap, mCenterX - mFlashBitmap.getWidth() / 2, mCenterY
                    - mFlashBitmap.getHeight(), mFlashPaint);
            canvas.restore();
        }

        // draw pointer
        canvas.save();
        canvas.rotate(-225 + mScoreAngle, mCenterX, mCenterY);
        canvas.drawBitmap(mPointerBitmap, mCenterX - mPointerBitmap.getHeight() / 2,
                mCenterY - mPointerBitmap.getHeight() / 2, mPointerPaint);
        canvas.restore();
    }

    private void drawFlash(Canvas canvas) {
        // draw arc
        canvas.drawBitmap(mArcBitmap, 0, 0, null);

        // draw piece gray
        for (int i = 0; i <= TOTAL_PIECE_COUNT; i++) {
            canvas.save();
            canvas.rotate(-135 + i * PIECE_ANGLE, mCenterX, mCenterY);
            canvas.drawBitmap(mGrayBitmap, mCenterX - mGrayBitmap.getWidth() / 2, mCenterY
                    - mGrayBitmap.getHeight(), mGrayPaint);
            canvas.restore();
        }

        // draw piece flash
        int flashCount = (int) (mFlashAngle / PIECE_ANGLE);
        for (int i = 0; i <= TOTAL_PIECE_COUNT; i++) {
            canvas.save();
            if (i <= flashCount && mFlashAngle > 0 && flashCount - i < FLASH_PIECE_COUNT) {
                int flashAlpha = (int) (FULL_ALPHA - (flashCount - i) * FLASH_STEP_ALPHA);
                flashAlpha = Math.min(flashAlpha, FULL_ALPHA);
                mFlashPaint.setAlpha(flashAlpha);
            } else {
                mFlashPaint.setAlpha(0);
            }

            canvas.rotate(-135 + i * PIECE_ANGLE, mCenterX, mCenterY);
            canvas.drawBitmap(mFlashBitmap, mCenterX - mFlashBitmap.getWidth() / 2, mCenterY
                    - mFlashBitmap.getHeight(), mFlashPaint);

            canvas.restore();
        }

        // draw pointer
        canvas.save();
        canvas.rotate(-225, mCenterX, mCenterY);
        canvas.drawBitmap(mPointerBitmap, mCenterX - mPointerBitmap.getHeight() / 2,
                mCenterY - mPointerBitmap.getHeight() / 2, mPointerPaint);
        canvas.restore();
    }
}
