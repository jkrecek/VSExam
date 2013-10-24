package com.frca.vsexam.helper;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;

import com.frca.vsexam.R;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StudyProgramParser {

    private static final Pattern pattern = Pattern.compile("([A-Z]{3}) ([A-Z])-([A-Z]*)-([A-Z]*)(?:-([0-9][A-Z]*))? ([a-z]*) \\[[a-z]* ([0-9]{1,2}), ([A-Z]*)\\]");

    private String programString;
    private Resources resources;

    private static final String EMPTY_STRING = "";

    public enum Faculty {
        FFU(1, R.string.faculty_ffu),
        FMV(2, R.string.faculty_fmv),
        FPH(3, R.string.faculty_fph),
        FIS(4, R.string.faculty_fis),
        NF(5,  R.string.faculty_nf),
        FMJH(6,R.string.faculty_fmjh);

        public final int id;
        public final int resourceId;
        private Faculty(int id, int resourceId) {
            this.id = id;
            this.resourceId = resourceId;
        }

        public static Faculty get(String formString) throws ParseException {
            try {
                return Faculty.valueOf(formString);
            } catch (IllegalArgumentException e) {
                throw new ParseException("Error while parsing form, unknown faculty `" + formString + "`", 6);
            }
        }

        public static Faculty fromInt(int i) {
            switch (i) {
                case 1: return FFU;
                case 2: return FMV;
                case 3: return FPH;
                case 4: return FIS;
                case 5: return NF;
                case 6: return FMJH;
                default:return null;
            }
        }
    }

    public enum Form {
        FULL_TIME (R.string.form_full_time),
        COMBINED(R.string.form_combined),
        DISTANCE (R.string.form_distance);

        public final int resourceId;
        private Form(int resourceId) {
            this.resourceId = resourceId;
        }

        private static Form get(String formString) throws ParseException {
            char first = formString.charAt(0);
            switch (first) {
                case 'p':   return Form.FULL_TIME;
                case 'c':   return Form.COMBINED;
                case 'd':   return Form.DISTANCE;
                default:    throw new ParseException("Error while parsing form, unknown study form `" + formString + "`", 6);
            }
        }
    }

    public enum Type {
        BACHELOR (R.string.type_bachelor),
        MASTER (R.string.type_master),
        DOCTORAL (R.string.type_doctoral);

        public final int resourceId;
        private Type(int resourceId) {
            this.resourceId = resourceId;
        }

        private static Type get(String typeString) throws ParseException {
            char first = typeString.charAt(0);
            switch (first) {
                case 'B':   return Type.BACHELOR;
                case 'N':
                case 'C':   return Type.MASTER;
                case 'D':   return Type.DOCTORAL;
                default:    throw new ParseException("Error while parsing form, unknown study type `" + typeString + "`", 6);
            }
        }
    }

    public enum Programme {
        FU, FINAC, ME, PL, EGEI, EM, ET, AI, KM, ES, HP;

        public int getResource() {
            return Helper.getResourceValue(this, "p_");
        }
    }

    public enum StudyField {
        BP, FI, UC, UP, DP, FO, FG, TVEP, RC, IB, MO, MS, PP, CR, EI, IDS, MP, PL, AM, CFM, EPM, PE, EK, PEM, PEME, AI, MM, SD, SM, SE, IN, ME, PS,
        EOV, ST, EO, ED, SP, IM, IT, KI, PI, ZT, EKE, NH, RE, LAE, EA, HP, HD, RS, VS, ZP, ET, M, _6MM, MANAG, ES, MOE, MH;

        public int getResource() {
            return Helper.getResourceValue(this, "f_");
        }
    }

    public enum Spec {
        AU, CO, DB, FG, OC, PE, UC, UP, VR, BS, CR, CS, DI, EE, IR, KJ, KK, MI, OP, PR, RS, ZP, AF, AM, AP, EN, FI, HR, LS, LT, MK, MS, SP, TV, AD, RV,
        BE, EP, EZ, HD, MA, MV, NO, RE, RR, SS, VS, MP, MZ;

        public int getResource(Faculty faculty) {
            return Helper.getResourceValue(this, "s_" + String.valueOf(faculty.id));
        }
    }

    private Faculty faculty;
    private Type type;
    private Programme programme;
    private StudyField field;
    private Spec spec;
    private Form form;
    private int semester;
    private String unknownField;

    private Context context;

    public StudyProgramParser(Context context, String programString) throws ParseException {
        this.programString = programString;
        this.resources = context.getResources();
        this.context = context;

        parse();
    }

    public StudyProgramParser(Context context){
        this.resources = context.getResources();
    }

    private void parse() throws ParseException {
        Matcher matcher = pattern.matcher(programString);

        if (matcher.find()) {
            faculty = Faculty.get(matcher.group(1));
            type = Type.get(matcher.group(2));
            programme = Programme.valueOf(matcher.group(3));
            String f = matcher.group(4);
            if (Character.isDigit(f.charAt(0)))
                f = "_" + f;
            field = StudyField.valueOf(f);
            String specText = matcher.group(5);
            if (!TextUtils.isEmpty(specText))
                spec = Spec.valueOf(matcher.group(5));
            else
                spec = null;
            form = Form.get(matcher.group(6));
            semester = Integer.valueOf(matcher.group(7));
            unknownField = matcher.group(8);
        }
    }

    public String getFacultyString() {
        try {
            return resources.getString(faculty.resourceId);
        } catch (Resources.NotFoundException e) {
            return EMPTY_STRING;
        }
    }

    public String getProgrammeString() {
        return resources.getString(programme.getResource());
    }

    public String getFieldString() {
        return resources.getString(field.getResource());
    }

    public String getSpecializationString() {
        return spec == null ? null : resources.getString(spec.getResource(faculty));
    }

    public String getFormTypeString() {
        try {
            return resources.getString(form.resourceId) + ", " + resources.getString(type.resourceId);
        } catch (Resources.NotFoundException e) {
            return EMPTY_STRING;
        }
    }

    public String getSemesterString() {
        String semesterFormatter = resources.getString(R.string.semester_nth);
        return String.format(semesterFormatter, semester);
    }

    /*
     TEST STUFF
      */
    public void runTest() {

        List<String> programmeIds = new ArrayList<String>();
        List<String> studyFieldIds = new ArrayList<String>();
        List<String> specsIds = new ArrayList<String>();
        Field[] fields = R.string.class.getDeclaredFields();

        List<String> errors = new ArrayList<String>();

        Helper.reportErrors = false;
        for (Field f : fields) {
            String fieldName = f.getName();

            if (fieldName.startsWith("p_"))
                programmeIds.add(fieldName.substring(2));
            else if (fieldName.startsWith("f_"))
                studyFieldIds.add(fieldName.substring(2));
            else if (fieldName.startsWith("s_"))
                specsIds.add(fieldName.substring(3));
        }

        for (Programme e : Programme.values()) {
            if (TextUtils.isEmpty(resources.getString(e.getResource())))
                errors.add("No resource for Programme enum `" + e.toString() + "`");
        }

        for (String id : programmeIds) {
            try {
                Programme.valueOf(id.toUpperCase());
            } catch (IllegalArgumentException e) {
                errors.add("No Programme enum for resource `" + id + "`");
            }
        }

        for (StudyField f : StudyField.values()) {
            if (TextUtils.isEmpty(resources.getString(f.getResource())))
                errors.add("No resource for StudyField enum `" + f.toString() + "`");
        }

        for (String id : studyFieldIds) {
            try {
                StudyField.valueOf(id.toUpperCase());
            } catch (IllegalArgumentException e) {
                errors.add("No StudyField enum for resource `" + id + "`");
            }
        }

        for (Spec s : Spec.values()) {
            boolean found = false;
            for (int i = 1; i <= Faculty.values().length; ++i) {
                int id = s.getResource(Faculty.fromInt(i));
                if (id != 0 && !TextUtils.isEmpty(resources.getString(id))) {
                    found = true;
                    break;
                }
            }

            if (!found)
                errors.add("Missing resource for Spec enum `" + s.toString() + "`");
        }

        for (String id : specsIds) {
            try {
                Spec.valueOf(id.toUpperCase());
            } catch (IllegalArgumentException e) {
                errors.add("No Spec enum for resource `" + id + "`");
            }
        }

        if (!errors.isEmpty()) {
            String listString = "";
            for (String s : errors)
                listString += "\n" + s;

            Log.e("ERRORS", listString);
        } else {
            Log.e("ERRORS", "none");
        }

        Helper.reportErrors = true;
    }
}
