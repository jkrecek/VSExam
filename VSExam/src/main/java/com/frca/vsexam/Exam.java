package com.frca.vsexam;

import android.text.TextUtils;
import android.util.Log;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Exam {

    public final static TimeZone TIME_ZONE = TimeZone.getTimeZone("Europe/Prague");
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    public static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
    public static final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd. MM. yyyy HH:mm");

    public final int id;
    public final int studyId;
    public final int periodId;
    public final String courseCode;
    public final String courseName;
    public final int examDate;
    public final String location;
    public final String type;
    public final int authorId;
    public final String authorName;
    public final int registerStart;
    public final int registerEnd;
    public final int unregisterEnd;
    public final boolean isRegistered;
    public final int currentCapacity;
    public final int maxCapacity;

    private Elements tempColumns;
    private int tempGroup;
    private int currentColumn;

    public static Exam get(Elements columns, int group) {
        try {
            return new Exam(columns, group);
        } catch (ExamParsingException e) {
            Log.e(Exam.class.getName(), e.getMessage());
            return null;
        }
    }

    private Exam(Elements columns, int group) throws ExamParsingException {
        tempColumns = columns;
        tempGroup = group;
        currentColumn = 0;

        courseCode = getColumnContent(1, true);
        courseName = getColumnContent(2, true);
        examDate = UTCTimestamp(getColumnContent(4, true));
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
        registerStart = UTCTimestamp(registrationParts[0]);
        registerEnd = UTCTimestamp(registrationParts[1]);
        unregisterEnd = UTCTimestamp(registrationParts[2]);

        isRegistered = group == 1;

        Element info = getLinkFromColumn(11);
        id = extractParameterFromLink(info, "termin");
        studyId = extractParameterFromLink(info, "studium");
        periodId = extractParameterFromLink(info, "obdobi");

        currentColumn = 0;
        tempColumns = null;
    }

    private String getColumnContent(int column, boolean stripHtml) throws ExamParsingException {
        currentColumn = column;
        Element element = getElement(column, "small");
        if (stripHtml)
            return element.text().trim();
        else
            return element.html().trim();
    }

    private Element getLinkFromColumn(int column) throws ExamParsingException {
        currentColumn = column;
        return getElement(column, "small a");
    }

    private Element getElement(int column, String select) throws ExamParsingException {
        if (tempGroup == 2)
            ++column;

        Element element = tempColumns.get(column).select(select).first();
        if (element != null)
            return element;
        else
            throw new ExamParsingException("No such element `" + select + "`" );
    }

    private int extractParameterFromLink(Element link, String parameter) throws ExamParsingException  {
        Pattern pattern = Pattern.compile(".*(?:" + parameter+ ")=(\\d*)");
        String text = link.attr("href");
        if (TextUtils.isEmpty(text))
            throw new ExamParsingException("Element does not contain href");

        Matcher matcher = pattern.matcher(text);
        if (!matcher.find())
            throw new ExamParsingException("Href doesn't contain such parameter");

        return Integer.parseInt(matcher.group(1));
    }

    private int UTCTimestamp(String text) throws ExamParsingException {
        SimpleDateFormat format = new SimpleDateFormat("dd. MM. yyyy HH:mm", Locale.ENGLISH);
        format.setTimeZone(TIME_ZONE);

        try {
            Date date = format.parse(text);
            long timestamp = date.getTime()/1000;
            return (int)timestamp;
        } catch (ParseException e) {
            throw new ExamParsingException("Error while parsing time string: `"+ text + "`");
        }
    }

    public class ExamParsingException extends Exception {
        public ExamParsingException(String error) {
            super("Error while parsing element " + String.valueOf(currentColumn)+ ":\n" + error + "\n" + tempColumns.get(currentColumn).html());
        }
    }
}
