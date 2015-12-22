package com.anzhuoshoudiantong;

import com.wandoujia.apkruntime.ias.IASPreferenceActivity;

import android.os.Bundle;
import android.preference.PreferenceActivity;


public class SettingActivity extends IASPreferenceActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.cat_pref);
  }
}
