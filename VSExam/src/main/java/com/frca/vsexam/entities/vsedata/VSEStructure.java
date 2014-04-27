package com.frca.vsexam.entities.vsedata;

import android.content.Context;
import android.content.res.AssetManager;
import android.widget.Toast;

import com.frca.vsexam.helper.Helper;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class VSEStructure extends VSEStructureElement.List<VSEStructureElement.Faculty> {

    private static String getFileName(Context context) {
        return VSEStructure.class.getSimpleName() + "_" + context.getResources().getConfiguration().locale.getLanguage() + ".json";
    }

    private static File getFilePath(Context context) {
        return context.getFileStreamPath(getFileName(context));
    }

    public void save(Context context) {
        File file = getFilePath(context);
        String output = new Gson().toJson(this);
        Helper.writeToFile(output, file, false);
    }

    public static VSEStructure load(Context context) {
        File file = getFilePath(context);
        String content = Helper.readFromFile(file);
        if (content == null) {
            AssetManager assets = context.getAssets();
            try {
                InputStream is = assets.open(getFileName(context));
                content = Helper.readFromStream(is);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "No VŠE structure could be found", Toast.LENGTH_LONG).show();
                return null;
            }
        }

        return new Gson().fromJson(content, VSEStructure.class);
    }

}
