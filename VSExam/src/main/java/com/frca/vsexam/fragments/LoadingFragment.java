package com.frca.vsexam.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.frca.vsexam.R;

/**
 * Created by KillerFrca on 11.10.13.
 */
public class LoadingFragment extends Fragment {

    private String message;

    private TextView messageField;

    public LoadingFragment(String message) {
        this.message = message;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_loading, container, false);
        messageField = (TextView) rootView.findViewById(R.id.textView);
        if (!TextUtils.isEmpty(message))
            setMessage(message);

        return rootView;
    }

    public void setMessage(String message) {
        if (message != null)
            this.message = message + Character.toString((char)0x85);

        // may be called before view is created
        if (messageField != null)
            messageField.setText(this.message);
    }
}