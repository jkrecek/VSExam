package com.frca.vsexam.fragments.classmate_detail;

import android.os.Bundle;

import com.frca.vsexam.entities.classmate.Classmate;
import com.frca.vsexam.entities.exam.Exam;
import com.frca.vsexam.fragments.base.ContentFragment;

public class ClassmateFragment extends ContentFragment {

    protected static final String EXTRA_EXAM_ID = "exam_id";

    private Classmate mClassmate;

    public ClassmateFragment() {
        super(ClassmateDataProvider.class, null, ClassmateButtonProvider.class);
    }

    public static ClassmateFragment newInstance(Exam exam, Classmate classmate) {
        ClassmateFragment f = new ClassmateFragment();
        Bundle bdl = new Bundle();
        bdl.putInt(EXTRA_EXAM_ID, exam.getId());
        bdl.putInt(EXTRA_ID, classmate.getId());
        f.setArguments(bdl);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int examId = getArguments().getInt(EXTRA_EXAM_ID);
        int id = getArguments().getInt(EXTRA_ID);
        Exam exam = getMainFragment().getExams().find(examId);
        mClassmate = exam.getClassmates().find(id);
    }

    @Override
    public void onResume() {
        super.onResume();

        getMainFragment().highlightExam(null);
    }

    @Override
    public String getTitle() {
        return mClassmate.getName();
    }

    public Classmate getClassmate() {
        return mClassmate;
    }
}
