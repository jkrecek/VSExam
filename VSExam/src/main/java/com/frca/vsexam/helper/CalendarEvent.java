package com.frca.vsexam.helper;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;

public class CalendarEvent {

    public static String DTSTART;
    public static String DTEND;
    public static String TITLE;
    public static String DESCRIPTION;
    public static String CALENDAR_ID;
    public static String EVENT_TIMEZONE;
    public static String EVENT_LOCATION;
    public static Uri CONTENT_URI;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            setUpICS();
        } else {
            setUpLegacy();
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static void setUpICS() {
        DTSTART = CalendarContract.Events.DTSTART;
        DTEND = CalendarContract.Events.DTEND;
        TITLE = CalendarContract.Events.TITLE;
        DESCRIPTION = CalendarContract.Events.DESCRIPTION;
        CALENDAR_ID = CalendarContract.Events.CALENDAR_ID;
        EVENT_TIMEZONE = CalendarContract.Events.EVENT_TIMEZONE;
        EVENT_LOCATION = CalendarContract.Events.EVENT_LOCATION;
        CONTENT_URI = CalendarContract.Events.CONTENT_URI;
    }

    private static void setUpLegacy() {
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
