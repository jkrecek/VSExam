package com.frca.vsexam.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.View;

/**
 * Created by KillerFrca on 15.10.13.
 */
@Deprecated
public class LogoDownloaderTask extends ImageDownloaderTask {

    private int userId;

    private SparseArray<Bitmap> bitmapSparseArray;

    public LogoDownloaderTask(Context context, int userId, View imageView) {
        super(context, imageView);

        this.userId = userId;
        this.bitmapSparseArray = DataHolder.getInstance(context).getBitmapContainer();
    }

    public LogoDownloaderTask(Context context, int userId, Callback callback) {
        super(context, callback);

        this.userId = userId;
        this.bitmapSparseArray = DataHolder.getInstance(context).getBitmapContainer();
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        Bitmap bitmap = bitmapSparseArray.get(userId);
        if (bitmap != null) {
            return bitmap;
        } else {
            return get("lide/foto.pl?id=" + String.valueOf(userId));
        }
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        if (bitmapSparseArray.get(userId) == null)
            bitmapSparseArray.put(userId, result);

        super.onPostExecute(result);
    }
}
