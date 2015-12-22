package nfc.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import nfc.ndef.NFCOperator;

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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Register extends Activity
{
	private Button Confirm;
	private Spinner cardtype;
	private EditText cardnumber;
	private EditText password;
	private EditText edverification;
	private TextView verification;
	
	private ProgressDialog register;
	private String card_no="";
	private String card_type="";
	//private String veri_input="";
	
	private String imei;
	private String mac;
	
	private String filename ="";
	
	private String serveraddress = "https://10.86.8.234/NFC/Service.asmx/";
	
	//private String serveraddress = "http://112.125.58.71:8080/NFC/Service.asmx/";
	
	private final int REGISTER_SUCCESS = 100;
	private final int REGISTER_ERROR = 101;
	
	private final int SETTING_ID = 200;
	private final int QUIT_ID = 201;
	
	private String serverIp = "10.86.8.234";

	private String[] card_kind =
	{
			"Bank of China",
			"Bank of Communications",
			"Bank of Agriculture"
	};
	
	 private X509TrustManager xtm = new X509TrustManager() {
	        public void checkClientTrusted(X509Certificate[] chain, String authType) {}

	            public void checkServerTrusted(X509Certificate[] chain, String authType) {
	               System.out.println("cert: " + chain[0].toString() + ", authType: " + authType);
	            }

	            public X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	    }; 
	    private HostnameVerifier hnv = new HostnameVerifier() {
	        public boolean verify(String hostname, SSLSession session) {
	         System.out.println("hostname: " + hostname);
	            return true;
	        }
	    }; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.register);
		
		find_widget();
		card_no = card_kind[0];
		
		final ArrayAdapter<String> Spinneradapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_dropdown_item,
				card_kind);
		cardtype.setAdapter(Spinneradapter);
		
		cardtype.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					final int position, long id) {
				card_type = card_kind[position];
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		verification.setText(createRandom());
		
		imei = getImei();
		mac = getLocalMacAddress();
		
		SharedPreferences preferences = getSharedPreferences("NFC",0);
		boolean register = preferences.getBoolean("Register", false);
		serverIp = preferences.getString("serverip", serverIp);
		
		if(!register)
		{
			Toast.makeText(this, "Please input your bank card information", Toast.LENGTH_LONG).show();
		}
		else
		{
			Intent intent = new Intent(this,NFCOperator.class);
			startActivity(intent);
			finish();
		}
		
		if(!IsconnectNetwork())
		{
			Toast.makeText(this, "Please connect the network!", Toast.LENGTH_LONG).show();
		}
		
		
		SSLContext sslContext = null;

        try {
            sslContext = SSLContext.getInstance("TLS");
            X509TrustManager[] xtmArray = new X509TrustManager[] { xtm };
            sslContext.init(null, xtmArray, new java.security.SecureRandom());
        } catch(GeneralSecurityException gse) {
        }
        if(sslContext != null) {
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        }

       
        HttpsURLConnection.setDefaultHostnameVerifier(hnv);
	
	}
	
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
	
	private boolean RegisterSoftware()
    {
    	try
    	{
    		final String SERVER_URL = serveraddress + "Register";
    		HttpPost request = new HttpPost(SERVER_URL);
    		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
    		card_no = cardnumber.getText().toString();
    		//card_type = cardtype.getText().toString();
    		params.add(new BasicNameValuePair("cardbank", card_type));
    		params.add(new BasicNameValuePair("cardnumber", card_no));
    		params.add(new BasicNameValuePair("password", password.getText().toString()));
    		params.add(new BasicNameValuePair("imei", imei));
    		params.add(new BasicNameValuePair("mac", mac));
    		request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
    		
    		HttpResponse httpResponse = getNewHttpClient().execute(request);
    		String result = "";
    		if (httpResponse.getStatusLine().getStatusCode() != 404) 
    		{
    			result = EntityUtils.toString(httpResponse.getEntity());
    			if(!ParseXml(result).contains("Payment"))
    			{
    				return false;
    			}
    			
    			String temp = ParseXml(result);
    			
    			if(temp.contains("#"))
    			{
    				filename = temp.substring(0,temp.indexOf('#'));
    				card_no = temp.substring(temp.indexOf('#') + 1);
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
	
	 //è§£æè¿åçç»æ
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
	
	private void find_widget()
	{
		cardtype = (Spinner)findViewById(R.id.myspinner);
		cardnumber = (EditText)findViewById(R.id.cardnumber);
		password =(EditText)findViewById(R.id.password);
		edverification = (EditText)findViewById(R.id.edverfication);
		
		verification = (TextView)findViewById(R.id.verification);
		
		Confirm = (Button)findViewById(R.id.confirm);
		
		Confirm.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				serveraddress = "https://" + serverIp + "/NFC/Service.asmx/";
				if(!edverification.getText().toString().equals(verification.getText().toString()))
				{
					Toast.makeText(Register.this, "Verification Code error!", Toast.LENGTH_LONG).show();
					verification.setText(createRandom());
					return;
				}
				
				register = ProgressDialog.show(Register.this, "Register",
						"Please wait for a moment..", true);
				new Thread()
				{
					public void run()
					{
						if(RegisterSoftware())
						{						
							downFile("http://" + serverIp + "/payment/" + filename,"/sdcard/" + filename);
							//downFile("http://112.125.58.71:8080/payment/" + filename,"/sdcard/" + filename);
							Message msg = mHandler.obtainMessage(REGISTER_SUCCESS);
							mHandler.sendMessage(msg);
						}
						else
						{
							Message msg = mHandler.obtainMessage(REGISTER_ERROR);
							mHandler.sendMessage(msg);
						}
						
					}
				}.start();
			}
		});
	}
	
	private Handler mHandler = new Handler()
	{

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			
			switch(msg.what)
			{
				case REGISTER_SUCCESS:
					register.dismiss();
					Toast.makeText(Register.this, "Register Success!", Toast.LENGTH_LONG).show();
					SharedPreferences preferences = getSharedPreferences("NFC",0);
					preferences.edit().putBoolean("Register", true).commit();
					preferences.edit().putString("cardnumber", card_no).commit();
					preferences.edit().putString("password", password.getText().toString()).commit();
					Uri uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/" + filename)); //è¿éæ¯APKè·¯å¾  
					Intent intent = new Intent(Intent.ACTION_VIEW);  
					intent.setDataAndType(uri,"application/vnd.android.package-archive");  
					startActivity(intent);  
					finish();
					break;
				case REGISTER_ERROR:
					register.dismiss();
					Toast.makeText(Register.this, "Register Failure! Card number has existed, please input right number", Toast.LENGTH_LONG).show();
					break;
			}
			
			super.handleMessage(msg);
		}
		
	};

	/**
	 * 
	 * @return
	 */
	private String createRandom()
	{
		Random r = new Random(System.currentTimeMillis());
		//r.
		int result = (r.nextInt(10000))% 9000 + 1000;
		return result + "";
	}
	
    public void downFile(String url,String filePath) { 
    	try
    	{
	    	//url="http://10.255.254.239/NFCClient.apk"; 
			//filePath="/sdcard/NFCClient.apk"; 
			URL Url = new URL(url); 
			URLConnection conn = Url.openConnection(); 
			conn.connect(); 
			InputStream is = conn.getInputStream(); 
			int fileSize = conn.getContentLength();//  
			if (fileSize <= 0) { // 
				throw new RuntimeException(""); 
			} 
			if (is == null) { // 
				//sendMsg(Down_ERROR); 
				throw new RuntimeException(""); 
			} 
			FileOutputStream FOS = new FileOutputStream(filePath); // 
			byte buf[] = new byte[1024]; 
			int downLoadFilePosition = 0; 
			int numread; 
			while ((numread = is.read(buf)) != -1) { 
				FOS.write(buf, 0, numread); 
				downLoadFilePosition += numread; 
			} 
			is.close(); 
    	}
    	catch(Exception e)
    	{
    		Log.d("NFCPayment", e.getMessage());
    	}
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
			Intent intent = new Intent(Register.this,AccountSetting.class);
			//we can catch the activity when it's invoke finish()
			startActivityForResult(intent,10);
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
			case 10:
				SharedPreferences preferences = getSharedPreferences("NFC",0);
				serverIp = preferences.getString("serverip", serverIp);
				break;
		}
	}
}