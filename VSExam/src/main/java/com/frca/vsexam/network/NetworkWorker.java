package com.frca.vsexam.network;

import android.graphics.Bitmap;
import android.net.http.AndroidHttpClient;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.text.ParseException;

/**
 * Created by KillerFrca on 26.10.13.
 */
public class NetworkWorker {

    private AndroidHttpClient client;
    private boolean isUsed;
    private HttpResponse httpResponse;

    public NetworkWorker() {
        client = AndroidHttpClient.newInstance(HttpRequestBuilder.USER_AGENT);
        isUsed = false;
        httpResponse = null;
    }

    public Response execute(HttpRequestBase request, Response.Type type) {
        Response response = new Response();
        try {
            httpResponse = client.execute(request);
            HttpEntity entity = httpResponse.getEntity();
            response.setStatusCode(httpResponse.getStatusLine().getStatusCode());
            response.setContentLength(entity.getContentLength());
            Log.d("execute", "Response content length: " + String.valueOf(entity.getContentLength()));
            response.setHeaders(httpResponse.getAllHeaders());

            if (type == Response.Type.TEXT) {
                Charset charset = Charset.forName(EntityUtils.getContentCharSet(entity));
                String text = Response.parseText(entity.getContent(), charset);
                Log.d("execute", "Parsed response text: " + String.valueOf(text.length()));
                response.setText(text);
            } else if (type == Response.Type.BITMAP) {
                Bitmap bitmap = Response.parseBitmap(entity.getContent());
                response.setBitmap(bitmap);
            } else
                throw new UnsupportedOperationException("Response type must be Text or Bitmap");

            response.setServerTime(httpResponse.getFirstHeader("Date").getValue());

        } catch (IOException e) {
            Log.e(getClass().getName(), "Error while executing http request on url `" + request.getURI() + "`" +
                (TextUtils.isEmpty(e.getMessage()) ? "." : ", error: `" + e.getMessage() + "`"));
        } catch (IllegalCharsetNameException e) {
            Log.e(getClass().getName(), "Unknown charset sent `" + e.getMessage() + "`");
        } catch (ParseException e) {
            Log.e(getClass().getName(), "Unable to parse Http Date Header, `" + e.getMessage() + "`");
        } catch (DateParseException e) {
            Log.e(getClass().getName(), "Unable to parse Date, `" + e.getMessage() + "`");
        } catch (UnsupportedOperationException e) {
            Log.e(getClass().getName(), "Unsupported operation: , `" + e.getMessage() + "`");
        }

        return response;
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
