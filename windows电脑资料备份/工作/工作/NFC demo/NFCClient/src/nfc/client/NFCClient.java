package nfc.client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.telephony.TelephonyManager;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//import com.google.common.base.Preconditions;

public class NFCClient extends Activity {
    /** Called when the activity is first created. */
	
	private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private NdefMessage[] msgs;
    
	private NdefMessage mMessage = null;
	
	private final int PAYMENT_SUCCESS = 100;
	private final int READ_SUCCESS = 101;
	private final int READ_START = 102;
	private final int READ_ERROR = 103;
	
	private final int SETTING_ID = 200;
	private final int QUIT_ID = 201;
	
	private final int SetIntent = 300;
	
	private String return_result = "";
	private String userbalance ="";
	private TextView Balance;
	
	private ProgressDialog readdata_dialog = null;
	private String account;
	private String serverip;
	private String username;
	
	private String HTTP_HEAD = "https://";
	private String WebService = "/NFC/Service.asmx/";
	private String hint_balance = "Account's Balance";
	private String serveraddress = "https://10.86.8.234/NFC/Service.asmx/";
	//private String httpsserveraddress = "https://10.86.8.234/NFCS/Service.asmx/";
	//private String serveraddress = "http://112.125.58.71:8080/NFC/Service.asmx/";
	
	private String Hint = "The NFC adapter is disabled.\nWould you like to go to he preferences screen to turn it on?";
    
	private String imei;
	private String mac;
	
	private String cardnumber;
	
	private static boolean pay = false;
	
	private String password;
	
	private String setmoney;
	
	
	public static native String CreateRespone(byte[] imei,byte[] mac,byte[] challenge);
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainclient);
        
        try
        {
        	Balance = (TextView)findViewById(R.id.balance);
       
	        imei = getImei();
			mac = getLocalMacAddress();
	        
			SharedPreferences preferences = getSharedPreferences("NFC",0);
	        
	        cardnumber = preferences.getString("cardnumber", null);
	        setmoney = preferences.getString("money", "200");
			
			
			if(!IsconnectNetwork())
			{
				Toast.makeText(this, "Please connect the network!", Toast.LENGTH_LONG).show();
			}
			
	        init();
	         
	        if(cardnumber.equals(null))
	        {
	        	Toast.makeText(this, "Read bank card information error!", Toast.LENGTH_LONG).show();
	        	return;
	        }
	        //Balance.setText("100");
	        
	        mAdapter = NfcAdapter.getDefaultAdapter(this);
	        
	        if(!mAdapter.isEnabled())
			{
				new AlertDialog.Builder(NFCClient.this).setTitle("NFC Eroor").setMessage(Hint).setPositiveButton("Yes",new DialogInterface.OnClickListener(){
	
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						Intent set = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
						startActivity(set);
					}}).setNegativeButton("No", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							finish();		
						}
					}).show();
			}
	        
	        mPendingIntent = PendingIntent.getActivity(this, 0,
	                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
	
	        IntentFilter tagdiscover = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
	        IntentFilter ndefdiscover = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
	        	     
	        mFilters = new IntentFilter[] {
	        		tagdiscover,
	        		ndefdiscover
	        };
        }
        catch(Exception e)
        {
        	
        }
        //mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, null);
    }
    
    private void init()
    {
    	 read_SharedPreferences();
         
    	 if(readdata_dialog != null)
    		 readdata_dialog.show();
    	 else
    	 {
    		 readdata_dialog = ProgressDialog.show(NFCClient.this, "Loading","Connect to the Server", true);
    	 }
         
         new Thread()
         {
         	public void run()
         	{
         		String response = AuthPhone();
         		if(Readbalance(response))
         		{
         			Message msg = mHandle.obtainMessage(READ_SUCCESS);
         			mHandle.sendMessage(msg);     			
         		}
         		else
         		{
         			Message msg = mHandle.obtainMessage(READ_ERROR);
         			mHandle.sendMessage(msg);
         		}
         	}
         }.start();
    }
    
    
    
    private void read_SharedPreferences()
	{
		SharedPreferences preferences = getSharedPreferences("NFC",0);
		serverip = preferences.getString("serverip",getString(R.string.server));
		username = preferences.getString("cardnumber", getString(R.string.account));
		
		serveraddress = HTTP_HEAD + serverip + WebService;
	}
    
    @Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		
		String action = intent.getAction();
		//Receive Confirm information
		if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action))
		{
			Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			if (rawMsgs != null) {
				msgs = new NdefMessage[rawMsgs.length];
				for (int i = 0; i < rawMsgs.length; i++) {
					msgs[i] = (NdefMessage) rawMsgs[i];
				}
				NdefRecord[] records = msgs[0].getRecords();
				if(records != null)
				{
					return_result = parse(records[0]);
					Message msg = mHandle.obtainMessage(PAYMENT_SUCCESS);
					mHandle.sendMessage(msg);			
				}
			}
		}
		else
		{
			Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			if (rawMsgs != null) {
				msgs = new NdefMessage[rawMsgs.length];
				for (int i = 0; i < rawMsgs.length; i++) {
					msgs[i] = (NdefMessage) rawMsgs[i];
				}
				NdefRecord[] records = msgs[0].getRecords();
				if(records != null)
				{
					return_result = parse(records[0]);
					account = parse(records[1]);
					Message msg = mHandle.obtainMessage(PAYMENT_SUCCESS);
					mHandle.sendMessage(msg);			
				}
			}
		}
	}
	
  //Create New NdefRecord
	private  NdefRecord newTextRecord(String text, Locale locale, boolean encodeInUtf8) {
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));

        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
        byte[] textBytes = text.getBytes(utfEncoding);

        int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        char status = (char) (utfBit + langBytes.length);

        byte[] data = new byte[1 + langBytes.length + textBytes.length]; 
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);

        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
    }
    
	private String parse(NdefRecord record) {
	//	Preconditions
	//			.checkArgument(record.getTnf() == NdefRecord.TNF_WELL_KNOWN);
	//	Preconditions.checkArgument(Arrays.equals(record.getType(),
	//			NdefRecord.RTD_TEXT));
		try {
			byte[] payload = record.getPayload();
			/*
			 * payload[0] contains the "Status Byte Encodings" field, per the
			 * NFC Forum "Text Record Type Definition" section 3.2.1.
			 * 
			 * bit7 is the Text Encoding Field.
			 * 
			 * if (Bit_7 == 0): The text is encoded in UTF-8 if (Bit_7 == 1):
			 * The text is encoded in UTF16
			 * 
			 * Bit_6 is reserved for future use and must be set to zero.
			 * 
			 * Bits 5 to 0 are the length of the IANA language code.
			 */
			String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8"
					: "UTF-16";
			int languageCodeLength = payload[0] & 0077;
			//String languageCode = new String(payload, 1, languageCodeLength,"US-ASCII");
			String text = new String(payload, languageCodeLength + 1,
					payload.length - languageCodeLength - 1, textEncoding);
			return text;
			//return new TextRecord(languageCode, text);
		} catch (UnsupportedEncodingException e) {
			// should never happen unless we get a malformed tag.
			throw new IllegalArgumentException(e);
		}
	}
	
	 private boolean WriteSql(String response)
	 {
	    	try
	    	{
	    		final String SERVER_URL = serveraddress + "Writebalance";
	    		HttpPost request = new HttpPost(SERVER_URL);
	    		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
	    		//这里需要写参数名称
	    		//Register(String imei,String ip, String location_x, String location_y, String contact)
	    		params.add(new BasicNameValuePair("username", username));
	    		params.add(new BasicNameValuePair("account", account));
	    		params.add(new BasicNameValuePair("custom", return_result));
	    		params.add(new BasicNameValuePair("response", response));
	    		request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
	    		HttpResponse httpResponse = getNewHttpClient().execute(request);
	    		String result = "";
	    		if (httpResponse.getStatusLine().getStatusCode() != 404) 
	    		{
	    			result = EntityUtils.toString(httpResponse.getEntity());
	    			if(ParseXml(result).equals("false"))
	    			{
	    				return false;
	    			}
	    			return true;
	    		}
	    		return false;
	    	}
	    	catch(Exception e)
	    	{
	    		return false;
	    	}
	 }
	    //从webservice读取用户的余额
	    private boolean Readbalance(String response)
	    {
	    	try
	    	{
	    		final String SERVER_URL = serveraddress + "Readbalance";
	    		HttpPost request = new HttpPost(SERVER_URL);
	    		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
	    		//这里需要写参数名称
	    		//Register(String imei,String ip, String location_x, String location_y, String contact)
	    		params.add(new BasicNameValuePair("username", username));
	    		params.add(new BasicNameValuePair("response", response));
	    		request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
	    		HttpResponse httpResponse = getNewHttpClient().execute(request);
	    		String result = "";
	    		if (httpResponse.getStatusLine().getStatusCode() != 404) 
	    		{
	    			result = EntityUtils.toString(httpResponse.getEntity());
	    			userbalance = ParseXml(result);
	    			if(userbalance.equals("false"))
	    			{
	    				return false;	
	    			}
	    			return true;
	    		}
	    		return false;
	    	}
	    	catch(Exception e)
	    	{
	    		return false;
	    	}
	    }
	    //解析返回的结果
	    public String ParseXml(String result)
	    {
	    	try
	    	{
	    		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    		DocumentBuilder builder = factory.newDocumentBuilder();
	    		InputStream is = new ByteArrayInputStream(result.getBytes("UTF-8"));
	    		Document dom = (Document) builder.parse(is);
	    		String res = dom.getDocumentElement().getChildNodes().item(0).getNodeValue();
	    		return res;
	    		//System.out.print(res);
	    	}
	    	catch(Exception e)
	    	{
	    		return "false";
	    	}

	    }
	
	private Handler mHandle = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
			case PAYMENT_SUCCESS:
				try
				{
					/*
					if(!AuthPhone())
					{
						Toast.makeText(NFCClient.this,"Error! The phone is illegal", Toast.LENGTH_SHORT).show();
						return;
					}
					*/
						
					int custom = Integer.parseInt(return_result);
					int set_moeny = Integer.parseInt(setmoney);
					if(custom >= set_moeny && !pay)
					{
						final EditText pwd = new EditText(NFCClient.this);
						pwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
						AlertDialog.Builder builder = new AlertDialog.Builder(NFCClient.this);
						                        builder.setTitle("Input Password")
						                        .setIcon(android.R.drawable.ic_dialog_info).setView(pwd
						                                        ).setPositiveButton("OK", new DialogInterface
								                                        .OnClickListener() {
					                                                
					                                                @Override
					                                                public void onClick(DialogInterface dialog, int which) {
					                                                        // TODO Auto-generated method stub
					                                                	//SharedPreferences preferences = getSharedPreferences("NFC",0);
					                                                	//password = preferences.getString("password","0");
					                                                	//if(password.equals(pwd.getText().toString()))
					                                                	//{
					                                                		
					                                                	//}
					                                                		if(AuthPassword(pwd.getText().toString().trim()))
					                                                		{
					                                                			pay = true;
					               	
					                                                		}
					                                                		else
					                                                		{
					                                                			Toast.makeText(NFCClient.this, "Error! Please input the right password", Toast.LENGTH_LONG).show();
					                                                			pay = false;
					                                                		}
					                                                	//Toast.makeText(NFCClient.this, "Press OK", Toast.LENGTH_LONG).show();
					                                                }
					                                        })
						                                        .setNegativeButton("Cancel", new DialogInterface
								                                        .OnClickListener() {
					                                                
					                                                @Override
					                                                public void onClick(DialogInterface dialog, int which) {
					                                                        // TODO Auto-generated method stub
					                                                		pay = false;
					                                                		Toast.makeText(NFCClient.this, "You cancel the payment", Toast.LENGTH_LONG).show();
					                                                }
					                                        }).show();
					                       
				}
				else
					pay = true;
				if(!pay)
					return;
				int client_balance = Integer.parseInt(Balance.getText().toString());
				if(client_balance < custom)
				{
				//	exchange = "Pay Failure";
					Toast.makeText(NFCClient.this, "Pay Failure, You don't have enough balance", Toast.LENGTH_LONG).show();
				}
				else
				{
					int bal = client_balance - custom;
					Balance.setText(bal + "");				
					Toast.makeText(NFCClient.this, "You have paied " + return_result, Toast.LENGTH_LONG).show();
				}
				String response = AuthPhone();
				
				if(response.equals("False"))
				{
					Toast.makeText(NFCClient.this,"Connect to server error!", Toast.LENGTH_SHORT).show();
					return;
				}
				if(!WriteSql(response))
				{
					//exchange = "Pay error";
				}
				
				//由于必须重新靠近后才能读取出内容，所以去除该函数
			//	mMessage = new NdefMessage(
		      //          new NdefRecord[] { newTextRecord(exchange, Locale.ENGLISH, true)});  
			//	mAdapter.enableForegroundNdefPush(NFCClient.this, mMessage);
				//mAdapter.disableForegroundNdefPush(NFCClient.this);
				pay = false;
				}
				catch(Exception e)
				{
					pay = false;
				}
				break;
			case READ_SUCCESS:
				if(readdata_dialog != null)
					readdata_dialog.dismiss();
				//readdata_dialog.cancel();
				Balance.setText(userbalance);
				break;
			case READ_START:
				//readdata_dialog = ProgressDialog.show(NFCClient.this, "Loading","Connect to the Server", true);
				//readdata_dialog.show();
				break;
			case READ_ERROR:
				if(readdata_dialog != null)
					readdata_dialog.dismiss();
				new AlertDialog.Builder(NFCClient.this).setTitle("Error").setMessage("Illegal User!").setPositiveButton("OK",new DialogInterface.OnClickListener() 
	        	{
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						//finish();
					}
				}).show();
				break;
			}
		}
	};
	
	
	 @Override
	 public void onResume() {
	        super.onResume();
	        //init();
	        if (mAdapter != null && mMessage != null) 
	        {
	        	mAdapter.enableForegroundNdefPush(this, mMessage);
	        	
	        }
	        if (mAdapter != null)
	        	mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, null);
	    }

	 @Override
	 public void onPause() {
	        super.onPause();
	        if (mAdapter != null && mMessage != null)
	        	{mAdapter.disableForegroundNdefPush(this);
	        
	        	 
	        	}
	        if (mAdapter != null)
	        mAdapter.disableForegroundDispatch(this);	
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
			Intent intent = new Intent(NFCClient.this,AccountSetting.class);
			//we can catch the activity when it's invoke finish()
			startActivityForResult(intent,SetIntent);
			break;
		case QUIT_ID:
			finish();
			break;
		}	
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		switch(requestCode)
		{
		case SetIntent:
			init();
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	 
	private String getChallenge()
	{
		try
    	{
    		final String SERVER_URL = serveraddress + "GetChallenge";
    		HttpPost request = new HttpPost(SERVER_URL);
    		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
    		//这里需要写参数名称
    		//Register(String imei,String ip, String location_x, String location_y, String contact)
    		params.add(new BasicNameValuePair("bankcard", cardnumber));
    		request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
    		HttpResponse httpResponse = getNewHttpClient().execute(request);
    		String result = "";
    		if (httpResponse.getStatusLine().getStatusCode() != 404) 
    		{
    			result = EntityUtils.toString(httpResponse.getEntity());
    			String challenge = ParseXml(result);
    			return challenge;
    		}
    		return "False";
    	}
    	catch(Exception e)
    	{
    		return "False";
    	}
	}
	
	private boolean AuthPassword(String pwd)
	{
		try
    	{
    		//final String SERVER_URL = httpsserveraddress + "AuthPassword";
			final String SERVER_URL = serveraddress + "AuthPassword";
    		HttpPost request = new HttpPost(SERVER_URL);
    		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
    		//这里需要写参数名称
    		//Register(String imei,String ip, String location_x, String location_y, String contact)
    		params.add(new BasicNameValuePair("bankcard", cardnumber));
    		params.add(new BasicNameValuePair("pwd", pwd));
    		request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
    		HttpResponse httpResponse = getNewHttpClient().execute(request);
    		String result = "";
    		if (httpResponse.getStatusLine().getStatusCode() != 404) 
    		{
    			result = EntityUtils.toString(httpResponse.getEntity());
    			String challenge = ParseXml(result);
    			if(challenge.equals("OK"))
    				return true;
    			else
    				return false;
    		}
    		return false;
    	}
    	catch(Exception e)
    	{
    		return false;
    	}
	}
	
	/**
	 *返回支持HTTPS的客户端
	 * @return
	 */
	public HttpClient getNewHttpClient(){

	    try{

	        KeyStore trustStore =KeyStore.getInstance(KeyStore.getDefaultType());

	        trustStore.load(null,null);

	        SSLSocketFactory sf =new MySSLSocketFactory(trustStore);

	        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

	        HttpParams params=new BasicHttpParams();

	        HttpProtocolParams.setVersion(params,HttpVersion.HTTP_1_1);

	        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

	        SchemeRegistry registry =new SchemeRegistry();

	        registry.register(new Scheme("http",PlainSocketFactory.getSocketFactory(),80));

	        registry.register(new Scheme("https", sf,443));

	        ClientConnectionManager ccm =new ThreadSafeClientConnManager(params, registry);

	        return new DefaultHttpClient(ccm,params);

	    }catch(Exception e){

	        return new DefaultHttpClient();

	    }

	}
	
	private boolean getResponseResult(String response)
	{
		try
    	{
    		final String SERVER_URL = serveraddress + "AuthResponse";
    		HttpPost request = new HttpPost(SERVER_URL);
    		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
    		//这里需要写参数名称
    		//Register(String imei,String ip, String location_x, String location_y, String contact)
    		params.add(new BasicNameValuePair("bankcard", cardnumber));
    		params.add(new BasicNameValuePair("response", response));
    		request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
    		HttpResponse httpResponse = getNewHttpClient().execute(request);
    		String result = "";
    		if (httpResponse.getStatusLine().getStatusCode() != 404) 
    		{
    			result = EntityUtils.toString(httpResponse.getEntity());
    			String challenge = ParseXml(result);
    			if(challenge.equals("OK"))
    				return true;
    			return false;
    		}
    		return false;
    	}
    	catch(Exception e)
    	{
    		return false;
    	}
	}
	
	private String AuthPhone()
	{
		String challenge = getChallenge();
		if(challenge.equals("False"))
			return "False";
		
		 return CreateRespone(imei.getBytes(),mac.getBytes(),challenge.getBytes());
		//return getResponseResult(response);
	}
	
	private String getImei()
	{
		TelephonyManager telephonyManager = (TelephonyManager) this
		.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getDeviceId();
	}
	
	private String getLocalMacAddress() {  
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);  
        WifiInfo info = wifi.getConnectionInfo();  
        return info.getMacAddress();  
    }  
	
	 /**
	 * 判断手机是否联网
	 * @return
	 */
	private boolean IsconnectNetwork()
	{
		ConnectivityManager cManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE); 
		NetworkInfo info = cManager.getActiveNetworkInfo(); 
		  if (info != null && info.isAvailable()){ 
		       //do something 
		       //能联网 
		        return true; 
		  }else{ 
		       //do something 
		       //不能联网 
		        return false; 
		  } 
	}
	
	static
    {
    	System.loadLibrary("Payment");
    }
	
}