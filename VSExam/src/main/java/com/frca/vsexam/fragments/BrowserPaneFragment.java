package com.frca.vsexam.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.frca.vsexam.R;
import com.frca.vsexam.adapters.ExamAdapter;
import com.frca.vsexam.entities.base.Exam;
import com.frca.vsexam.entities.lists.ExamList;
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

    private View lastHighlighted;

    private ExamAdapter adapter;

    private AppSparseArray<String> adapterData;

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
                mActionBar.setTitle("Exams");
                getMainActivity().setActionBarAdapter(exams.getCourseNames());
            }

            @Override
            public void onPanelClosed(View view) {

            }
        });
        mSlidingLayout.openPane();
        //mSlidingLayout.setCoveredFadeColor(0x000000);
        mSlidingLayout.setSliderFadeColor(0x66cccccc);
        mSlidingLayout.setShadowResource(R.drawable.border_right);

        setAdapter();

        return rootView;
    }

    public SlidingPaneLayout getSlidingLayout() {
        return mSlidingLayout;
    }

    private class ListItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Exam exam = adapter.getExam(position);
            if (exam == null)
                return;

            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.container, new DetailFragment(exam));
            transaction.addToBackStack(null);
            transaction.commit();

            if (mSlidingLayout.isSlideable()) {
                mActionBar.setTitle(exam.getCourseCode() + " | " + exam.getCourseName());
                getMainActivity().setActionBarAdapter(null);
                mActionBar.setDisplayHomeAsUpEnabled(true);

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mSlidingLayout.closePane();
                    }
                });
            } else {
                if (lastHighlighted != null) {
                    lastHighlighted.setBackgroundResource(R.color.standard_grey);
                    /*lastHighlighted.findViewById(R.id.layout_datetime).setBackgroundResource(0);*/
                }

                view.setBackgroundResource(R.color.white);
                /*view.findViewById(R.id.layout_datetime).setBackgroundResource(R.drawable.invert_arrow_right_pos_right);*/
                lastHighlighted = view;
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

    public ExamList getExams() {
        return exams;
    }

    public ArrayAdapter getAdapter() {
        return (ArrayAdapter) mList.getAdapter();
    }

    private void setAdapter() {
        adapterData = exams.getCourses();
        adapterData.put(-1, "Zobrazit všechny");
        getMainActivity().setActionBarAdapter(adapterData.getValues());
    }

    @Override
    public void onNavigationItemSelected(final int id) {
        final int courseId = adapterData.keyAt(id);

        adapter = new ExamAdapter(getActivity(), exams.filter(new ExamList.MatchChecker() {
            @Override
            public boolean isMatch(Exam exam) {
                return courseId == -1 || exam.getCourseId() == courseId;
            }
        }));

        mList.setAdapter(adapter);

        if (mList.getOnItemClickListener() == null) {
            mList.setOnItemClickListener(new ListItemClickListener());
        }
   }
}
