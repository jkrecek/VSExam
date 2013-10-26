package com.frca.vsexam.network.tasks;

import android.content.Context;

import com.frca.vsexam.helper.DataHolder;
import com.frca.vsexam.network.Response;

import org.apache.http.client.methods.HttpRequestBase;

public class TextNetworkTask extends BaseNetworkTask {

    public TextNetworkTask(Context context, HttpRequestBase requestBase, ResponseCallback responseCallback) {
        super(DataHolder.getInstance(context), requestBase, responseCallback, Response.Type.TEXT);
    }

    public TextNetworkTask(Context context, String url, ResponseCallback responseCallback, ExceptionCallback exceptionCallback) {
        super(DataHolder.getInstance(context), url, responseCallback, exceptionCallback, Response.Type.TEXT);
    }

}
