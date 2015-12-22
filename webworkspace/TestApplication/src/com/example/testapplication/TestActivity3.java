package com.example.testapplication;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class TestActivity3 extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		
		Log.d("test", "TestActivity3");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d("test", "onResume_TestActivity3");
	}
	
	@Override
	protected void onDestroy () {
		super.onDestroy();
		Log.d("test", "onDestroy_TestActivity3");
	}

//	@Override
//	protected void onNewIntent (Intent intent) {
//		setIntent(intent);
//	}

}

