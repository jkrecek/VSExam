package com.frca.vsexam.adapters;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.frca.vsexam.R;
import com.frca.vsexam.entities.exam.Exam;
import com.frca.vsexam.fragments.exam_detail.ExamDataProvider;
import com.frca.vsexam.helper.Utils;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ExamAdapter extends ArrayAdapter<String> {

    private List<Item> mItems = new ArrayList<Item>();
    private Exam selectedExam;

    private static final int resourceLayout = R.layout.exam_list_item;

    private LayoutInflater inflater;

    private SparseArray<View> mExistingViews = new SparseArray<View>();

    public static class Item {
        boolean mIsHeader;
        Exam mExam;
        Exam.Group mGroup;

        private Item(boolean isHeader, Exam exam, Exam.Group group) {
            mIsHeader = isHeader;
            mExam = exam;
            mGroup = group;
        }
    }

    public ExamAdapter(Context context) {
        super(context, resourceLayout);

        inflater = (LayoutInflater) getContext().getSystemService (Context.LAYOUT_INFLATER_SERVICE);
    }

    public void clear() {
        mItems.clear();
    }

    public void addHeader(Exam.Group group) {
        mItems.add(new Item(true, null, group));
    }

    public void addItem(Exam exam) {
        mItems.add(new Item(false, exam, null));
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View view = mExistingViews.get(position);
        if (view == null)
            view = createNewView(position);

        return view;
    }

    private View createNewView(int position) {
        Item item = getArrayItem(position);
        if (item == null) {
            return inflater.inflate(R.layout.list_header, null);
        } else if (item.mIsHeader) {
            TextView view = (TextView) inflater.inflate(R.layout.list_header, null);
            view.setText(item.mGroup.getTitleRes());
            return view;
        } else {
            View view = inflater.inflate(resourceLayout, null);
            if (view != null) {
                TextView text_day = (TextView)view.findViewById(R.id.text_day);
                TextView text_month = (TextView)view.findViewById(R.id.text_month);
                TextView text_code = (TextView)view.findViewById(R.id.text_code);
                TextView text_time = (TextView)view.findViewById(R.id.text_time);
                TextView text_name = (TextView)view.findViewById(R.id.text_name);
                TextView text_type = (TextView)view.findViewById(R.id.text_type);
                TextView text_capacity = (TextView)view.findViewById(R.id.text_capacity);

                Calendar examDate = Calendar.getInstance();
                examDate.setTime(item.mExam.getExamDate());
                text_day.setText(String.valueOf(examDate.get(Calendar.DAY_OF_MONTH)));
                text_month.setText(new DateFormatSymbols().getMonths()[examDate.get(Calendar.MONTH)]);

                text_code.setText(item.mExam.getCourseCode());
                text_time.setText(Utils.getDateOutput(item.mExam.getExamDate(), Utils.DateOutputType.TIME));
                text_name.setText(item.mExam.getCourseName());
                text_type.setText(item.mExam.getType());
                text_capacity.setText(String.valueOf(item.mExam.getCurrentCapacity()) + "/" + String.valueOf(item.mExam.getMaxCapacity()));
                text_capacity.setTextColor(getContext().getResources().getColor(
                    ExamDataProvider.getColorForCapacity(item.mExam.getCurrentCapacity(), item.mExam.getMaxCapacity())
                ));

                view.setTag(item.mExam.getId());
                if (item.mExam == selectedExam)
                    view.setBackgroundResource(R.color.highlighted);
            }

            return view;
        }
    }

    private Item getArrayItem(int position) {
        return position >= 0 && position < mItems.size() ? mItems.get(position) : null;
    }

    public Exam getExam(final int position) {
        Item item = getArrayItem(position);
        if (item != null)
            return item.mExam;
        else
            return null;
    }

    @Override
    public boolean isEnabled(int position) {
        return getExam(position) != null;
    }

    public void setHighlightedExam(Exam exam) {
        selectedExam = exam;

        if (!mItems.isEmpty())
            notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        mExistingViews.clear();
        super.notifyDataSetChanged();
    }
}
