package com.frca.vsexam;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.frca.vsexam.fragments.BrowserPaneFragment;
import com.frca.vsexam.fragments.LoadingFragment;
import com.frca.vsexam.fragments.LoginFragment;
import com.frca.vsexam.network.HttpRequestBuilder;
import com.frca.vsexam.network.NetworkTask;
import com.frca.vsexam.network.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    public Data data;

    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        data = new Data();
        data.preferences = getPreferences(MODE_PRIVATE);
        data.configuration = getResources().getConfiguration();

        if (data.preferences.contains(HttpRequestBuilder.KEY_LOGIN) && data.preferences.contains(HttpRequestBuilder.KEY_PASSWORD)) {
            setFragment(new LoadingFragment("Preparing"));
            loadExams();
        } else {
            setFragment(new LoginFragment());
        }
    }

    void setFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.view, fragment).commit();
        currentFragment = fragment;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class Data {
        public SharedPreferences preferences;
        public Configuration configuration;
    }

    public void loadExams() {
        if (!(currentFragment instanceof LoadingFragment)) {
            setFragment(new LoadingFragment(null));
        }

        ((LoadingFragment)currentFragment).setMessage("Downloading exams");

        try {
            new NetworkTask(new NetworkTask.ResponseCallback() {

                @Override
                public void call(Response response) {
                    if (response.statusCode == 401) {
                        setFragment(new LoginFragment());
                        Toast.makeText(MainActivity.this, "Invalid access", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (currentFragment instanceof LoadingFragment)
                        ((LoadingFragment)currentFragment).setMessage("Processing data");

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

                        exams.add(Exam.get(columns, group));
                    }

                    setFragment(new BrowserPaneFragment(exams));

                }
            }).execute(new HttpRequestBuilder(data, "student/terminy_seznam.pl").build());

        } catch (NoAuthException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (currentFragment instanceof BrowserPaneFragment) {
            SlidingPaneLayout slidingPaneLayout = ((BrowserPaneFragment) currentFragment).getSlidingLayout();
            if (!slidingPaneLayout.isOpen()) {
                slidingPaneLayout.openPane();
                return;
            }
        }

        super.onBackPressed();
    }
}
