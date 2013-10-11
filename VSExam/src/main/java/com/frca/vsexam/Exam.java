package com.frca.vsexam;

import android.util.Log;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Exam {

    public final static TimeZone TIME_ZONE = TimeZone.getTimeZone("Europe/Prague");

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

    private Elements tempColumns;
    public Exam(Elements columns, int group) {
        tempColumns = columns;
        courseCode = getTextFromColumn(1);
        courseName = getTextFromColumn(2);
        examDate = UTCTimestamp(getTextFromColumn(4));
        location = getTextFromColumn(5);
        type = getTextFromColumn(6);
        Element author = getLinkFromColumn(7);
        authorId = extractParameterFromLink(author, "id");
        authorName = author.text().trim();

        String registrationDates = getHtmlFromColumn(10);
        String[] registrationParts = registrationDates.split("<br />");
        registerStart = UTCTimestamp(registrationParts[0]);
        registerEnd = UTCTimestamp(registrationParts[1]);
        unregisterEnd = UTCTimestamp(registrationParts[2]);

        isRegistered = group == 1;
        Element info = getLinkFromColumn(11);
        id = extractParameterFromLink(info, "termin");
        studyId = extractParameterFromLink(info, "studium");
        periodId = extractParameterFromLink(info, "obdobi");
        tempColumns = null;
    }

    private String getTextFromColumn(int column) {
        //return tempRow.select("td:nth-of-type(" + String.valueOf(column)+ ") small").first().text().trim();
        return tempColumns.get(column).select("small").first().text().trim();
    }

    private String getHtmlFromColumn(int column) {
        //return tempRow.select("td:nth-of-type(" + String.valueOf(column)+ ") small").first().text().trim();
        return tempColumns.get(column).select("small").first().html().trim();
    }

    private Element getLinkFromColumn(int column) {
        //return tempRow.select("td:nth-of-type(" + String.valueOf(column)+ ") small").first().text().trim();
        return tempColumns.get(column).select("small a").first();
    }

    private int extractParameterFromLink(Element link, String parameter) {
        Pattern pattern = Pattern.compile(".*(?:" + parameter+ ")=(\\d*)");
        String text = link.attr("href");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find())
            return Integer.parseInt(matcher.group(1));
        return 0;
    }

    private int UTCTimestamp(String text) {
        SimpleDateFormat format = new SimpleDateFormat("dd. MM. yyyy HH:mm", Locale.ENGLISH);
        format.setTimeZone(TIME_ZONE);

        Date date;
        try {
            date = format.parse(text);
        } catch (ParseException e) {
            Log.e(Exam.class.getName(), "Error while parsing time string: `"+ text + "`");
            return 0;
        }

        long timestamp = date.getTime()/1000;
        //timestamp -= zoneDiff * 60 * 60;
        return (int)timestamp;
    }
}
