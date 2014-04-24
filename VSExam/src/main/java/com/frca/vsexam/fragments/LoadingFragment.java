package com.frca.vsexam.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.frca.vsexam.R;
import com.frca.vsexam.entities.exam.ExamList;
import com.frca.vsexam.exceptions.NoAuthException;
import com.frca.vsexam.helper.AppConfig;
import com.frca.vsexam.network.Response;
import com.frca.vsexam.network.tasks.BaseNetworkTask;
import com.frca.vsexam.network.tasks.TextNetworkTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class LoadingFragment extends BaseFragment {

    private String message;

    private TextView messageField;

    private static ProgressBar progressBar;

    public LoadingFragment() {

    }

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
        loadExams();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.layout_loading, container, false);
        messageField = (TextView) rootView.findViewById(R.id.textView);
        if (!TextUtils.isEmpty(message))
            setMessage(message);

        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        getMainActivity().setActionBarAdapter(null);

        return rootView;
    }

    public void setMessage(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (message != null)
                    LoadingFragment.this.message = message + Character.toString((char)0x85);

                // may be called before view is created
                if (messageField != null)
                    messageField.setText(LoadingFragment.this.message);
            }
        });
    }

    private void loadExams() {
        setMessage("Downloading exams");

        BaseNetworkTask.run(
            new TextNetworkTask(
                getActivity(), "student/terminy_seznam.pl",
                new TextNetworkTask.ResponseCallback() {

                    @Override
                    public void onSuccess(Response response) {
                        if (!response.isValid()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Error").setMessage("Error while accesing exam data, do you want to try again?")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        getMainActivity().setFragment(new LoadingFragment());
                                    }
                                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).create().show();
                            return;
                        }

                        if (response.getStatusCode() == 401) {
                            getMainActivity().setFragment(new LoginFragment());
                            Toast.makeText(getActivity(), "Invalid access", Toast.LENGTH_LONG).show();
                            return;
                        }

                        setMessage("Processing data");

                        Document doc = Jsoup.parse(response.getText());
                        Elements elements = doc.body().select("table[id] tr");

                        ExamList exams = new ExamList();
                        if (AppConfig.USE_TEST_EXAMS)
                            exams.addTestExams(getActivity());
                        else
                            exams.parseAndAdd(getActivity(), elements);

                        getMainActivity().setFragment(new BrowserPaneFragment(exams));
                    }

                },
                new BaseNetworkTask.ExceptionCallback() {
                    @Override
                    public void onException(Exception e) {
                        if (e instanceof NoAuthException) {
                            Toast.makeText(getActivity(), "No auth data set.", Toast.LENGTH_LONG).show();
                            getMainActivity().setFragment(new LoginFragment());
                        }
                    }
                }
        ));
    }
}