package com.frca.vsexam.entities.lists;


import android.widget.ArrayAdapter;

import com.frca.vsexam.entities.base.Exam;
import com.frca.vsexam.entities.parsers.ExamParser;
import com.frca.vsexam.helper.AppSparseArray;
import com.frca.vsexam.helper.Helper;
import com.frca.vsexam.helper.ObjectMap;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by KillerFrca on 5.10.13.
 */
public class ExamList extends ArrayList<Exam> {

    private int[] groupCounts;

    public ExamList(Elements elements) {

        ExamParser parser = new ExamParser();
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

    private ExamList() {

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
        groupCounts = new int[Exam.Group.values().length];
        for (Exam exam : this) {
            ++groupCounts[exam.getGroup().toInt()];
        }

        if (groupCounts[0] != 0 && groupCounts[1] + groupCounts[2] != 0) {
            for (int i = 0; i < groupCounts[0]; ++i) {
                Exam exam = get(i);
                if (!exam.isRegistered())
                    continue;

                for (int altPos = groupCounts[0]; altPos < size(); ++altPos) {
                    Exam altExam = get(altPos);
                    if (altExam.isRegistered())
                        continue;

                    altExam.setRegisteredOnId(exam.getId());
                }
            }
        }

        sort();
    }

    public List<String> getCourseNames() {
        return Helper.getValue(this, "courseName", true);
    }

    public AppSparseArray<String> getCourses() {
        List<ObjectMap> maps = Helper.getValuesMap(this, new String[] { "courseId", "courseName" }, true);
        AppSparseArray<String> sparseArray = new AppSparseArray<String>();
        for (ObjectMap map : maps) {
            int id = (Integer)map.get("courseId");
            String value = (String) map.get("courseName");
            sparseArray.put(id, value);
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

    public Object getExamOrVoid(int position) {
        if (position >= getAdapterSize())
            return null;

        int counter = -1;
        for (int i = 0; i < Exam.Group.values().length; ++i) {
            if (groupCounts[i] > 0) {
                if (++counter >= position)
                    return Exam.Group.fromInt(i);

                counter += groupCounts[i];
                if (counter >= position)
                    return get(position - i - 1);
            }

        }

        return null;
    }

    public List<String> getExamClassNames() {
        return Helper.getValue(this, "className", false);
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

    public Exam getExamById(int id) {
        for (Exam exam : this)
            if (exam.getId() == id)
                return exam;

        return null;
    }

    public void setExamRegister(Exam exam, boolean apply, ArrayAdapter adapter) {
        exam.setRegistered(apply);

        // update this
        if (apply) {
            if (exam.getRegisteredOnId() != 0) {
                Exam registeredOn = getExamById(exam.getRegisteredOnId());
                registeredOn.setRegistered(false);
            }
        }

        // update others
        List<Exam> courseExams = getSameCourseExams(exam, false);
        int otherCourseRegisterOnId = apply ? exam.getId() : 0;
        for (Exam courseExam : courseExams)
            courseExam.setRegisteredOnId(otherCourseRegisterOnId);

        sort();

        if (adapter != null)
            adapter.notifyDataSetChanged();
    }

    private List<Exam> getSameCourseExams(Exam compareExam, boolean include) {
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

    public static interface MatchChecker {
        boolean isMatch(Exam exam);
    }
}
