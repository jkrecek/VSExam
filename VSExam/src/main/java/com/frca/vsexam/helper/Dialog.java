package com.frca.vsexam.helper;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.frca.vsexam.R;
import com.frca.vsexam.network.HttpRequestBuilder;
import com.frca.vsexam.network.ImageDownloaderTask;

import java.text.ParseException;

/**
 * Created by KillerFrca on 21.10.13.
 */
public abstract class Dialog {

    public static void ClassmateDetails(final Context context, final int userId, String username, String studyDetails) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        View view = null;
        // TODO
        try {
            StudyProgramParser parser = new StudyProgramParser(context, studyDetails);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.classmate_details, null);

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

            ImageDownloaderTask.startUserAvatarTask(context, logo, userId);
            name.setText(username);
            faculty.setText(parser.getFacultyString());
            form_type.setText(parser.getFormTypeString());
            semester.setText(parser.getSemesterString());
            programme.setText(parser.getProgrammeString());
            field.setText(parser.getFieldString());
            String specString =  parser.getSpecializationString();
            if (TextUtils.isEmpty(specString))
                ((View)spec.getParent()).setVisibility(View.GONE);
            else
                spec.setText(specString);

            mail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("message/rfc822");
                    i.putExtra(Intent.EXTRA_EMAIL, new String[]{String.valueOf(userId) + "@vse.com"});

                    try {
                        context.startActivity(Intent.createChooser(i, context.getResources().getString(R.string.send_mail)));
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(context, "There are no email clients installed.", Toast.LENGTH_LONG).show();
                    }
                }
            });

            goTo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String authorUrl = HttpRequestBuilder.completeURLString("lide/clovek.pl?id=" + String.valueOf(userId));
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(authorUrl));
                    context.startActivity(browserIntent);
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }

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
