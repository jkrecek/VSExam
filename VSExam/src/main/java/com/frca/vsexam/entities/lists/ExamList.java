package com.frca.vsexam.entities.lists;


import android.util.SparseArray;

import com.frca.vsexam.entities.base.Exam;
import com.frca.vsexam.entities.parsers.ExamParser;
import com.frca.vsexam.helper.Helper;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by KillerFrca on 5.10.13.
 */
public class ExamList extends SparseArray<ArrayList<Exam>> {

    private List<Exam> combined = new ArrayList<Exam>();

    public ExamList(Elements elements) {

        for (int i = 0; i < 3; ++i)
            put(i, new ArrayList<Exam>());

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

            List<Exam> currentList = get(parser.currentGroup.toInt());
            currentList.add((Exam)parser.parse(columns));
        }

        for (Exam mainExam : get(0)) {
            for (int i = 1; i < 3; ++i) {
                for (Exam exam : get(i)) {
                    if (exam.getId() != mainExam.getId() && exam.getCourseCode().equals(mainExam.getCourseCode()) && exam.getType().equals(mainExam.getType())) {
                        exam.setRegisteredOnId(mainExam.getId());
                    }
                }
            }
        }
    }


    public List<Exam> getCombined() {
        if (combined.isEmpty()) {
            for (int i = 0; i < size(); ++i)
                combined.addAll(get(i));
        }
        return combined;
    }

    public List<String> getCourseNames() {
        return Helper.extractObjectValues(getCombined(), "courseName");
    }

    public void sort() {
        Collections.sort(combined, new Comparator<Exam>() {
            @Override
            public int compare(Exam exam, Exam exam2) {
                if (exam.getGroup() != exam2.getGroup())
                    return exam.getGroup().toInt() - exam2.getGroup().toInt();

                return (int) (exam.getExamDate().getTime() - exam2.getExamDate().getTime());
            }
        });
    }
}
