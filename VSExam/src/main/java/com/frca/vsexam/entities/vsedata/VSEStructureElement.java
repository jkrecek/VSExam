package com.frca.vsexam.entities.vsedata;

import com.frca.vsexam.R;

import org.apache.http.ParseException;

import java.util.ArrayList;

public class VSEStructureElement {

    public static class List<T extends VSEStructureElement> extends ArrayList<T> {
        public T getByCode(String code) throws ParseException {
            for (T element : this) {
                if (element.hasCode(code))
                    return element;
            }

            throw new ParseException("Code `" + code + "` not found.");
        }
    }

    private ArrayList<String> codes = new ArrayList<String>();
    public String name;

    public void addCode(String code) {
        if (!codes.contains(code))
            codes.add(code);
    }

    public boolean hasCode(String code) {
        return codes.contains(code);
    }

    protected enum Plan {
        OTHERS(5, new int[] {1, 2, 4}),
        DOC(23, new int[] {3});

        public int mId;
        public int[] mPlans;
        private Plan(int id, int[] plans) {
            mId = id;
            mPlans = plans;
        }

        public static Plan getValueForId(int id) {
            for (Plan plan : values()) {
                if (plan.mId == id)
                    return plan;
            }

            return null;
        }
    }

    public static class Faculty extends VSEStructureElement {
        public int id;
        public List<StudyType> types = new List<StudyType>();
    }

    public static class StudyType extends VSEStructureElement {
        public List<Programme> programmes = new List<Programme>();
        public List<VSEStructureElement> specializations = new List<VSEStructureElement>();
    }

    public static class Programme extends VSEStructureElement {
        public List<VSEStructureElement> fields = new List<VSEStructureElement>();
    }

    public enum Form {
        FULL_TIME (R.string.form_full_time),
        COMBINED(R.string.form_combined),
        DISTANCE (R.string.form_distance);

        public final int resourceId;
        private Form(int resourceId) {
            this.resourceId = resourceId;
        }

        public static Form fromString(String formString) throws ParseException {
            char first = formString.charAt(0);
            switch (first) {
                case 'p':   return Form.FULL_TIME;
                case 'c':   return Form.COMBINED;
                case 'd':   return Form.DISTANCE;
                default:    throw new ParseException("Error while parsing form, unknown study form `" + formString + "`");
            }
        }
    }
}
