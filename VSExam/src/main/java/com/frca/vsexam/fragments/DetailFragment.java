package com.frca.vsexam.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.frca.vsexam.NoAuthException;
import com.frca.vsexam.R;
import com.frca.vsexam.entities.Classmate;
import com.frca.vsexam.entities.ClassmateList;
import com.frca.vsexam.entities.Exam;
import com.frca.vsexam.helper.Helper;
import com.frca.vsexam.helper.ImageDownloaderTask;
import com.frca.vsexam.network.HttpRequestBuilder;
import com.frca.vsexam.network.NetworkTask;
import com.frca.vsexam.network.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by KillerFrca on 11.10.13.
 */
public class DetailFragment extends Fragment {

    private final Exam exam;

    public DetailFragment(Exam exam) {
        this.exam = exam;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.exam_list_details, container, false);
        ((TextView)rootView.findViewById(R.id.text_courseCode)).setText(exam.courseCode);
        ((TextView)rootView.findViewById(R.id.text_courseName)).setText(exam.courseName);
        ((TextView)rootView.findViewById(R.id.text_authorName)).setText(exam.authorName);
        ((TextView)rootView.findViewById(R.id.text_type)).setText(exam.type);
        ((TextView)rootView.findViewById(R.id.text_isRegistered)).setText(exam.isRegistered ? "Registrován" : "Neregistrován");
        ((TextView)rootView.findViewById(R.id.text_capacity)).setText(String.valueOf(exam.currentCapacity)+"/"+String.valueOf(exam.maxCapacity));
        ((TextView)rootView.findViewById(R.id.text_examDate)).setText(Helper.getDateOutput(exam.examDate, Helper.DateOutputType.DATE_TIME));
        ((TextView)rootView.findViewById(R.id.text_registerStart)).setText(Helper.getDateOutput(exam.registerStart, Helper.DateOutputType.DATE_TIME));
        ((TextView)rootView.findViewById(R.id.text_registerEnd)).setText(Helper.getDateOutput(exam.registerEnd, Helper.DateOutputType.DATE_TIME));
        ((TextView)rootView.findViewById(R.id.text_unregisterEnd)).setText(Helper.getDateOutput(exam.unregisterEnd, Helper.DateOutputType.DATE_TIME));


        rootView.findViewById(R.id.button_author).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String authorUrl = HttpRequestBuilder.completeURLString("lide/clovek.pl?id=" + String.valueOf(exam.authorId));
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(authorUrl));
                startActivity(browserIntent);
            }
        });

        //new LogoDownloaderTask(getActivity(), exam.authorId, rootView.findViewById(R.id.logo_author)).execute();
        new ImageDownloaderTask(getActivity(), rootView.findViewById(R.id.logo_author)).loadLogo(exam.authorId);

        /*((TextView)mContent.findViewById(R.id.text_courseCode)).setText(exam.courseCode);
            ((TextView)mContent.findViewById(R.id.text_courseName)).setText(exam.courseName);
            ((TextView)mContent.findViewById(R.id.text_authorName)).setText(exam.authorName);

            new LogoDownloaderTask(exam.authorId, new SparseArray<Bitmap>(), ((MainActivity)getActivity()).data, (ImageView)mContent.findViewById(R.id.logo_author).findViewById(R.id.image)).execute();*/

        return rootView;
    }

    private void getClassmates() {
        try {

            HttpRequestBuilder builder = new HttpRequestBuilder(getActivity(), "student/terminy_info.pl?termin="+exam.id+";spoluzaci=1;studium="+exam.studyId+";obdobi="+exam.periodId).build();
            new NetworkTask(new NetworkTask.ResponseCallback() {

                @Override
                public void call(Response response) {
                    if (response.statusCode == 401) {
                        Toast.makeText(getActivity(), "Access denied", Toast.LENGTH_LONG).show();
                        return;
                    }

                    Document doc = Jsoup.parse(response.http);
                    Elements elements = doc.body().select("table#studenti tbody tr");

                    ClassmateList classmates = new ClassmateList();

                    for (Element element : elements) {
                        Elements columns = element.select("td");
                        if (columns.size() <= 1)
                            continue;

                        classmates.add(Classmate.get(columns));
                    }

                    onClassmatesLoaded(classmates);
                }
            }).execute(builder);

        } catch (NoAuthException e) {
            e.printStackTrace();
        }
    }

    private void onClassmatesLoaded(ClassmateList classmates) {
        // TODO
    }
}