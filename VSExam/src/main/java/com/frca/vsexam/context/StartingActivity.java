package com.frca.vsexam.context;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.frca.vsexam.R;
import com.frca.vsexam.adapters.BasePagerAdapter;
import com.frca.vsexam.entities.exam.ExamList;
import com.frca.vsexam.fragments.LoginFragment;
import com.frca.vsexam.fragments.base.BaseFragment;
import com.frca.vsexam.helper.AppConfig;
import com.frca.vsexam.helper.DataHolder;
import com.frca.vsexam.network.HttpRequestBuilder;

public class StartingActivity extends BaseActivity {

    public static final String KEY_EXAMS = "exams";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_starting);

        BasePagerAdapter.appendToPager((ViewPager) findViewById(R.id.pager));
        doStart();
    }

    private void doStart() {
        if (AppConfig.LAUNCH_ON_START != null) {
            if (Activity.class.isAssignableFrom(AppConfig.LAUNCH_ON_START)) {
                startActivity(new Intent(this, AppConfig.LAUNCH_ON_START));

            } else if (BaseFragment.class.isAssignableFrom(AppConfig.LAUNCH_ON_START)) {
                try {
                    //setFragment((BaseFragment) AppConfig.LAUNCH_ON_START.newInstance());
                } catch (Exception e) { /* just ignore */ }

            }
        } else if (hasSavedLoginData()) {
            startExamLoading();
        } else {
            requestCredentials();
        }
    }

    public void startExamLoading() {
        loadExams(new LoadingExamResult() {
            @Override
            public void onExamLoadingSuccess(ExamList exams) {
                startMainActivity(exams);
            }

            @Override
            public void onExamLoadingDenied() {
                requestCredentials();
            }

            @Override
            public void onExamLoadingError() {
                startExamLoading();
            }
        });
    }

    private void requestCredentials() {
        //FragmentManager fm = getSupportFragmentManager();
        //LoginFragment editNameDialog = new LoginFragment();
        //editNameDialog.show(fm, "fragment_edit_name");
    }

    private void startMainActivity(ExamList exams) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(KEY_EXAMS, exams);
        startActivity(intent);
        finish();
    }
}
