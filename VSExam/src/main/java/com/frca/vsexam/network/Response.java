package com.frca.vsexam.network;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Created by KillerFrca on 14.10.13.
 */
public class Response {
    public String http;
    public int statusCode;

    public Response(HttpResponse response) {

        InputStream stream;
        try {
            stream = response.getEntity().getContent();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(stream, EntityUtils.getContentCharSet(response.getEntity())), 8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null)
                sb.append(line + "\n");
        } catch (IOException e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        http = sb.toString();
        statusCode = response.getStatusLine().getStatusCode();
    }

}
