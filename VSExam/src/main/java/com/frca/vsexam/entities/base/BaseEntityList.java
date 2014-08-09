package com.frca.vsexam.entities.base;

import android.content.Context;

import com.frca.vsexam.helper.Utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseEntityList<T extends ParentEntity> extends ArrayList<T> {

    protected T getFromFile(Context context, int id) {
        String filename = ParentEntity.getFileName(getGenericTypeClass(), id);

        T object = null;
        FileInputStream fis = null;
        ObjectInputStream is = null;

        try {
            fis = context.openFileInput(filename);
            is = new ObjectInputStream(fis);
            object = (T) is.readObject();
            object.isKeptLocally = true;
            return object;
        } catch (FileNotFoundException e) {
            return null;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            Utils.close(fis);
            Utils.close(is);
        }
    }


    private Class getGenericTypeClass() {
        return (Class)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public T find(int id) {
        for (T entity: this)
            if (entity.getId() == id)
                return entity;

        return null;
    }

    public void loadSaved(Context context) {
        final String filePrefix = getGenericTypeClass().getSimpleName() + "_";
        Pattern pattern = Pattern.compile(filePrefix + "(\\d+).data");
        Matcher matcher;
        for (String fileName : context.getFilesDir().list()) {
            if (fileName.startsWith(filePrefix) && fileName.endsWith(".data")) {
                matcher = pattern.matcher(fileName);
                if (matcher.find()) {
                    int id = Integer.parseInt(matcher.group(1));
                    if (find(id) == null) {
                        T entity = getFromFile(context, id);
                        if (entity != null)
                            add(entity);
                    }
                }
            }
        }
    }

    public T load(Context context, int id) {
        T entity = find(id);
        if (entity != null)
            return entity;

        entity = getFromFile(context, id);
        if (entity != null) {
            return entity;
        }

        return null;
    }
}
