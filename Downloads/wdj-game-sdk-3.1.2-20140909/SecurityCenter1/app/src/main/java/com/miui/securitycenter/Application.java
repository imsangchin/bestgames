
package com.miui.securitycenter;

import android.content.Intent;

import com.miui.common.PreferenceStore;

public class Application extends miui.external.Application {

    @Override
    public miui.external.ApplicationDelegate onCreateApplicationDelegate() {
        return new ApplicationDelegate();
    }
}
