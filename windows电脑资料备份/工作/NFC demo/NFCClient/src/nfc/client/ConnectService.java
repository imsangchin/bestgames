package nfc.client;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

public class ConnectService extends Service
{
	private String serverip;
	
	private String HTTP_HEAD = "http://";
	private String WebService = "/NFC/Service.asmx/";
	private String serveraddress = "http://10.255.254.239/NFC/Service.asmx/";

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		
		new Thread()
		{
			public void run()
			{
				try
				{
					while(true)
					{
						read_SharedPreferences();
						final String SERVER_URL = serveraddress + "HelloWorld";
						HttpPost request = new HttpPost(SERVER_URL);
						request.setEntity(null);
						HttpResponse httpResponse = new DefaultHttpClient().execute(request);
						String result = "";
						if (httpResponse.getStatusLine().getStatusCode() != 404) 
						{
							result = EntityUtils.toString(httpResponse.getEntity());
							Log.v("Test", result);
						}
						
						Thread.sleep(60000);
					}
				}
				catch(Exception e)
				{
					
				}
			}
		}.start();
	}
	
	private void read_SharedPreferences()
	{
		SharedPreferences preferences = getSharedPreferences("NFC",0);
		serverip = preferences.getString("serverip",getString(R.string.server));
		
		serveraddress = HTTP_HEAD + serverip + WebService;
	}
	
}