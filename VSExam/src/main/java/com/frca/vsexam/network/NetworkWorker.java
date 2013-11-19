package com.frca.vsexam.network;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.frca.vsexam.fragments.LoadingFragment;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.text.ParseException;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by KillerFrca on 26.10.13.
 */
public class NetworkWorker {

    private HttpClient client;
    private boolean isUsed;
    private boolean isWorking;

    public NetworkWorker() {
        client = new DefaultHttpClient(NetworkInterface.httpClientParams);
        isUsed = false;
        isWorking = false;
        //httpResponse = null;
    }

    public Response execute(HttpURLConnection request, Response.Type type) {
        isWorking = true;
        Response response = new Response();
        try {
            /*httpResponse = client.execute(request);*/
            request.connect();

            //HttpEntity entity = httpResponse.getEntity();

            response.setStatusCode(request.getResponseCode());
            Log.e("bla", String.valueOf(response.getStatusCode()));
            InputStream is = request.getInputStream();
            response.setContentLength(Long.valueOf(request.getHeaderField("Content-Length")));
            Log.d("execute", "Response content length: " + String.valueOf(response.getContentLength()));

            if (type == Response.Type.TEXT) {
                Charset charset = Charset.forName("iso-8859-2");
                String text = Response.parseText(is, charset);
                Log.d("execute", "Parsed response text: " + String.valueOf(text.length()));
                response.setText(text);
            } else if (type == Response.Type.BITMAP) {
                Bitmap bitmap = Response.parseBitmap(is);
                response.setBitmap(bitmap);
            } else
                throw new UnsupportedOperationException("Response type must be Text or Bitmap");

            response.setServerTime(request.getHeaderField("Date"));

        } catch (IOException e) {
            Log.e(getClass().getName(), "Error while executing http request on url `" + request.getURL().toExternalForm() + "`" +
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

        isWorking = false;
        return response;
    }

    /*public HttpResponse getHttpResponse() {
        return httpResponse;
    }

    public void setHttpResponse(HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
    }*/

    public boolean isWorking() {
        return isWorking;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean isUsed) {
        this.isUsed = isUsed;
    }

}
