package com.frca.vsexam.entities.base;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Created by KillerFrca on 16.10.13.
 */
public class ParentEntity implements Serializable {

    protected int id;

    protected static String[] fieldNamesToIgnore = null;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static File getDir(Context context, String type) {
        return context.getDir(type, Context.MODE_PRIVATE);
    }

    protected static String getFileName(Context context, String type, int id) {
        return getDir(context, type).getName() + '/' + String.valueOf(id) + ".data";
    }

    public boolean saveToFile(Context context) {
        String filename = getFileName(context, getClass().getName(), id);

        FileOutputStream fos  = null;
        ObjectOutputStream oos  = null;
        boolean keep = true;
        Object object;

        try {
            object = clone();
            if (fieldNamesToIgnore != null) {
                for (String fieldName : fieldNamesToIgnore) {
                    Field field = object.getClass().getField(fieldName);
                    field.set(object, null);
                }
            }

            fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
        } catch (Exception e) {
            keep = false;
            e.printStackTrace();
        } finally {
            try {
                if (oos != null)
                    oos.close();
                if (fos != null)
                    fos.close();
                if (keep == false)
                    context.deleteFile(filename);
            } catch (Exception e) {
                /* do nothing */
            }
        }

        return keep;
    }


    public void deleteFile(Context context) {
        String filename = getFileName(context, getClass().getName(), id);
        context.deleteFile(filename);
    }

    public static ParentEntity getFromFile(Class<? extends ParentEntity> requestedClass, Context context, int id) {
        String filename = getFileName(context, requestedClass.getName(), id);

        ParentEntity object = null;

        try {
            object = getFromFile(context.openFileInput(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return object;
    }

    public static ParentEntity getFromFile(File file) {
        ParentEntity object = null;

        try {
            object = getFromFile(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return object;
    }

    private static ParentEntity getFromFile(FileInputStream stream) {
        ObjectInputStream is = null;
        ParentEntity object = null;

        try {
            is = new ObjectInputStream(stream);
            object = (ParentEntity) is.readObject();
        } catch(Exception e) {
            //String val = e.getMessage();
            e.printStackTrace();
        } finally {
            try {
                if (stream != null)
                    stream.close();
                if (is != null)
                    is.close();
            } catch (Exception e) {
                /* do nothing */
            }
        }

        return object;
    }
}
