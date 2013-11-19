package com.frca.vsexam.network.tasks;

import android.os.AsyncTask;

import com.frca.vsexam.helper.DataHolder;
import com.frca.vsexam.network.HttpRequestBuilder;
import com.frca.vsexam.network.Response;

import org.apache.http.client.methods.HttpRequestBase;

import java.net.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by KillerFrca on 26.10.13.
 */
public abstract class BaseNetworkTask extends AsyncTask<Void, Void, Response> {
    protected final DataHolder dataHolder;
    protected final Response.Type responseType;

    protected ResponseCallback responseCallback;
    protected HttpURLConnection request;

    public BaseNetworkTask(DataHolder dataHolder, String url, ResponseCallback responseCallback, ExceptionCallback exceptionCallback, Response.Type responseType) {
        this.responseCallback = responseCallback;
        this.dataHolder = dataHolder;
        this.responseType = responseType;

        try {
            this.request = new HttpRequestBuilder(dataHolder, url).build();
        } catch (Exception e) {
            exceptionCallback.onException(e);
            cancel(true);
        }
    }

    public BaseNetworkTask(DataHolder dataHolder, HttpURLConnection request, ResponseCallback responseCallback, Response.Type responseType) {
        this(dataHolder, responseCallback, responseType);
        this.request = request;
    }

    public BaseNetworkTask(DataHolder dataHolder, ResponseCallback responseCallback, Response.Type responseType) {
        this(dataHolder, responseType);
        this.responseCallback = responseCallback;
    }

    public BaseNetworkTask(DataHolder dataHolder, Response.Type responseType) {
        this.dataHolder = dataHolder;
        this.responseType = responseType;
    }

    protected Response doInBackground(Void... arg) {
        return dataHolder.getNetworkInterface().execute(request, responseType);
    }

    final protected void onPostExecute(Response result) {
        if (isCancelled())
            return;

        onFinish(result);

        if (result != null && responseCallback != null)
            responseCallback.onSuccess(result);
    }

    protected void onFinish(Response result) {

    }

    public static void run(BaseNetworkTask task) {
        task.execute();
    }

    public static interface ResponseCallback {
        abstract void onSuccess(Response response);
    }

    public static interface ExceptionCallback {
        abstract void onException(Exception e);
    }
}
