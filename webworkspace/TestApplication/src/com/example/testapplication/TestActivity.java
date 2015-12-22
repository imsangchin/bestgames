package com.example.testapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class TestActivity extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		
		Button button = (Button) findViewById(R.id.test2);
		button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(TestActivity.this,TestActivity3.class);
				startActivity(intent);	
				
			}
		});
		
		Log.d("test", "TestActivity");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d("test", "onResume_TestActivity");
	}
	
	@Override
	protected void onDestroy () {
		super.onDestroy();
		Log.d("test", "onDestroy_TestActivity");
	}

//	@Override
//	protected void onNewIntent (Intent intent) {
//		setIntent(intent);
//	}

}
