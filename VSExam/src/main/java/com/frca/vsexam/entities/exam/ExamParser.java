package com.frca.vsexam.entities.exam;

import android.content.Context;

import com.frca.vsexam.entities.base.BaseParser;

import org.jsoup.nodes.Element;

public class ExamParser extends BaseParser {

    public Exam.Group currentGroup = null;

    private ExamList parentList;

    private Context context;

    ExamParser(Context context, ExamList parentList) {
        this.context = context;
        this.parentList = parentList;
    }

    @Override
    protected Exam doParse() {
        int id = extractParameterFromLink(getLinkFromColumn(11), "termin");

        Exam exam = parentList.createExam(context, id);
        exam.setCourseCode(getColumnContent(1, true));

        Element course = getLinkFromColumn(2);
        exam.setCourseName(course.text().trim());
        exam.setCourseId(extractParameterFromLink(course, "predmet"));

        exam.setExamDate(parseDate(getColumnContent(4, true)));
        exam.setLocation(getColumnContent(5, true));
        exam.setType(getColumnContent(6, true));

        Element author = getLinkFromColumn(7);
        exam.setAuthorId(extractParameterFromLink(author, "id"));
        exam.setAuthorName(author.text().trim());

        String capacityString = getColumnContent(8, true).trim();
        int idx = capacityString.indexOf(" ", 1);
        if (idx > 0)
            capacityString = capacityString.substring(0, idx);
        String[] capacityParts = capacityString.split("/");
        exam.setCurrentCapacity(Integer.parseInt(capacityParts[0]));
        exam.setMaxCapacity(Integer.parseInt(capacityParts[1]));

        String registrationDates = getColumnContent(10, false);
        String[] registrationParts = registrationDates.split("<br />");
        exam.setRegisterStart(parseDate(registrationParts[0]));
        exam.setRegisterEnd(parseDate(registrationParts[1]));
        exam.setUnregisterEnd(parseDate(registrationParts[2]));

        Element info = getLinkFromColumn(11);
        exam.setStudyId(extractParameterFromLink(info, "studium"));
        exam.setPeriodId(extractParameterFromLink(info, "obdobi"));

        if (exam.getGroup() == null || exam.getGroup() != Exam.Group.TO_BE_REGISTERED)
            exam.setGroup(currentGroup);

        return exam;
    }

    @Override
    protected Element getElement(int column, String select) {
        if (currentGroup == Exam.Group.CAN_REGISTER)
            ++column;

        return super.getElement(column, select);
    }
}
