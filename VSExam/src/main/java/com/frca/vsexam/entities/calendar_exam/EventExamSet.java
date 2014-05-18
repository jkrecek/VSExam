package com.frca.vsexam.entities.calendar_exam;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.frca.vsexam.helper.DataHolder;
import com.google.gson.Gson;

import java.util.HashSet;
import java.util.Iterator;

public class EventExamSet extends HashSet<EventExam> {

    public final static String KEY_EVENT_EXAMS = "eventExams";

    public void add(Context context, long examId, long eventId) {
        doRemove(examId);
        add(new EventExam(examId, eventId));
        save(context);
    }

    public void remove(Context context, long examId) {
        doRemove(examId);
        save(context);
    }

    public EventExam get(long examId) {
        for (EventExam eventExam : this)
            if (eventExam.getExamId() == examId)
                return eventExam;

        return null;
    }

    public boolean has(long examId) {
        return get(examId) != null;
    }

    private void doRemove(long examId) {
        Iterator<EventExam> itr = iterator();
        if (itr.hasNext()) {
            EventExam eventExam = itr.next();
            if (eventExam.getExamId() == examId) {
                itr.remove();
            }
        }
    }

    private String asString() {
        return new Gson().toJson(this);
    }

    private void save(Context context) {
        SharedPreferences preferences = DataHolder.getInstance(context).getPreferences();
        preferences
            .edit()
            .putString(KEY_EVENT_EXAMS, asString())
            .commit();
    }

    private static EventExamSet fromString(String string) {
        if (!TextUtils.isEmpty(string))
            return new Gson().fromJson(string, EventExamSet.class);
        else
            return new EventExamSet();
    }

    public static EventExamSet load(SharedPreferences preferences) {
        String eventExamString = preferences.getString(KEY_EVENT_EXAMS, null);
        return fromString(eventExamString);
    }
}
