package com.frca.vsexam;

import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
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
}
