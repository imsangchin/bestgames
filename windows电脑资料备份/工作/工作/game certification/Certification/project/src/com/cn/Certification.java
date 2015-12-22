package com.cn;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Certification extends Activity {
    /** Called when the activity is first created. */
public native String HandleFunction(byte[] iemi,byte[] pdata,int len,byte[] key,byte[] keyiv);
private TextView OutText;
private Button ConfirmButton;
private EditText InputEdit;
private String Input_str;
private int input_len;
private byte[] Key= new byte[]{1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,1};
private byte[] Keyiv= new byte[]{0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5};
private String ismi;
TelephonyManager telephonyManager;
    @Override
   
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        telephonyManager =(TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        ismi = telephonyManager.getSubscriberId();
        OutText = (TextView)findViewById(R.id.TextView03);
        ConfirmButton= (Button)findViewById(R.id.Button01);
        InputEdit = (EditText)findViewById(R.id.EditText01);
        ConfirmButton.setOnClickListener(new Button.OnClickListener()
        {
        	@Override
        	public void onClick(View v)
        	{
        		int i,temp;
        		String tempstr;
        		Input_str = InputEdit.getText().toString();
                input_len = Input_str.length();
                if(input_len<15)
                {
                	for(i=0;i<15-input_len;i++)
                	{
                		temp = i%10;
                		Input_str = Input_str+temp;
                	}
                }
                if(input_len>15)
                {
                	tempstr = Input_str.substring(0,15);
                	Input_str = tempstr;
                }
                String result =  HandleFunction(ismi.getBytes(),Input_str.getBytes(),15,Key,Keyiv);
                OutText.setText(result);
        	}
        }
        );
    }
    


    static
    {
    	System.loadLibrary("Certification-second");
    }
}