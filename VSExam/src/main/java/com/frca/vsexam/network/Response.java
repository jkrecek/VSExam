package com.frca.vsexam.network;

import android.graphics.Bitmap;

import com.frca.vsexam.helper.Helper;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Response {

    private int statusCode;
    private String text;
    private Bitmap bitmap;

    private Type type;

    private Date serverTime;
    private Date requestTime;
    private Date responseTime;
    private long contentLength;
    private HttpRequestBase request;

    private boolean complete;

    private Map<String, String> responseHeaders;

    public enum Type {
        TEXT,
        BITMAP
    }

    public Response(HttpRequestBase request) {
        requestTime = new Date();
        complete = false;
        this.request = request;
    }

    public static byte[] readInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        try {
            while ((length = is.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
        } finally {
            Helper.close(is);
        }

        return outputStream.toByteArray();
    }

    public void logToFile() {
        StringBuilder builder = new StringBuilder();
        builder.append("-- REQUEST\n");
        builder.append(Helper.outputRequest(request));

        builder.append("\n\n-- RESPONSE HEADERS\n");
        builder.append(Helper.outputResponseHeaders(getResponseHeaders()));

        builder.append("\n\n-- RESPONSE CONTENT\n");
        if (text != null)
            builder.append(text);
        else if (bitmap != null)
            builder.append("<-- bitmap -->");
        else
            builder.append("null");

        String filename = request.getURI().getPath().substring(1).replaceAll("/|\\.", "-");
        filename += "_" + String.valueOf(System.currentTimeMillis() / 1000L);
        Helper.writeToFile(
            builder.toString(),
            Helper.getDataDirectoryFile("http", filename, "log"),
            false
        );
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setText(String text) {
        this.text = text;
        this.bitmap = null;
        this.type = Type.TEXT;
    }

    public void setBitmap(Bitmap bitmap) {
        this.text = null;
        this.bitmap = bitmap;
        this.type = Type.BITMAP;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setServerTime(String serverTime) throws ParseException, DateParseException {
        this.serverTime = DateUtils.parseDate(serverTime);
    }

    public Type getType() {
        return type;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getText() {
        return text;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public Date getServerTime() {
        return serverTime;
    }

    public Date getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Date requestTime) {
        this.requestTime = requestTime;
    }

    public long getServerLocalTimeDiff() {
        if (serverTime != null)
            return serverTime.getTime() - requestTime.getTime();

        return 0L;
    }

    public Date getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Date responseTime) {
        this.responseTime = responseTime;
    }

    public long getDuration() {
        return responseTime.getTime() - requestTime.getTime();
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public HttpRequestBase getRequest() {
        return request;
    }

    public boolean isComplete() {
        return complete;
    }

    public void markComplete() {
        this.complete = true;
    }

    public void setResponseHeaders(Header[] responseHeaders) {
        this.responseHeaders = new HashMap<String, String>();
        for (Header header : responseHeaders) {
            String value = header.getValue();
            if (this.responseHeaders.containsKey(header.getName()))
                value = this.responseHeaders.get(header.getName()) + " | " + value;

            this.responseHeaders.put(header.getName(), value);
        }
    }

    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }

}

