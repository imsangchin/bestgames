
package com.miui.optimizecenter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import com.miui.securitycenter.R;

public class FloatTextView extends TextView {
    private static final int ERROR_NUMBER = -1;

    private final int SHORT_ANIM_DURATION = 650;

    private int mOldNumber = ERROR_NUMBER;

    private ObjectAnimator mScoreFlipAnimator;

    private int mFlipNumber = ERROR_NUMBER;

    public FloatTextView(Context context) {
        this(context, null);
    }

    public FloatTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(Typeface.create("miuiex-light", Typeface.NORMAL));
    }

    public void setNumber(int number) {
        if (mOldNumber == ERROR_NUMBER || mOldNumber == number) {
            if (mOldNumber != number) {
                updateFlip(String.valueOf(number));
            }
        } else {
            startFlipAnimation(mOldNumber, number);
        }
        mOldNumber = number;
    }

    public void updateFlip(int number) {
        if (mScoreFlipAnimator != null) {
            mScoreFlipAnimator.cancel();
            mScoreFlipAnimator = null;
        }

        updateFlip(String.valueOf(number));
        mOldNumber = number;
    }

    private synchronized void startFlipAnimation(int oldNumber, int newNumber) {
        if (mScoreFlipAnimator != null) {
            mScoreFlipAnimator.cancel();
            mScoreFlipAnimator = null;
        }

        mScoreFlipAnimator = ObjectAnimator.ofInt(FloatTextView.this, "FlipNumber", oldNumber,
                newNumber);
        mScoreFlipAnimator.setDuration(SHORT_ANIM_DURATION);
        mScoreFlipAnimator.setInterpolator(new DecelerateInterpolator());
        mScoreFlipAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                updateFlip(String.valueOf(mOldNumber));
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                updateFlip(String.valueOf(mOldNumber));
            }
        });
        mScoreFlipAnimator.start();
    }

    public void setFlipNumber(int number) {
        if (mFlipNumber != number) {
            mFlipNumber = number;
            updateFlip(String.valueOf(number));
        }
    }

    public int getFlipNumber() {
        return mFlipNumber;
    }

    public void updateFlip(String numbers) {
        setText(numbers);
    }
}
