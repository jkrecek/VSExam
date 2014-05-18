package com.frca.vsexam.helper;

import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;

public class CalendarEvent {

    public static final String DTSTART;
    public static final String DTEND;
    public static final String TITLE;
    public static final String DESCRIPTION;
    public static final String CALENDAR_ID;
    public static final String EVENT_TIMEZONE;
    public static final String EVENT_LOCATION;
    public static final Uri CONTENT_URI;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            DTSTART = CalendarContract.Events.DTSTART;
            DTEND = CalendarContract.Events.DTEND;
            TITLE = CalendarContract.Events.TITLE;
            DESCRIPTION = CalendarContract.Events.DESCRIPTION;
            CALENDAR_ID = CalendarContract.Events.CALENDAR_ID;
            EVENT_TIMEZONE = CalendarContract.Events.EVENT_TIMEZONE;
            EVENT_LOCATION = CalendarContract.Events.EVENT_LOCATION;
            CONTENT_URI = CalendarContract.Events.CONTENT_URI;
        } else {
            DTSTART = "dtstart";
            DTEND = "dtend";
            TITLE = "title";
            DESCRIPTION = "description";
            CALENDAR_ID = "calendar_id";
            EVENT_TIMEZONE = "eventTimezone";
            EVENT_LOCATION = "eventLocation";
            CONTENT_URI = Uri.parse("content://com.android.calendar/events");
        }
    }
}
