
package com.miui.securitycenter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import com.miui.securitycenter.R;

public class ScoreTextView extends TextView {
    private static final int MAX_SCORE = 100;
    private static final int ERROR_SCROE = -1;

    private final int SHORT_ANIM_DURATION = 650;

    private int mOldScore = ERROR_SCROE;

    private ObjectAnimator mScoreFlipAnimator;

    private int mFlipScore = ERROR_SCROE;

    public ScoreTextView(Context context) {
        this(context, null);
    }

    public ScoreTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(Typeface.create("miuiex-light", Typeface.NORMAL));
    }

    public void setScore(int score) {
        if (mOldScore == ERROR_SCROE || mOldScore == score || score == MAX_SCORE) {
            if (mOldScore != score) {
                updateFlip(String.valueOf(score));
            }
        } else {
            startFlipAnimation(mOldScore, score);
        }
        mOldScore = score;
    }

    public void setNumber(int number) {
        if (mOldScore == ERROR_SCROE || mOldScore == number) {
            if (mOldScore != number) {
                updateFlip(String.valueOf(number));
            }
        } else {
            startFlipAnimation(mOldScore, number);
        }
        mOldScore = number;
    }

    public void updateFlip(int number) {
        if (mScoreFlipAnimator != null) {
            mScoreFlipAnimator.cancel();
            mScoreFlipAnimator = null;
        }
        updateFlip(String.valueOf(number));
        mOldScore = number;
    }

    private synchronized void startFlipAnimation(int oldScore, int newScore) {
        if (mScoreFlipAnimator != null) {
            mScoreFlipAnimator.cancel();
            mScoreFlipAnimator = null;
        }

        mScoreFlipAnimator = ObjectAnimator.ofInt(ScoreTextView.this, "FlipScore", oldScore,
                newScore);
        mScoreFlipAnimator.setDuration(SHORT_ANIM_DURATION);
        mScoreFlipAnimator.setInterpolator(new DecelerateInterpolator());
        mScoreFlipAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                updateFlip(String.valueOf(mOldScore));
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                updateFlip(String.valueOf(mOldScore));
            }
        });
        mScoreFlipAnimator.start();
    }

    public void setFlipScore(int score) {
        if (mFlipScore != score) {
            mFlipScore = score;
            updateFlip(String.valueOf(score));
        }
    }

    public int getFlipScore() {
        return mFlipScore;
    }

    private void updateFlip(CharSequence chars) {
        setText(chars);
    }
}
