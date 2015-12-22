
package com.miui.securitycenter.manualitem;

import android.content.Context;
import miui.os.Build;
import android.os.SystemProperties;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class WebApiAccessHelper {

    private static final String TAG = "WebApiAccessHelper";

    private static WebApiAccessHelper INST;

    private static final String URL_SERVER_ROOT = "https://api.sec.miui.com";
    //private static final String URL_SERVER_ROOT = "https://10.237.80.19:8082";
    //private static final String URL_SERVER_ROOT = "https://10.99.168.118:8087"; //线上测试环境
    private static String URL_SECURITYCENTER_EXAMINATION = URL_SERVER_ROOT + "/health/optimization";

    private static final String JSON_KEY_APP_DEIVCE = "device";
    private static final String JSON_KEY_APP_CARRIER = "carrier";
    private static final String JSON_KEY_APP_REGION = "region";
    private static final String JSON_KEY_APP_MIUIVERSION = "miuiVersion";
    private static final String JSON_KEY_APP_APPVERSION = "appVersion";
    private static final String JSON_KEY_APP_VERSIONTYPE = "versionType";
    private static final String JSON_KEY_APP_IMEI = "imei";
    private static final String JSON_KEY_APP_MAC = "mac";

    private static final String JSON_KEY_APP_DATAVERSION = "dataVersion";
    private static final String JSON_KEY_APP_ISDIFF = "isDiff";

    private static final String DEVICE = Build.DEVICE;
    private static final String REGION = SystemProperties.get("ro.product.locale.region");
    private static final String MIUI_VERSION = Build.VERSION.INCREMENTAL;
    private static final String APP_VERSION = "1.0.140709";
    private static String CARRIER = SystemProperties.get("ro.carrier");
    private static String VERSION_TYPE;
    private static String IMEI = "null";
    private static String MAC = "null";

    private Context mContext;

    public static WebApiAccessHelper getInstance(Context context) {
        if (INST == null) {
            INST = new WebApiAccessHelper(context.getApplicationContext());
        }
        return INST;
    }

    public WebApiAccessHelper(Context context) {
        mContext = context;
    }

    private void initDeviceDetails() {
//        Delete due to Privacy
//        IMEI = ((TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE))
//                .getDeviceId();

        CARRIER = (CARRIER == null) ? "null" : CARRIER;

        if (Build.IS_STABLE_VERSION) {
            VERSION_TYPE = "stable";
        }
        else if (Build.IS_DEVELOPMENT_VERSION) {
            VERSION_TYPE = "development";
        }
        else {
            VERSION_TYPE = "alpha";
        }

    }

    private List<NameValuePair> getBaseParams() {
        initDeviceDetails();
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(JSON_KEY_APP_DEIVCE, DEVICE));
        params.add(new BasicNameValuePair(JSON_KEY_APP_CARRIER, CARRIER));
        params.add(new BasicNameValuePair(JSON_KEY_APP_REGION, REGION));
        params.add(new BasicNameValuePair(JSON_KEY_APP_MIUIVERSION, MIUI_VERSION));
        params.add(new BasicNameValuePair(JSON_KEY_APP_VERSIONTYPE, VERSION_TYPE));
        params.add(new BasicNameValuePair(JSON_KEY_APP_APPVERSION, APP_VERSION));
        params.add(new BasicNameValuePair(JSON_KEY_APP_IMEI, IMEI));
        params.add(new BasicNameValuePair(JSON_KEY_APP_MAC, MAC));
        return params;
    }

    private static String accessInternet(String param, String apiPath) {
        return accessInternet(param, apiPath, null);
    }

    private static String accessInternet(String param, String apiPath, List<NameValuePair> basic) {
        assert param != null && apiPath != null;
        String result = "";
        if (basic == null) {
            basic = new ArrayList<NameValuePair>();
        }
        HttpPost httpPost = new HttpPost(apiPath);
        basic.add(new BasicNameValuePair("param", Base64.encodeToString(param.getBytes(),
                Base64.DEFAULT)));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(basic, "UTF-8"));
            HttpResponse httpResponse;
            try {
                httpResponse = new DefaultHttpClient().execute(httpPost);
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    result = EntityUtils.toString(httpResponse.getEntity());
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                result = e.getMessage();
            } catch (IOException e) {
                // TODO: handle exception
                e.printStackTrace();
                result = e.getMessage();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            result = e.getMessage();
        }
        return result;
    }

    public ExaminationResult dataConnection(long dataVersion, boolean isDiff) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(JSON_KEY_APP_DATAVERSION, dataVersion);
            jsonObject.put(JSON_KEY_APP_ISDIFF, isDiff);

        } catch (Exception e) {
            e.printStackTrace();
        }
        String result = accessInternet(jsonObject.toString(), URL_SECURITYCENTER_EXAMINATION,
                getBaseParams());
        return new ExaminationResult(result);
    }
}
