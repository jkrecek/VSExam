package com.frca.vsexam.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.frca.vsexam.fragments.LoadingFragment;

import org.apache.http.HttpEntity;
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

public class Response {

    private int statusCode;
    private String text;
    private Bitmap bitmap;

    private Type type;

    private Date serverTime;
    private Date localTime;
    private long contentLength;

    private Boolean isValid = null;

    public enum Type {
        TEXT,
        BITMAP
    }

    public Response() {
        localTime = new Date();
    }

    public static String parseText(InputStream is, Charset charset) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1536];
            int length = 0;
            /*InputStream is = entity.getContent();
            Log.e("chunked", String.valueOf(entity.isChunked()));
            Log.e("rep", String.valueOf(entity.isRepeatable()));
            Log.e("str", String.valueOf(entity.isStreaming()));*/

            while ((length = is.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
                Log.e("state", String.valueOf(baos.size()));
            }

            result = baos.toString(charset.name());

        } catch (IOException e ){
            e.printStackTrace();
        }


        saveToFile("create", String.valueOf(new Date().getTime()) + ".txt");
        saveToFile(result, "file" + String.valueOf(new Date().getTime()) + ".txt");
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

    public static Bitmap parseBitmap(InputStream is) {
        return BitmapFactory.decodeStream(is);
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

    public Date getLocalTime() {
        return localTime;
    }

    public void setLocalTime(Date localTime) {
        this.localTime = localTime;
    }

    public long getServerLocalTimeDiff() {
        if (serverTime != null)
            return serverTime.getTime() - localTime.getTime();

        return 0L;
    }

    public boolean isValid() {
        if (isValid == null)
            isValid = type != null && (text != null || bitmap != null) && localTime != null && serverTime != null;

        return true;
        //return isValid;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }
}

