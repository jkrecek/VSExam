package com.frca.vsexam.entities;

import android.util.Log;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Date;

public class Exam extends ParentEntity {

    public final int id;
    public final int studyId;
    public final int periodId;
    public final int authorId;
    public final int currentCapacity;
    public final int maxCapacity;
    public final String courseCode;
    public final String courseName;
    public final String location;
    public final String type;
    public final String authorName;
    public final Date examDate;
    public final Date registerStart;
    public final Date registerEnd;
    public final Date unregisterEnd;
    public final boolean isRegistered;

    private int tempGroup;

    public static Exam get(Elements columns, int group) {
        try {
            return new Exam(columns, group);
        } catch (EntityParsingException e) {
            Log.e(Exam.class.getName(), e.getMessage());
            return null;
        }
    }

    private Exam(Elements columns, int group) throws EntityParsingException {
        super.init(columns);

        tempGroup = group;

        courseCode = getColumnContent(1, true);
        courseName = getColumnContent(2, true);
        examDate = parseDate(getColumnContent(4, true));
        location = getColumnContent(5, true);
        type = getColumnContent(6, true);

        Element author = getLinkFromColumn(7);
        authorId = extractParameterFromLink(author, "id");
        authorName = author.text().trim();

        String capacityString = getColumnContent(8, true);
        String[] capacityParts = capacityString.split("/");
        currentCapacity = Integer.parseInt(capacityParts[0]);
        maxCapacity = Integer.parseInt(capacityParts[1]);

        String registrationDates = getColumnContent(10, false);
        String[] registrationParts = registrationDates.split("<br />");
        registerStart = parseDate(registrationParts[0]);
        registerEnd = parseDate(registrationParts[1]);
        unregisterEnd = parseDate(registrationParts[2]);

        isRegistered = group == 1;

        Element info = getLinkFromColumn(11);
        id = extractParameterFromLink(info, "termin");
        studyId = extractParameterFromLink(info, "studium");
        periodId = extractParameterFromLink(info, "obdobi");

        super.initDone();
    }

    @Override
    protected Element getElement(int column, String select) throws EntityParsingException {
        if (tempGroup == 2)
            ++column;

        return super.getElement(column, select);
    }
}
