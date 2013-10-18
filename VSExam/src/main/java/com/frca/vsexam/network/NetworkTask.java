package com.frca.vsexam.network;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;

/**
 * Created by KillerFrca on 14.10.13.
 */
public class NetworkTask extends AsyncTask<HttpRequestBuilder, Void, Response> {
    private final ResponseCallback callback;
    public NetworkTask(ResponseCallback callback) {
        this.callback = callback;
    }
    protected Response doInBackground(HttpRequestBuilder... builders) {
        HttpResponse response = builders[0].execute();
        return new Response(response);
    }

    protected void onPostExecute(Response result) {
        callback.call(result);
    }

    public static interface ResponseCallback {
        void call(Response httpString);
    }
}
