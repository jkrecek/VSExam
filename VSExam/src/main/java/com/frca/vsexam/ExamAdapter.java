package com.frca.vsexam;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by KillerFrca on 5.10.13.
 */
public class ExamAdapter extends ArrayAdapter<String> {

    private ExamList exams;

    private static final int resourceLayout = R.layout.exam_list_item;
    private static final int displayField = R.id.text_title;

    public ExamAdapter(Context context, ExamList exams) {
        super(context, resourceLayout, displayField, exams.getCourseName() );
        this.exams = exams;
    }

    @Override
    public int getCount() {
        return exams.size();
    }


    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        /*Exam exam = exams.get(position);

        View itemView;
        if (convertView != null)
            itemView = convertView;
        else {
            final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService (Context.LAYOUT_INFLATER_SERVICE);

            itemView = inflater.inflate(resourceLayout, null);
        }*/
        View view = super.getView(position, convertView, parent);
        //view.findViewById(displayField).setOnClickListener(new OnExamClickListener());
        //view.setOnClickListener(new OnExamClickListener());
        //view.findViewById(displayField).setTag(position);
        //itemView



        return view;
    }

    public class OnExamClickListener implements AdapterView.OnItemClickListener {
    //public class OnExamClickListener implements View.OnClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            /*Log.e("AA", "call");
            Exam exam = ExamAdapter.this.exams.get(i);
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

            LinearLayout layoutInner = (LinearLayout) view.findViewById(R.id.layout_inner);
            final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService (Context.LAYOUT_INFLATER_SERVICE);

            if (layoutInner.getChildCount() == 0)
                layoutInner.addView(inflater.inflate(R.layout.exam_list_details, null));

            ((TextView)layoutInner.findViewById(R.id.text_deeper)).setText(str);
            layoutInner.findViewById(R.id.text_deeper).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((View)view.getParent()).setVisibility(View.GONE);
                }
            });
            layoutInner.setVisibility(View.VISIBLE);*/
        }

        /*@Override
        public void onClick(View view) {
            Exam exam = exams.get((Integer)view.getTag());
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

            LinearLayout layoutInner = (LinearLayout) ((View)view.getParent()).findViewById(R.id.layout_inner);
            if (layoutInner.getVisibility() == View.GONE) {
                if (layoutInner.getChildCount() == 0) {
                    final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService (Context.LAYOUT_INFLATER_SERVICE);
                    layoutInner.addView(inflater.inflate(R.layout.exam_list_details, null));
                }
            }

            ((TextView)layoutInner.findViewById(R.id.text_deeper)).setText(str);
            /*layoutInner.findViewById(R.id.text_deeper).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((View)view.getParent()).setVisibility(View.GONE);
                }
            });*//*
            layoutInner.setVisibility(layoutInner.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        }*/
    }


}
