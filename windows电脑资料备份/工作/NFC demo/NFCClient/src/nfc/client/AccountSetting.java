package nfc.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AccountSetting extends Activity
{
	private EditText serverIp;
	//private EditText account;
	private Button bset_ok;
	private EditText cusmer;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set);
		
		serverIp = (EditText)findViewById(R.id.serverip);
		cusmer= (EditText)findViewById(R.id.money);
		//account = (EditText)findViewById(R.id.account);
		
		read_SharedPreferences();
		
		bset_ok = (Button)findViewById(R.id.bset_ok);
		bset_ok.setOnClickListener(Setting_OK);
	}
	
	private View.OnClickListener Setting_OK = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			write_to_SharedPreferences();
			new AlertDialog.Builder(AccountSetting.this).setTitle("Setting Success").setMessage("You have set account and server address successful!").setPositiveButton("Yes",new DialogInterface.OnClickListener()
			{

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					finish();
				}
				
			}).show();
		}
	};
	
	private void read_SharedPreferences()
	{
		SharedPreferences preferences = getSharedPreferences("NFC",0);
		String ip = preferences.getString("serverip","");
		String money = preferences.getString("money","200");
		//String account_bank = preferences.getString("cardnumber", "");
		if(!ip.equals(""))
		{
			serverIp.setText(ip);
		}
		if(!money.equals(""))
		{
			cusmer.setText(money);
		}
		/*
		if(!account_bank.equals(""))
		{
		//	account.setText(account_bank);
		}*/	
	}
	
	private void write_to_SharedPreferences()
	{
		String ip = serverIp.getText().toString();
		String money = cusmer.getText().toString();
	//	String shop_account = account.getText().toString();
		
		SharedPreferences preferences = getSharedPreferences("NFC",0);
		
		preferences.edit().putString("serverip", ip).commit();
		preferences.edit().putString("money", money).commit();
		
		//preferences.edit().putString("cardnumber", shop_account).commit();
	}
}