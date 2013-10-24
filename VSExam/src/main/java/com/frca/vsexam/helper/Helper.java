package com.frca.vsexam.helper;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frca.vsexam.R;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public abstract class Helper {
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

    public static boolean reportErrors = true;
    public static int getResourceValue(Enum value, String prefix) {
        String fieldName = "";
        if (prefix != null)
            fieldName += prefix;
        fieldName += value.toString().toLowerCase();

        try {
            return R.string.class.getField(fieldName).getInt(null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            if (reportErrors)
                Log.e(Helper.class.getName(), "Missing resource for enum `" + value.toString() + "` (" + fieldName + ")");
        }

        return 0;
    }

    public enum Orientation {
        VERTICAL,
        HORIZONTAL
    }

    public static View getDivider(LayoutInflater inflater, Orientation orientation) {
        ViewGroup.LayoutParams params;
        if (orientation == Orientation.VERTICAL)
            params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        else
            params = new ViewGroup.LayoutParams(1, ViewGroup.LayoutParams.MATCH_PARENT);

        View div = new View(inflater.getContext());
        div.setLayoutParams(params);
        div.setBackgroundResource(R.color.divider_light);

        return div;
    }

}
