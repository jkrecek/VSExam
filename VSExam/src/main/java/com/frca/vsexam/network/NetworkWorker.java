package com.frca.vsexam.network;

import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

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
        Response response = new Response(request);
        try {
            httpResponse = client.execute(request);
            HttpEntity entity = httpResponse.getEntity();
            response.setStatusCode(httpResponse.getStatusLine().getStatusCode());
            response.setContentLength(entity.getContentLength());
            byte[] content = Response.readInputStream(entity.getContent());
            response.setResponseHeaders(httpResponse.getAllHeaders());
            response.setServerTime(httpResponse.getFirstHeader("Date").getValue());
            response.setResponseTime(new Date());

            if (content.length != response.getContentLength())
                throw new IOException("Partial content received (" + content.length + " of " + response.getContentLength() + " bytes)");

            if (type == Response.Type.TEXT) {
                response.setText(new String(content, EntityUtils.getContentCharSet(entity)));
            } else if (type == Response.Type.BITMAP) {
                response.setBitmap(BitmapFactory.decodeByteArray(content, 0, content.length));
            } else
                throw new UnsupportedOperationException("Response type must be Text or Bitmap");

            response.markComplete();
        } catch (IOException e) {
            Log.e(getClass().getName(), "Network error: " + (TextUtils.isEmpty(e.getMessage()) ? "." : "`" + e.getMessage() + "`") + "\n" + request.getURI());
        } catch (ParseException e) {
            Log.e(getClass().getName(), "Unable to parse http date header. " + e.getMessage());
        } catch (DateParseException e) {
            Log.e(getClass().getName(), "Unable to parse date. " + e.getMessage());
        } catch (UnsupportedOperationException e) {
            Log.e(getClass().getName(), "Unsupported operation. " + e.getMessage());
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
