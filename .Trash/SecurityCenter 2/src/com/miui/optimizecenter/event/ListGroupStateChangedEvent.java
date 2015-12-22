
package com.miui.optimizecenter.event;

import com.miui.optimizecenter.cache.StateButton.State;

public class ListGroupStateChangedEvent {

    private String mAppName;
    private String mPkgName;
    private State mState;

    private ListGroupStateChangedEvent() {

    }

    public static ListGroupStateChangedEvent create(String pkgName, String appName, State state) {
        ListGroupStateChangedEvent res = new ListGroupStateChangedEvent();
        res.mPkgName = pkgName;
        res.mAppName = appName;
        res.mState = state;
        return res;
    }

    public String getPkgName() {
        return mPkgName;
    }

    public State getState() {
        return mState;
    }

    public String getAppName() {
        return mAppName;
    }

    @Override
    public String toString() {
        return "ExpandListGroupEvent PkgName = " + mPkgName + " mState = " + mState;
    }
}
