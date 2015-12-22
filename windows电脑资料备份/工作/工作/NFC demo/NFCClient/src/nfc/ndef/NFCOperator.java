package nfc.ndef;

import nfc.client.AccountSetting;
import nfc.client.NFCClient;
import nfc.client.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

public class NFCOperator extends Activity {
    /** Called when the activity is first created. */
	private ImageButton paymentButton;
	private ImageButton posButton;
	private final int SETTING_ID = 200;
	private final int QUIT_ID = 201;

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        paymentButton = (ImageButton)findViewById(R.id.payment);
        posButton = (ImageButton)findViewById(R.id.pos);
        
        paymentButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(NFCOperator.this,NFCClient.class);
				startActivity(intent);
			}
		});
        
        posButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(NFCOperator.this,Payment.class);
				startActivity(intent);
			}
		});
 
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, SETTING_ID, 0, "Settings").setIcon(android.R.drawable.ic_menu_set_as);
		menu.add(0, QUIT_ID, 1, "Quit").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId())
		{
		case SETTING_ID:
			Intent intent = new Intent(NFCOperator.this,AccountSetting.class);
			//we can catch the activity when it's invoke finish()
			startActivity(intent);
			break;
		case QUIT_ID:
			finish();
			break;
		}	
		return super.onOptionsItemSelected(item);
	}
    
}