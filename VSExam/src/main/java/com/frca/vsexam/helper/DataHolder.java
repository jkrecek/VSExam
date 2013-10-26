package com.frca.vsexam.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.util.SparseArray;

import com.frca.vsexam.R;
import com.frca.vsexam.network.ImageDownloaderTask;
import com.frca.vsexam.network.NetworkInterface;

public class DataHolder {

    private static DataHolder instance;

    private final SharedPreferences preferences;

    private final Configuration configuration;

    private final SparseArray<Bitmap> bitmapContainer = new SparseArray<Bitmap>();

    private final SparseArray<ImageDownloaderTask> downloadTaskContainer = new SparseArray<ImageDownloaderTask>();

    private final NetworkInterface networkInterface;

    private DataHolder(Context context) {
        preferences = context.getSharedPreferences(context.getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
        configuration = context.getResources().getConfiguration();
        networkInterface = new NetworkInterface();
    }

    public static DataHolder getInstance(Context context) {
        if (instance == null)
            instance = new DataHolder(context);

        return instance;
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public SparseArray<Bitmap> getBitmapContainer() {
        return bitmapContainer;
    }

    public SparseArray<ImageDownloaderTask> getDownloadTaskContainer() {
        return downloadTaskContainer;
    }

    public NetworkInterface getNetworkInterface() {
        return networkInterface;
    }
}
