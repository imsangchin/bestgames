
package com.miui.common;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

import com.miui.common.ExpandableListView.ListItemCollapseListener;
import com.miui.common.ExpandableListView.ListItemDeleteListener;
import com.miui.common.ExpandableListView.ListItemExpandListener;

public class ExpandableListItem extends LinearLayout {

    public static final int ANIM_OFFSET = 0;
    public static final int ANIM_DURATION = 150;

    private ViewGroup mMainCont, mSubCont;

    private Animation mAnimation;
    private AnimationListener mAnimationListener;

    private Handler mHandler;

    private final Runnable mExpandAnimStartRunnable = new Runnable() {
        @Override
        public void run() {
            notifyWillExpand();
            // mSubCont.setVisibility(View.VISIBLE);
        }
    };

    private final Runnable mCollapseAnimStartRunnable = new Runnable() {
        @Override
        public void run() {
            notifyWillCollapse();
        }
    };

    private final Runnable mDeleteAnimStartRunnable = new Runnable() {
        @Override
        public void run() {
            notifyWillDelete();
        }
    };

    private final AnimationListener mExpandListener = new AnimationListenerAdapter() {
        @Override
        public void onAnimationStart(Animation animation) {
            mHandler.postDelayed(mExpandAnimStartRunnable, animation.getStartOffset());
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mAnimation = null;
            mAnimationListener.onAnimationEnd(animation);
            notifyDidExpand();
        }
    };

    private final AnimationListener mCollapseListener = new AnimationListenerAdapter() {
        @Override
        public void onAnimationStart(Animation animation) {
            mHandler.postDelayed(mCollapseAnimStartRunnable, animation.getStartOffset());
        };

        @Override
        public void onAnimationEnd(Animation animation) {
            // mSubCont.setVisibility(View.GONE);
            mAnimation = null;
            mAnimationListener.onAnimationEnd(animation);
            notifyDidCollapse();
        }
    };

    private final AnimationListener mTranslateListener = new AnimationListenerAdapter() {

        @Override
        public void onAnimationStart(Animation animation) {
            mHandler.postDelayed(mDeleteAnimStartRunnable, animation.getStartOffset());
        };

        @Override
        public void onAnimationEnd(Animation animation) {
            setVisibility(View.GONE);
            mAnimation = null;
            mAnimationListener.onAnimationEnd(animation);
            notifyDidDelete();
        }

    };

    public ExpandableListItem(Context context) {
        this(context, null);
    }

    public ExpandableListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHandler = new Handler(context.getMainLooper());
        setOrientation(LinearLayout.VERTICAL);
    }

    public void notifyDataSetChanged() {
        mMainCont = (ViewGroup) getChildAt(0);
        mSubCont = (ViewGroup) getChildAt(1);
    }

    public void setMainView(View view) {
        mMainCont.removeAllViews();
        mMainCont.addView(view);
    }

    /**
     * if(height > 0){the mSubViewHeight = height} else { mSubViewHeight
     * mSubCont.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
     * mSubViewHeight = mSubCont.getMeasuredHeight();}
     * 
     * @param view
     * @param height
     */
    public void setSubView(View view) {
        mSubCont.removeAllViews();
        mSubCont.addView(view);
    }

    public View getMainView() {
        return mMainCont.getChildAt(0);
    }

    public View getSubView() {
        return mSubCont.getChildAt(0);
    }

    public void setAnimationListener(AnimationListener listener) {
        mAnimationListener = listener;
    }

    private void notifyWillExpand(View v) {
        if (v instanceof ListItemExpandListener) {
            ((ListItemExpandListener) v).willExpand(v);
        }
    }

    private void notifyWillExpand() {
        notifyWillExpand(getMainView());
        notifyWillExpand(getSubView());
    }

    private void notifyDidExpand(View v) {
        if (v instanceof ListItemExpandListener) {
            ((ListItemExpandListener) v).didExpand(v);
        }
    }

    private void notifyDidExpand() {
        notifyDidExpand(getMainView());
        notifyDidExpand(getSubView());
    }

    private void notifyWillCollapse(View v) {
        if (v instanceof ListItemCollapseListener) {
            ((ListItemCollapseListener) v).willCollapse(v);
        }
    }

    private void notifyWillCollapse() {
        notifyWillCollapse(getMainView());
        notifyWillCollapse(getSubView());
    }

    private void notifyDidCollapse(View v) {
        if (v instanceof ListItemCollapseListener) {
            ((ListItemCollapseListener) v).didCollapse(v);
        }
    }

    private void notifyDidCollapse() {
        notifyDidCollapse(getMainView());
        notifyDidCollapse(getSubView());
    }

    private void notifyWillDelete() {
        notifyWillDelete(getMainView());
        notifyWillDelete(getSubView());
    }

    private void notifyWillDelete(View v) {
        if (v instanceof ListItemDeleteListener) {
            ((ListItemDeleteListener) v).willDelete(v);
        }
    }

    private void notifyDidDelete() {
        notifyDidDelete(getMainView());
        notifyDidDelete(getSubView());
    }

    private void notifyDidDelete(View v) {
        if (v instanceof ListItemDeleteListener) {
            ((ListItemDeleteListener) v).didDelete(v);
        }
    }

    private void setViewHeight(View view, int height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = height;
        view.setLayoutParams(params);
        ViewParent parent = view.getParent();
        if (parent != null) {
            parent.requestLayout();
        }
    }

    public void delete(boolean anim) {
        delete(anim, false);
    }

    public void delete(boolean anim, boolean delayed) {
        if (anim && mAnimation == null) {
            int width = getWidth();
            if (width == 0) {
                measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
                width = getMeasuredWidth();
            }

            mAnimation = new TranslateAnimation(0, -width, 0, 0);
            mAnimation.setStartOffset(ANIM_OFFSET * 2 + ANIM_DURATION);
            mAnimation.setDuration(ANIM_DURATION * 2);
            mAnimation.setAnimationListener(mTranslateListener);
            this.clearAnimation();
            this.startAnimation(mAnimation);
        } else {
            setViewHeight(this, 0);
            notifyWillDelete();
            notifyDidDelete();
        }
    }

    public void expand(boolean anim) {
        expand(anim, false);
    }

    public void expand(boolean anim, boolean delayed) {
        if (anim && mAnimation == null) {
            mAnimation = createAnimation(true);
            if (delayed) {
                mAnimation.setStartOffset(ANIM_OFFSET * 2 + ANIM_DURATION);
            }
            mSubCont.clearAnimation();
            mSubCont.startAnimation(mAnimation);
        } else {
            // restores view height to avoid list refresh flicker
            // setViewHeight(mSubCont, mSubViewHeight);
            mSubCont.setVisibility(View.VISIBLE);
            notifyWillExpand();
            notifyDidExpand();
        }
    }

    public void collapse(boolean anim) {
        if (anim && mAnimation == null) {
            mAnimation = createAnimation(false);
            mSubCont.clearAnimation();
            mSubCont.startAnimation(mAnimation);
        } else {
            // restores view height to avoid list refresh flicker
            // setViewHeight(mSubCont, 0);
            mSubCont.setVisibility(View.GONE);
            notifyWillCollapse();
            notifyDidCollapse();
        }
    }

    public void cancelAnimation() {
        if (mAnimation != null) {
            mAnimation.cancel();
        }
    }

    private Animation createAnimation(boolean expand) {
        Animation anim = new ExpandableAnimation(mSubCont, expand ? ExpandableType.EXPAND
                : ExpandableType.COLLAPSE);
        anim.setStartOffset(ANIM_OFFSET);
        anim.setDuration(ANIM_DURATION);
        anim.setAnimationListener(expand ? mExpandListener : mCollapseListener);
        return anim;
    }

    private enum ExpandableType {
        COLLAPSE, EXPAND
    }

    private class ExpandableAnimation extends Animation {
        private View mAnimatedView;
        private int mEndHeight;
        private ExpandableType mExpandableType;
        private LinearLayout.LayoutParams mLayoutParams;

        public ExpandableAnimation(View view, ExpandableType expandableType) {
            mAnimatedView = view;
            int height = view.getHeight();
            if (height != 0) {
                mEndHeight = height;
            } else {
                mAnimatedView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
                mEndHeight = mAnimatedView.getMeasuredHeight();
            }
            mLayoutParams = ((LinearLayout.LayoutParams) view.getLayoutParams());
            mExpandableType = expandableType;
            if (expandableType == ExpandableType.EXPAND) {
                mLayoutParams.bottomMargin = -mEndHeight;
            } else {
                mLayoutParams.bottomMargin = 0;
            }
            view.setVisibility(View.VISIBLE);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {

            super.applyTransformation(interpolatedTime, t);
            if (interpolatedTime < 1.0f) {
                if (mExpandableType == ExpandableType.EXPAND) {
                    mLayoutParams.bottomMargin = -mEndHeight
                            + (int) (mEndHeight * interpolatedTime);
                } else {
                    mLayoutParams.bottomMargin = -(int) (mEndHeight * interpolatedTime);
                }
                mAnimatedView.requestLayout();
            } else {
                if (mExpandableType == ExpandableType.EXPAND) {
                    mLayoutParams.bottomMargin = 0;
                    mAnimatedView.getParent().requestLayout();
                } else {
                    mLayoutParams.bottomMargin = -mEndHeight;
                    mAnimatedView.getParent().requestLayout();
                    mAnimatedView.setVisibility(View.GONE);
                }
            }
        }
    }
}
