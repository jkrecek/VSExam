package com.frca.vsexam.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.frca.vsexam.R;
import com.frca.vsexam.adapters.ExamAdapter;
import com.frca.vsexam.entities.exam.Exam;
import com.frca.vsexam.entities.exam.ExamList;
import com.frca.vsexam.helper.AppSparseArray;

/**
 * Created by KillerFrca on 11.10.13.
 */
public class BrowserPaneFragment extends BaseFragment {

    private ExamList exams;

    private SlidingPaneLayout mSlidingLayout;

    private ListView mList;

    private LinearLayout mContent;

    private ActionBar mActionBar;

    //private View lastHighlighted;
    private Exam currentlySelected;

    private ExamAdapter adapter;

    private AppSparseArray<String> actionBarAdapterData;

    private int currentCourseId = -1;

    public BrowserPaneFragment(ExamList exams) {
        this.exams = exams;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mSlidingLayout = (SlidingPaneLayout) rootView.findViewById(R.id.sliding_pane);
        mList = (ListView) rootView.findViewById(R.id.left_pane);
        mContent = (LinearLayout) rootView.findViewById(R.id.layout_details);
        mActionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();

        mSlidingLayout.setPanelSlideListener(new SlidingPaneLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View view, float v) {

            }

            @Override
            public void onPanelOpened(View view) {
                mActionBar.setDisplayShowTitleEnabled(false);
                mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
                mActionBar.setDisplayHomeAsUpEnabled(false);
            }

            @Override
            public void onPanelClosed(View view) {
                DetailFragment child = getDetailFragment();
                if (child != null) {
                    mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                    mActionBar.setTitle(child.getExam().getCourseCode() + " | " + child.getExam().getCourseName());
                    mActionBar.setDisplayShowTitleEnabled(true);
                    mActionBar.setDisplayHomeAsUpEnabled(true);
                }
            }
        });
        mSlidingLayout.openPane();
        mSlidingLayout.setSliderFadeColor(0x66cccccc);
        mSlidingLayout.setShadowResource(R.drawable.border_right);

        setActionBarAdapter();

        return rootView;
    }

    private class ListItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Exam exam = adapter.getExam(position);
            if (exam == null)
                return;

            Exam previouslySelected = currentlySelected;
            currentlySelected = exam;

            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.container, new DetailFragment(exam));
            transaction.addToBackStack(null);
            transaction.commit();

            if (mSlidingLayout.isSlideable()) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mSlidingLayout.closePane();
                    }
                });
            } else {
                adapter.highlightExam(previouslySelected, false);
                adapter.highlightExam(currentlySelected, true);
                adapter.highlightView(view, true);
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        SlidingPaneLayout slidingPaneLayout = getSlidingLayout();
        if (!slidingPaneLayout.isOpen()) {
            slidingPaneLayout.openPane();
            return true;
        } else if (!slidingPaneLayout.isSlideable()) {
            FragmentManager manager = getChildFragmentManager();

            if (manager.getBackStackEntryCount() > 0) {
                manager.popBackStack();
                return true;
            }
        }

        return false;
    }

    private void setActionBarAdapter() {
        actionBarAdapterData = exams.getCourses();
        actionBarAdapterData.put(-1, "Zobrazit všechny");
        getMainActivity().setActionBarAdapter(actionBarAdapterData.getValues());
    }

    @Override
    public void onNavigationItemSelected(final int id) {
        currentCourseId = actionBarAdapterData.keyAt(id);

        updateActionBarAdapter();
    }

    private void updateActionBarAdapter() {
        adapter = new ExamAdapter(getActivity(), exams.filter(new ExamList.MatchChecker() {
            @Override
            public boolean isMatch(Exam exam) {
                return currentCourseId == -1 || exam.getCourseId() == currentCourseId;
            }
        }), currentlySelected);

        mList.setAdapter(adapter);

        if (mList.getOnItemClickListener() == null) {
            mList.setOnItemClickListener(new ListItemClickListener());
        }
    }

    public void updateView() {
        updateActionBarAdapter();

        DetailFragment fragment = getDetailFragment();
        if (fragment != null) {
            fragment.updateView();
        }
    }

    public ExamList getExams() {
        return exams;
    }

    public SlidingPaneLayout getSlidingLayout() {
        return mSlidingLayout;
    }

    public DetailFragment getDetailFragment() {
        Fragment currentFragment = getChildFragmentManager().findFragmentById(R.id.container);
        if (currentFragment != null && currentFragment instanceof DetailFragment)
            return (DetailFragment)currentFragment;

        return null;
    }
}
