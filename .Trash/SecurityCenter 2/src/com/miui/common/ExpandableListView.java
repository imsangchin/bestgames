
package com.miui.common;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

public class ExpandableListView extends ListView implements OnItemSelectedListener,
        OnItemClickListener {

    public interface ListItemExpandListener {
        void willExpand(View v);

        void didExpand(View v);
    }

    public interface ListItemCollapseListener {
        void willCollapse(View v);

        void didCollapse(View v);
    }

    public interface ListItemDeleteListener {
        void willDelete(View v);

        void didDelete(View v);
    }

    public interface ListItemDataSetChangeListener {
        void onDataSetChanged(View v);
    }

    public interface OnListAllItemsCollapsedListener {
        void onListAllItemsCollapsed(boolean collapsed);
    }

    public static abstract class ExpandableListAdapter {
        public static final int IGNORE_VALUE = -1;

        private ExpandableListView mListView;

        public final void refresh() {
            notifyDataSetChanged();
        }

        public final void notifyDataSetChanged() {
            mListView.mPrivateAdapter.notifyDataSetChanged();
        }

        public final ListView getListView() {
            return mListView;
        }

        public abstract int getCount();

        public abstract View getMainView(LayoutInflater inflater, int position, View convertView,
                ViewGroup parent);

        public abstract View getSubView(LayoutInflater inflater, int position, View convertView,
                ViewGroup parent);
    }

    private static final int NO_POS = -100;

    private static final int SCROLL_DELAY = ExpandableListItem.ANIM_DURATION * 2;

    private AnimationListener mDeleteAnimationListener = new AnimationListenerAdapter() {
        @Override
        public void onAnimationEnd(Animation animation) {
            // do not animate on refresh
            mShouldAnimate = false;
            // restores scrolling
            setEnabled(true);
        }
    };

    private class PrivateListAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private Context mContext;

        PrivateListAdapter() {
            mContext = getContext();
            mInflater = LayoutInflater.from(getContext());
        }

        @Override
        public int getCount() {
            return mAdapter.getCount();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ExpandableListItem cont;
            if (convertView != null) {
                cont = (ExpandableListItem) convertView;
                cont.setVisibility(View.VISIBLE);
            } else {
                cont = new ExpandableListItem(mContext);
                cont.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
                FrameLayout main_view = new FrameLayout(mContext);
                FrameLayout sub_view = new FrameLayout(mContext);
                cont.addView(main_view);
                cont.addView(sub_view);
                cont.notifyDataSetChanged();
            }
            cont.setAnimationListener(mDeleteAnimationListener);
            View mainConvertView = cont.getMainView();
            View main = mAdapter.getMainView(mInflater, position, mainConvertView, parent);
            cont.setMainView(main);
            View subConvertView = cont.getSubView();
            View sub = mAdapter.getSubView(mInflater, position, subConvertView, parent);
            cont.setSubView(sub);
            if (position == mDeletePos) {
                mDeletePos = NO_POS;
                cont.delete(mShouldAnimate);
            } else if (position == mExpandPos) {
                cont.collapse(false);
                // wait for collapse anim to finish first
                // cont.expand(mShouldAnimate, mCollapsePos != NO_POS);
                cont.expand(mShouldAnimate);
            } else if (position == mCollapsePos) {
                cont.expand(false);
                cont.collapse(mShouldAnimate);
            } else {
                cont.collapse(false);
            }
            return cont;
        }
    };

    private final Runnable mScrollToBottomRunnable = new Runnable() {

        @Override
        public void run() {
            smoothScrollBy(mItemHeight, SCROLL_DELAY);
        }

    };

    private ExpandableListAdapter mAdapter;
    private PrivateListAdapter mPrivateAdapter = new PrivateListAdapter();

    private OnListAllItemsCollapsedListener mOnListAllItemsCollapsedListener;

    private int mExpandPos = NO_POS, mCollapsePos = NO_POS, mDeletePos = NO_POS;
    private boolean mShouldAnimate;

    private int mItemHeight;

    private Handler mHandler;

    public ExpandableListView(Context context) {
        super(context);
        mHandler = new Handler(context.getMainLooper());
    }

    public ExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHandler = new Handler(context.getMainLooper());
    }

    public ExpandableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mHandler = new Handler(context.getMainLooper());
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        // ignored
    }

    public void setAdapter(ExpandableListAdapter adapter) {
        mAdapter = adapter;
        mAdapter.mListView = this;
        super.setAdapter(mPrivateAdapter);
        // TODO: add dpad nav support
        // setOnItemSelectedListener(this);
        setOnItemClickListener(this);
    }

    public ExpandableListAdapter getListAdapter() {
        return mAdapter;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == mExpandPos) {
            setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
            view.requestFocus();
        } else if (!isFocused()) {
            setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
            requestFocus();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mExpandPos == position) {
            // already expanded
            mCollapsePos = position;
            mExpandPos = NO_POS;
        } else {
            // expanded elsewhere
            mCollapsePos = mExpandPos;
            mExpandPos = position;
        }
        if (mExpandPos != NO_POS) {
            int first = getFirstVisiblePosition();
            int last = getLastVisiblePosition();
            if (first <= last) {
                int scrollPos = NO_POS;
                if (mExpandPos == last || mExpandPos == last - 1) {
                    // expands near bottom
                    scrollPos = mExpandPos + 1;
                    if (mCollapsePos != NO_POS) {
                        // collapse occurs else where, scroll less
                        scrollPos -= 1;
                    }
                }
                if (scrollPos != NO_POS) {
                    smoothScrollToPosition(scrollPos);
                }
                if (mExpandPos == parent.getCount() - 1) {
                    // expands last item, scrolls list after expanding
                    if (mItemHeight == 0) {
                        mItemHeight = mCollapsePos == position ? view.getHeight() / 2 : view
                                .getHeight();
                    }
                    scrollToBottom();
                }
            }
        }
        mShouldAnimate = true;
        // disables scrolling while animating
        setEnabled(false);
        mPrivateAdapter.notifyDataSetChanged();
        notifyAllItemCollapsed(mExpandPos == NO_POS);
    }

    private void notifyAllItemCollapsed(boolean collapsed) {
        if (mOnListAllItemsCollapsedListener != null) {
            mOnListAllItemsCollapsedListener.onListAllItemsCollapsed(collapsed);
        }
    }

    public void setOnListAllItemsCollapsedListener(OnListAllItemsCollapsedListener listener) {
        mOnListAllItemsCollapsedListener = listener;
    }

    private void scrollToBottom() {
        // TODO smooth scroll to bottom
        // mHandler.postDelayed(mScrollToBottomRunnable, SCROLL_DELAY);
    }

    public void collapseAllItem() {
        collapseAllItem(false);
    }

    public void collapseAllItem(boolean animate) {
        if (mExpandPos != NO_POS) {
            mCollapsePos = mExpandPos;
            mExpandPos = NO_POS;
            mShouldAnimate = animate;
            mPrivateAdapter.notifyDataSetChanged();
            notifyAllItemCollapsed(true);
        }
    }

    public void deleteExpandedItem(boolean animate) {
        if (mExpandPos != NO_POS) {
            mDeletePos = mExpandPos;
            mShouldAnimate = animate;
            mExpandPos = NO_POS;
            mCollapsePos = NO_POS;
            mPrivateAdapter.notifyDataSetChanged();
        }
    }

}
