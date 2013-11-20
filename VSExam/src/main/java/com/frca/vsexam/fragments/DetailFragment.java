package com.frca.vsexam.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.frca.vsexam.R;
import com.frca.vsexam.adapters.ClassmateAdapter;
import com.frca.vsexam.entities.base.Exam;
import com.frca.vsexam.entities.lists.ClassmateList;
import com.frca.vsexam.exceptions.NoAuthException;
import com.frca.vsexam.helper.DataHolder;
import com.frca.vsexam.helper.Helper;
import com.frca.vsexam.network.HttpRequestBuilder;
import com.frca.vsexam.network.Response;
import com.frca.vsexam.network.tasks.BaseNetworkTask;
import com.frca.vsexam.network.tasks.TextNetworkTask;
import com.frca.vsexam.network.tasks.UserImageNetworkTask;

import org.apache.http.client.methods.HttpRequestBase;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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

        if (!(getParentFragment() instanceof BrowserPaneFragment)) {
            Log.e(getClass().getName(), "This class must be child of BrowserPaneFramgnet");
            getActivity().finish();
        }

        inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.exam_list_details, container, false);

        TextView text_courseCode = ((TextView)view.findViewById(R.id.text_courseCode));
        TextView text_courseName = ((TextView)view.findViewById(R.id.text_courseName));
        TextView text_authorName = ((TextView)view.findViewById(R.id.text_authorName));
        TextView text_type = ((TextView)view.findViewById(R.id.text_type));
        TextView text_isRegistered = ((TextView)view.findViewById(R.id.text_isRegistered));
        TextView text_capacity = ((TextView)view.findViewById(R.id.text_capacity));
        TextView text_examDate = ((TextView)view.findViewById(R.id.text_examDate));
        TextView text_registerStart = ((TextView)view.findViewById(R.id.text_registerStart));
        TextView text_registerEnd = ((TextView)view.findViewById(R.id.text_registerEnd));
        TextView text_unregisterEnd = ((TextView)view.findViewById(R.id.text_unregisterEnd));
        View button_author = view.findViewById(R.id.button_author);
        View logo_author = view.findViewById(R.id.logo_author);
        View button_left = view.findViewById(R.id.button_left);
        View button_right = view.findViewById(R.id.button_right);

        text_courseCode.setText(exam.getCourseCode());
        text_courseName.setText(exam.getCourseName());
        text_authorName.setText(exam.getAuthorName());
        text_type.setText(exam.getType());
        text_isRegistered.setText(exam.isRegistered() ? R.string.registered : R.string.unregistered);
        text_capacity.setText(String.valueOf(exam.getCurrentCapacity()) + "/" + String.valueOf(exam.getMaxCapacity()));
        text_examDate.setText(Helper.getDateOutput(exam.getExamDate(), Helper.DateOutputType.DATE_TIME));
        text_registerStart.setText(Helper.getDateOutput(exam.getRegisterStart(), Helper.DateOutputType.DATE_TIME));
        text_registerEnd.setText(Helper.getDateOutput(exam.getRegisterEnd(), Helper.DateOutputType.DATE_TIME));
        text_unregisterEnd.setText(Helper.getDateOutput(exam.getUnregisterEnd(), Helper.DateOutputType.DATE_TIME));

        button_author.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String authorUrl = HttpRequestBuilder.completeURLString("lide/clovek.pl?id=" + String.valueOf(exam.getAuthorId()));
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(authorUrl));
                startActivity(browserIntent);
            }
        });

        BaseNetworkTask.run(new UserImageNetworkTask(getActivity(), exam.getAuthorId(), logo_author));

        button_left.setOnClickListener(new OnRegisterOnTimeClick());
        button_right.setOnClickListener(new OnRegisterClick());

        if (exam.getCurrentCapacity() != 0)
            loadClassmates();
        else {
            View left_layout = view.findViewById(R.id.layout_detail_left);
            View right_layout = view.findViewById(R.id.layout_detail_right);
            right_layout.setVisibility(View.GONE);
            LinearLayout.LayoutParams left_params = (LinearLayout.LayoutParams)left_layout.getLayoutParams();
            ((LinearLayout)left_layout.getParent()).setGravity(Gravity.CENTER_HORIZONTAL);
            left_params.weight = 0;
            left_params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 500, getResources().getDisplayMetrics());
        }

        View returnView = view;
        view = null;

        return returnView;
    }

    private void loadClassmates() {

        if (Helper.isValid(exam.getClassmates())) {
            onClassmatesLoaded(exam.getClassmates());
            return;
        }

         BaseNetworkTask.run(
            new TextNetworkTask(
                getActivity(), "student/terminy_info.pl?termin=" + exam.getId() + ";spoluzaci=1;studium=" + exam.getStudyId() + ";obdobi=" + exam.getPeriodId(),
                new TextNetworkTask.ResponseCallback() {

                    @Override
                    public void onSuccess(Response response) {
                        if (response.getStatusCode() == 401) {
                            Toast.makeText(getActivity(), "Access denied", Toast.LENGTH_LONG).show();
                            return;
                        }

                        Document doc = Jsoup.parse(response.getText());
                        Elements elements = doc.body().select("table#studenti tbody tr");

                        ClassmateList classmates = new ClassmateList(elements);

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

    private class OnRegisterClick implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            HttpRequestBase requestBase = HttpRequestBuilder.getRegisterRequest(DataHolder.getInstance(getActivity()), exam, true);
            BaseNetworkTask.run(new TextNetworkTask(getActivity(), requestBase, new BaseNetworkTask.ResponseCallback() {
                @Override
                public void onSuccess(Response response) {
                    Toast.makeText(getActivity(), "Registrace! " + String.valueOf(response.getStatusCode()), Toast.LENGTH_LONG).show();
                    BrowserPaneFragment browserPaneFragment = (BrowserPaneFragment) getParentFragment();
                    if (true)
                        browserPaneFragment.getExams().setExamRegister(exam, true, browserPaneFragment.getAdapter());
                }
            }));
        }
    }

    private class OnDeRegisterClick implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            HttpRequestBase requestBase = HttpRequestBuilder.getRegisterRequest(DataHolder.getInstance(getActivity()), exam, true);
            BaseNetworkTask.run(new TextNetworkTask(getActivity(), requestBase, new BaseNetworkTask.ResponseCallback() {
                @Override
                public void onSuccess(Response response) {
                    Toast.makeText(getActivity(), "Unregistrace! " + String.valueOf(response.getStatusCode()), Toast.LENGTH_LONG).show();
                    BrowserPaneFragment browserPaneFragment = (BrowserPaneFragment) getParentFragment();
                    if (true)
                        browserPaneFragment.getExams().setExamRegister(exam, true, browserPaneFragment.getAdapter());
                }
            }));
        }
    }

    private class OnRegisterOnTimeClick implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            exam.setRegisterOnTime(getActivity(), true);
        }
    }
}