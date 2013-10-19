package com.frca.vsexam.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;

import com.frca.vsexam.R;
import com.frca.vsexam.helper.DataHolder;

import java.util.ArrayList;
import java.util.List;

public class ImageDownloaderTask extends AsyncTask<String, Void, Response> {
    private List<ImageView> imageViews = new ArrayList<ImageView>();
    private Callback callback;
    private Context context;


    public ImageDownloaderTask(Context context, View imageView) {
        imageViews.add(getProperImage(imageView));
        this.context = context;
    }

    public ImageDownloaderTask(Context context, Callback callback) {
        this.callback = callback;
        this.context = context;
    }

    private static ImageView getProperImage(View childOrParent) {
        if (childOrParent instanceof ImageView)
            return (ImageView) childOrParent;
        else
            return (ImageView)childOrParent.findViewById(R.id.image);
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
        setImageViews(result.getBitmap());

        if (callback != null && result != null)
            callback.call(result);
    }

    private void setImageViews(Bitmap bitmap) {
        if (!imageViews.isEmpty()) {
            for (ImageView view : imageViews) {
                setImage(view, bitmap);
            }
        }
    }

    private static void setImage(ImageView imageView, Bitmap bitmap) {
        if (bitmap == null)
            imageView.setImageResource(android.R.drawable.ic_delete);
        else
            imageView.setImageBitmap(bitmap);

        View parent = (View)imageView.getParent();
        if (parent != null) {
            View hide = parent.findViewById(R.id.hide);
            if (hide != null)
                hide.setVisibility(View.GONE);
        }
    }

    public static interface Callback {
        void call(Response result);
    }

    public static String getLogoUrl(int userId) {
        return "lide/foto.pl?id=" + String.valueOf(userId);
    }

    private void addImageView(ImageView view) {
        if (!imageViews.contains(view))
            imageViews.add(view);
    }

    public static void startUserAvatarTask(Context context, View view, final int userId) {
        DataHolder dataHolder = DataHolder.getInstance(context);
        final SparseArray<Bitmap> bitmapContainer = dataHolder.getBitmapContainer();
        Bitmap bitmap = bitmapContainer.get(userId);

        ImageView properImageView = getProperImage(view);
        if (bitmap != null)
            setImage(properImageView, bitmap);
        else {
            final SparseArray<ImageDownloaderTask> taskContainer = dataHolder.getDownloadTaskContainer();
            synchronized (taskContainer) {
                ImageDownloaderTask task = taskContainer.get(userId);
                if (task != null) {
                    task.addImageView(properImageView);
                } else {
                    task = new ImageDownloaderTask(context, properImageView);
                    taskContainer.put(userId, task);

                    task.callback = new Callback() {
                        @Override
                        public void call(Response result) {
                            if (bitmapContainer.get(userId) == null)
                                bitmapContainer.put(userId, result.getBitmap());
                        }
                    };

                    task.execute(getLogoUrl(userId));
                }
            }
        }
    }
}

