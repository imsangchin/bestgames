
package com.miui.permcenter.root;

import miui.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.miui.securitycenter.R;
import com.lbe.security.bean.AppPermissionConfig;
import com.lbe.security.service.provider.Permissions;
import com.miui.analytics.AnalyticsUtil;
import com.miui.common.AndroidUtils;
import com.miui.permcenter.PermissionUtils;
import com.miui.permcenter.permissions.AppPermissionsEditorActivity;

import java.util.HashMap;

public class RootApplyActivity extends Activity implements OnClickListener {

    public static final String EXTRA_PKGNAME = "extra_pkgname";

    private static final int ROOT_MSG_NEXT_STEP_WHAT = 100;

    private static final int ROOT_MSG_NEXT_STEP_INTERVAL = 1000;

    private static final int MAX_STEPS = 5;

    private TextView mWarningInfoView;

    private Button mRejectButton;
    private Button mAcceptButton;

    private String mPkgName;
    private CharSequence mAppLabel;

    private int mCurrentStep = 1;

    private int mAutoNextStepTime = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pm_activity_root_apply);

        mPkgName = getIntent().getStringExtra(EXTRA_PKGNAME);
        if (TextUtils.isEmpty(mPkgName)) {
            finish();
            return;
        }

        mAppLabel = AndroidUtils.loadAppLabel(this, mPkgName);

        mWarningInfoView = (TextView) findViewById(R.id.warning_info);

        mRejectButton = (Button) findViewById(R.id.reject);
        mRejectButton.setOnClickListener(this);
        mAcceptButton = (Button) findViewById(R.id.accept);
        mAcceptButton.setOnClickListener(this);

        mWarningInfoView.setText(getWarningInfo(mCurrentStep, mAppLabel));

        mAcceptButton.setText(getString(R.string.button_text_next_step_timer, mAutoNextStepTime));
        mAcceptButton.setEnabled(false);

        mHandler.sendEmptyMessageDelayed(ROOT_MSG_NEXT_STEP_WHAT, ROOT_MSG_NEXT_STEP_INTERVAL);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            --mAutoNextStepTime;
            if (mCurrentStep == MAX_STEPS && mAutoNextStepTime == 0) {
                mAcceptButton.setText(R.string.button_text_accept);
                mAcceptButton.setEnabled(true);
            } else {
                if (mAutoNextStepTime == 0) {
                    // mAcceptButton.performClick();
                    mAcceptButton.setText(R.string.button_text_next_step);
                    mAcceptButton.setEnabled(true);
                } else {
                    if (mCurrentStep == MAX_STEPS) {
                        mAcceptButton.setText(getString(R.string.button_text_accept_timer,
                                mAutoNextStepTime));
                    } else {
                        mAcceptButton.setText(getString(R.string.button_text_next_step_timer,
                                mAutoNextStepTime));
                    }

                    mHandler.removeMessages(ROOT_MSG_NEXT_STEP_WHAT);
                    mHandler.sendEmptyMessageDelayed(ROOT_MSG_NEXT_STEP_WHAT,
                            ROOT_MSG_NEXT_STEP_INTERVAL);
                }
            }
        };
    };

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
    };

    @Override
    protected void onDestroy() {
        mHandler.removeMessages(ROOT_MSG_NEXT_STEP_WHAT);
        super.onDestroy();
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reject:
                mHandler.removeMessages(ROOT_MSG_NEXT_STEP_WHAT);

                rejectRootApply();

                finish();
                break;
            case R.id.accept:
                if (mCurrentStep == MAX_STEPS) {
                    mHandler.removeMessages(ROOT_MSG_NEXT_STEP_WHAT);

                    // app root权限(获取)打点
                    String AppName = AndroidUtils.loadAppLabel(
                            RootApplyActivity.this, mPkgName).toString();
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(AnalyticsUtil.TRACK_ID_PERMISSION_APP_PACKAGE, mPkgName);
                    map.put(AnalyticsUtil.TRACK_ID_PERMISSION_APP_NAME, AppName);
                    map.put(AnalyticsUtil.TRACK_ID_PERMISSION_NAME, Permissions.PERM_ID_ROOT + "");
                    map.put(AnalyticsUtil.TRACK_ID_PERMISSION_STATUS, 1 + "");

                    AnalyticsUtil.track(RootApplyActivity.this,
                            AnalyticsUtil.TRACK_ID_PERMISSION_CHANGE, map);

                    acceptRootApply();

                    finish();
                } else {
                    ++mCurrentStep;
                    mAutoNextStepTime = 5;

                    mWarningInfoView.setText(getWarningInfo(mCurrentStep, mAppLabel));

                    if (mCurrentStep == MAX_STEPS) {
                        mAcceptButton.setText(getString(R.string.button_text_accept_timer,
                                mAutoNextStepTime));
                    } else {
                        mAcceptButton.setText(getString(R.string.button_text_next_step_timer,
                                mAutoNextStepTime));
                    }
                    mAcceptButton.setEnabled(false);

                    mHandler.removeMessages(ROOT_MSG_NEXT_STEP_WHAT);
                    mHandler.sendEmptyMessageDelayed(ROOT_MSG_NEXT_STEP_WHAT,
                            ROOT_MSG_NEXT_STEP_INTERVAL);
                }
                break;
            default:
                break;
        }
    }

    private void rejectRootApply() {
        AppPermissionConfig config = PermissionUtils.loadAppPermissionConfig(
                RootApplyActivity.this, mPkgName);
        if (config != null) {
            PermissionUtils.setPermissionAction(this, config, Permissions.PERM_ID_ROOT,
                    AppPermissionConfig.ACTION_REJECT);

            Toast.makeText(RootApplyActivity.this,
                    getString(R.string.toast_root_apply_reject, mAppLabel),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void acceptRootApply() {
        AppPermissionConfig config = PermissionUtils.loadAppPermissionConfig(
                RootApplyActivity.this, mPkgName);
        if (config != null) {
            PermissionUtils.setPermissionAction(this, config, Permissions.PERM_ID_ROOT,
                    AppPermissionConfig.ACTION_ACCEPT);

            Toast.makeText(RootApplyActivity.this,
                    getString(R.string.toast_root_apply_accept, mAppLabel),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private String getWarningInfo(int step, CharSequence appLabel) {
        switch (step) {
            case 1:
                return getString(R.string.root_apply_step_1, appLabel);
            case 2:
                return getString(R.string.root_apply_step_2, appLabel);
            case 3:
                return getString(R.string.root_apply_step_3, appLabel);
            case 4:
                return getString(R.string.root_apply_step_4, appLabel);
            case 5:
                return getString(R.string.root_apply_step_5, appLabel);
            default:
                return null;
        }
    }
}
