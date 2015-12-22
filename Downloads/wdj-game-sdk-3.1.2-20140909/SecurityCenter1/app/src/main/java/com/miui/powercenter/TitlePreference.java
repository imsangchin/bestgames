package com.miui.powercenter;

import com.miui.powercenter.provider.PowerUtils;
import com.miui.powercenter.provider.PowerData.PowerMode;
import com.miui.securitycenter.R;

import android.content.Context;
import android.preference.Preference;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

public class TitlePreference extends Preference {
    private EditText mEditor;
    private PowerMode mMode;
    private int mModeType;
    private Context mContext;

    public TitlePreference(Context context, PowerMode powerMode, int modeType) {
        super(context);
        mContext = context;
        mMode = powerMode;
        mModeType = modeType;
        setLayoutResource(R.layout.pc_label_editor);
    }

    @Override
    protected void onBindView(View view) {
        mEditor = (EditText) view.findViewById(R.id.label);
        if (mEditor != null) {
            if (mModeType == PowerModeCustomizer.MODE_TYPE_ADD) {
                String name = PowerUtils.getAvailableUserDefineName(mContext);
                mEditor.setHint(name);
                mMode.mDBValue[PowerMode.INDEX_TITLE] = name;
                mMode.mDBValue[PowerMode.INDEX_NAME] = name;
            } else if (mModeType == PowerModeCustomizer.MODE_TYPE_CUSTOM) {
                mEditor.setText(String.valueOf(mMode.mDBValue[PowerMode.INDEX_NAME]));
            }

            mEditor.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start,
                        int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                        int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    int editStart = mEditor.getSelectionStart();
                    int editEnd = mEditor.getSelectionEnd();

                    mEditor.removeTextChangedListener(this);

                    while (PowerUtils.getStringLength(s.toString()) > 20) {
                        s.delete(editStart - 1, editEnd);
                        editStart--;
                        editEnd--;
                    }

                    mEditor.setText(s);
                    mEditor.setSelection(editStart);

                    mEditor.addTextChangedListener(this);

                    String modeName = s.toString();
                    mMode.mDBValue[PowerMode.INDEX_TITLE] = modeName;
                    mMode.mDBValue[PowerMode.INDEX_NAME] = modeName;
                }

            });
        }
        super.onBindView(view);
    }
}
