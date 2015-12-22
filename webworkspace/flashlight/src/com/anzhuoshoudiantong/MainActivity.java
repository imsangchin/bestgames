package com.anzhuoshoudiantong;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.anzhuoshoudiantong.ads.AdController;
import com.anzhuoshoudiantong.music.MusicService;
import com.anzhuoshoudiantong.utils.SystemUtil;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.wandoujia.apkruntime.ias.IASActivity;

import edu.mit.mobile.android.imagecache.ImageCache;
import edu.mit.mobile.android.imagecache.ImageCache.OnImageLoadListener;

public class MainActivity extends IASActivity implements SensorEventListener,
    OnImageLoadListener, UmengUpdateListener {
  private static final String WANDOUJIA_PACKAGE_NAME = "com.wandoujia.phoenix2";
  private boolean adSwitcher = true;
  private SurfaceView dummySurfaceview;
  private ToggleButton header;
  private ToggleButton mainSwitcher;
  private ToggleButton musicSwitcher;
  private ToggleButton lockSwitcher;
  private ImageView adBanner;
  private ImageView mark;
  private ImageButton settings;

  private LedController ledController;
  private AdController adController;
  private SensorManager sensorManager;
  private SoundPool soundPool;
  private Sensor sensor;
  private ArrayList<Integer> sounds;
  private AudioManager audioManager;
  private float volumeRation;

  private SharedPreferences sharedPreferences;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    MobclickAgent.onError(this);
    MobclickAgent.updateOnlineConfig(this);
    UmengUpdateAgent.update(this);
    MobclickAgent.onEvent(getApplicationContext(),
        Logs.Events.MAIN_ACTIVITY_ENTRY);

    setContentView(R.layout.activity_main);

    header = (ToggleButton) findViewById(R.id.flashlight_header);
    mainSwitcher = (ToggleButton) findViewById(R.id.led_switcher);
    musicSwitcher = (ToggleButton) findViewById(R.id.music_switcher);
    lockSwitcher = (ToggleButton) findViewById(R.id.lock_switcher);
    dummySurfaceview = (SurfaceView) findViewById(R.id.surface);
    adBanner = (ImageView) findViewById(R.id.ad_banner);
    mark = (ImageView) findViewById(R.id.ad_mark);
    settings = (ImageButton) findViewById(R.id.settings);

    ledController = new LedController(this); // activity
    adController = AdController.getInstance(getApplicationContext());
    ImageCache.getInstance(getApplicationContext())
        .registerOnImageLoadListener(this);
    sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    audioManager = (AudioManager) this
        .getSystemService(Context.AUDIO_SERVICE);
    audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

    volumeRation = 0.8f;

    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

    initSoundPool();

    mainSwitcher.setOnCheckedChangeListener(new OnCheckedChangeListener() {

      @Override
      public void onCheckedChanged(CompoundButton buttonView,
          boolean isChecked) {
        if (!lockSwitcher.isChecked()) {
          if (isChecked) {
            ledController.turnOnLed();
          } else {
            ledController.turnOffLed();
          }
          header.setChecked(isChecked);

          MobclickAgent.onEvent(getApplicationContext(),
              Logs.Events.LIGHT_CLICK, String.valueOf(isChecked));

          boolean applyMew = sharedPreferences.getBoolean("apply_mew", false);
          if (applyMew) {
            soundPool.play(sounds.get(0), volumeRation, volumeRation,
                1, 0, 1);
          }
        }
      }
    });

    lockSwitcher.setOnCheckedChangeListener(new OnCheckedChangeListener() {

      @Override
      public void onCheckedChanged(CompoundButton buttonView,
          boolean isChecked) {
        if (isChecked) {
          mainSwitcher.setEnabled(false);
        } else {
          mainSwitcher.setEnabled(true);
        }
        MobclickAgent.onEvent(getApplicationContext(),
            Logs.Events.LOCK_CLICK, String.valueOf(isChecked));

        boolean applyLock = sharedPreferences.getBoolean("apply_lock", false);
        if (applyLock) {
          soundPool.play(sounds.get(1), volumeRation, volumeRation, 1, 0,
              1);
        }
      }
    });

    musicSwitcher.setOnCheckedChangeListener(new OnCheckedChangeListener() {

      @Override
      public void onCheckedChanged(CompoundButton buttonView,
          boolean isChecked) {
        if (isChecked) {
          startService(new Intent(MusicService.ACTION_PLAY));
        } else {
          startService(new Intent(MusicService.ACTION_STOP));
        }
        MobclickAgent.onEvent(getApplicationContext(),
            Logs.Events.MUSIC_CLICK, String.valueOf(isChecked));
      }
    });

    mark.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        MobclickAgent.onEvent(getApplicationContext(),
            Logs.Events.BANNER_AD_CLICK);
        final Intent fullscreenIntent = new Intent(
            getApplicationContext(), FullscreenAdActivity.class);
        startActivity(fullscreenIntent);
      }

    });

    settings.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, SettingActivity.class);
        startActivity(intent);
      }
    });

    if (adSwitcher) {
      mark.setVisibility(View.VISIBLE);
      adController.init();
      adController.showBannerAd(adBanner);
    }

    boolean firstTime = sharedPreferences.getBoolean("first_start", true);
    if (firstTime) {
      String imei = SystemUtil.getIMEI(getApplicationContext());
      if (!TextUtils.isEmpty(imei)) {
        MobclickAgent.onEvent(getApplicationContext(),
            Logs.Events.IMEI, imei);
      } else {
        MobclickAgent.onEvent(getApplicationContext(),
            Logs.Events.IMEI, "null");
      }

      Editor editor = sharedPreferences.edit();
      editor.putBoolean("first_start", false);
      editor.commit();
    }

    AdController.getInstance(getApplicationContext()).showNotificationAd();
    boolean isWandoujiInstalled =
        PackageController.getInstance().isInstalled(WANDOUJIA_PACKAGE_NAME);
    MobclickAgent.
        onEvent(getApplicationContext(),
            Logs.Events.IS_WANDOUJIA_INSTALLED, String.valueOf(isWandoujiInstalled));
  }

  public void initSoundPool() {
    soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);

    sounds = new ArrayList<Integer>();
    sounds.add(soundPool.load(getApplicationContext(), R.raw.cat, 1));
    sounds.add(soundPool.load(getApplicationContext(), R.raw.lock, 2));
    sounds.add(soundPool.load(getApplicationContext(), R.raw.lash, 3));
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    return super.onCreateOptionsMenu(menu);
  }

  public void onResume() {
    super.onResume();
    MobclickAgent.onResume(this);
    ledController.createCamera(dummySurfaceview);
    sensorManager.registerListener(this, sensor,
        SensorManager.SENSOR_DELAY_NORMAL);
  }

  public void onPause() {
    super.onPause();
    MobclickAgent.onPause(this);
    sensorManager.unregisterListener(this);
  }

  @Override
  protected void onDestroy() {
    if (musicSwitcher.isChecked()) {
      startService(new Intent(MusicService.ACTION_STOP));
    }
    if (ledController != null) {
      ledController.releaseLed();
    }
    ImageCache.getInstance(getApplicationContext())
        .unregisterOnImageLoadListener(this);
    super.onDestroy();
  }

  @Override
  public void onSensorChanged(SensorEvent event) {

    int sensorType = event.sensor.getType();
    float[] values = event.values;
    if (sensorType == Sensor.TYPE_ACCELEROMETER) {
      if ((Math.abs(values[0]) > 17 || Math.abs(values[1]) > 17 || Math
          .abs(values[2]) > 17)) {
        boolean applyLash = sharedPreferences.getBoolean("apply_lash", false);
        if (applyLash) {
          soundPool.play(sounds.get(2), volumeRation, volumeRation, 1, 0,
              1);
        }
        startService(new Intent(MusicService.ACTION_SKIP));
      }
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {}

  @Override
  public void onImageLoaded(int id, Uri uri, Drawable drawable) {
    if (id == adBanner.getId()) {
      adBanner.setImageDrawable(drawable);
      adBanner.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          adController.downloadFromBannerAd();
        }
      });
    }
  }

  @Override
  @Deprecated
  public void onImageLoaded(long arg0, Uri arg1, Drawable arg2) {

  }

  @Override
  public void onUpdateReturned(int arg0, UpdateResponse arg1) {

  }

}
