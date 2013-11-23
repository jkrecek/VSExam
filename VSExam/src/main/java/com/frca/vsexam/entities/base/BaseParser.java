package com.frca.vsexam.entities.base;

import android.text.TextUtils;

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
        } catch (EntityParsingException e) {
            e.printStackTrace();
            return null;
        } finally {
            this.tempColumns = null;
            currentColumn = 0;
        }
    }

    protected abstract ParentEntity doParse() throws EntityParsingException;

    protected String getColumnContent(int column, boolean stripHtml) throws EntityParsingException {
        currentColumn = column;
        Element element = getElement(column, "small");
        if (stripHtml)
            return element.text().trim();
        else
            return element.html().trim();
    }

    protected Element getLinkFromColumn(int column) throws EntityParsingException {
        currentColumn = column;
        return getElement(column, "small a");
    }

    protected Element getElement(int column, String select) throws EntityParsingException {
        Element element = tempColumns.get(column).select(select).first();
        if (element != null)
            return element;
        else
            throw new EntityParsingException("No such element `" + select + "`" );
    }

    protected int extractParameterFromLink(Element link, String parameter) throws EntityParsingException {
        Pattern pattern = Pattern.compile(".*(?:" + parameter+ ")=(\\d*)");
        String text = link.attr("href");
        if (TextUtils.isEmpty(text))
            throw new EntityParsingException("Element does not contain href");

        Matcher matcher = pattern.matcher(text);
        if (!matcher.find())
            throw new EntityParsingException("Href doesn't contain such parameter");

        return Integer.parseInt(matcher.group(1));
    }

    protected Date parseDate(String text) throws EntityParsingException {
        try {
            return PARSING_FORMAT.parse(text);
        } catch (ParseException e) {
            throw new EntityParsingException("Error while parsing time string: `"+ text + "`");
        }
    }

    protected int UTCTimestamp(String text) throws EntityParsingException {
        return (int)(parseDate(text).getTime()/1000L);
    }

    public class EntityParsingException extends Exception {
        public EntityParsingException(String error) {
            super("Error while parsing element " + String.valueOf(currentColumn)+ ":\n" + error + "\n" + tempColumns.get(currentColumn).html());
        }
    }

}