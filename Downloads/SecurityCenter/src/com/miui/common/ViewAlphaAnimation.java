
package com.miui.common;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ViewAlphaAnimation extends Animation {

    private View mViewToVisiable;
    private View mViewToGone;

    public ViewAlphaAnimation(View viewToVisiable, View viewToGone) {
        mViewToVisiable = viewToVisiable;
        mViewToGone = viewToGone;
    }

    @Override
    protected void applyTransformation(float paramFloat, Transformation paramTransformation) {
        mViewToGone.setAlpha(1.0f * (1 - paramFloat));
        mViewToVisiable.setAlpha(1.0f * paramFloat);
    }
}
