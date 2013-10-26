package com.frca.vsexam.network.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;

import com.frca.vsexam.network.Response;

public class UserImageNetworkTask extends ImageNetworkTask {

    private final int userId;

    public UserImageNetworkTask(Context context, int userId, View imageView) {
        super(context, getLogoUrl(userId), imageView);
        this. userId = userId;
    }

    public static String getLogoUrl(int userId) {
        return "lide/foto.pl?id=" + String.valueOf(userId);
    }

    protected void onPreExecute() {
        SparseArray<Bitmap> bitmapContainer = dataHolder.getBitmapContainer();
        Bitmap bitmap = bitmapContainer.get(userId);

        if (bitmap != null) {
            finishOnViews(bitmap);
            cancel(true);
            return;
        } else {
            SparseArray<UserImageNetworkTask> taskContainer = dataHolder.getDownloadTaskContainer();
            synchronized (taskContainer) {
                ImageNetworkTask task = taskContainer.get(userId);
                if (task != null) {
                    for (ImageView imageView : imageViews)
                        task.addImageView(imageView);
                    cancel(true);
                    return;
                } else {
                    taskContainer.put(userId, this);
                    super.onPreExecute();
                }
            }
        }
    }

    @Override
    protected void onFinish(Response response) {
        SparseArray<Bitmap> bitmapContainer = dataHolder.getBitmapContainer();
        if (bitmapContainer.get(userId) == null)
            bitmapContainer.put(userId, response.getBitmap());

        super.onFinish(response);
    }
}

