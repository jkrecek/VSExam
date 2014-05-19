package com.frca.vsexam.network.tasks;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.frca.vsexam.helper.DataHolder;
import com.frca.vsexam.network.HttpRequestBuilder;
import com.frca.vsexam.network.Response;

import org.apache.http.client.methods.HttpRequestBase;

import java.util.concurrent.Executor;

public abstract class BaseNetworkTask extends AsyncTask<Void, Void, Response> {

    public enum Result {
        ERROR,
        CANCELED,
        SUCCESS
    }

    protected final DataHolder dataHolder;
    protected final Response.Type responseType;

    protected ResponseCallback responseCallback;
    protected FinishCallback finishCallback;
    protected HttpRequestBase request;

    private static Executor executor = new Executor() {
        @Override
        public void execute(final Runnable runnable) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                }
            }).start();
        }
    };

    public HttpRequestBase getRequest() {
        return request;
    }

    public BaseNetworkTask(DataHolder dataHolder, String url, ResponseCallback responseCallback, ExceptionCallback exceptionCallback, Response.Type responseType) {
        this.responseCallback = responseCallback;
        this.dataHolder = dataHolder;
        this.responseType = responseType;

        try {
            this.request = HttpRequestBuilder.getAuthorizedRequestBuilder(dataHolder, url).build();
        } catch (Exception e) {
            if (exceptionCallback != null)
                exceptionCallback.onException(e);
            cancel(true);
        }
    }

    public BaseNetworkTask(DataHolder dataHolder, HttpRequestBase request, ResponseCallback responseCallback, Response.Type responseType) {
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

    @Override
    protected Response doInBackground(Void... arg) {
        return dataHolder.getNetworkInterface().execute(request, responseType);
    }

    @Override
    final protected void onPostExecute(Response response) {
         if (!isCancelled()) {

            onFinish(response);

            if (response != null && responseCallback != null)
                responseCallback.onSuccess(response);
        }

        if (finishCallback != null) {
            Result result = Result.SUCCESS;
            if (response == null)
                result = Result.ERROR;
            else if (isCancelled())
                result = Result.CANCELED;

            finishCallback.onFinish(result);
        }
    }

    protected void onFinish(Response result) {

    }

    public void setRequest(HttpRequestBase request) {
        this.request = request;
    }

    public static void run(final BaseNetworkTask task) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    executeHoneycomb(task);
                else
                    executeLegacy(task);
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void executeHoneycomb(final BaseNetworkTask task) {
        task.executeOnExecutor(executor);
    }

    private static void executeLegacy(final BaseNetworkTask task) {
        task.execute();
    }

    public void setResponseCallback(ResponseCallback responseCallback) {
        this.responseCallback = responseCallback;
    }

    public void setFinishCallback(FinishCallback finishCallback) {
        this.finishCallback = finishCallback;
    }

    public static interface ResponseCallback {
        abstract void onSuccess(Response response);
    }

    public static interface ExceptionCallback {
        abstract void onException(Exception e);
    }

    public static interface FinishCallback {
        abstract void onFinish(Result result);
    }
}
