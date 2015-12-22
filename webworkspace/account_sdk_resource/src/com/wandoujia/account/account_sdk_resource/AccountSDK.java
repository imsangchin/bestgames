package com.wandoujia.account.account_sdk_resource;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class AccountSDK extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.accountsdk);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.account_sdk, menu);
		return true;
	}

}
