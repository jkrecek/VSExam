package com.frca.vsexam.adapters;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.frca.vsexam.R;
import com.frca.vsexam.entities.Classmate;
import com.frca.vsexam.entities.ClassmateList;
import com.frca.vsexam.helper.Dialog;
import com.frca.vsexam.helper.Helper;
import com.frca.vsexam.network.tasks.BaseNetworkTask;
import com.frca.vsexam.network.tasks.UserImageNetworkTask;

public class ClassmateAdapter extends ArrayAdapter<String> {

    private ClassmateList classmates;

    private static final int resourceLayout = R.layout.classmates_item;

    private SparseArray<View> existingViews = new SparseArray<View>();

    private final LayoutInflater inflater;

    public ClassmateAdapter(Context context, ClassmateList classmates) {
        super(context, resourceLayout);
        this.classmates = classmates;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

            View imageHolder = view.findViewById(R.id.logo);                // logo
            TextView text1 = (TextView)view.findViewById(R.id.text1);       // name
            TextView text2 = (TextView)view.findViewById(R.id.text2);       // date
            TextView text3 = (TextView)view.findViewById(R.id.text3);       // time

            BaseNetworkTask.run(new UserImageNetworkTask(getContext(), classmate.id, imageHolder));

            text1.setText(classmate.name);
            text2.setText(Helper.getDateOutput(classmate.registered, Helper.DateOutputType.DATE));
            text3.setText(Helper.getDateOutput(classmate.registered, Helper.DateOutputType.TIME));

            view.setTag(classmate);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Dialog.ClassmateDetails(getContext(), classmate.id, classmate.name, classmate.identification);
                }
            });

            existingViews.put(position, view);
        }

        return view;
    }
}
