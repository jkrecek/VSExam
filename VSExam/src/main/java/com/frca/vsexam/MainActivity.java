package com.frca.vsexam;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends FragmentActivity {

    public Data data;

    public static final int[] categoryName = new int[] {};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        data = new Data();
        data.preferences = getPreferences(MODE_PRIVATE);
        data.configuration = getResources().getConfiguration();

        try {
            new NetworkTask(new ResponseCallback() {

                @Override
                public void call(Response response) {
                    Document doc = Jsoup.parse(response.http);
                    Elements elements = doc.body().select("table[id] tr");
                    List<String> spinnerStrings = new ArrayList<String>();

                    ExamList exams = new ExamList();
                    int group = 0;
                    for (Element element : elements) {
                        if (element.className().equals("zahlavi")) {
                            ++group;
                            spinnerStrings.add(String.valueOf(group));
                            continue;
                        }

                        Elements columns = element.select("td");
                        if (columns.size() <= 1)
                            continue;

                        exams.add(new Exam(columns, group));
                        /*for (int i = 0; i < columns.size(); ++i) {
                            Log.e(String.valueOf(i), columns.get(i).text().trim());
                        }*/
                        //if (1==1)return;
                        //Exam exam = new Exam(columns, group);

                        //spinnerStrings.add(String.valueOf(exam.examDate));
                        /*String str = "";
                        for (Field field : exam.getClass().getDeclaredFields()) {
                            field.setAccessible(true);
                            String name = field.getName();
                            Object value = null;
                            try {
                                value = field.get(exam);
                                if (value instanceof Integer) {
                                    if ((Integer)value > 1000000000)
                                        value = new Date(((Integer)value)*1000L);
                                }
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                            str += name + ": " + value + "\n";
                        }

                        spinnerStrings.add(str);*/
                    }

                    ListView view = new ListView(MainActivity.this);

                    //view.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, spinnerStrings));
                    ExamAdapter adapter = new ExamAdapter(MainActivity.this, exams);
                    view.setAdapter(adapter);
                    //view.setOnItemClickListener(adapter.new OnExamClickListener());
                    //view.setOnClickListener(adapter.new OnExamClickListener());
                    setContentView(view);

                    /*WebView view = new WebView(MainActivity.this);
                    setContentView(view);
                    view.loadData(response.http, "text/html; charset=UTF-8", null);*/
                }
            }).execute(new HttpRequestBuilder(data, "student/terminy_seznam.pl").build());

        } catch (NoAuthException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public static class Data {
        public SharedPreferences preferences;
        public Configuration configuration;
    }

    private class NetworkTask extends AsyncTask<HttpRequestBuilder, Void, Response> {
        private final ResponseCallback callback;
        public NetworkTask(ResponseCallback callback) {
            this.callback = callback;
        }
        protected Response doInBackground(HttpRequestBuilder... builders) {
            int count = builders.length;
            HttpResponse response = null;
            for (int i = 0; i < count; i++) {
                response = builders[i].execute();
            }
            return new Response(response);
        }

        protected void onPostExecute(Response result) {
            callback.call(result);

        }
    }

    private interface ResponseCallback {
        void call(Response httpString);
    }

    public static class Response {
        public String http;
        //public String contentType;

        public Response(HttpResponse response) {

            InputStream stream;
            try {
                stream = response.getEntity().getContent();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(stream, EntityUtils.getContentCharSet(response.getEntity())), 8);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            StringBuilder sb = new StringBuilder();
            String line = null;
            try {
                while ((line = reader.readLine()) != null)
                    sb.append(line + "\n");
            } catch (IOException e) {
                Log.e("Buffer Error", "Error converting result " + e.toString());
            }

            http = sb.toString();
            /*contentType = response.getFirstHeader("Content-Type").getValue();
            for (Header header : response.getAllHeaders()) {
                Log.d(header.getName(), header.getValue());
            }

            Log.e("TYPE", contentType);*/

        }

    }
}
