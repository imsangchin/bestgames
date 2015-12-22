package nfc.ndef;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import nfc.client.MySSLSocketFactory;
import nfc.client.NFCClient;
import nfc.client.R;

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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class Payment extends Activity
{
	private EditText priceText;
	private Button BOK;
	private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    
	private NdefMessage mMessage = null;
	private String Hint = "The NFC adapter is disabled.\nWould you like to go to he preferences screen to turn it on?";
	private String return_result = null;
	
	private final int PAYMENT_STATUS = 100;
	private final int ERROR_STATUS = 101;
	private final int PAYMENT_END = 102;
	
	private NdefMessage[] msgs = null;
	
	private int balance;
	
	private int custommoney;
	
	private String userbalance;
	private String customerbalance;
	private String username;
	
	private String imei,mac;
	
	private String serveraddress = "https://10.86.8.234/NFC/Service.asmx/";
	//private String serveraddress = "http://112.125.58.71:8080/NFC/Service.asmx/";
	private Thread checkthread;
	Timer mTimer = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.payment);
		
		BOK = (Button)findViewById(R.id.OK);
		priceText = (EditText)findViewById(R.id.price);
		
		SharedPreferences preferences = getSharedPreferences("NFC",0);
	
		username = preferences.getString("cardnumber", getString(R.string.account));
		
		imei = getImei();
		mac = getLocalMacAddress();
		
		String response = AuthPhone();
		
		if(!Readbalance(response))
		{
			Toast.makeText(this, "Connection Failed", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		else
		{
			balance = Integer.parseInt(userbalance);
			customerbalance = userbalance;
			Toast.makeText(this, "Your balance is " + userbalance, Toast.LENGTH_LONG).show();
		}
		
		
		
		mAdapter = NfcAdapter.getDefaultAdapter(this);
		
		if(!mAdapter.isEnabled())
		{
			new AlertDialog.Builder(Payment.this).setTitle("NFC Eroor").setMessage(Hint).setPositiveButton("Yes",new DialogInterface.OnClickListener(){

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
		
		BOK.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				SharedPreferences settings = getSharedPreferences("NFC",0);
				String shop_account = settings.getString("cardnumber",getString(R.string.account));

				mMessage = new NdefMessage(
		                new NdefRecord[] { newTextRecord(priceText.getText().toString(), Locale.ENGLISH, true),
		                				   newTextRecord(shop_account,Locale.ENGLISH,true)});  
				
				mAdapter.enableForegroundDispatch(Payment.this, mPendingIntent, mFilters, null);
				Message msg = mHandle.obtainMessage(PAYMENT_STATUS);
				mHandle.sendMessage(msg);
				

				 //if(checkthread.)
				checkthread =  new Thread()
				 {
					 public void run()
					 {
						 while(true)
						 {
							 try
							 {
							 Thread.sleep(1000);
							// String response = AuthPhone();
							 
							 String result = Read(customerbalance);
							 if(!result.equals("false"))
							 {
								int temp = Integer.parseInt(result.trim());
								if(temp >= 0)
								{
									balance = temp;
								
									custommoney = balance;
									customerbalance = balance + Integer.parseInt(customerbalance.trim()) + "";
									Message msg = mHandle.obtainMessage(PAYMENT_END);
									mHandle.sendMessage(msg);
								}
							}
						}
						 catch(Exception e)
						 {
							 Log.d("Text", e.getMessage());
						 }
					 }
					 }
				 };
				 
				 checkthread.start();
			}
		});
			
		if(mAdapter == null)
		{
			new AlertDialog.Builder(Payment.this).setTitle("NFC Eroor").setMessage(Hint).setPositiveButton("Yes",new DialogInterface.OnClickListener(){

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
	    		 ndefdiscover,
	    		 tagdiscover
	     };
	}
	
	 //从webservice读取用户的余额
    private String Read(String balance)
    {
    	try
    	{
    		final String SERVER_URL = serveraddress + "Read";
    		HttpPost request = new HttpPost(SERVER_URL);
    		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
    		//这里需要写参数名称
    		//Register(String imei,String ip, String location_x, String location_y, String contact)
    		params.add(new BasicNameValuePair("username", username));
    		params.add(new BasicNameValuePair("balance", balance));
    		request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
    		HttpResponse httpResponse = getNewHttpClient().execute(request);
    		String result = "";
    		if (httpResponse.getStatusLine().getStatusCode() != 404) 
    		{
    			result = EntityUtils.toString(httpResponse.getEntity());
    			String temp = ParseXml(result);
    			Log.d("Balance", userbalance);
    			
    			if(temp.equals("false"))
    			{
    				return "false";	
    			}
    			
    			return temp;
    		}
    		return "false";
    	}
    	catch(Exception e)
    	{
    		return "false";
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
    			Log.d("Balance", userbalance);
    			
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
	
	 @Override
	    public void onResume() {
	        super.onResume();
	        if (mAdapter != null && mMessage != null) 
	        	mAdapter.enableForegroundNdefPush(this, mMessage);
	        mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, null);
	    }

	    @Override
	    public void onPause() {
	        super.onPause();
	        if (mAdapter != null && mMessage != null) mAdapter.disableForegroundNdefPush(this);
	        
	        mAdapter.disableForegroundDispatch(this);
	        try
	        {
	        	checkthread.stop();
	        }
	        catch(Exception e)
	        {
	        	
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
	
	private Handler mHandle = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
			case PAYMENT_STATUS:
				if(priceText.getText().toString().isEmpty())
				{
					Toast.makeText(Payment.this, "Please input price", Toast.LENGTH_LONG).show();
					return;
				}
				Toast.makeText(Payment.this, "Payment Status", Toast.LENGTH_SHORT).show();
				//push information to another nfc device
				if (mAdapter != null && mMessage != null) 
		        	mAdapter.enableForegroundNdefPush(Payment.this, mMessage);
				 //mAdapter.enableForegroundDispatch(Payment.this, mPendingIntent, mFilters, null);
				//mAdapter.disableForegroundDispatch(Payment.this);
				break;
			case ERROR_STATUS:
				Toast.makeText(Payment.this, "Error", Toast.LENGTH_SHORT).show();
				break;
			case PAYMENT_END:
				Toast.makeText(Payment.this, "You have received " + custommoney, Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};
	
	private void receiveNdef(Intent intent)
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
				Message msg = mHandle.obtainMessage(PAYMENT_END);
				mHandle.sendMessage(msg);			
			}
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		
		String action = intent.getAction();
		//Receive Confirm information
		if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action))
		{
			receiveNdef(intent);
		}
		else if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(action))
		{
			receiveNdef(intent);
		}
	}
	
	private String parse(NdefRecord record) {
		//Preconditions
		//		.checkArgument(record.getTnf() == NdefRecord.TNF_WELL_KNOWN);
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
	
	private String AuthPhone()
	{
		String challenge = getChallenge();
		if(challenge.equals("False"))
			return "False";
		
		 return NFCClient.CreateRespone(imei.getBytes(),mac.getBytes(),challenge.getBytes());
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
	
	private String getChallenge()
	{
		try
    	{
    		final String SERVER_URL = serveraddress + "GetChallenge";
    		HttpPost request = new HttpPost(SERVER_URL);
    		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
    		//这里需要写参数名称
    		//Register(String imei,String ip, String location_x, String location_y, String contact)
    		params.add(new BasicNameValuePair("bankcard", username));
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
	
	
}