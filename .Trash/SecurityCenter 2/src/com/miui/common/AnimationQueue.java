
package com.miui.common;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

import java.util.ArrayList;

public class AnimationQueue implements AnimationListener {

    static final String TAG = "AnimationQueue";

    public static class AnimationUnit {
        private View mView;
        private Animation mAnim;
        private AnimationListener mAnimListener;

        public AnimationUnit(View view, Animation anim, AnimationListener listener) {
            mView = view;
            mAnim = anim;
            mAnimListener = listener;
        }
    }

    private ArrayList<AnimationUnit> mUnits = new ArrayList<AnimationUnit>();
    private int mCurrentAnim;
    private Handler mHandler;

    private final Runnable RunAnimRunnable = new Runnable() {

        @Override
        public void run() {
            AnimationUnit unit = mUnits.get(mCurrentAnim);
            View view = unit.mView;
            Animation anim = unit.mAnim;

            anim.setAnimationListener(AnimationQueue.this);
            view.setVisibility(View.VISIBLE);
            view.startAnimation(anim);
        }

    };

    public AnimationQueue(Context context) {
        mHandler = new Handler(context.getMainLooper());
    }

    public AnimationQueue add(View view, Animation anim, AnimationListener listener) {
        mUnits.add(new AnimationUnit(view, anim, listener));
        return this;
    }

    public void start() {
        if (mUnits.size() == 0) {
            return;
        }
        mHandler.post(RunAnimRunnable);
    }

    @Override
    public void onAnimationStart(Animation animation) {
        AnimationUnit unit = mUnits.get(mCurrentAnim);
        View view = unit.mView;
        AnimationListener listener = unit.mAnimListener;

        view.setEnabled(false);
        if (listener != null) {
            listener.onAnimationStart(animation);
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        AnimationUnit unit = mUnits.get(mCurrentAnim);
        AnimationListener listener = unit.mAnimListener;
        if (listener != null) {
            listener.onAnimationRepeat(animation);
        }
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        AnimationUnit unit = mUnits.get(mCurrentAnim);
        View view = unit.mView;
        AnimationListener listener = unit.mAnimListener;

        if (listener != null) {
            listener.onAnimationEnd(animation);
        }
        mCurrentAnim++;
        if (mCurrentAnim < mUnits.size()) {
            mHandler.post(RunAnimRunnable);
        }
        view.setEnabled(true);
    }

}
