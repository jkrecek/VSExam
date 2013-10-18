package com.frca.vsexam.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Created by KillerFrca on 14.10.13.
 */
public class Response {

    private final int statusCode;
    private final String text;
    private final Bitmap bitmap;

    private final Type type;

    public enum Type{
        TEXT,
        BITMAP
    }

    public Response(String text, int statusCode) {
        this.text = text;
        this.bitmap = null;
        this.statusCode = statusCode;
        this.type = Type.TEXT;
    }

    public Response(Bitmap bitmap, int statusCode) {
        this.text = null;
        this.bitmap = bitmap;
        this.statusCode = statusCode;
        this.type = Type.BITMAP;
    }

    public static String parseText(InputStream is, Charset charset) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset), 8);
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null)
                sb.append(line + "\n");
        } catch (IOException e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        return sb.toString();

    }

    public static Bitmap parseBitmap(InputStream is) {
        return BitmapFactory.decodeStream(is);
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
}
