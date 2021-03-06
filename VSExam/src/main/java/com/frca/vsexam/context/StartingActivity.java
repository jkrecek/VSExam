package com.frca.vsexam.context;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import com.frca.vsexam.R;
import com.frca.vsexam.adapters.BasePagerAdapter;
import com.frca.vsexam.context.base.BaseActivity;
import com.frca.vsexam.entities.exam.ExamList;
import com.frca.vsexam.helper.DataHolder;
import com.frca.vsexam.views.ViewPagerIndicator;
import com.frca.vsexam.views.ViewPagerManager;

public class StartingActivity extends BaseActivity {

    public static final String KEY_EXAMS = "exams";

    private ViewPager mPager;

    private ViewPagerManager mPagerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_starting);

        mPager = (ViewPager) findViewById(R.id.pager);
        PagerAdapter pagerAdapter = new BasePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(pagerAdapter);
        ViewPagerIndicator pageIndicator = (ViewPagerIndicator) findViewById(R.id.page_indicator);
        mPagerManager = new ViewPagerManager(mPager, pageIndicator);

        doStart();
    }

    private void doStart() {
        if (wasAlreadyStarted()) {
            if (hasSavedLoginData()) {
                startExamLoading();
            } else {
                requestCredentials();
            }
        }
    }

    public void requestCredentials() {
        mPager.setCurrentItem(2);
    }

    public void startExamLoading() {
        mPager.setCurrentItem(3);
    }

    public void startNextActivity(ExamList exams) {
        Intent intent;
        if (!exams.isEmpty()) {
            intent = new Intent(this, ExamActivity.class);
            intent.putExtra(KEY_EXAMS, exams);
        } else {
            intent = new Intent(this, ExamlessActivity.class);
        }

        startActivity(intent);
        finish();
    }

    public ViewPager getPager() {
        return mPager;
    }
}
