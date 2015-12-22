package com.example.testssl;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class MainActivity extends Activity {

  private Button clickButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    clickButton = (Button) findViewById(R.id.click);
    clickButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        new Thread() {
          @Override
          public void run() {
            try {

              HttpGet request =
                  new HttpGet(
                      "https://account.wandoujia.com/api/external/validate?uid=14861053&token=Kx%2BIb3sEN%2BEjyxfHr%2BSpb%2BUSJ35%2Fpk48TBBf3zmK5GM%3D");
              HttpClient client = new DefaultHttpClient();
              HttpResponse response;
              try {
                response = client.execute(request);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                  String content = EntityUtils.toString(response.getEntity(),
                      "UTF-8");
                  MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                      Toast.makeText(MainActivity.this, "true", Toast.LENGTH_SHORT).show();
                    }
                  });
                }
              } catch (ClientProtocolException e) {
                final String msg = e.getMessage();
                MainActivity.this.runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                  }
                });
                e.printStackTrace();
              } catch (IOException e) {
                e.printStackTrace();
                final String msg = e.getMessage();
                MainActivity.this.runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                  }
                });
              } catch (Exception e) {
                e.printStackTrace();
                final String msg = e.getMessage();
                MainActivity.this.runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                  }
                });
              }

            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }.start();
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

}
