package com.frca.vsexam;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Date;

/**
 * Created by KillerFrca on 5.10.13.
 */
public class ExamAdapter extends ArrayAdapter<String> {

    private ExamList exams;

    private static final int resourceLayout = R.layout.exam_list_item;
    private static final int displayField = R.id.layout;

    public ExamAdapter(Context context, ExamList exams) {
        super(context, resourceLayout);
        this.exams = exams;
    }

    @Override
    public int getCount() {
        return exams.size();
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        Exam exam = exams.get(position);

        View view;
        if (convertView != null)
            view = convertView;
        else {
            final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService (Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(resourceLayout, null);
        }

        TextView text1 = (TextView)view.findViewById(R.id.text1);       // code
        TextView text2 = (TextView)view.findViewById(R.id.text2);       // name
        TextView text3 = (TextView)view.findViewById(R.id.text3);       // date
        TextView text4 = (TextView)view.findViewById(R.id.text4);       // time

        text1.setText(exam.courseCode);
        text2.setText(exam.courseName);

        Date examDate = new Date(exam.examDate*1000L);
        text3.setText(Exam.DATE_FORMAT.format(examDate));
        text4.setText(Exam.TIME_FORMAT.format(examDate));

        return view;
    }
}
