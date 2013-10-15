package com.frca.vsexam.fragments;

import android.os.Bundle;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.frca.vsexam.Exam;
import com.frca.vsexam.ExamAdapter;
import com.frca.vsexam.ExamList;
import com.frca.vsexam.R;

import java.lang.reflect.Field;
import java.util.Date;

/**
 * Created by KillerFrca on 11.10.13.
 */
public class BrowserPaneFragment extends MainActivityFragment {

    private ExamList exams;

    private SlidingPaneLayout mSlidingLayout;

    private ListView mList;

    private TextView mContent;

    private ActionBar mActionBar;

    public BrowserPaneFragment(ExamList exams) {
        this.exams = exams;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mSlidingLayout = (SlidingPaneLayout) rootView.findViewById(R.id.sliding_pane);
        mList = (ListView) rootView.findViewById(R.id.left_pane);
        mContent = (TextView) rootView.findViewById(R.id.content_text);
        mActionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();

        mSlidingLayout.setPanelSlideListener(new SlidingPaneLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View view, float v) {

            }

            @Override
            public void onPanelOpened(View view) {
                mActionBar.setTitle("Exams");
                mActionBar.setDisplayHomeAsUpEnabled(false);
            }

            @Override
            public void onPanelClosed(View view) {

            }
        });
        mSlidingLayout.openPane();
        //mSlidingLayout.setCoveredFadeColor(0x000000);
        mSlidingLayout.setSliderFadeColor(0x66cccccc);
        mSlidingLayout.setShadowResource(R.drawable.border_right);

        mList.setAdapter(new ExamAdapter(getActivity(), exams));
        mList.setOnItemClickListener(new ListItemClickListener());

        return rootView;
    }

    public SlidingPaneLayout getSlidingLayout() {
        return mSlidingLayout;
    }

    private class ListItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Exam exam = exams.get(position);
            String str = "";
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

            mContent.setText(str);
            mActionBar.setTitle(exam.courseCode);
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mSlidingLayout.closePane();
        }
    }
}
