package com.frca.vsexam.helper;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.frca.vsexam.R;
import com.frca.vsexam.entities.vsedata.VSEStringParser;
import com.frca.vsexam.entities.vsedata.VSEStructure;
import com.frca.vsexam.entities.vsedata.VSEStructureElement;
import com.frca.vsexam.network.HttpRequestBuilder;
import com.frca.vsexam.network.tasks.BaseNetworkTask;
import com.frca.vsexam.network.tasks.UserImageNetworkTask;

import java.text.ParseException;

public abstract class Dialog {

    public static void ClassmateDetails(final Context context, final int userId, String username, String studyDetails) {
        VSEStringParser parser = null;
        try {
            VSEStructure structure = VSEStructure.load(context);
            if (structure != null)
                parser = VSEStringParser.parse(studyDetails, structure);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (parser == null)
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.classmate_details, null);

        View logo               = view.findViewById(R.id.logo);
        TextView name           = (TextView) view.findViewById(R.id.text_name);
        TextView faculty        = (TextView) view.findViewById(R.id.text_faculty);
        TextView form_type      = (TextView) view.findViewById(R.id.text_form_type);
        TextView semester       = (TextView) view.findViewById(R.id.text_semester);
        TextView programme      = (TextView) view.findViewById(R.id.text_programme);
        TextView field          = (TextView) view.findViewById(R.id.text_field);
        TextView spec           = (TextView) view.findViewById(R.id.text_spec);
        ImageButton mail        = (ImageButton) view.findViewById(R.id.button_mail);
        ImageButton goTo        = (ImageButton) view.findViewById(R.id.button_goto);

        BaseNetworkTask.run(new UserImageNetworkTask(context, userId, logo));

        name.setText(username);

        Resources resources = context.getResources();

        faculty.setText(parser.getFaculty().name);

        String type = parser.getStudyType().name;
        int formResource = parser.getForm().resourceId;

        form_type.setText(resources.getString(R.string.study_type_form_string, type, resources.getString(formResource)));
        semester.setText(resources.getString(R.string.nth_semester_study_plan_string, parser.getSemester(), parser.getStudyPlan()));

        programme.setText(parser.getProgramme().name);
        field.setText(parser.getStudyField().name);
        VSEStructureElement specialization = parser.getSpecialization();
        if (specialization != null)
            spec.setText(specialization.name);
        else
            ((View)spec.getParent()).setVisibility(View.GONE);

        mail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{String.valueOf(userId) + "@vse.com"});

                try {
                    context.startActivity(i);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(context, "There are no email clients installed.", Toast.LENGTH_LONG).show();
                }
            }
        });
        goTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String authorUrl = HttpRequestBuilder.completeURLString("lide/clovek.pl?id=" + String.valueOf(userId), true);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(authorUrl));
                context.startActivity(browserIntent);
            }
        });

        builder.setTitle("Profil studenta")
            .setView(view)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

        builder.create().show();
    }
}
