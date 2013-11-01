package com.frca.vsexam.network;

import android.text.TextUtils;
import android.util.Log;

import com.frca.vsexam.helper.Helper;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;

public class NetworkInterface {

    private final static int TIMEOUT_MS = 15000;

    private final static int CLIENT_COUNT = 5;

    static HttpParams httpClientParams = new BasicHttpParams();

    static {
        HttpConnectionParams.setConnectionTimeout(httpClientParams, TIMEOUT_MS);
        HttpConnectionParams.setSoTimeout(httpClientParams, TIMEOUT_MS);
    }


    private NetworkWorker[] networkWorkers = new NetworkWorker[CLIENT_COUNT];

    private long lastServerLocalTimeDiff;

    public NetworkInterface() {
        for (int i = 0; i < networkWorkers.length; ++i)
            networkWorkers[i] = new NetworkWorker();

        lastServerLocalTimeDiff = 0;
    }

    public NetworkWorker getFreeClient() {
        synchronized (this) {
            while (true) {
                for (NetworkWorker networkWorker : networkWorkers) {
                    if (!networkWorker.isUsed()) {
                        networkWorker.setUsed(true);
                        return networkWorker;
                    }
                }

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Log.e(getClass().getName(), "Error while waiting on HttpClients to finish" + (TextUtils.isEmpty(e.getMessage()) ? "." : ", error: `" + e.getMessage() + "`"));
                }
            }
        }
    }


    public void freeClient(NetworkWorker networkWorker) {
        if (networkWorker.getHttpResponse() != null) {
            try {
                if (networkWorker.getHttpResponse().getEntity() != null)
                    networkWorker.getHttpResponse().getEntity().consumeContent();
            } catch (IOException e) {
                e.printStackTrace();
            }

            networkWorker.setHttpResponse(null);
        }

        networkWorker.setUsed(false);
    }

    public Response execute(HttpRequestBase request, Response.Type type) {
        Log.d("Currently Processing", request.getURI().toString());
        if (type == Response.Type.TEXT)
            Helper.dumpRequest(request);

        NetworkWorker networkWorker = getFreeClient();
        try {
            Response response = networkWorker.execute(request, type);
            lastServerLocalTimeDiff = response.getServerLocalTimeDiff();
            return response;
        } finally {
            freeClient(networkWorker);
        }
    }

    public long getCurrentServerTime() {
        return System.currentTimeMillis() + lastServerLocalTimeDiff;
    }

}
