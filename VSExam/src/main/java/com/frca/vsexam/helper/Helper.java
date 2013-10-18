package com.frca.vsexam.helper;

import android.util.Log;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Helper {
    public static <T> List<T> extractObjectValues(List<?> list, String valueName) {
        if (list.isEmpty())
            return null;

        Class originalListClass = list.get(0).getClass();
        Field field;
        try {
            field = originalListClass.getField(valueName);
        } catch (NoSuchFieldException e) {
            Log.e("extractValues", "No such field in class " + originalListClass.getName());
            return null;
        }

        List<T> newList = new ArrayList<T>();
        for (Object o : list) {
            try {
                newList.add((T)field.get(o));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch(ClassCastException e) {
                e.printStackTrace();
            }
        }
        return newList;
    }

    public enum DateOutputType {
        DATE,
        TIME,
        DATE_TIME,
        TIME_DATE
    }

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
    public static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd. MM. yyyy HH:mm");
    public static final SimpleDateFormat TIME_DATE_FORMAT = new SimpleDateFormat("HH:mm dd. MM. yyyy");

    public static String getDateOutput(int seconds, DateOutputType outputType) {
        return getDateOutput(new Date(seconds*1000L), outputType);
    }

    public static String getDateOutput(Date date, DateOutputType outputType) {
        SimpleDateFormat format;
        switch (outputType) {
            case DATE: format = DATE_FORMAT; break;
            case TIME: format = TIME_FORMAT; break;
            case DATE_TIME: format = DATE_TIME_FORMAT; break;
            case TIME_DATE: format = TIME_DATE_FORMAT; break;
            default: return null;
        }

        return format.format(date);
    }

    public static boolean isValid(List list) {
        return list != null && !list.isEmpty();
    }
}
