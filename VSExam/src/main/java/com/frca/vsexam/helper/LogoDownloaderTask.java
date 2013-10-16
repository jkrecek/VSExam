package com.frca.vsexam.helper;

import android.graphics.Bitmap;
import android.util.SparseArray;
import android.widget.ImageView;

import com.frca.vsexam.MainActivity;

/**
 * Created by KillerFrca on 15.10.13.
 */
public class LogoDownloaderTask extends ImageDownloaderTask {

    private int userId;

    private SparseArray<Bitmap> bitmapSparseArray;

    public LogoDownloaderTask(int userId, SparseArray<Bitmap> bitmapSparseArray, MainActivity.Data data, ImageView imageView) {
        super(imageView, data);

        this.userId = userId;
        this.bitmapSparseArray = bitmapSparseArray;
    }

    public LogoDownloaderTask(int userId, SparseArray<Bitmap> bitmapSparseArray, MainActivity.Data data, Callback callback) {
        super(callback, data);

        this.userId = userId;
        this.bitmapSparseArray = bitmapSparseArray;
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
