package com.frca.vsexam.helper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.frca.vsexam.MainActivity;
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
    private MainActivity.Data data;

    public ImageDownloaderTask(ImageView imageView, MainActivity.Data data) {
        this.imageView = imageView;
        this.data = data;
    }

    public ImageDownloaderTask(Callback callback, MainActivity.Data data) {
        this.callback = callback;
        this.data = data;
    }

    protected Bitmap doInBackground(String... urls) {
        return get(urls[0]);
    }

    protected Bitmap get(String url) {

        url = HttpRequestBuilder.completeURLString(url);
        Bitmap mIcon11 = null;
        try {
            HttpResponse httpResponse = new HttpRequestBuilder(data, url).build().execute();
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
            Log.e("dl done", "dl");
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
}

