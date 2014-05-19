package com.frca.vsexam.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.frca.vsexam.R;
import com.frca.vsexam.adapters.ExamAdapter;
import com.frca.vsexam.entities.exam.Exam;
import com.frca.vsexam.entities.exam.ExamList;
import com.frca.vsexam.fragments.base.BaseFragment;
import com.frca.vsexam.fragments.base.ContentFragment;
import com.frca.vsexam.fragments.exam_detail.DetailFragment;
import com.frca.vsexam.helper.AppSparseArray;
import com.frca.vsexam.helper.DataHolder;
import com.frca.vsexam.helper.Helper;

public class MainFragment extends BaseFragment {

    public static final String KEY_LAST_EXAM = "key_last_exam";

    private ExamList exams;

    private SlidingPaneLayout mSlidingLayout;

    private ListView mList;

    private TextView mListEmptyText;

    private ActionBar mActionBar;

    private Exam currentlySelected;

    private ExamAdapter adapter;

    private AppSparseArray<String> actionBarAdapterData;

    private int currentCourseId = -1;

    public MainFragment(ExamList exams) {
        this.exams = exams;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mSlidingLayout = (SlidingPaneLayout) rootView.findViewById(R.id.sliding_pane);
        mList = (ListView) rootView.findViewById(R.id.left_pane);
        mListEmptyText = (TextView) rootView.findViewById(R.id.left_pane_empty_text);
        mActionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
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
        mSlidingLayout.setShadowResource(R.drawable.right_shadow);

        setActionBarAdapter();

        return rootView;
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

    public void openExam(Exam exam) {
        highlightExam(exam);
        replaceFragment(DetailFragment.newInstance(exam));
        DataHolder.getInstance(getActivity()).getPreferences().edit().putInt(KEY_LAST_EXAM, exam.getId()).commit();
    }

    public void replaceFragment(ContentFragment fragment) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        if (getContentFragment() != null && !isOnlyMenuOpen())
            transaction.addToBackStack(null);
        transaction.commit();
    }

    public void highlightExam(Exam exam) {
        adapter.highlightExam(currentlySelected, mList, false);
        if (exam != null)
            adapter.highlightExam(exam, mList, true);

        currentlySelected = exam;
    }

    @Override
    public boolean onBackPressed() {
        FragmentManager manager = getChildFragmentManager();
        if (manager.getBackStackEntryCount() > 0) {
            manager.popBackStack();
            return true;
        }

        SlidingPaneLayout slidingPaneLayout = getSlidingLayout();
        if (!slidingPaneLayout.isOpen()) {
            slidingPaneLayout.openPane();
            return true;
        }

        return false;
    }

    private void setActionBarAdapter() {
        actionBarAdapterData = exams.getCourses();
        actionBarAdapterData.put(-1, getString(R.string.display_all));
        getMainActivity().setActionBarAdapter(actionBarAdapterData.getValues());
    }

    @Override
    public void onNavigationItemSelected(final int id) {
        currentCourseId = actionBarAdapterData.keyAt(id);

        updateActionBarAdapter();
    }

    private void updateActionBarAdapter() {
        if (!exams.isEmpty()) {
            adapter = new ExamAdapter(getActivity(), exams.filter(new ExamList.MatchChecker() {
                @Override
                public boolean isMatch(Exam exam) {
                    return currentCourseId == -1 || exam.getCourseId() == currentCourseId;
                }
            }), currentlySelected);

            mList.setAdapter(adapter);
            mList.setVisibility(View.VISIBLE);
            mListEmptyText.setVisibility(View.GONE);

            if (currentlySelected == null) {
                int lastExamId = DataHolder.getInstance(getActivity()).getPreferences().getInt(KEY_LAST_EXAM, 0);
                if (lastExamId != 0)
                    currentlySelected = exams.find(lastExamId);

                if (currentlySelected == null && Helper.isValid(exams))
                    currentlySelected = exams.get(0);

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
        return exams;
    }

    public SlidingPaneLayout getSlidingLayout() {
        return mSlidingLayout;
    }

    public ContentFragment getContentFragment() {
        Fragment currentFragment = getChildFragmentManager().findFragmentById(R.id.container);
        if (currentFragment != null && currentFragment instanceof ContentFragment)
            return (ContentFragment) currentFragment;

        return null;
    }

    private boolean isOnlyMenuOpen() {
        return mSlidingLayout.isSlideable() && mSlidingLayout.isOpen();
    }
}
