package com.anzhuoshoudiantong;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.PowerManager;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;

import com.wandoujia.apkruntime.ias.IASActivity;

public class LedController implements SurfaceHolder.Callback {
    private static final int SDKVERSION = Build.VERSION.SDK_INT;
    private Camera camera = null;
    private String ledOnFlag = null;
    LedStatus lightStatus = LedStatus.OFF;
    private SurfaceHolder mHolder = null;
    private SurfaceView preview = null;
    private PowerManager.WakeLock wakeLock = null;
    private IASActivity activity;

    public LedController(IASActivity activity) {
        this.activity = activity;
        PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "FlashLight");
        if(wakeLock != null) {
            wakeLock.acquire();
        }
    }

    private String getFlashOnParam(Camera.Parameters paramParameters) {
        if (SDKVERSION > 9) {
            return Camera.Parameters.FLASH_MODE_TORCH;
        }
        return Camera.Parameters.FLASH_MODE_ON;
    }

    public void createCamera(SurfaceView surfaceView) {
        this.preview = surfaceView;
        try {
            if (this.camera == null) {
                this.camera = Camera.open();
            }
            this.ledOnFlag = getFlashOnParam(this.camera.getParameters());

            this.mHolder = this.preview.getHolder();
            this.mHolder.addCallback(this);
            // this.mHolder.setType(3);
            try {
                this.camera.setPreviewDisplay(this.mHolder);
                this.camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

    }

    public String getLightStatus() {
        return this.lightStatus.toString();
    }

    public boolean ledExist() {
        return activity.getBaseContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_FLASH);
    }

    public void releaseLed() {
        if (this.wakeLock != null) {
            if (this.wakeLock.isHeld()) this.wakeLock.release();
            this.wakeLock = null;
        }
        if (this.camera != null) {
            this.camera.stopPreview();
            this.camera.release();
            this.camera = null;
        }
    }

    public void setLightStatus(LedStatus status) {
        this.lightStatus = status;
    }

    public void sleepModeOff() {
        if (this.wakeLock == null) {
            this.wakeLock = ((PowerManager) activity.getSystemService("power"))
                    .newWakeLock(26, getClass().getName());
        }
        if (this.wakeLock != null) this.wakeLock.acquire();
    }

    public void sleepModeOn() {
        if ((this.wakeLock != null) && (this.wakeLock.isHeld()))
            this.wakeLock.release();
    }

    public void surfaceChanged(SurfaceHolder paramSurfaceHolder, int paramInt1,
            int paramInt2, int paramInt3) {}

    public void surfaceCreated(SurfaceHolder paramSurfaceHolder) {
        if (this.camera == null) {
            return;
        }
        this.mHolder = paramSurfaceHolder;
        try {
            this.camera.setPreviewDisplay(this.mHolder);
            return;
        } catch (IOException localIOException) {
            localIOException.printStackTrace();
        }
    }

    public void surfaceDestroyed(SurfaceHolder paramSurfaceHolder) {
        if (this.camera != null) this.camera.stopPreview();
    }

    public void turnOffLed() {
        if(camera == null) {
            return;
        }
        Camera.Parameters localParameters = this.camera.getParameters();
        if (localParameters == null) {
            return;
        }
        localParameters.setFlashMode(LedStatus.OFF.toString());
        this.camera.setParameters(localParameters);
        this.lightStatus = LedStatus.OFF;
    }

    public void turnOnLed() {
        if(camera == null) {
            return;
        }
        Camera.Parameters localParameters = this.camera.getParameters();
        if (localParameters == null) {
            return;
        }
        localParameters.setFlashMode(this.ledOnFlag);
        this.camera.setParameters(localParameters);
        this.lightStatus = LedStatus.ON;
    }
}
