package com.frca.vsexam.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.frca.vsexam.Exam;
import com.frca.vsexam.ExamList;
import com.frca.vsexam.MainActivity;
import com.frca.vsexam.NoAuthException;
import com.frca.vsexam.R;
import com.frca.vsexam.network.HttpRequestBuilder;
import com.frca.vsexam.network.NetworkTask;
import com.frca.vsexam.network.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class LoadingFragment extends Fragment {

    private String message;

    private TextView messageField;

    public LoadingFragment(String message) {
        this.message = message;
    }

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);

        loadExams();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (!(getActivity() instanceof MainActivity)) {
            Log.e(getClass().getName(), "This class must be child of MainActivity");
            return null;
        }

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

    private MainActivity getMainActivity() {
        return (MainActivity)getActivity();
    }

    private void loadExams() {
        setMessage("Downloading exams");

        try {
            new NetworkTask(new NetworkTask.ResponseCallback() {

                @Override
                public void call(Response response) {
                    if (response.statusCode == 401) {
                        getMainActivity().setFragment(new LoginFragment());
                        Toast.makeText(getActivity(), "Invalid access", Toast.LENGTH_LONG).show();
                        return;
                    }

                    setMessage("Processing data");

                    Document doc = Jsoup.parse(response.http);
                    Elements elements = doc.body().select("table[id] tr");
                    List<String> spinnerStrings = new ArrayList<String>();

                    ExamList exams = new ExamList();
                    int group = 0;
                    for (Element element : elements) {
                        if (element.className().equals("zahlavi")) {
                            ++group;
                            spinnerStrings.add(String.valueOf(group));
                            continue;
                        }

                        Elements columns = element.select("td");
                        if (columns.size() <= 1)
                            continue;

                        exams.add(Exam.get(columns, group));
                    }

                    getMainActivity().setFragment(new BrowserPaneFragment(exams));

                }
            }).execute(new HttpRequestBuilder(getMainActivity().data, "student/terminy_seznam.pl").build());

        } catch (NoAuthException e) {
            e.printStackTrace();
        }
    }
}