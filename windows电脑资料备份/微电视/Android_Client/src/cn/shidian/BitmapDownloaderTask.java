package cn.shidian;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> { 
    private String mImageUrl;
    private ImageView mImageView;

    public BitmapDownloaderTask(String imageUrl, ImageView imageView) { 
       mImageUrl = imageUrl;
       mImageView = imageView;
    } 

    @Override 
    protected Bitmap doInBackground(String... params) { 
        return downloadBitmap(mImageUrl); 
    } 

    @Override 
    protected void onPostExecute(Bitmap bitmap) { 
        if (mImageView != null) { 
            mImageView.setImageBitmap(bitmap);                   
        }
    }

    Bitmap downloadBitmap(String image_url) {
        Bitmap bitmap = null;

        try {
            URL url = new URL(image_url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.connect(); 
            InputStream inputStream=conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        } catch (Exception e) {
            if (e != null) {
                e.printStackTrace();
            }
        }
        
        return bitmap;
    }
} 
