package com.frca.vsexam.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frca.vsexam.Exam;
import com.frca.vsexam.R;

/**
 * Created by KillerFrca on 11.10.13.
 */
public class DetailFragment extends Fragment {

    private Exam exam;

    public DetailFragment(Exam exam) {
        this.exam = exam;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.exam_list_details, container, false);
        return rootView;
    }
}