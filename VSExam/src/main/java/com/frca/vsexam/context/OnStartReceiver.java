package com.frca.vsexam.context;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.frca.vsexam.entities.base.Exam;
import com.frca.vsexam.entities.base.ParentEntity;
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

    private static File[] listExamFiles(Context context) {
        return ParentEntity.getDir(context, Exam.class.getName()).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return file.isFile() && file.getName().endsWith(".data");
            }
        });
    }
}
