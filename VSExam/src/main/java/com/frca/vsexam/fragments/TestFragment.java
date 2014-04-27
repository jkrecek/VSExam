package com.frca.vsexam.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.frca.vsexam.R;
import com.frca.vsexam.entities.vsedata.VSEStructure;
import com.frca.vsexam.entities.vsedata.VSEStructureParser;
import com.frca.vsexam.helper.Helper;
import com.google.gson.Gson;

public class TestFragment extends BaseFragment {

    private TextView textView;

    private String text = "";

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);

        if (!getMainActivity().isOnline()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle("No internet connection")
                .setMessage("To be able to use this app properly, you need to connect the device to the internet. Please do so and try this again.")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getMainActivity().finish();
                    }
                });

            builder.create().show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_test, container, false);

        textView = (TextView) rootView.findViewById(R.id.textView);

        textView.setMovementMethod(new ScrollingMovementMethod());

        getMainActivity().setActionBarAdapter(null);

        return rootView;
    }

    public static TestFragment instance;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        instance = this;

        VSEStructureParser.loadData(getActivity(), new VSEStructureParser.OnLoadedCallback() {
            @Override
            public void loaded(VSEStructure vseStructure) {
                String str = new Gson().toJson(vseStructure);
                Helper.appendLog(str);
                setMessage(str, Type.CURRENT);
                vseStructure.save(getActivity());
            }
        });


    }

    public static void postMessage(final String message, Type type) {
        if (instance != null)
            instance.setMessage(message, type);
    }

    public enum Type {
        ADD("green", "+"),
        CURRENT("yellow", "o"),
        REMOVE("red", "-");

        private String mColor;
        private String mPrepend;
        private Type(String color, String prepend) {
            mColor = color;
            mPrepend = prepend;
        }
    }

    public void setMessage(final String message, final Type type) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(text))
                    text += "<br/>";

                text += "<font color='" + type.mColor + "'><b>" + type.mPrepend + "</b> " + message + "</font>";

                textView.setText(Html.fromHtml(text), TextView.BufferType.SPANNABLE);
            }
        });
    }
}
