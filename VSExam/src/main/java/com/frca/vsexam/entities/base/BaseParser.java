package com.frca.vsexam.entities.base;

import android.text.TextUtils;
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

public abstract class BaseParser {

    public final static TimeZone TIME_ZONE = TimeZone.getTimeZone("Europe/Prague");

    protected static final SimpleDateFormat PARSING_FORMAT = new SimpleDateFormat("dd. MM. yyyy HH:mm", Locale.ENGLISH);

    static {
        PARSING_FORMAT.setTimeZone(TIME_ZONE);
    }

    protected Elements tempColumns;
    protected int currentColumn;

    public ParentEntity parse(Elements columns) {
        this.tempColumns = columns;
        currentColumn = 0;

        try {
            return doParse();
        } finally {
            this.tempColumns = null;
            currentColumn = 0;
        }
    }

    protected abstract ParentEntity doParse();

    protected String getColumnContent(int column, boolean stripHtml) {
        try {
            currentColumn = column;
            Element element = getElement(column, "small");
            if (stripHtml)
                return element.text().trim();
            else
                return element.html().trim();
        } catch(Exception e) {
            Log.d(getClass().getName(), e.getMessage());
            e.printStackTrace();
        }

        return "";
    }

    protected Element getLinkFromColumn(int column) {
        currentColumn = column;
        return getElement(column, "small a");
    }

    protected Element getElement(int column, String select){
        try {
            Element element = tempColumns.get(column).select(select).first();
            if (element != null)
                return element;
            else
                throw new EntityParsingException("No such element `" + select + "`" );
        } catch (EntityParsingException e) {
            Log.d(getClass().getName(), e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    protected int extractParameterFromLink(Element link, String parameter) {
        try {
            Pattern pattern = Pattern.compile(".*(?:" + parameter+ ")=(\\d*)");
            String text = link.attr("href");
            if (TextUtils.isEmpty(text))
                throw new EntityParsingException("Element does not contain href");

            Matcher matcher = pattern.matcher(text);
            if (!matcher.find())
                throw new EntityParsingException("Href doesn't contain such parameter");

            return Integer.parseInt(matcher.group(1));
        } catch (Exception e) {
            Log.d(getClass().getName(), e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    protected Date parseDate(String text) {
        try {
            if (text.equals("--"))
                return new Date(0L);

            try {
                return PARSING_FORMAT.parse(text);
            } catch (ParseException e) {
                throw new EntityParsingException("Error while parsing time string: `"+ text + "`");
            }
        } catch (Exception e) {
            Log.d(getClass().getName(), e.getMessage());
            e.printStackTrace();
        }

        return new Date(0L);
    }

    protected int UTCTimestamp(String text) {
        return (int)(parseDate(text).getTime()/1000L);
    }

    public class EntityParsingException extends Exception {
        public EntityParsingException(String error) {
            super("Error while parsing element " + String.valueOf(currentColumn)+ ":\nError: " + error + "\nElement: " + tempColumns.get(currentColumn).html());
        }
    }

}
