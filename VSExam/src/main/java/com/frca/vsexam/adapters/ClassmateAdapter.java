package com.frca.vsexam.adapters;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.frca.vsexam.R;
import com.frca.vsexam.entities.classmate.Classmate;
import com.frca.vsexam.entities.classmate.ClassmateList;
import com.frca.vsexam.helper.Helper;
import com.frca.vsexam.network.tasks.BaseNetworkTask;
import com.frca.vsexam.network.tasks.UserImageNetworkTask;

public class ClassmateAdapter extends ArrayAdapter<String> {

    private ClassmateList classmates;

    private static final int resourceLayout = R.layout.exam_detail_classmates_item;

    private SparseArray<View> existingViews = new SparseArray<View>();

    private final LayoutInflater inflater;

    private OnClassmateClicked mClickCallback;

    public ClassmateAdapter(Context context, ClassmateList classmates, OnClassmateClicked clickCallback) {
        super(context, resourceLayout);
        this.classmates = classmates;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mClickCallback = clickCallback;
    }

    @Override
    public int getCount() {
        return classmates.size();
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view = existingViews.get(position);
        if (view == null) {
            final Classmate classmate = classmates.get(position);

            view = inflater.inflate(resourceLayout, null);
            if (view != null) {
                View imageHolder = view.findViewById(R.id.logo);
                TextView text1 = (TextView) view.findViewById(R.id.text1);
                TextView text2 = (TextView) view.findViewById(R.id.text2);
                TextView text3 = (TextView) view.findViewById(R.id.text3);

                BaseNetworkTask.run(new UserImageNetworkTask(getContext(), classmate.getId(), imageHolder));

                text1.setText(classmate.getName());
                text2.setText(Helper.getDateOutput(classmate.getRegistered(), Helper.DateOutputType.DATE));
                text3.setText(Helper.getDateOutput(classmate.getRegistered(), Helper.DateOutputType.TIME));

                view.setTag(classmate);

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mClickCallback != null)
                            mClickCallback.onClick(classmate);
                    }
                });

                existingViews.put(position, view);
            }
        }

        return view;
    }

    public interface OnClassmateClicked {
        abstract void onClick(Classmate classmate);
    }
}
