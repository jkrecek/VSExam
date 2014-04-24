package com.frca.vsexam.entities.base;

import android.content.Context;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ParentEntity implements Serializable, Cloneable {

    protected int id;

    protected boolean isKeptLocally = false;

    protected ParentEntity(int id) {
        this.id = id;

    }

    protected void removeUnsavedValues() { }

    protected static String getFileName(Class<? extends ParentEntity> entityClass, int id) {
        return entityClass.getSimpleName() + "_" + String.valueOf(id) + ".data";
    }

    public boolean saveToFile(Context context) {
        String filename = getFileName(getClass(), id);

        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        boolean keep = true;
        ParentEntity object;

        try {
            isKeptLocally = true;
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
                if (!keep)
                    context.deleteFile(filename);
            } catch (Exception e) {
                /* do nothing */
            }
        }

        return keep;
    }

    public void deleteFile(Context context) {
        isKeptLocally = false;
        String filename = getFileName(getClass(), id);
        context.deleteFile(filename);
    }


    public int getId() {
        return id;
    }

    public boolean isKeptLocally() {
        return isKeptLocally;
    }
}
