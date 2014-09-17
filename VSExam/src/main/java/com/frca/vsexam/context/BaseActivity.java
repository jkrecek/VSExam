package com.frca.vsexam.context;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import com.frca.vsexam.R;
import com.frca.vsexam.entities.exam.ExamList;
import com.frca.vsexam.entities.exam.ExamTester;
import com.frca.vsexam.exceptions.NoAuthException;
import com.frca.vsexam.helper.AppConfig;
import com.frca.vsexam.helper.DataHolder;
import com.frca.vsexam.helper.Utils;
import com.frca.vsexam.network.HttpRequestBuilder;
import com.frca.vsexam.network.Response;
import com.frca.vsexam.network.tasks.BaseNetworkTask;
import com.frca.vsexam.network.tasks.TextNetworkTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public abstract class BaseActivity extends ActionBarActivity {

    public final static String KEY_ALREADY_RAN = "key_already_ran";

    public final static String KEY_REAL_NAME = "key_real_name";

    private static BaseActivity instance = null;

    public static <T extends BaseActivity> T  getInstance(Class<T> instanceClass) {
        if (instance.getClass() == instanceClass)
            return (T) instance;

        return null;
    }

    protected boolean isActive() {
        //return instance.getClass() == this.getClass();
        return instance == this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;
    }

    @Override
    protected void onResume() {
        super.onResume();

        instance = this;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (instance == this)
            instance = null;
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public TextNetworkTask loadExams(final LoadingExamResult result) {
        TextNetworkTask examLoadingTask = new TextNetworkTask(this, "student/terminy_seznam.pl", new TextNetworkTask.ResponseCallback() {
            @Override
            public void onSuccess(Response response) {
                if (!response.isComplete()) {
                    new AlertDialog.Builder(BaseActivity.this)
                        .setTitle(R.string.network_error_exams_title)
                        .setMessage(R.string.network_error_exams_message)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                result.onExamLoadingError();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .create()
                            .show();
                } else if (response.getStatusCode() == 401) {
                    result.onExamLoadingDenied();
                    Toast.makeText(BaseActivity.this, R.string.access_denied, Toast.LENGTH_LONG).show();
                } else {
                    Document doc = Jsoup.parse(response.getText());
                    Elements elements = doc.body().select("table[id] tr");
                    saveRealName(doc.body().select("#logs"));

                    ExamList exams = new ExamList();
                    if (AppConfig.USE_TEST_EXAMS)
                        ExamTester.fill(exams, BaseActivity.this);
                    else
                        exams.parseAndAdd(BaseActivity.this, elements);

                    result.onExamLoadingSuccess(exams);
                }
            }
        },
        new BaseNetworkTask.ExceptionCallback() {
            @Override
            public void onException(Exception e) {
                if (e instanceof NoAuthException) {
                    result.onExamLoadingDenied();
                    Toast.makeText(BaseActivity.this, R.string.no_auth_data, Toast.LENGTH_LONG).show();
                }
            }
        });

        return (TextNetworkTask) BaseNetworkTask.run(examLoadingTask);
    }

    protected boolean hasSavedLoginData() {
        return DataHolder.getInstance(this).getPreferences().contains(HttpRequestBuilder.KEY_AUTH_KEY);
    }

    protected boolean wasAlreadyStarted() {
        SharedPreferences pref = DataHolder.getInstance(this).getPreferences();
        boolean alreadyRun = pref.getBoolean(KEY_ALREADY_RAN, false);
        if (!alreadyRun)
            pref.edit().putBoolean(KEY_ALREADY_RAN, true).commit();

        return alreadyRun;
    }

    private void saveRealName(Elements elements) {
        if (!Utils.isValid(elements))
            return;

        Element element = elements.get(0);
        element.children().remove();
        String realName = element.text();
        DataHolder.getInstance(this).getPreferences().edit().putString(KEY_REAL_NAME, realName).commit();
    }

    public static interface LoadingExamResult {
        public abstract void onExamLoadingSuccess(ExamList exams);
        public abstract void onExamLoadingDenied();
        public abstract void onExamLoadingError();
    }


}
