package com.frca.vsexam.network;

import android.os.AsyncTask;

public class NetworkTask extends AsyncTask<HttpRequestBuilder, Void, Response> {
    private final ResponseCallback callback;
    public NetworkTask(ResponseCallback callback) {
        this.callback = callback;
    }
    protected Response doInBackground(HttpRequestBuilder... builders) {
        HttpRequestBuilder builder = builders[0];
        return builder.execute(Response.Type.TEXT);
    }

    protected void onPostExecute(Response result) {
        if (result != null)
            callback.call(result);
    }

    public static interface ResponseCallback {
        void call(Response httpString);
    }
}
