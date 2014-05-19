package com.frca.vsexam.entities.vsedata;

import android.content.Context;
import android.content.res.AssetManager;
import android.widget.Toast;

import com.frca.vsexam.R;
import com.frca.vsexam.helper.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.ParseException;

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
        Utils.writeToFile(toJsonString(), file, false);
    }

    public static VSEStructure load(Context context) {
        File file = getFilePath(context);
        String content = Utils.readFromFile(file);
        if (content == null) {
            AssetManager assets = context.getAssets();
            try {
                InputStream is = assets.open(getFileName(context));
                content = Utils.readFromStream(is);
            } catch (IOException e) {
                Toast.makeText(context, R.string.no_vse_structure_found, Toast.LENGTH_LONG).show();
                return null;
            }
        }

        return fromString(content);
    }

    public VSEStructureElement getSpecialization(String code) {
        for (VSEStructureElement.Faculty faculty : this) {
            for (VSEStructureElement.StudyType studyType : faculty.types) {
                try {
                    return studyType.specializations.getByCode(code);
                } catch (ParseException e) { /* expected */ }
            }
        }

        return null;
    }

    public String toJsonString() {
        return new Gson().toJson(this);
    }

    public String toPrettyJsonString() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }

    public static VSEStructure fromString(String string) {
        return new Gson().fromJson(string, VSEStructure.class);
    }

}
