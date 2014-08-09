package com.frca.vsexam.fragments.classmate_detail;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.frca.vsexam.R;
import com.frca.vsexam.entities.classmate.Classmate;
import com.frca.vsexam.entities.vsedata.VSEStringParser;
import com.frca.vsexam.entities.vsedata.VSEStructure;
import com.frca.vsexam.helper.ViewProvider;
import com.frca.vsexam.network.HttpRequestBuilder;
import com.frca.vsexam.network.tasks.BaseNetworkTask;
import com.frca.vsexam.network.tasks.UserImageNetworkTask;

import org.apache.http.ParseException;


public class ClassmateDataProvider extends ViewProvider {

    private Classmate mClassmate;

    public ClassmateDataProvider(ClassmateFragment baseFragment, ViewGroup parent, LayoutInflater inflater) {
        super(baseFragment, parent, inflater, R.layout.classmate_detail);

        mClassmate = baseFragment.getClassmate();
    }

    @Override
    public Result doLoad() {
        VSEStringParser parser = getStringParser(mClassmate.getIdentification());
        if (parser == null)
            return Result.HIDE;

        Resources resources = getContext().getResources();

        BaseNetworkTask.run(new UserImageNetworkTask(getContext(), mClassmate.getId(), findViewById(R.id.logo)));

        setViewText(R.id.text_name, mClassmate.getName());
        setViewText(R.id.text_semester, resources.getString(R.string.nth_semester_study_plan_string, parser.getSemester(), parser.getStudyPlan()));

        setViewText(R.id.text_faculty, parser.getFaculty().name);

        setViewText(R.id.text_type, parser.getStudyType().name);
        setViewText(R.id.text_form, parser.getForm().resourceId);

        setViewText(R.id.text_programme, parser.getProgramme().name);
        setViewText(R.id.text_field, parser.getStudyField().name);

        if (parser.getSpecialization() != null)
            setViewText(R.id.text_spec, parser.getSpecialization().name);
        else
            setViewText(R.id.text_spec, R.string.two_dash);

        setOnClickListener(R.id.button_mail, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{String.valueOf(mClassmate.getId()) + "@vse.cz"});

                try {
                    getContext().startActivity(i);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getContext(), R.string.no_email_clients_installed, Toast.LENGTH_LONG).show();
                }
            }
        });

        setOnClickListener(R.id.button_goto, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String authorUrl = HttpRequestBuilder.completeURLString("lide/clovek.pl?id=" + String.valueOf(mClassmate.getId()), true);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(authorUrl));
                getContext().startActivity(browserIntent);
            }
        });

        return Result.DONE;
    }

    private VSEStringParser getStringParser(String identificationString) {
        try {
            VSEStructure structure = VSEStructure.load(getContext());
            if (structure != null)
                return VSEStringParser.parse(identificationString, structure);
        } catch (ParseException e) {
            // TODO error while parsing, redirect to settings ??
            e.printStackTrace();
        }

        getMainActivity().onBackPressed();
        return null;
    }
}
