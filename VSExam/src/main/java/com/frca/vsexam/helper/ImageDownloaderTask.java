package com.frca.vsexam.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.frca.vsexam.R;
import com.frca.vsexam.network.HttpRequestBuilder;
import com.frca.vsexam.network.Response;

public class ImageDownloaderTask extends AsyncTask<String, Void, Response> {
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

    protected Response doInBackground(String... urls) {
        Response bitmap = get(urls[0]);
        return bitmap;
    }

    protected Response get(String url) {

        url = HttpRequestBuilder.completeURLString(url);
        Response response = null;
        try {
            HttpRequestBuilder httpResponse = new HttpRequestBuilder(context, url).build();
            response = httpResponse.execute(Response.Type.BITMAP);
        } catch (Exception e) {
            Log.e("Error", e.getClass().getName());
            e.printStackTrace();
        }


        return response;
    }

    protected void onPostExecute(Response result) {
        if (result == null)
            return;

        setImage(result.getBitmap());

        if (callback != null)
            callback.call(result);
    }

    private void setImage(Bitmap bitmap) {
        if (imageView != null) {
            imageView.setImageBitmap(bitmap);

            View parent = (View)imageView.getParent();
            if (parent != null) {
                View hide = parent.findViewById(R.id.hide);
                if (hide != null)
                    hide.setVisibility(View.GONE);
            }
        }
    }

    public static interface Callback {
        void call(Response result);
    }

    public static String getLogoUrl(int userId) {
        return "lide/foto.pl?id=" + String.valueOf(userId);
    }

    public void loadLogo(final int userId) {
        final DataHolder dateHolder = DataHolder.getInstance(context);
        Bitmap bitmap = dateHolder.getBitmapContainer().get(userId);
        if (bitmap != null) {
            setImage(bitmap);
        } else {
            callback = new Callback() {
                @Override
                public void call(Response result) {
                    if (dateHolder.getBitmapContainer().get(userId) == null)
                        dateHolder.getBitmapContainer().put(userId, result.getBitmap());
                }
            };

            execute(getLogoUrl(userId));
        }
    }
}

