package com.frca.vsexam.entities.base;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by KillerFrca on 16.10.13.
 */
public class ParentEntity implements Serializable, Cloneable {

    protected int id;

    protected void removeUnsavedValues() { }

    protected static String getFileName(Context context, Class<? extends ParentEntity> entityClass, int id) {
        return entityClass.getSimpleName() + "_" + String.valueOf(id) + ".data";
    }

    public boolean saveToFile(Context context) {
        String filename = getFileName(context, getClass(), id);

        FileOutputStream fos  = null;
        ObjectOutputStream oos  = null;
        boolean keep = true;
        ParentEntity object;

        try {
            object = (ParentEntity) clone();
            object.removeUnsavedValues();
            fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
        } catch (Exception e) {
            keep = false;
            Log.e(getClass().getName(), "Error in saveToFile()");
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
        String filename = getFileName(context, getClass(), id);
        context.deleteFile(filename);
    }

    public static ParentEntity getFromFile(Class<? extends ParentEntity> requestedClass, Context context, int id) {
        String filename = getFileName(context, requestedClass, id);

        ParentEntity object = null;

        try {
            object = getFromFile(context.openFileInput(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(ParentEntity.class.getName(), "Error in getFromFile(...)");
        }

        return object;
    }

    public static ParentEntity getFromFile(File file) {
        ParentEntity object = null;

        try {
            object = getFromFile(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(ParentEntity.class.getName(), "Error in getFromFile(File)");
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
