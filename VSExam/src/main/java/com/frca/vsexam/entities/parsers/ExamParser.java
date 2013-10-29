package com.frca.vsexam.entities.parsers;

import com.frca.vsexam.entities.base.Exam;

import org.jsoup.nodes.Element;

public class ExamParser extends BaseParser {

    public Exam.Group currentGroup = null;

    @Override
    protected Exam doParse() throws EntityParsingException {
        Element author = getLinkFromColumn(7);
        String capacityString = getColumnContent(8, true);
        String[] capacityParts = capacityString.split("/");
        String registrationDates = getColumnContent(10, false);
        String[] registrationParts = registrationDates.split("<br />");
        Element info = getLinkFromColumn(11);

        Exam exam = new Exam();
        exam.setCourseCode(getColumnContent(1, true));
        exam.setCourseName(getColumnContent(2, true));
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
        exam.setId(extractParameterFromLink(info, "termin"));
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
