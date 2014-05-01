package com.frca.vsexam.network;

import android.util.Log;

import com.frca.vsexam.helper.AppConfig;
import com.frca.vsexam.helper.Helper;

import org.apache.http.client.methods.HttpRequestBase;

import java.io.IOException;

public class NetworkInterface {

    public final static int TIMEOUT_MS = 15000;

    private final static int CLIENT_COUNT = 5;

    private final static int MAX_ATTEMPTS = 3;

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

                Helper.sleepThread(50);
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

        NetworkWorker networkWorker = getFreeClient();
        Response response = null;
        for (int i = 0; i < MAX_ATTEMPTS && (response == null || !response.isComplete()); ++i) {
            response = networkWorker.execute(request, type);
        }

        try {
            return response;
        } finally {
            if (response != null) {
                if (response.getServerTime() != null)
                    lastServerLocalTimeDiff = response.getServerLocalTimeDiff();
                if (AppConfig.LOG_HTTP_PACKETS)
                    response.logToFile();
            }

            freeClient(networkWorker);
        }
    }

    public long getCurrentServerTime() {
        return System.currentTimeMillis() + lastServerLocalTimeDiff;
    }

    public long getLastServerLocalTimeDiff() {
        return lastServerLocalTimeDiff;
    }
}
