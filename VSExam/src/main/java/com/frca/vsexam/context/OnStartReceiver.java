package com.frca.vsexam.context;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.frca.vsexam.entities.exam.Exam;
import com.frca.vsexam.entities.exam.ExamList;
import com.frca.vsexam.helper.RegisteringService;

public class OnStartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        setExamsToRegister(context);
    }

    public static void setExamsToRegister(Context context) {
        ExamList examList = new ExamList();
        examList.loadSaved(context);
        for (Exam exam: examList) {
            if (exam.isToBeRegistered())
                RegisteringService.setExamRegister(context, exam);
        }
    }
}
