package com.frca.vsexam.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.frca.vsexam.R;
import com.frca.vsexam.adapters.ClassmateAdapter;
import com.frca.vsexam.entities.Classmate;
import com.frca.vsexam.entities.ClassmateList;
import com.frca.vsexam.entities.Exam;
import com.frca.vsexam.exceptions.NoAuthException;
import com.frca.vsexam.helper.Helper;
import com.frca.vsexam.network.HttpRequestBuilder;
import com.frca.vsexam.network.Response;
import com.frca.vsexam.network.tasks.BaseNetworkTask;
import com.frca.vsexam.network.tasks.TextNetworkTask;
import com.frca.vsexam.network.tasks.UserImageNetworkTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class DetailFragment extends BaseFragment {

    private final Exam exam;

    private View view;

    private LayoutInflater inflater;

    public DetailFragment(Exam exam) {
        this.exam = exam;
    }

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);

        inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.exam_list_details, container, false);
        ((TextView)view.findViewById(R.id.text_courseCode)).setText(exam.courseCode);
        ((TextView)view.findViewById(R.id.text_courseName)).setText(exam.courseName);
        ((TextView)view.findViewById(R.id.text_authorName)).setText(exam.authorName);
        ((TextView)view.findViewById(R.id.text_type)).setText(exam.type);
        ((TextView)view.findViewById(R.id.text_isRegistered)).setText(exam.isRegistered ? "Registrován" : "Neregistrován");
        ((TextView)view.findViewById(R.id.text_capacity)).setText(String.valueOf(exam.currentCapacity)+"/"+String.valueOf(exam.maxCapacity));
        ((TextView)view.findViewById(R.id.text_examDate)).setText(Helper.getDateOutput(exam.examDate, Helper.DateOutputType.DATE_TIME));
        ((TextView)view.findViewById(R.id.text_registerStart)).setText(Helper.getDateOutput(exam.registerStart, Helper.DateOutputType.DATE_TIME));
        ((TextView)view.findViewById(R.id.text_registerEnd)).setText(Helper.getDateOutput(exam.registerEnd, Helper.DateOutputType.DATE_TIME));
        ((TextView)view.findViewById(R.id.text_unregisterEnd)).setText(Helper.getDateOutput(exam.unregisterEnd, Helper.DateOutputType.DATE_TIME));


        view.findViewById(R.id.button_author).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String authorUrl = HttpRequestBuilder.completeURLString("lide/clovek.pl?id=" + String.valueOf(exam.authorId));
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(authorUrl));
                startActivity(browserIntent);
            }
        });

        BaseNetworkTask.run(new UserImageNetworkTask(getActivity(), exam.authorId, view.findViewById(R.id.logo_author)));

        getClassmates();

        View returnView = view;
        view = null;

        return returnView;
    }

    private void getClassmates() {

        if (Helper.isValid(exam.getClassmates())) {
            onClassmatesLoaded(exam.getClassmates());
            return;
        }

         BaseNetworkTask.run(
            new TextNetworkTask(
                getActivity(),"student/terminy_info.pl?termin=" + exam.id + ";spoluzaci=1;studium=" + exam.studyId + ";obdobi=" + exam.periodId,
                new TextNetworkTask.ResponseCallback() {

                    @Override
                    public void onSuccess(Response response) {
                        if (response.getStatusCode() == 401) {
                            Toast.makeText(getActivity(), "Access denied", Toast.LENGTH_LONG).show();
                            return;
                        }

                        Document doc = Jsoup.parse(response.getText());
                        Elements elements = doc.body().select("table#studenti tbody tr");

                        ClassmateList classmates = new ClassmateList();

                        for (Element element : elements) {
                            Elements columns = element.select("td");
                            if (columns.size() <= 1)
                                continue;

                            classmates.add(Classmate.get(columns));
                        }

                        exam.setClassmates(classmates);
                        if (getView() != null)
                            onClassmatesLoaded(classmates);

                    }
                }, new BaseNetworkTask.ExceptionCallback() {
                    @Override
                    public void onException(Exception e) {
                        if (e instanceof NoAuthException) {
                            Toast.makeText(getActivity(), "No auth data set.", Toast.LENGTH_LONG).show();
                            getMainActivity().setFragment(new LoginFragment());
                        }
                    }
            }));
    }

    private void onClassmatesLoaded(ClassmateList classmates) {
        ClassmateAdapter classmateAdapter = new ClassmateAdapter(getActivity(), classmates);
        View classmateProgress = getView().findViewById(R.id.classmates_progress);

        LinearLayout classmateLayout = (LinearLayout) getView().findViewById(R.id.layout_classmates);

        // temp solution
        for (int i = 0; i < classmateAdapter.getCount(); ++i) {
            View view = classmateAdapter.getView(i, null, null);
            classmateLayout.addView(view);

            if (i != classmateAdapter.getCount() - 1)
                classmateLayout.addView(Helper.getDivider(inflater, Helper.Orientation.VERTICAL));
        }

        classmateProgress.setVisibility(View.GONE);
        classmateLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public View getView() {
        if (view != null)
            return view;
        else
            return super.getView();

    }
}