package com.frca.vsexam.context;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.frca.vsexam.R;
import com.frca.vsexam.adapters.ExamAdapter;
import com.frca.vsexam.adapters.FilterAdapter;
import com.frca.vsexam.context.base.BaseActivity;
import com.frca.vsexam.context.base.BaseCoreActivity;
import com.frca.vsexam.entities.exam.Exam;
import com.frca.vsexam.entities.exam.ExamList;
import com.frca.vsexam.fragments.base.ContentFragment;
import com.frca.vsexam.fragments.exam_detail.DetailFragment;
import com.frca.vsexam.network.tasks.BaseNetworkTask;
import com.frca.vsexam.network.tasks.UserImageNetworkTask;

import java.io.Serializable;
import java.util.ArrayList;

public class ExamActivity extends BaseCoreActivity {

    public static final String KEY_LAST_EXAM = "key_last_exam";

    private ExamList mExams;

    private SlidingPaneLayout mSlidingLayout;

    private ListView mList;

    private View mFilterSpinnerContainer;

    private Toolbar mActionToolbar;

    private Exam mSelectedExam;

    private ExamAdapter mExamAdapter;

    private FilterAdapter mFilterAdapter;

    private int currentCourseId = -1;

    private ContentFragment mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Serializable serializable = getIntent().getSerializableExtra(StartingActivity.KEY_EXAMS);
        mExams = ExamList.fromArrayList((ArrayList<Exam>) serializable);

        setContentView(R.layout.activity_exam);

        mSlidingLayout = (SlidingPaneLayout) findViewById(R.id.sliding_pane);
        mList = (ListView) findViewById(R.id.left_pane);

        mActionToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mActionToolbar);

        final ActionBar actionBar = getSupportActionBar();

        mSlidingLayout.setPanelSlideListener(new SlidingPaneLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View view, float v) { }

            @Override
            public void onPanelOpened(View view) {
                setUpToolbar();
                actionBar.setDisplayShowTitleEnabled(false);
                actionBar.setDisplayHomeAsUpEnabled(false);
            }

            @Override
            public void onPanelClosed(View view) {
                if (mCurrentFragment != null) {
                    mFilterSpinnerContainer.setVisibility(View.GONE);
                    actionBar.setTitle(mCurrentFragment.getTitle());
                    actionBar.setDisplayShowTitleEnabled(true);
                    actionBar.setDisplayHomeAsUpEnabled(true);
                }
            }
        });

        setUpToolbar();

        mSlidingLayout.openPane();
        mSlidingLayout.setSliderFadeColor(0x66cccccc);
        mSlidingLayout.setShadowResourceLeft(R.drawable.shadow_right);

        String realName = getDataHolder().getPreferences().getString(KEY_REAL_NAME, null);
        if (realName != null) {
            View imageHolder = findViewById(R.id.logo);
            BaseNetworkTask.run(new UserImageNetworkTask(this, 0, imageHolder));
            TextView view = (TextView) findViewById(R.id.text_real_name);
            view.setText(realName);
        } else {
            View view = findViewById(R.id.layout_user);
            view.setVisibility(View.GONE);
        }

        setExamListView();
    }

    @Override
    public void onResume() {
        super.onResume();

        Fragment fragment = getContentFragment();
        if (mCurrentFragment != null && !mCurrentFragment.equals(fragment))
            replaceFragment(mCurrentFragment);
    }

    @Override
    public void onBackPressed() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 0) {
            manager.popBackStack();
            return;
        }

        SlidingPaneLayout slidingPaneLayout = getSlidingLayout();
        if (!slidingPaneLayout.isOpen()) {
            slidingPaneLayout.openPane();
            return;
        }

        super.onBackPressed();
    }

    private void setExamListView() {
        mExamAdapter = getExamAdapter(true);
        mList.setAdapter(mExamAdapter);

        if (mSelectedExam == null) {
            mSelectedExam = mExams.get(0);
            int lastExamId = getDataHolder().getPreferences().getInt(KEY_LAST_EXAM, 0);
            if (lastExamId != 0) {
                Exam lastExam = mExams.find(lastExamId);
                if (lastExam != null)
                    mSelectedExam = lastExam;
            }

            if (mSelectedExam != null)
                openExam(mSelectedExam);
        }

        if (mList.getOnItemClickListener() == null) {
            mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Exam exam = mExamAdapter.getExam(position);
                    if (exam == null)
                        return;

                    openExam(exam);

                    closeSlidingPane();
                }
            });
        }
    }

    public void updateView() {
        setExamListView();

        ContentFragment fragment = getContentFragment();
        if (fragment != null) {
            fragment.reload();
        }
    }

    public ExamList getExams() {
        return mExams;
    }

    public SlidingPaneLayout getSlidingLayout() {
        return mSlidingLayout;
    }

    public ContentFragment getContentFragment() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (currentFragment != null && currentFragment instanceof ContentFragment)
            return (ContentFragment) currentFragment;

        return null;
    }

    private boolean isOnlyMenuOpen() {
        return mSlidingLayout.isSlideable() && mSlidingLayout.isOpen();
    }

    public void openExam(Exam exam) {
        setSelectedExam(exam);
        replaceFragment(DetailFragment.newInstance(exam));
        getDataHolder().getPreferences().edit().putInt(KEY_LAST_EXAM, exam.getId()).commit();
    }

    public void replaceFragment(ContentFragment fragment) {
        mCurrentFragment = fragment;

        if (isActive()) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            transaction.replace(R.id.container, fragment);
            if (getContentFragment() != null && !isOnlyMenuOpen())
                transaction.addToBackStack(null);

            transaction.commit();
        }
    }

    public void setSelectedExam(Exam exam) {
        mSelectedExam = exam;
        mExamAdapter.setHighlightedExam(exam);
    }

    private FilterAdapter getFilterAdapter(boolean forceUpdate) {
        if (mFilterAdapter == null || forceUpdate) {
            mFilterAdapter = new FilterAdapter(this);

            SparseArray<String> courses = mExams.getCourses();
            if (courses.size() > 0) {
                mFilterAdapter.addItem(-1, getString(R.string.display_all));
                mFilterAdapter.addHeader("Předměty");

                for (int i = 0; i < courses.size(); ++i) {
                    int key = courses.keyAt(i);
                    String value = courses.valueAt(i);
                    mFilterAdapter.addItem(key, value);
                }
            }
        }

        return mFilterAdapter;
    }

    private ExamAdapter getExamAdapter(boolean forceUpdate) {
        if (mExamAdapter == null || forceUpdate) {
            mExamAdapter = new ExamAdapter(this);
            mExamAdapter.setHighlightedExam(mSelectedExam);

            Exam.Group lastGroup = null;
            for (Exam exam : mExams) {
                if (currentCourseId != -1 && exam.getCourseId() != currentCourseId)
                    continue;

                if (exam.getGroup() != lastGroup) {
                    mExamAdapter.addHeader(exam.getGroup());
                    lastGroup = exam.getGroup();
                }

                mExamAdapter.addItem(exam);
            }
        }

        return mExamAdapter;
    }

    public void closeSlidingPane() {
        if (mSlidingLayout.isSlideable()) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    mSlidingLayout.closePane();
                }
            });
        }
    }

    private void setUpToolbar() {
        if (mFilterSpinnerContainer == null) {
            mFilterSpinnerContainer = LayoutInflater.from(this).inflate(R.layout.filter_spinner, mActionToolbar, false);
            ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mActionToolbar.addView(mFilterSpinnerContainer, lp);
        } else {
            mFilterSpinnerContainer.setVisibility(View.VISIBLE);
        }

        FilterAdapter adapter = getFilterAdapter(false);
        Spinner spinner = (Spinner) mFilterSpinnerContainer.findViewById(R.id.actionbar_spinner);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> spinner, View view, int position, long itemId) {
                //onTopLevelTagSelected(mTopLevelSpinnerAdapter.getTag(position));
                currentCourseId = mFilterAdapter.getId(position);

                setExamListView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    @Override
    protected void handleRefreshRequest() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.update_in_progress));
        dialog.show();
        loadExams(new LoadingExamResult() {
            @Override
            public void onExamLoadingSuccess(ExamList exams) {
                mExams = exams;
                dialog.hide();
                updateView();
            }

            @Override
            public void onExamLoadingDenied() {
                Toast.makeText(ExamActivity.this, R.string.network_error_exams_title, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onExamLoadingError() {
                Toast.makeText(ExamActivity.this, R.string.network_error_exams_title, Toast.LENGTH_LONG).show();
            }
        });
    }
}
