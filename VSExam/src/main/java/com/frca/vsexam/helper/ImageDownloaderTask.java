package com.frca.vsexam.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.frca.vsexam.R;
import com.frca.vsexam.network.HttpRequestBuilder;

import org.apache.http.HttpResponse;

import java.io.InputStream;

/**
 * Created by KillerFrca on 15.10.13.
 */
public class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {
    private ImageView imageView;
    private Callback callback;
    private Context context;

    public ImageDownloaderTask(Context context, View imageView) {
        if (imageView instanceof ImageView)
            this.imageView = (ImageView) imageView;
        else
            this.imageView = (ImageView)imageView.findViewById(R.id.image);
        this.context = context;
    }

    public ImageDownloaderTask(Context context, Callback callback) {
        this.callback = callback;
        this.context = context;
    }

    protected Bitmap doInBackground(String... urls) {
        return get(urls[0]);
    }

    protected Bitmap get(String url) {

        url = HttpRequestBuilder.completeURLString(url);
        Bitmap mIcon11 = null;
        try {
            HttpResponse httpResponse = new HttpRequestBuilder(context, url).build().execute();
            InputStream in = httpResponse.getEntity().getContent();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }


        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        if (imageView != null) {
            imageView.setImageBitmap(result);

            View parent = (View)imageView.getParent();
            if (parent != null) {
                View hide = parent.findViewById(R.id.hide);
                if (hide != null)
                    hide.setVisibility(View.GONE);
            }
        }

        if (callback != null)
            callback.call(result);
    }

    public static interface Callback {
        void call(Bitmap result);
    }

    public static String getLogoUrl(int userId) {
        return "lide/foto.pl?id=" + String.valueOf(userId);
    }

    public void loadLogo(final int userId) {
        final DataHolder dateHolder = DataHolder.getInstance(context);
        Bitmap bitmap = dateHolder.getBitmapContainer().get(userId);
        if (bitmap != null) {
            onPostExecute(bitmap);
        } else {
            callback = new Callback() {
                @Override
                public void call(Bitmap result) {
                    if (dateHolder.getBitmapContainer().get(userId) == null)
                        dateHolder.getBitmapContainer().put(userId, result);
                }
            };

            execute(getLogoUrl(userId));
        }
    }
}

