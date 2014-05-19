package com.frca.vsexam.entities.exam;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.SparseArray;
import android.widget.Toast;

import com.frca.vsexam.R;
import com.frca.vsexam.context.MainActivity;
import com.frca.vsexam.entities.base.BaseEntityList;
import com.frca.vsexam.entities.base.BaseParser;
import com.frca.vsexam.entities.calendar_exam.EventExam;
import com.frca.vsexam.entities.calendar_exam.EventExamSet;
import com.frca.vsexam.fragments.MainFragment;
import com.frca.vsexam.helper.AppSparseArray;
import com.frca.vsexam.helper.CalendarEvent;
import com.frca.vsexam.helper.DataHolder;
import com.frca.vsexam.helper.ObjectMap;
import com.frca.vsexam.helper.RegisteringService;
import com.frca.vsexam.helper.Utils;
import com.frca.vsexam.network.Response;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class ExamList extends BaseEntityList<Exam> {

    private int[] groupCounts;

    public void parseAndAdd(Context context, Elements elements) {
        ExamParser parser = new ExamParser(context, this);
        List<Exam.Group> parseGroups = Arrays.asList(Exam.Group.IS_REGISTERED, Exam.Group.CAN_REGISTER, Exam.Group.CAN_NOT_REGISTER);

        Iterator<Exam.Group> iterator = parseGroups.iterator();
        for (Element element : elements) {
            if (element.className().equals("zahlavi")) {
                if (iterator.hasNext())
                    parser.currentGroup = iterator.next();
                continue;
            }

            Elements columns = element.select("td");
            if (columns.size() <= 1)
                continue;

            add((Exam)parser.parse(columns));
        }

        finalizeInit();
    }

    public ExamList filter(MatchChecker checker) {
        ExamList examList = new ExamList();
        for (Exam exam : this) {
            if (checker.isMatch(exam))
                examList.add(exam);
        }

        examList.finalizeInit();

        return examList;
    }

    protected void finalizeInit() {
        setGroupCounts();

        if (groupCounts[Exam.Group.IS_REGISTERED.toInt()] != 0 &&
            groupCounts[Exam.Group.CAN_REGISTER.toInt()] + groupCounts[Exam.Group.CAN_NOT_REGISTER.toInt()] != 0) {

            for (Exam exam : this) {
                if (exam.isRegistered())
                    notifyRegisteredExamChange(exam, true);
            }
        }

        sort();
    }

    private void setGroupCounts() {
        groupCounts = new int[Exam.Group.values().length];
        for (Exam exam : this) {
            ++groupCounts[exam.getGroup().toInt()];
        }
    }

    public List<String> getCourseNames() {
        return Utils.getValue(this, "courseName", true);
    }

    public AppSparseArray<String> getCourses() {
        AppSparseArray<String> sparseArray = new AppSparseArray<String>();
        List<ObjectMap> maps = Utils.getValuesMap(this, new String[]{"courseId", "courseName"}, true);
        if (maps != null) {
            for (ObjectMap map : maps) {
                int id = (Integer)map.get("courseId");
                String value = (String) map.get("courseName");
                sparseArray.put(id, value);
            }
        }
        return sparseArray;
    }

    public void sort() {
        Collections.sort(this, new Comparator<Exam>() {
            @Override
            public int compare(Exam exam, Exam exam2) {
                if (exam.getGroup() != exam2.getGroup())
                    return exam.getGroup().toInt() - exam2.getGroup().toInt();

                long diff = exam.getExamDate().getTime() - exam2.getExamDate().getTime();
                return (int) (diff / 1000L);
            }
        });
    }

    public Object getFromAdapter(int position) {
        if (position >= getAdapterSize())
            return null;

        int counter = -1;
        int groupHeaders = -1;
        for (int i = 0; i < Exam.Group.values().length; ++i) {
            if (groupCounts[i] > 0) {
                ++groupHeaders;
                if (++counter >= position) {
                    return Exam.Group.fromInt(i);
                }

                counter += groupCounts[i];
                if (counter >= position)
                    return get(position - groupHeaders - 1);
            }

        }

        return null;
    }

    public int getAdapterPosition(Exam exam) {
        int position = indexOf(exam);
        int[] groupCounts = getGroupCounts();
        for (int i = 0; i <= exam.getGroup().toInt(); ++i)
            if (groupCounts[i] > 0)
                ++position;

        return position;
    }

    public int getAdapterSize() {
        return size() + getGroupsFilled();
    }

    public int getGroupsFilled() {
        int count = 0;
        for (int c : groupCounts)
            if (c > 0)
                ++count;

        return count;
    }

    public static interface MatchChecker {
        boolean isMatch(Exam exam);
    }

    public Exam createExam(Context context, int id) {
        Exam exam = load(context, id);
        if (exam == null)
            exam = new Exam(id);

        return exam;
    }

    private void notifyRegisteredExamChange(Exam exam, boolean registered) {
        List<Exam> exams = getSameExamCategory(exam, false);
        int newId = registered ? exam.getId() : 0;
        for (Exam otherExam : exams)
            otherExam.setRegisteredOnId(newId);
    }

    private List<Exam> getSameExamCategory(Exam compareExam, boolean include) {
        List<Exam> list = new ArrayList<Exam>();
        for (Exam exam : this) {
            if (exam == compareExam) {
                if (include)
                    list.add(exam);
                continue;
            }

            if (exam.getCourseId() == compareExam.getCourseId() && exam.getType().equals(compareExam.getType()))
                list.add(exam);
        }

        return list;
    }

    public boolean onRegistrationResponse(Context context, Exam exam, Response response) {
        if (response == null || response.getStatusCode() != 302)
            return false;

        if (exam.getRegisteredOnId() != 0) {
            Exam currentlyRegistered = find(exam.getRegisteredOnId());
            if (currentlyRegistered != null)
                currentlyRegistered.setRegistered(false);
        }

        exam.setRegistered(true);

        exam.removeToBeRegistered(context, true);

        notifyRegisteredExamChange(exam, true);

        setGroupCounts();

        sort();

        SharedPreferences preferences = DataHolder.getInstance(context).getPreferences();
        if (preferences.getBoolean("autoEventCreate", false)) {
            putExamToCalendar(exam, context);
        }

        MainFragment mainFragment = MainActivity.getMainFragment();
        if (mainFragment != null)
            mainFragment.updateView();

        if (!(context instanceof RegisteringService)) {
            SparseArray<RegisteringService> container = DataHolder.getInstance(context).getRegisteringServiceContainer();
            synchronized (container) {
                RegisteringService runningService = container.get(exam.getId());
                if (runningService != null)
                    runningService.stopSelf();
            }
        }

        return true;
    }

    public boolean onUnregistrationResponse(Context context, Exam exam, Response response) {
        if (response == null || response.getStatusCode() != 200)
            return false;

        exam.setRegistered(false);

        notifyRegisteredExamChange(exam, false);

        setGroupCounts();

        sort();

        SharedPreferences preferences = DataHolder.getInstance(context).getPreferences();
        if (preferences.getBoolean("autoEventCreate", false)) {
            removeExamFromCalendar(exam, context);
        }

        MainFragment mainFragment = MainActivity.getMainFragment();
        if (mainFragment != null)
            mainFragment.updateView();

        return true;
    }

    public int[] getGroupCounts() {
        return groupCounts;
    }

    public void putExamToCalendar(Exam exam, Context context) {
        long calID = 1;

        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarEvent.DTSTART, exam.getExamDate().getTime());
        values.put(CalendarEvent.DTEND, exam.getExamDate().getTime() + 90 * 60 * 1000);
        values.put(CalendarEvent.TITLE, exam.getCourseName() + " - " + exam.getType());
        values.put(CalendarEvent.CALENDAR_ID, calID);
        values.put(CalendarEvent.EVENT_TIMEZONE, BaseParser.TIME_ZONE.getID());
        values.put(CalendarEvent.EVENT_LOCATION, exam.getLocation());

        Uri uri = cr.insert(CalendarEvent.CONTENT_URI, values);

        long eventID = Long.parseLong(uri.getLastPathSegment());

        DataHolder.getInstance(context).getEventExamSet().add(context, exam.getId(), eventID);

        Toast.makeText(context, R.string.event_created, Toast.LENGTH_LONG).show();
    }

    public void removeExamFromCalendar(Exam exam, Context context) {
        EventExamSet eventExamSet = DataHolder.getInstance(context).getEventExamSet();
        EventExam eventExam = eventExamSet.get(exam.getId());
        if (eventExam != null) {
            ContentResolver cr = context.getContentResolver();
            Uri deleteUri = ContentUris.withAppendedId(CalendarEvent.CONTENT_URI, eventExam.getEventId());
            cr.delete(deleteUri, null, null);
        }

        eventExamSet.remove(context, exam.getId());
        Toast.makeText(context, R.string.event_deleted, Toast.LENGTH_LONG).show();
    }
}
