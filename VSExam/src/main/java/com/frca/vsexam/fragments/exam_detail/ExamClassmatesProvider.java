package com.frca.vsexam.fragments.exam_detail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.frca.vsexam.R;
import com.frca.vsexam.adapters.ClassmateAdapter;
import com.frca.vsexam.entities.classmate.Classmate;
import com.frca.vsexam.entities.classmate.ClassmateList;
import com.frca.vsexam.fragments.classmate_detail.ClassmateFragment;
import com.frca.vsexam.helper.Helper;
import com.frca.vsexam.network.Response;
import com.frca.vsexam.network.tasks.BaseNetworkTask;
import com.frca.vsexam.network.tasks.TextNetworkTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ExamClassmatesProvider extends DetailFragment.BaseExamProvider {

    public ExamClassmatesProvider(DetailFragment baseFragment, ViewGroup parent, LayoutInflater inflater) {
        super(baseFragment, parent, inflater, R.layout.exam_detail_classmates);
    }

    @Override
    public Result doLoad() {
        if (mExam.getCurrentCapacity() == 0) {
            return Result.HIDE;
        } else if (Helper.isValid(mExam.getClassmates())) {
            setUpView();
            return Result.DONE;
        } else {
            loadData();
            return Result.DELAYED;
        }
    }

    private void loadData() {
        BaseNetworkTask.run(
            new TextNetworkTask(
                getContext(), "student/terminy_info.pl?termin=" + mExam.getId() + ";spoluzaci=1;studium=" + mExam.getStudyId() + ";obdobi=" + mExam.getPeriodId(),
                new TextNetworkTask.ResponseCallback() {

                    @Override
                    public void onSuccess(Response response) {
                        if (response.getStatusCode() == 401) {
                            Toast.makeText(getContext(), R.string.access_denied, Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (!response.isComplete()) {
                            return;
                        }

                        Document doc = Jsoup.parse(response.getText());
                        Elements elements = doc.body().select("table#studenti tbody tr");

                        ClassmateList classmates = new ClassmateList(elements);

                        mExam.setClassmates(classmates);
                        load();
                    }
                },
                null
            )
        );
    }

    private void setUpView() {

        if (getContext() == null)
            return;

        ClassmateAdapter classmateAdapter = new ClassmateAdapter(getContext(), mExam.getClassmates(), new ClassmateAdapter.OnClassmateClicked() {
            @Override
            public void onClick(Classmate classmate) {
                getMainFragment().replaceFragment(ClassmateFragment.newInstance(mExam, classmate));
            }
        });

        LinearLayout classmateLayout = (LinearLayout) findViewById(R.id.layout_classmates);

        for (int i = 0; i < classmateAdapter.getCount(); ++i) {
            View view = classmateAdapter.getView(i, null, null);
            classmateLayout.addView(view);

            if (i != classmateAdapter.getCount() - 1)
                classmateLayout.addView(Helper.getDivider(getInflater(), Helper.Orientation.VERTICAL));

            classmateLayout.setVisibility(View.VISIBLE);
        }
    }
}
