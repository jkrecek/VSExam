package com.frca.vsexam;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by KillerFrca on 3.10.13.
 */
public class HttpRequestBuilder {

    public final static String KEY_LOGIN = "key_login";

    public final static String KEY_PASSWORD = "key_password";

    private final static String BASE_URL = "http://isis.vse.cz/auth/";

    private final static int TIMEOUT_MS = 15000;

    private MainActivity.Data contextData;

    private Class<? extends HttpRequestBase> requestType = HttpGet.class;

    private String partialUrl;

    private AbstractHttpEntity entity;

    private HttpRequestBase request = null;

    private static HttpClient client;

    static {
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MS);
        HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MS);
        client = new DefaultHttpClient(httpParams);
    }


    public HttpRequestBuilder(MainActivity.Data contextData,  String partialUrl) {
        this.contextData = contextData;
        this.partialUrl = partialUrl;
    }

    public HttpRequestBuilder build() throws NoAuthException {

        try {
            request = requestType.getDeclaredConstructor(String.class).newInstance(BASE_URL + "/" + partialUrl);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        //request.setHeader("User-Agent", "Apache-HttpClient/4.1 (java 1.5)");
        request.setHeader("Host", getHost());
        request.setHeader("Authorization", getB64Auth());
        request.setHeader("Accept-Language", contextData.configuration.locale.getLanguage() + ",en;q=0.8");
        //List <NameValuePair> nvps = new ArrayList <NameValuePair>();
        //nvps.add(new BasicNameValuePair("data[body]", "test"));
        //AbstractHttpEntity ent=new UrlEncodedFormEntity(nvps, HTTP.UTF_8);
        //ent.setContentType("application/x-www-form-urlencoded; charset=UTF-8");
        //ent.setContentEncoding("UTF-8");
        if (request instanceof HttpEntityEnclosingRequestBase)
            ((HttpPost)request).setEntity(entity);

        return this;
    }

    public HttpResponse execute() {
        try {
            return client.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setHttpEntity(Class<? extends HttpRequestBase> requestType, AbstractHttpEntity entity) {
        this.requestType = requestType;
        this.entity = entity;
    }

    public HttpRequestBase getRequest() {
        return request;
    }

    private String getB64Auth () throws NoAuthException {
        String login = contextData.preferences.getString(KEY_LOGIN, null);
        String password = contextData.preferences.getString(KEY_PASSWORD, null);

        if (TextUtils.isEmpty(login) || TextUtils.isEmpty(password))
            throw new NoAuthException();

        String source = login + ":" + password;
        String auth = "Basic "+ Base64.encodeToString(source.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP);
        return auth;
    }

    private String getHost() {
        String host = BASE_URL;
        if (host.startsWith("http://"))
            host = host.substring(7);

        host = host.substring(0, host.indexOf("/"));
        return host;
    }
}
