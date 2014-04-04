package com.frca.vsexam.entities.exam;


import android.content.Context;
import android.util.SparseArray;

import com.frca.vsexam.context.MainActivity;
import com.frca.vsexam.entities.base.BaseEntityList;
import com.frca.vsexam.fragments.BrowserPaneFragment;
import com.frca.vsexam.helper.AppSparseArray;
import com.frca.vsexam.helper.DataHolder;
import com.frca.vsexam.helper.Helper;
import com.frca.vsexam.helper.ObjectMap;
import com.frca.vsexam.helper.RegisteringService;
import com.frca.vsexam.network.Response;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by KillerFrca on 5.10.13.
 */
public class ExamList extends BaseEntityList<Exam> {

    private int[] groupCounts;

    public ExamList() {

    }

    public void parseAndAdd(Context context, Elements elements) {
        ExamParser parser = new ExamParser(context, this);
        for (Element element : elements) {
            if (element.className().equals("zahlavi")) {
                if (parser.currentGroup == null)
                    parser.currentGroup = Exam.Group.IS_REGISTERED;
                else {
                    int currentId = parser.currentGroup.toInt();
                    parser.currentGroup = Exam.Group.fromInt(++currentId);
                }
                continue;
            }

            Elements columns = element.select("td");
            if (columns.size() <= 1)
                continue;

            add((Exam)parser.parse(columns));
        }

        /*Random random = new Random();
        int times[] = new int[] {1383325200, 1383328800, 1383332400, 1383321600, 1383318000 };
        for (int time : times) {
            Exam exam = new Exam();
            Exam.Group group = Exam.Group.fromInt(random.nextInt(2));
            exam.setGroup(group);
            exam.setCourseName("Státní zkouška ze studijního oboru");
            exam.setCourseCode("PHM");
            exam.setType("zkouška (ústní)");
            exam.setExamDate(new Date(time*1000L));
            add(exam);
        }*/

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

    private void finalizeInit() {
        setGroupCounts();

        if (groupCounts[0] != 0 && groupCounts[1] + groupCounts[2] != 0) {
            /*for (int i = 0; i < groupCounts[0]; ++i) {
                Exam exam = get(i);
                if (!exam.isRegistered())
                    continue;

                for (int altPos = groupCounts[0]; altPos < size(); ++altPos) {
                    Exam altExam = get(altPos);
                    if (altExam.isRegistered())
                        continue;

                    altExam.setRegisteredOnId(exam.getId());
                }
            }*/

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
        return Helper.getValue(this, "courseName", true);
    }

    public AppSparseArray<String> getCourses() {
        AppSparseArray<String> sparseArray = new AppSparseArray<String>();
        List<ObjectMap> maps = Helper.getValuesMap(this, new String[] { "courseId", "courseName" }, true);
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

        exam.setRegistered(true);
        exam.setToBeRegistered(context, false);

        if (exam.getRegisteredOnId() != 0) {
            Exam currentlyRegistered = find(exam.getRegisteredOnId());
            if (currentlyRegistered != null)
                currentlyRegistered.setRegistered(false);
        }

        notifyRegisteredExamChange(exam, true);

        setGroupCounts();

        sort();

        BrowserPaneFragment browserPaneFragment = MainActivity.getBrowserPaneFragment();
        if (browserPaneFragment != null)
            browserPaneFragment.updateView();

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

    public boolean onUngistrationResponse(Exam exam, Response response) {
        if (response == null || response.getStatusCode() != 200)
            return false;

        exam.setRegistered(false);

        notifyRegisteredExamChange(exam, false);

        setGroupCounts();

        sort();

        BrowserPaneFragment browserPaneFragment = MainActivity.getBrowserPaneFragment();
        if (browserPaneFragment != null)
            browserPaneFragment.updateView();

        return true;
    }

    public int[] getGroupCounts() {
        return groupCounts;
    }
}
