package com.frca.vsexam.network;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.text.ParseException;
import java.util.Date;

/**
 * Created by KillerFrca on 26.10.13.
 */
public class NetworkWorker {

    private HttpClient client;
    private boolean isUsed;
    private HttpResponse httpResponse;

    public NetworkWorker() {
        client = new DefaultHttpClient(NetworkInterface.httpClientParams);
        isUsed = false;
        httpResponse = null;
    }

    public Response execute(HttpRequestBase request, Response.Type type) {
        try {
            Date startTime = new Date();
            httpResponse = client.execute(request);
            HttpEntity entity = httpResponse.getEntity();
            InputStream is = entity.getContent();
            int statusCode = httpResponse.getStatusLine().getStatusCode();

            Response response;
            if (type == Response.Type.TEXT) {
                Charset charset = Charset.forName(EntityUtils.getContentCharSet(entity));
                String text = Response.parseText(is, charset);
                response = new Response(text, statusCode);
            } else if (type == Response.Type.BITMAP) {
                Bitmap bitmap = Response.parseBitmap(is);
                response = new Response(bitmap, statusCode);
            } else
                return null;

            response.setLocalTime(startTime);
            response.setServerTime(httpResponse.getFirstHeader("Date").getValue());

            return response;
        } catch (IOException e) {
            Log.e(getClass().getName(), "Error while executing http request on url `" + request.getURI() + "`" +
                (TextUtils.isEmpty(e.getMessage()) ? "." : ", error: `" + e.getMessage() + "`"));
        } catch (IllegalCharsetNameException e) {
            Log.e(getClass().getName(), "Unknown charset sent `" + e.getMessage() + "`");
        } catch (ParseException e) {
            Log.e(getClass().getName(), "Unable to parse Http Date Header, `" + e.getMessage() + "`");
        } catch (DateParseException e) {
            Log.e(getClass().getName(), "Unable to parse Date, `" + e.getMessage() + "`");
        }

        return null;
    }

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }

    public void setHttpResponse(HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean isUsed) {
        this.isUsed = isUsed;
    }
}
