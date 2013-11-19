package com.frca.vsexam.network;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.frca.vsexam.entities.base.Exam;
import com.frca.vsexam.exceptions.NoAuthException;
import com.frca.vsexam.helper.DataHolder;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by KillerFrca on 3.10.13.
 */
public class HttpRequestBuilder {

    public final static String KEY_LOGIN = "key_login";

    public final static String KEY_PASSWORD = "key_password";

    public final static String BASE_URL = "https://isis.vse.cz/auth/";

    private final DataHolder dataHolder;

    private String method = "GET";

    private String partialUrl;

    private AbstractHttpEntity entity;

    private HttpURLConnection mConnection = null;

    public HttpRequestBuilder(Context context,  String partialUrl) {
        this(DataHolder.getInstance(context), partialUrl);
    }

    public HttpRequestBuilder(DataHolder dataHolder,  String partialUrl) {
        this.dataHolder = dataHolder;
        this.partialUrl = partialUrl;
    }

    public HttpURLConnection build() throws NoAuthException {

        if (mConnection != null)
            return mConnection;



        try {
            URL url = new URL(completeURLString(partialUrl));
            mConnection = (HttpURLConnection) url.openConnection();
            mConnection.setRequestMethod(method);
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        mConnection.setRequestProperty("Host", "isis.vse.cz");
        mConnection.setRequestProperty("Authorization", getB64Auth());
        mConnection.setRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
        mConnection.setRequestProperty("Accept-Language", dataHolder.getConfiguration().locale.getLanguage() + ",en;q=0.8");
        mConnection.setRequestProperty("Connection", "keep-alive");
        mConnection.setRequestProperty("Origin", "https://isis.vse.cz");
        mConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.101 Safari/537.36");

        if (entity != null) {
            mConnection.setRequestProperty(entity.getContentType().getName(), entity.getContentType().getValue());
            mConnection.setRequestProperty(entity.getContentEncoding().getName(), entity.getContentEncoding().getValue());
            mConnection.setRequestProperty("Content-Length", String.valueOf(entity.getContentLength()));
            mConnection.setDoInput(true);
        }

        mConnection.setUseCaches(false);
        mConnection.setDoOutput(true);

        Log.d(getClass().getName(), "Http request to url `" + mConnection.getURL().toExternalForm() + "`");

        return mConnection;
    }

    public void setHttpEntity(String method, AbstractHttpEntity entity) {
        this.method = method;
        this.entity = entity;
    }

    public HttpURLConnection getRequest() {
        return mConnection;
    }

    private String getB64Auth () throws NoAuthException {
        String login = dataHolder.getPreferences().getString(KEY_LOGIN, null);
        String password = dataHolder.getPreferences().getString(KEY_PASSWORD, null);

        if (TextUtils.isEmpty(login) || TextUtils.isEmpty(password))
            throw new NoAuthException();

        String source = login + ":" + password;
        String auth = "Basic "+ Base64.encodeToString(source.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP);
        return auth;
    }

    private String getHost() {
        String host = BASE_URL;
        if (host.startsWith("https://"))
            host = host.substring(7);

        host = host.substring(0, host.indexOf("/"));
        return host;
    }

    public static String completeURLString(String url) {
        if (url.startsWith("https://"))
            return url;

        boolean slashPartial = (int)url.charAt(0) == 0x2f;
        if (isBaseUrlSlashEnding() && slashPartial)
            url = BASE_URL + url.substring(1);
        else if (isBaseUrlSlashEnding() || slashPartial)
            url = BASE_URL + url;
        else
            url = BASE_URL + "/" + url;

        return url;
    }

    public static boolean isBaseUrlSlashEnding() {
        return (int)BASE_URL.charAt(BASE_URL.length()-1) == 0x2f;
    }

    public boolean isBuilt() {
        return mConnection != null;
    }

    public static HttpURLConnection getRegisterRequest(DataHolder holder, Exam exam, boolean apply) {
        HttpRequestBuilder builder = new HttpRequestBuilder(holder, "student/terminy_prihlaseni.pl");

        try {
            List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
            urlParameters.add(new BasicNameValuePair("termin", String.valueOf(exam.getId())));
            urlParameters.add(new BasicNameValuePair("predmet", ""));
            urlParameters.add(new BasicNameValuePair("studium", String.valueOf(exam.getStudyId())));
            urlParameters.add(new BasicNameValuePair("obdobi", String.valueOf(exam.getPeriodId())));
            if (apply) {
                if (exam.getRegisteredOnId() != 0) {
                    urlParameters.add(new BasicNameValuePair("odhlas_termin", String.valueOf(exam.getRegisteredOnId())));
                    urlParameters.add(new BasicNameValuePair("odhlasit_prihlasit", "Přihlásit na termín"));
                } else {
                    urlParameters.add(new BasicNameValuePair("prihlasit", "Přihlásit na termín"));
                }
            } else {
                urlParameters.add(new BasicNameValuePair("odhlasit", "Odhlásit z termínu"));
            }

            builder.setHttpEntity("POST", new UrlEncodedFormEntity(urlParameters, "iso-8859-2"));
            HttpURLConnection post = builder.build();
            post.setRequestProperty("Referer", "https://isis.vse.cz/auth/student/terminy_prihlaseni.pl");
            return post;

        } catch (NoAuthException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }
}
