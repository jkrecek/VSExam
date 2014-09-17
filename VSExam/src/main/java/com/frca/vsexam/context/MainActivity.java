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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.frca.vsexam.R;
import com.frca.vsexam.adapters.ExamAdapter;
import com.frca.vsexam.entities.exam.Exam;
import com.frca.vsexam.entities.exam.ExamList;
import com.frca.vsexam.fragments.base.ContentFragment;
import com.frca.vsexam.fragments.exam_detail.DetailFragment;
import com.frca.vsexam.helper.AppSparseArray;
import com.frca.vsexam.helper.DataHolder;
import com.frca.vsexam.helper.Utils;
import com.frca.vsexam.network.tasks.BaseNetworkTask;
import com.frca.vsexam.network.tasks.UserImageNetworkTask;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    public static final String KEY_LAST_EXAM = "key_last_exam";

    private ExamList mExams;

    private SlidingPaneLayout mSlidingLayout;

    private ListView mList;

    private TextView mListEmptyText;

    private ActionBar mActionBar;

    private Exam currentlySelected;

    private ExamAdapter adapter;

    private AppSparseArray<String> actionBarAdapterData;

    private int currentCourseId = -1;

    private ContentFragment mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Serializable serializable = getIntent().getSerializableExtra(StartingActivity.KEY_EXAMS);
        mExams = ExamList.fromArrayList((ArrayList<Exam>) serializable);

        setContentView(R.layout.activity_main);

        mSlidingLayout = (SlidingPaneLayout) findViewById(R.id.sliding_pane);
        mList = (ListView) findViewById(R.id.left_pane);
        mListEmptyText = (TextView) findViewById(R.id.left_pane_empty_text);
        mActionBar = getSupportActionBar();
        mSlidingLayout.setPanelSlideListener(new SlidingPaneLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View view, float v) { }

            @Override
            public void onPanelOpened(View view) {
                mActionBar.setDisplayShowTitleEnabled(false);
                mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
                mActionBar.setDisplayHomeAsUpEnabled(false);
            }

            @Override
            public void onPanelClosed(View view) {
                ContentFragment contentFragment = getContentFragment();
                if (contentFragment != null) {
                    mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                    mActionBar.setTitle(contentFragment.getTitle());
                    mActionBar.setDisplayShowTitleEnabled(true);
                    mActionBar.setDisplayHomeAsUpEnabled(true);
                }
            }
        });

        mSlidingLayout.openPane();
        mSlidingLayout.setSliderFadeColor(0x66cccccc);
        mSlidingLayout.setShadowResource(R.drawable.shadow_right);

        String realName = DataHolder.getInstance(this).getPreferences().getString(KEY_REAL_NAME, null);
        if (realName != null) {
            View imageHolder = findViewById(R.id.logo);
            BaseNetworkTask.run(new UserImageNetworkTask(this, 0, imageHolder));
            TextView view = (TextView) findViewById(R.id.text_real_name);
            view.setText(realName);
        } else {
            View view = findViewById(R.id.layout_user);
            view.setVisibility(View.GONE);
        }

        setActionBarAdapter();
    }

    public void onResume() {
        super.onResume();

        if (!hasSavedLoginData()) {
            Intent intent = new Intent(this, StartingActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        Fragment fragment = getContentFragment();
        if (mCurrentFragment != null && !mCurrentFragment.equals(fragment))
            replaceFragment(mCurrentFragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
        // TODO
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                SlidingPaneLayout slidingPaneLayout = getSlidingLayout();
                if (slidingPaneLayout.isSlideable()) {
                    onBackPressed();
                    return true;
                }
                break;
            }
            case R.id.action_refresh: {
                final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
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
                        Toast.makeText(MainActivity.this, R.string.network_error_exams_title, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onExamLoadingError() {
                        Toast.makeText(MainActivity.this, R.string.network_error_exams_title, Toast.LENGTH_LONG).show();
                    }
                });

                break;
            }
            case R.id.action_settings: {
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            }
        }

        return super.onOptionsItemSelected(item);
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

    private void setActionBarAdapter() {
        actionBarAdapterData = mExams.getCourses();
        actionBarAdapterData.put(-1, getString(R.string.display_all));
        setActionBarAdapter(actionBarAdapterData.getValues());
    }

    public void setActionBarAdapter(List<String> values) {
        ActionBar actionBar = getSupportActionBar();
        if (values == null) {
            actionBar.setDisplayShowTitleEnabled(true);
            getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        } else {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, android.R.id.text1, values);
            adapter.setDropDownViewResource(R.layout.actionbar_dropdown_item);
            actionBar.setListNavigationCallbacks(adapter, new ActionBarAdapterClickListener());
        }
    }

    private void updateActionBarAdapter() {
        if (!mExams.isEmpty()) {
            adapter = new ExamAdapter(this, mExams.filter(new ExamList.MatchChecker() {
                @Override
                public boolean isMatch(Exam exam) {
                    return currentCourseId == -1 || exam.getCourseId() == currentCourseId;
                }
            }), currentlySelected);

            mList.setAdapter(adapter);
            mList.setVisibility(View.VISIBLE);
            mListEmptyText.setVisibility(View.GONE);

            if (currentlySelected == null) {
                int lastExamId = DataHolder.getInstance(this).getPreferences().getInt(KEY_LAST_EXAM, 0);
                if (lastExamId != 0)
                    currentlySelected = mExams.find(lastExamId);

                if (currentlySelected == null && Utils.isValid(mExams))
                    currentlySelected = mExams.get(0);

                if (currentlySelected != null)
                    openExam(currentlySelected);
            }
        } else {
            mList.setVisibility(View.GONE);
            mListEmptyText.setVisibility(View.VISIBLE);
            if (currentCourseId == -1)
                mListEmptyText.setText(R.string.no_exams_in_this_period);
            else
                mListEmptyText.setText(R.string.no_exams_with_filter);
        }

        if (mList.getOnItemClickListener() == null) {
            mList.setOnItemClickListener(new ListItemClickListener());
        }
    }

    public void updateView() {
        updateActionBarAdapter();

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
        highlightExam(exam);
        replaceFragment(DetailFragment.newInstance(exam));
        DataHolder.getInstance(this).getPreferences().edit().putInt(KEY_LAST_EXAM, exam.getId()).commit();
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

    public void highlightExam(Exam exam) {
        adapter.highlightExam(currentlySelected, mList, false);
        if (exam != null)
            adapter.highlightExam(exam, mList, true);

        currentlySelected = exam;
    }

    private class ListItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Exam exam = adapter.getExam(position);
            if (exam == null)
                return;

            openExam(exam);

            if (mSlidingLayout.isSlideable()) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mSlidingLayout.closePane();
                    }
                });
            }
        }
    }

    private class ActionBarAdapterClickListener implements ActionBar.OnNavigationListener {

        @Override
        public boolean onNavigationItemSelected(int id, long l) {
            currentCourseId = actionBarAdapterData.keyAt(id);

            updateActionBarAdapter();
            return false;
        }
    }

}
