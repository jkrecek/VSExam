package com.frca.vsexam.network;

import android.content.Context;
import android.os.AsyncTask;

import com.frca.vsexam.helper.DataHolder;

import org.apache.http.client.methods.HttpRequestBase;

public class NetworkTask extends AsyncTask<HttpRequestBase, Void, Response> {
    private final ResponseCallback callback;
    private final NetworkInterface netInt;

    public NetworkTask(Context context, ResponseCallback callback) {
        this.callback = callback;
        this.netInt = DataHolder.getInstance(context).getNetworkInterface();
    }

    protected Response doInBackground(HttpRequestBase... request) {
        HttpRequestBase onlyRequest = request[0];
        return netInt.execute(onlyRequest, Response.Type.TEXT);
    }

    protected void onPostExecute(Response result) {
        if (result != null)
            callback.call(result);
    }

    public static interface ResponseCallback {
        abstract void call(Response httpString);
    }
}
