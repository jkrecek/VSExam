package com.frca.vsexam.network;

import com.frca.vsexam.entities.exam.Exam;
import com.frca.vsexam.exceptions.NoAuthException;
import com.frca.vsexam.helper.DataHolder;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class HttpRequest {
    public static HttpRequestBase getRegisterRequest(DataHolder holder, Exam exam, boolean apply) {
        HttpRequestBuilder builder = HttpRequestBuilder.getAuthorizedRequestBuilder(holder, "student/terminy_prihlaseni.pl");

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

            builder.setHttpEntity(HttpPost.class, new UrlEncodedFormEntity(urlParameters, "iso-8859-2"));
            HttpPost post = (HttpPost) builder.build();
            post.setHeader("Referer", "https://isis.vse.cz/auth/student/terminy_prihlaseni.pl");
            return post;

        } catch (NoAuthException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static HttpRequestBase getPlanRequest(DataHolder holder, Map<String, String> urlParameters) {
        HttpRequestBuilder builder = HttpRequestBuilder.getUnAuthorizedRequestBuilder(holder, "katalog/plany.pl");
        builder.setGetArguments(urlParameters, '=', ';');
        try {
            return builder.build();
        } catch (NoAuthException e) {
            return null;
        }
    }
}
