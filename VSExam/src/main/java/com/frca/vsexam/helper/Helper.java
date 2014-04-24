package com.frca.vsexam.helper;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frca.vsexam.R;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.util.EntityUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public abstract class Helper {
    public static <T> List<T> getValue(List<?> list, String valueName, boolean exclusive) {
        if (list.isEmpty())
            return null;

        Object[] sources = getSource(list, new String[] { valueName } );
        if (sources == null)
            return null;

        Object source = sources[0];

        List<T> newList = new ArrayList<T>();
        for (Object o : list) {
            try {
                T val;
                if (source instanceof Field)
                    val = (T)((Field)source).get(o);
                else
                    val = (T)((Method)source).invoke(o);

                if (!exclusive || !newList.contains(val))
                    newList.add(val);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return newList;
    }

    public static List<ObjectMap> getValuesMap(List<?> list, String[] valuesName, boolean exclusive) {
        if (list.isEmpty())
            return null;

        Object[] sources = getSource(list, valuesName);
        if (sources == null)
            return null;

        List<ObjectMap> newList = new ArrayList<ObjectMap>();
        for (Object o : list) {
            ObjectMap objectValues = new ObjectMap();
            for (int i = 0; i < valuesName.length; ++i) {
                Object source = sources[i];
                try {
                    Object val;
                    if (source instanceof Field)
                        val = ((Field)source).get(o);
                    else
                        val = ((Method)source).invoke(o);

                    objectValues.put(valuesName[i], val);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (!exclusive || !objectValues.isInList(newList))
                newList.add(objectValues);
        }

        return newList;
    }

    private static Object[] getSource(List<?> list, String[] valueNames) {
        Class originalListClass = list.get(0).getClass();
        Object[] sources = new Object[valueNames.length];
        for (int i = 0; i < valueNames.length; ++i) {
            String valueName = valueNames[i];
            String[] values = new String[] {
                valueName,
                "get" + valueName.substring(0, 1).toUpperCase() + valueName.substring(1)
            };

            for (String value : values) {
                try {
                    sources[i] = originalListClass.getField(value);
                    break;
                } catch (NoSuchFieldException e) { }

                try {
                    sources[i] = originalListClass.getMethod(value);
                    break;
                } catch (NoSuchMethodException e) { }
            }

            if (sources[i] == null) {
                Log.e("extractValues", "No such field or method `" + valueName + "` in class " + originalListClass.getName());
                return null;
            }
        }

        return sources;
    }

    public enum DateOutputType {
        DATE,
        TIME,
        DATE_TIME,
        TIME_DATE,
        FULL
    }

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
    public static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd. MM. yyyy HH:mm");
    public static final SimpleDateFormat TIME_DATE_FORMAT = new SimpleDateFormat("HH:mm dd. MM. yyyy");
    public static final SimpleDateFormat FULL_FORMAT = new SimpleDateFormat("dd. MM. yyyy HH:mm:ss.SSS");


    public static String getDateOutput(long milis, DateOutputType outputType) {
        return getDateOutput(new Date(milis), outputType);
    }

    public static String getDateOutput(Date date, DateOutputType outputType) {
        if (date == null)
            return "";

        if (date.getTime() == 0L)
            return "--";

        SimpleDateFormat format;
        switch (outputType) {
            case DATE: format = DATE_FORMAT; break;
            case TIME: format = TIME_FORMAT; break;
            case DATE_TIME: format = DATE_TIME_FORMAT; break;
            case TIME_DATE: format = TIME_DATE_FORMAT; break;
            case FULL: format = FULL_FORMAT; break;
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

    public static void dumpRequest(HttpRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("Dumping http request\n");
        sb.append(request.getRequestLine().getMethod() + " " + request.getRequestLine().getUri() + " " + request.getRequestLine().getProtocolVersion().toString() + "\n");

        for (Header header : request.getAllHeaders())
            sb.append(header.getName() + ": " + header.getValue() + "\n");

        if (request instanceof HttpEntityEnclosingRequest) {
            HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
            if (entity.getContentEncoding() != null)
                sb.append(entity.getContentEncoding().getName() + ": " + entity.getContentEncoding().getValue() + "\n");
            if (entity.getContentLength() != 0L)
                sb.append("Content-Length: " + String.valueOf(entity.getContentLength()) + "\n");
            if (entity.getContentType() != null)
                sb.append(entity.getContentType().getName() + ": " + entity.getContentType().getValue() + "\n");

            try {
                sb.append("Content: " + EntityUtils.toString(entity) + "\n");
            } catch (IOException e) {
                Log.e("ERROR_DUMPING", "Error:" + e.getMessage());
                e.printStackTrace();
            }
        }

        Log.d("REQUEST_DUMP", sb.toString());
    }

    private static String[] dayPart = new String[] { "den", "dny", "dní" };
    private static String[] hourPart = new String[] { "hodina", "hodiny", "hodin" };
    private static String[] minutePart = new String[] { "minuta", "minuty", "minut" };
    private static String[] secondPart = new String[] { "vteřina", "vteřiny", "vteřin" };

    public static String secondsCountdown(long milis, boolean show_seconds) {
        int total_seconds = (int) (milis / 1000L);
        int days    = (int) Math.floor(total_seconds / 86400);
        int hours   = (int) Math.floor((total_seconds - (days * 86400))/ 3600);
        int minutes = (int) Math.floor((total_seconds - (days * 86400) - (hours * 3600)) / 60);
        int seconds = (show_seconds || minutes == 0) ? (int) Math.floor(total_seconds - (days * 86400) - (hours * 3600) - (minutes * 60)) : 0;

        return (getNamedTimeValue(days, dayPart) + " " +
                getNamedTimeValue(hours, hourPart) + " " +
                getNamedTimeValue(minutes, minutePart) + " " +
                getNamedTimeValue(seconds, secondPart)).trim();
    }

    private static String getNamedTimeValue(int unit, String[] parts) {
        if (unit == 0)
            return "";
        else if (unit == 1)
            return String.valueOf(unit) + " " + parts[0];
        else if (unit < 5)
            return String.valueOf(unit) + " " + parts[1];
        else
            return String.valueOf(unit) + " " + parts[2];
    }

    private static String logFileName = null;
    public static synchronized void appendLog(String text) {
        try {
            File logFile;
            if (logFileName == null) {
                logFile = getDataDirectoryFile("log", "output_" + String.valueOf(System.currentTimeMillis() / 1000L), "log");
                logFileName = logFile.getPath();
            } else {
                logFile = new File(logFileName);
            }

            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            Log.d("TRACE_DEBUG", text);
            buf.append(getDateOutput(new Date(), DateOutputType.FULL) + ":  " + text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File getDataDirectoryFile(String subDir, String filename, String fileType) throws IOException {
        String path = Environment.getExternalStorageDirectory().getPath() + "/VSExam/";
        if (!TextUtils.isEmpty(subDir))
            path += subDir + "/";

        path += filename + "." + fileType;
        File file = new File(path);
        file.getParentFile().mkdirs();
        if (file.exists())
            file.createNewFile();

        return file;
    }

}
