
package com.miui.common;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.AbsListView.OnScrollListener;

public class PinnedHeaderExpandableListView extends ExpandableListView implements OnScrollListener {

    private View mHeaderGroupView;
    private int mHeaderGroupWidth;
    private int mHeaderGroupHeight;

    private OnScrollListener mScrollListener;

    public PinnedHeaderExpandableListView(Context context) {
        this(context, null);
    }

    public PinnedHeaderExpandableListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PinnedHeaderExpandableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOnScrollListener(this);
    }

    @Override
    public void setOnScrollListener(OnScrollListener listener) {
        if (listener != this) {
            mScrollListener = listener;
        }
        super.setOnScrollListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mHeaderGroupView == null) {
            return;
        }
        ViewGroup.LayoutParams layoutParams = mHeaderGroupView.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            mHeaderGroupView.setLayoutParams(layoutParams);
        }
        measureChild(mHeaderGroupView, widthMeasureSpec, heightMeasureSpec);
        mHeaderGroupWidth = mHeaderGroupView.getMeasuredWidth();
        mHeaderGroupHeight = mHeaderGroupView.getMeasuredHeight();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mHeaderGroupView == null) {
            return;
        }
        mHeaderGroupView.layout(0, 0, mHeaderGroupWidth, mHeaderGroupHeight);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mHeaderGroupView != null) {
            drawChild(canvas, mHeaderGroupView, getDrawingTime());
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        int pos = pointToPosition(x, y);
        if (y >= mHeaderGroupView.getTop() && y <= mHeaderGroupView.getBottom()) {
            if (ev.getAction() == MotionEvent.ACTION_UP) {
                int groupPosition = getPackedPositionGroup(getExpandableListPosition(pos));
                if (groupPosition != INVALID_POSITION) {
                    if (isGroupExpanded(groupPosition)) {
                        collapseGroup(groupPosition);
                    } else {
                        expandGroup(groupPosition);
                    }
                }

            }
            return true;
        }

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (mHeaderGroupView != null && scrollState == SCROLL_STATE_IDLE) {
            int firstVisiblePos = getFirstVisiblePosition();
            if (firstVisiblePos == 0) {
                mHeaderGroupView.layout(0, 0, mHeaderGroupWidth, mHeaderGroupHeight);
            }
        }
        if (mScrollListener != null) {
            mScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        if (totalItemCount > 0) {

            ExpandableListAdapter adapter = getExpandableListAdapter();

            int firstVisiblePos = getFirstVisiblePosition();
            int pos = firstVisiblePos + 1;
            int firstVisibleGroupPos = getPackedPositionGroup(getExpandableListPosition(firstVisiblePos));
            int group = getPackedPositionGroup(getExpandableListPosition(pos));

            mHeaderGroupView = adapter.getGroupView(firstVisibleGroupPos,
                    isGroupExpanded(firstVisibleGroupPos), mHeaderGroupView, this);

            if (group == firstVisibleGroupPos + 1) {
                View childView = getChildAt(1);
                if (childView.getTop() <= mHeaderGroupHeight) {
                    int delta = mHeaderGroupHeight - childView.getTop();
                    mHeaderGroupView.layout(0, -delta, mHeaderGroupWidth, mHeaderGroupHeight
                            - delta);
                }
            } else {
                mHeaderGroupView.layout(0, 0, mHeaderGroupWidth, mHeaderGroupHeight);
            }

        }
        if (mScrollListener != null) {
            mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }
}
