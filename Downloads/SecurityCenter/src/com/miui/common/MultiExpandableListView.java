
package com.miui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.miui.common.ExpandableListView.OnListAllItemsCollapsedListener;

public class MultiExpandableListView extends ExpandableListView implements
        ExpandableListView.OnChildClickListener, ExpandableListView.OnGroupClickListener {

    public static abstract class MultiExpandableListAdapter {
        public static final int IGNORE_VALUE = -1;

        private MultiExpandableListView mListView;

        public final void refresh() {
            notifyDataSetChanged();
        }

        public final void notifyDataSetChanged() {
            mListView.mPrivateAdapter.notifyDataSetChanged();
        }

        public final ListView getListView() {
            return mListView;
        }

        public Context getContext() {
            return mListView.getContext();
        }

        public abstract int getGroupCount();

        public abstract int getChildrenCount(int groupPosition);

        public abstract View getGroupView(LayoutInflater inflater, int groupPosition,
                boolean isExpanded, View convertView, ViewGroup parent);

        public abstract View getChildMainView(LayoutInflater inflater, int groupPosition,
                int childPosition, boolean isLastChild, View convertView, ViewGroup parent);

        public abstract View getChildSubView(LayoutInflater inflater, int groupPosition,
                int childPosition, boolean isLastChild, View convertView, ViewGroup parent);
    }

    public static final int NO_POS = -100;

    private AnimationListener mAnimationListener = new AnimationListenerAdapter() {
        @Override
        public void onAnimationEnd(Animation animation) {
            // do not animate on refresh
            mShouldAnimate = false;
            // restores scrolling
            setEnabled(true);
        }
    };

    private class PrivateListAdapter extends BaseExpandableListAdapter {

        private LayoutInflater mInflater;
        private Context mContext;

        PrivateListAdapter() {
            mContext = getContext();
            mInflater = LayoutInflater.from(getContext());
        }

        @Override
        public int getGroupCount() {
            return mAdapter.getGroupCount();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mAdapter.getChildrenCount(groupPosition);
        }

        @Override
        public Object getGroup(int groupPosition) {
            return groupPosition;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return groupPosition * 10000 + childPosition;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return groupPosition * 10000 + childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                ViewGroup parent) {
            return mAdapter.getGroupView(mInflater, groupPosition, isExpanded, convertView, parent);
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                View convertView, ViewGroup parent) {
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

            cont.setAnimationListener(mAnimationListener);
            View mainConvertView = cont.getMainView();
            View main = mAdapter.getChildMainView(mInflater, groupPosition, childPosition,
                    isLastChild, mainConvertView, parent);
            cont.setMainView(main);
            View subConvertView = cont.getSubView();
            View sub = mAdapter.getChildSubView(mInflater, groupPosition, childPosition,
                    isLastChild, subConvertView, parent);
            cont.setSubView(sub);

            int position = groupPosition * 10000 + childPosition;

            if (position == mDeleteChildPos) {
                mDeleteChildPos = NO_POS;
                cont.delete(mShouldAnimate);
            } else if (position == mExpandChildPos) {
                cont.collapse(false);
                // wait for collapse anim to finish first
                // cont.expand(mShouldAnimate, mCollapseChildPos != NO_POS);
                cont.expand(mShouldAnimate);
            } else if (position == mCollapseChildPos) {
                cont.expand(false);
                cont.collapse(mShouldAnimate);
            } else {
                cont.collapse(false);
            }
            return cont;
        }

    };

    private MultiExpandableListAdapter mAdapter;
    private PrivateListAdapter mPrivateAdapter = new PrivateListAdapter();

    private OnListAllItemsCollapsedListener mOnListAllItemsCollapsedListener;

    private boolean mShouldAnimate;

    public MultiExpandableListView(Context context) {
        super(context);
    }

    public MultiExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MultiExpandableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        // ignored
    }

    @Override
    public void setAdapter(ExpandableListAdapter adapter) {
        // ignored
    }

    public void setAdapter(MultiExpandableListAdapter adapter) {
        mAdapter = adapter;
        mAdapter.mListView = this;
        super.setAdapter(mPrivateAdapter);
        // TODO: add dpad nav support
        // setOnGroupClickListener(this);
        setOnChildClickListener(this);
    }

    public MultiExpandableListAdapter getListAdapter() {
        return mAdapter;
    }

    private void notifyAllItemCollapsed(boolean collapsed) {
        if (mOnListAllItemsCollapsedListener != null) {
            mOnListAllItemsCollapsedListener.onListAllItemsCollapsed(collapsed);
        }
    }

    public void setOnListAllItemsCollapsedListener(OnListAllItemsCollapsedListener listener) {
        mOnListAllItemsCollapsedListener = listener;
    }

    //
    // @Override
    // public boolean collapseGroup(int groupPos) {
    // // ignore
    // return false;
    // }

    public void collapseAllItem() {
        collapseAllItem(false);
    }

    public void collapseAllItem(boolean animate) {
        if (mExpandChildPos != NO_POS) {
            mCollapseChildPos = mExpandChildPos;
            mExpandChildPos = NO_POS;
            mShouldAnimate = animate;
            mPrivateAdapter.notifyDataSetChanged();
            notifyAllItemCollapsed(true);
        }
    }

    public void deleteExpandedItem(boolean animate) {
        if (mExpandChildPos != NO_POS) {
            mDeleteChildPos = mExpandChildPos;
            mShouldAnimate = animate;
            mExpandChildPos = NO_POS;
            mCollapseChildPos = NO_POS;
            mPrivateAdapter.notifyDataSetChanged();
        }
    }

    private int mExpandChildPos = NO_POS, mCollapseChildPos = NO_POS, mDeleteChildPos = NO_POS;

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
            int childPosition, long id) {
        int position = groupPosition * 10000 + childPosition;

        if (mExpandChildPos == position) {
            // already expanded
            mCollapseChildPos = position;
            mExpandChildPos = NO_POS;
        } else {
            // expanded elsewhere
            mCollapseChildPos = mExpandChildPos;
            mExpandChildPos = position;
        }

        mShouldAnimate = true;
        // disables scrolling while animating
        setEnabled(false);
        mPrivateAdapter.notifyDataSetChanged();
        notifyAllItemCollapsed(mExpandChildPos == NO_POS);
        return true;
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        return false;
    }
}
