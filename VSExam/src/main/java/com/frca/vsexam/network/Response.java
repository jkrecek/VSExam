package com.frca.vsexam.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import org.apache.http.Header;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
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

    private Boolean isValid = null;

    private Map<String, String> headers;

    public enum Type {
        TEXT,
        BITMAP
    }

    public Response() {
        requestTime = new Date();
    }

    public static String parseText(InputStream is, Charset charset) throws IOException {

        String result;
        ByteArrayOutputStream baos;

        baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        try {
            while ((length = is.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
                /*Log.e("ParsedBytes", String.valueOf(baos.size()));*/
            }
        } finally {
            is.close();
        }

        result = baos.toString(charset.name());

        saveToFile(result, "file_" + String.valueOf(new Date().getTime()) + ".txt");
        saveToFile(result, "file_" + String.valueOf(new Date().getTime()) + ".html");

        return result;

    }

    private static void saveToFile(String text, String filename) {
        FileOutputStream stream = null;
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
            stream = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(stream);
            outputStreamWriter.write(text);
            outputStreamWriter.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap parseBitmap(InputStream is) throws IOException {
        try {
            return BitmapFactory.decodeStream(is);
        } finally {
            is.close();
        }
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

    public boolean isValid() {
        if (isValid == null)
            isValid = type != null && (text != null || bitmap != null) && requestTime != null && serverTime != null;

        return isValid;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public void setHeaders(Header[] headers) {
        this.headers = new HashMap<String, String>();
        for (Header header : headers) {
            String value = header.getValue();
            if (this.headers.containsKey(header.getName()))
                value = this.headers.get(header.getName()) + " | " + value;

            this.headers.put(header.getName(), value);
        }
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeadersString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : headers.entrySet())
            sb.append(entry.getKey() + ": " + entry.getValue() + "\n");

        return sb.toString();
    }
}

