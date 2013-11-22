package com.frca.vsexam.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.util.SparseArray;

import com.frca.vsexam.R;
import com.frca.vsexam.network.NetworkInterface;
import com.frca.vsexam.network.tasks.UserImageNetworkTask;

public class DataHolder {

    private static DataHolder instance;

    private final SharedPreferences preferences;

    private final Configuration configuration;

    private final SparseArray<Bitmap> bitmapContainer = new SparseArray<Bitmap>();

    private final SparseArray<UserImageNetworkTask> downloadTaskContainer = new SparseArray<UserImageNetworkTask>();

    private final NetworkInterface networkInterface;

    private final SparseArray<RegisteringService> registeringServiceContainer = new SparseArray<RegisteringService>();

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

    public SparseArray<UserImageNetworkTask> getDownloadTaskContainer() {
        return downloadTaskContainer;
    }

    public NetworkInterface getNetworkInterface() {
        return networkInterface;
    }

    public SparseArray<RegisteringService> getRegisteringServiceContainer() {
        return registeringServiceContainer;
    }
}
