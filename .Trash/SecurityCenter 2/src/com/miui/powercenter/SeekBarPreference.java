
package com.miui.powercenter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.miui.securitycenter.R;
import com.miui.powercenter.provider.DataManager;
import com.miui.powercenter.provider.PowerUtils;
import com.miui.securitycenter.NotificationService;

public class SeekBarPreference extends Preference {
    private static final String TAG = "TitlePreference";

    private static final int SEEK_BAR_RANGE = 40;

    private DataManager mDataManager;
    private TextView mTextView;
    private String mTitleFormat;
    private SeekBar mSeekBar;
    private Drawable mSeekBarThumb;
    private SeekBarListener mListener;

    //这个接口只用于 ui 的改变
    public interface SeekBarListener {
        public void onSetCurProgress(int progress);
        public void onProgressChanged(int progress);
        public int  getCurrentProgress();
    }

    public void addListener(SeekBarListener listener) {
        mListener = listener;
    }

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTitleFormat = context.getString(R.string.power_save_low_battery_seekbar_title);
        mSeekBarThumb = context.getResources().getDrawable(R.drawable.multipositionbar_thumb);
        mDataManager = DataManager.getInstance(context.getApplicationContext());
    }

    public SeekBarPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mSeekBar = (SeekBar) view.findViewById(R.id.seekbar);
        mTextView = (TextView) view.findViewById(R.id.title);

        int value = 0;
        if (mListener != null) {
             value = mListener.getCurrentProgress();
             Log.d("LDEBUG", "value: " + value);
        }

        if (mTextView != null) {
            mTextView.setText(String.format(mTitleFormat, value + 10));
        }

        if (mSeekBar != null) {
            mSeekBar.setMax(SEEK_BAR_RANGE);

            if (null != mListener) {
                mListener.onSetCurProgress(value);
            }

            mSeekBar.setProgress(value);
            mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int value = seekBar.getProgress();
                    NotificationService.sendBatteryConfigChangeBroadcast(getContext());
                    if (null != mListener) {
                        Log.d("LIUWEI", "当前的 低电门限: " + value);
                        mListener.onProgressChanged(value);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (mTextView != null) {
                        int value = progress;
                        mTextView.setText(String.format(mTitleFormat, value + 10));
                    }
                }
            });
        }
    }
}
