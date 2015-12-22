
package com.miui.securitycenter.manualitem;

import com.miui.securitycenter.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.miui.common.EventHandler;

public class WhiteListItemView extends LinearLayout implements OnClickListener,
        OnCheckedChangeListener {

    private EventHandler mEventHandler;
    private TextView mTitle;
    private TextView mSummary;
    private CheckBox mCheckBoxView;
    private WhiteListItemModel mModel;

    public WhiteListItemView(Context context) {
        this(context, null);
    }

    public WhiteListItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WhiteListItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub
        mModel.setChecked(!mCheckBoxView.isChecked());
    }

    @Override
    protected void onFinishInflate() {
        // TODO Auto-generated method stub
        super.onFinishInflate();
        mTitle = (TextView) findViewById(R.id.title);
        mSummary = (TextView) findViewById(R.id.summary);
        mCheckBoxView = (CheckBox) findViewById(R.id.checkbox);
        mCheckBoxView.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // TODO Auto-generated method stub
        mModel.setChecked(isChecked);
    }

    public void setEventHandler(EventHandler handler) {
        mEventHandler = handler;
    }

    public void fillData(WhiteListItemModel model) {
        mModel = model;
        mTitle.setText(model.getTitle());
        mSummary.setText(model.getSummary());
        mCheckBoxView.setChecked(model.isChecked());
    }
}
