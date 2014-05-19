package com.frca.vsexam.fragments.exam_detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.frca.vsexam.entities.exam.Exam;
import com.frca.vsexam.fragments.base.ContentFragment;
import com.frca.vsexam.helper.ViewProvider;


public class DetailFragment extends ContentFragment {

    private Exam mExam;

    public DetailFragment() {
        super(ExamDataProvider.class, ExamClassmatesProvider.class, ExamButtonProvider.class);
    }

    public static DetailFragment newInstance(Exam exam) {
        DetailFragment f = new DetailFragment();
        Bundle bdl = new Bundle();
        bdl.putInt(EXTRA_ID, exam.getId());
        f.setArguments(bdl);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int id = getArguments().getInt(EXTRA_ID);
        mExam = getMainFragment().getExams().find(id);
    }

    @Override
    public void onResume() {
        super.onResume();

        getMainFragment().highlightExam(mExam);
    }

    @Override
    public String getTitle() {
        return mExam.getCourseCode() + " | " + mExam.getCourseName();
    }

    public Exam getExam() {
        return mExam;
    }

    public abstract static class BaseExamProvider extends ViewProvider {

        protected Exam mExam;

        public BaseExamProvider(DetailFragment baseFragment, ViewGroup parent, LayoutInflater inflater, int resourceId) {
            super(baseFragment, parent, inflater, resourceId);

            mExam = baseFragment.getExam();
        }
    }
}