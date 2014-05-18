package com.frca.vsexam.fragments.classmate_detail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frca.vsexam.R;
import com.frca.vsexam.helper.ViewProvider;

public class ClassmateButtonProvider extends ViewProvider {

    public ClassmateButtonProvider(ClassmateFragment baseFragment, ViewGroup parent, LayoutInflater inflater) {
        super(baseFragment, parent, inflater, R.layout.classmate_detail_buttons);
    }

    @Override
    public Result doLoad() {
        findViewById(R.id.button_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getMainFragment().onBackPressed();
            }
        });

        return Result.DONE;
    }


}
