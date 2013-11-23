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
    protected Exam doParse() throws EntityParsingException {
        Element author = getLinkFromColumn(7);
        String capacityString = getColumnContent(8, true);
        String[] capacityParts = capacityString.split("/");
        String registrationDates = getColumnContent(10, false);
        String[] registrationParts = registrationDates.split("<br />");
        Element info = getLinkFromColumn(11);
        Element course = getLinkFromColumn(2);
        int id = extractParameterFromLink(info, "termin");

        Exam exam = parentList.createExam(context, id);
        exam.setCourseCode(getColumnContent(1, true));
        exam.setCourseName(course.text().trim());
        exam.setCourseId(extractParameterFromLink(course, "predmet"));
        exam.setExamDate(parseDate(getColumnContent(4, true)));
        exam.setLocation(getColumnContent(5, true));
        exam.setType(getColumnContent(6, true));
        exam.setAuthorId(extractParameterFromLink(author, "id"));
        exam.setAuthorName(author.text().trim());
        exam.setCurrentCapacity(Integer.parseInt(capacityParts[0]));
        exam.setMaxCapacity(Integer.parseInt(capacityParts[1]));
        exam.setRegisterStart(parseDate(registrationParts[0]));
        exam.setRegisterEnd(parseDate(registrationParts[1]));
        exam.setUnregisterEnd(parseDate(registrationParts[2]));
        exam.setStudyId(extractParameterFromLink(info, "studium"));
        exam.setPeriodId(extractParameterFromLink(info, "obdobi"));
        exam.setGroup(currentGroup);

        return exam;
    }

    @Override
    protected Element getElement(int column, String select) throws EntityParsingException {
        if (currentGroup == Exam.Group.CAN_REGISTER)
            ++column;

        return super.getElement(column, select);
    }
}