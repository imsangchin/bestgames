
package com.miui.securitycenter.memory;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MemoryCheckService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return MemoryCheck.getInstance(this).asBinder();
    }

}
