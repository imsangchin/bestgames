
package com.miui.optimizecenter.cache;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import com.miui.securitycenter.R;

public class StateButton extends ImageView implements OnClickListener {

    public interface OnStateChangeListener {
        void onStateChanged(View v, State state);
    }

    public enum State {
        UNCHECK, MIDDLE, CHECKED
    }

    private OnStateChangeListener mOnStateChangeListener;

    private State mState = State.UNCHECK;

    public StateButton(Context context) {
        this(context, null);
    }

    public StateButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StateButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setImageResource(R.drawable.icon_state_uncheck);
        setOnClickListener(this);
    }

    public void setOnStateChangeListener(OnStateChangeListener listener) {
        mOnStateChangeListener = listener;
    }

    public void setState(State state) {
        updateState(state);
    }

    public State getState() {
        return mState;
    }

    private void updateState(State state) {
        if (mState != state) {
            mState = state;
            switch (state) {
                case CHECKED:
                    setImageResource(R.drawable.icon_state_checked);
                    break;
                case MIDDLE:
                    setImageResource(R.drawable.icon_state_middle);
                    break;
                case UNCHECK:
                    setImageResource(R.drawable.icon_state_uncheck);
                    break;
                default:
                    break;
            }

            if (mOnStateChangeListener != null) {
                mOnStateChangeListener.onStateChanged(this, mState);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (mState == State.UNCHECK) {
            updateState(State.CHECKED);
        } else if (mState == State.CHECKED) {
            updateState(State.UNCHECK);
        } else {
            updateState(State.CHECKED);
        }

    }
}
