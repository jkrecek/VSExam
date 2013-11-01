package com.frca.vsexam.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.frca.vsexam.R;
import com.frca.vsexam.entities.base.Exam;
import com.frca.vsexam.entities.lists.ExamList;
import com.frca.vsexam.helper.Helper;

/**
 * Created by KillerFrca on 5.10.13.
 */
public class ExamAdapter extends ArrayAdapter<String> {

    private ExamList exams;

    private static final int resourceLayout = R.layout.exam_list_item;
    private static final int displayField = R.id.layout;
    private LayoutInflater inflater;

    private SparseArray<View> existingViews = new SparseArray<View>();

    public ExamAdapter(Context context, ExamList exams) {
        super(context, resourceLayout);
        this.exams = exams;
        inflater = (LayoutInflater) getContext().getSystemService (Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return exams.getAdapterSize();
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View view = existingViews.get(position);
        if (view == null) {
            Object object = exams.getExamOrVoid(position);
            if (object == null)
                view = inflater.inflate(R.layout.list_header, null);
            else if (object instanceof Exam.Group) {
                Exam.Group group = (Exam.Group) object;
                view = inflater.inflate(R.layout.list_header, null);
                ((TextView)view).setText(group.getTitleRes());
            }
            if (object instanceof Exam) {
                Exam exam = (Exam) object;
                /*if (convertView != null && convertView instanceof LinearLayout)
                    view = convertView;
                else*/
                    view = inflater.inflate(resourceLayout, null);

                TextView text1 = (TextView)view.findViewById(R.id.text1);       // code
                TextView text2 = (TextView)view.findViewById(R.id.text2);       // name
                TextView text3 = (TextView)view.findViewById(R.id.text3);       // date
                TextView text4 = (TextView)view.findViewById(R.id.text4);       // time

                String pre = "N";
                if (exam.getGroup() == Exam.Group.IS_REGISTERED)
                    pre = "R";

                text1.setText(pre+exam.getCourseCode());
                text2.setText(exam.getCourseName());

                if (TextUtils.isEmpty(exam.getCourseName()))
                    text2.setText(String.valueOf(exam.getGroup()));

                text3.setText(Helper.getDateOutput(exam.getExamDate(), Helper.DateOutputType.DATE));
                text4.setText(Helper.getDateOutput(exam.getExamDate(), Helper.DateOutputType.TIME));
            }
        }

        return view;
    }

    public Exam getExam(final int position) {
        Object obj = exams.getExamOrVoid(position);
        if (obj instanceof Exam)
            return (Exam)obj;
        else
            return null;
    }

    @Override
    public boolean isEnabled(int position) {
        return exams.getExamOrVoid(position) instanceof Exam;
    }

    @Override
    public void notifyDataSetChanged() {
        existingViews.clear();
        super.notifyDataSetChanged();
    }
}
