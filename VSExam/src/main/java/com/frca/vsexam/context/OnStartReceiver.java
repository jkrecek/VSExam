package com.frca.vsexam.context;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.frca.vsexam.entities.base.Exam;
import com.frca.vsexam.helper.RegisteringService;

import java.io.File;
import java.io.FilenameFilter;

public class OnStartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        setExamsToRegister(context);
    }

    public static void setExamsToRegister(Context context) {
        for (File file : listExamFiles(context)) {
            Exam exam = (Exam) Exam.getFromFile(file);
            if (exam.isRegisterOnTime())
                RegisteringService.setExamRegister(context, exam);
        }
    }

    public static Exam[] getSavedExams(Context context) {
        File[] files = listExamFiles(context);
        Exam[] exams = new Exam[files.length];
        for (int i = 0; i < files.length; ++i)
            exams[i] = (Exam) Exam.getFromFile(files[i]);

        return exams;
    }

    private static File[] listExamFiles(Context context) {
        return context.getFilesDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String s) {
                return s.startsWith("Exam") && s.endsWith(".data");
            }
        });
    }
}
