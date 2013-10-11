package com.frca.vsexam;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Created by KillerFrca on 11.10.13.
 */
public class ListFragment extends Fragment {

    private ExamList examList;

    public ListFragment(ExamList examList) {
        this.examList = examList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.exam_list, container, false);
        ExamAdapter adapter = new ExamAdapter(getActivity(), examList);
        ListView lv = (ListView)rootView.findViewById(R.id.list_view);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(adapter.new OnExamClickListener());
        //view.setOnClickListener(adapter.new OnExamClickListener());

        return rootView;
    }
}