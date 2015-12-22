
package com.miui.securitycenter;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.miui.securitycenter.R;
import com.miui.common.AnimationListenerAdapter;
import com.miui.common.AnimationQueue;
import com.miui.common.EventHandler;
import com.miui.common.ViewAlphaAnimation;

import com.miui.securitycenter.handlebar.HandleBar;
import com.miui.securitycenter.handlebar.HandleHeaderType;
import com.miui.securitycenter.handlebar.HandleItem;
import com.miui.securitycenter.handlebar.HandleItemModel;
import com.miui.securitycenter.system.SystemItemModel;

public class MainActivityView extends RelativeLayout {

    enum BackgroundColor {
        GREEN, BLUE, ORANGE
    }

    private ImageView mForegroundView;
    private ImageView mBackgroundView;

    private ContentView mContentView;
    private MenuBar mMenuBar;
    private HandleBar mHandleBar;
    private ScanningBar mScanningBar;

    public MainActivityView(Context context) {
        this(context, null);
    }

    public MainActivityView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainActivityView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mForegroundView = (ImageView) findViewById(R.id.foreground);
        mBackgroundView = (ImageView) findViewById(R.id.background);

        mContentView = (ContentView) findViewById(R.id.content_frame);
        mMenuBar = (MenuBar) findViewById(R.id.menu_bar);
        mHandleBar = (HandleBar) findViewById(R.id.handle_bar);
        mScanningBar = (ScanningBar) findViewById(R.id.scanning_bar);
    }

    public void updateAdapterData(Map<HandleHeaderType, List<HandleItemModel>> modelMap) {
        mHandleBar.updateAdapterData(modelMap);
    }

    public void setEventHandler(EventHandler handler) {
        mContentView.setEventHandler(handler);
        mMenuBar.setEventHandler(handler);
        mHandleBar.setEventHandler(handler);
        mScanningBar.setEventHandler(handler);
    }

    public void setHandleListAdapter(Context context, EventHandler handler) {
        mHandleBar.setHandleListAdapter(context, handler);
    }

    public void setScore(int score, AnimatorListener listener) {
        mContentView.setScore(score, listener);
        toggleForeground(score);
    }

    public void setListViewSelection(int position) {
        mHandleBar.setListViewSelection(position);
    }

    public void startFlashAnimation() {
        mContentView.startFlashAnimation();
    }

    public void setStatusText(CharSequence text) {
        mContentView.setStatusText(text);
    }

    public void setCheckingText(CharSequence text) {
        mContentView.setCheckingText(text);
    }

    public void resetListAdapter() {
        mScanningBar.resetListAdapter();
    }

    public void setScanningBarAnimation(Animation anim) {
        mScanningBar.setScanningBarAnimation(anim);
    }

    public void setScanningHeaderVisibility(int visibility) {
        mScanningBar.setScanningHeaderVisibility(visibility);
    }

    public void setScanningText(String text) {
        mScanningBar.setScanningText(text);
    }

    public void setScanningTitle(CharSequence text) {
        mScanningBar.setScanningTitle(text);
    }

    public void setScanningNumber(CharSequence text) {
        mScanningBar.setScanningNumber(text);
    }

    public void startCheckingAnimation() {
        mContentView.startCheckingAnimation();
        switchView(mMenuBar, mScanningBar, true);
    }

    public void finishScanningAnimation() {
        mContentView.finishScanningAnimation();
        switchView(mScanningBar, mHandleBar, true);
    }

    public void stopScanningAnimation() {
        mScanningBar.resetListAdapter();
        mContentView.stopCheckingAnimation();
        switchView(mScanningBar, mMenuBar, true);
    }

    public void stopCheckingAnimation() {
        mScanningBar.resetListAdapter();
        switchView(mHandleBar, mMenuBar, true);
        mContentView.stopCheckingAnimation();
    }

    public void removeScanningBarCallbacks(Runnable action) {
        mScanningBar.removeScanningBarCallbacks(action);
    }

    public void setActionButtonText(CharSequence text) {
        mHandleBar.setActionButtonText(text);
    }

    public boolean isHandleBarVisible() {
        return mHandleBar.getVisibility() == View.VISIBLE;
    }

    private AnimationListenerAdapter mCollapseListener = new AnimationListenerAdapter() {
        public void onAnimationEnd(Animation animation) {
            if (mCollapseView != null) {
                mCollapseView.setVisibility(View.GONE);
            }
        };
    };

    private View mCollapseView;

    private void switchView(View hideView, View showView, boolean anim) {
        if (hideView.getVisibility() != View.VISIBLE && showView.getVisibility() == View.VISIBLE) {
            return;
        }
        if (anim) {
            mCollapseView = hideView;
            AnimationQueue animQueue = new AnimationQueue(getContext());
            animQueue.add(hideView, getAnimation(false), mCollapseListener);
            animQueue.add(showView, getAnimation(true), null);
            animQueue.start();
        } else {
            showView.setVisibility(View.VISIBLE);
            hideView.setVisibility(View.GONE);
        }
    }

    private Animation getAnimation(boolean expand) {
        return AnimationUtils.loadAnimation(getContext(),
                expand ? R.anim.expand_to_top : R.anim.collapse_from_top);
    }

    private BackgroundColor mCurrentBackground = BackgroundColor.GREEN;
    private View mToGoneView;
    private View mToVisiableView;

    public void toggleForeground(int score) {
        BackgroundColor target;

        if (score > 80) {
            target = BackgroundColor.GREEN;
        } else if (score > 60) {
            target = BackgroundColor.BLUE;
        } else {
            target = BackgroundColor.ORANGE;
        }

        if (target != mCurrentBackground) {
            if (mForegroundView.getAlpha() == 1) {
                mToGoneView = mForegroundView;
                mToVisiableView = mBackgroundView;
            } else {
                mToVisiableView = mForegroundView;
                mToGoneView = mBackgroundView;
            }

            switch (target) {
                case GREEN:
                    mToVisiableView.setBackgroundResource(R.drawable.main_bg_green);
                    break;
                case BLUE:
                    mToVisiableView.setBackgroundResource(R.drawable.main_bg_blue);
                    break;
                case ORANGE:
                    mToVisiableView.setBackgroundResource(R.drawable.main_bg_orange);
                    break;
                default:
                    break;
            }

            mCurrentBackground = target;
            ViewAlphaAnimation anim = new ViewAlphaAnimation(mToVisiableView, mToGoneView);
            anim.setStartOffset(0);
            anim.setDuration(500);
            mToVisiableView.startAnimation(anim);
        }
    }
}
