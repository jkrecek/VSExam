package com.frca.vsexam.adapters;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.frca.vsexam.R;
import com.frca.vsexam.entities.exam.Exam;
import com.frca.vsexam.entities.exam.ExamList;
import com.frca.vsexam.helper.Helper;

public class ExamAdapter extends ArrayAdapter<String> {

    private ExamList exams;
    private Exam selectedExam;

    private static final int resourceLayout = R.layout.exam_list_item;
    private static final int displayField = R.id.layout;
    private LayoutInflater inflater;

    private SparseArray<View> existingViews = new SparseArray<View>();

    public ExamAdapter(Context context, ExamList exams, Exam selectedExam) {
        super(context, resourceLayout);
        this.exams = exams;
        inflater = (LayoutInflater) getContext().getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        this.selectedExam = selectedExam;
    }

    @Override
    public int getCount() {
        return exams.getAdapterSize();
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View view = existingViews.get(position);
        if (view == null)
            view = createCustomView(position);

        return view;
    }

    private View createCustomView(int position) {
        Object object = exams.getFromAdapter(position);
        if (object == null) {
            return inflater.inflate(R.layout.list_header, null);
        } else if (object instanceof Exam.Group) {
            Exam.Group group = (Exam.Group) object;
            View view = inflater.inflate(R.layout.list_header, null);
            ((TextView)view).setText(group.getTitleRes());
            return view;
        } else if (object instanceof Exam) {
            Exam exam = (Exam) object;
            View view = inflater.inflate(resourceLayout, null);

            TextView text1 = (TextView)view.findViewById(R.id.text1);       // code
            TextView text2 = (TextView)view.findViewById(R.id.text2);       // name
            TextView text3 = (TextView)view.findViewById(R.id.text3);       // date
            TextView text4 = (TextView)view.findViewById(R.id.text4);       // time

            text1.setText(exam.getCourseCode());
            text2.setText(exam.getCourseName());
            text3.setText(Helper.getDateOutput(exam.getExamDate(), Helper.DateOutputType.DATE));
            text4.setText(Helper.getDateOutput(exam.getExamDate(), Helper.DateOutputType.TIME));

            if (selectedExam == exam)
                highlightView(view, true);

            return view;
        }

        return null;
    }

    public Exam getExam(final int position) {
        Object obj = exams.getFromAdapter(position);
        if (obj instanceof Exam)
            return (Exam)obj;
        else
            return null;
    }

    @Override
    public boolean isEnabled(int position) {
        return exams.getFromAdapter(position) instanceof Exam;
    }

    public View getViewForExam(Exam exam, ListView listView) {
        if (exam == null)
            return null;

        int position = exams.getAdapterPosition(exam);
        return listView.getChildAt(position);
    }

    public void highlightExam(Exam exam, ListView listView, boolean apply) {
        if (apply)
            selectedExam = exam;

        highlightView(getViewForExam(exam, listView), apply);
    }

    private void highlightView(View view, boolean apply) {
        if (view == null)
            return;

        if (apply) {
            view.setBackgroundResource(R.color.white);
            /*view.findViewById(R.id.layout_datetime).setBackgroundResource(R.drawable.invert_arrow_right_pos_right);*/
        } else {
            view.setBackgroundResource(R.color.standard_grey);
            /*viewfindViewById(R.id.layout_datetime).setBackgroundResource(0);*/
        }
    }

    /*@Override
    public void notifyDataSetChanged() {
        existingViews.clear();
        super.notifyDataSetChanged();
    }*/
}
