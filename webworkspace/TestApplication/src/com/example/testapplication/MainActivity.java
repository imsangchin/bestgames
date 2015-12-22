package com.example.testapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d("test", "onCreate_MainActivity");
		
		Button button = (Button) findViewById(R.id.test1);
		button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this,TestActivity.class);
				startActivity(intent);	
				
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d("test", "onResume_MainActivity");
	}
	
	@Override
	protected void onNewIntent (Intent intent) {
		Log.d("test", "onNew_MainActivity");
//	setIntent(intent);
	}
	
	@Override
	protected void onDestroy () {
		super.onDestroy();
		Log.d("test", "onDestroy_MainActivity");
	}

}
