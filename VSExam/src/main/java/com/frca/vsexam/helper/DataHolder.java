package com.frca.vsexam.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.SparseArray;

import com.frca.vsexam.entities.calendar_exam.EventExamSet;
import com.frca.vsexam.network.NetworkInterface;
import com.frca.vsexam.network.tasks.UserImageNetworkTask;

public class DataHolder {

    private static DataHolder sInstance;

    private final SharedPreferences mPreferences;

    private final Configuration mConfiguration;

    private final NetworkInterface mNetworkInterface;

    private final EventExamSet mEventExamSet;

    private final SparseArray<Bitmap> mBitmapContainer = new SparseArray<Bitmap>();

    private final SparseArray<UserImageNetworkTask> mDownloadTaskContainer = new SparseArray<UserImageNetworkTask>();

    private final SparseArray<RegisteringService> mRegisteringServiceContainer = new SparseArray<RegisteringService>();

    public static DataHolder getInstance(Context context) {
        if (sInstance == null)
            sInstance = new DataHolder(context);

        return sInstance;
    }

    private DataHolder(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mConfiguration = context.getResources().getConfiguration();
        mNetworkInterface = new NetworkInterface();
        mEventExamSet = EventExamSet.load(mPreferences);
    }

    public SharedPreferences getPreferences() {
        return mPreferences;
    }

    public Configuration getConfiguration() {
        return mConfiguration;
    }

    public NetworkInterface getNetworkInterface() {
        return mNetworkInterface;
    }

    public EventExamSet getEventExamSet() {
        return mEventExamSet;
    }

    public SparseArray<Bitmap> getBitmapContainer() {
        return mBitmapContainer;
    }

    public SparseArray<UserImageNetworkTask> getDownloadTaskContainer() {
        return mDownloadTaskContainer;
    }

    public SparseArray<RegisteringService> getRegisteringServiceContainer() {
        return mRegisteringServiceContainer;
    }
}
