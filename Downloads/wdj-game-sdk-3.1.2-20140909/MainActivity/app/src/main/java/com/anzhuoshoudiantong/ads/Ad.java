package com.anzhuoshoudiantong.ads;

import java.io.IOException;
import java.io.Serializable;

import com.anzhuoshoudiantong.Const;

import edu.mit.mobile.android.imagecache.ImageCache;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.ImageView;

public class Ad implements Serializable {
    public String imageUrl;
    public String switcher;
    public String content;

    public Ad(String imageUrl, String switcher, String content) {
        super();
        this.imageUrl = imageUrl;
        this.switcher = switcher;
        this.content = content;
    }

    public void show(Context context, ImageView imageView) {
        if (TextUtils.isEmpty(switcher) || switcher.equals(Const.OFF)) {
            return;
        }
        if (!TextUtils.isEmpty(imageUrl)) {
            Uri adUri = Uri.parse(imageUrl);
            if (adUri != null) {
                try {
                    Drawable drawable = ImageCache.getInstance(context)
                            .loadImage(imageView.getId(), adUri, 800, 800);
                    if (drawable != null) {
                        imageView.setImageDrawable(drawable);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void dismiss() {}
}
