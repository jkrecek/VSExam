package com.frca.vsexam.entities.vsedata;

import android.text.TextUtils;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VSEStringParser {

    private static final Pattern pattern = Pattern.compile("([A-Z]{3}) ([A-Z])-([A-Z]+)-([A-Z]+)-?([0-9]?[A-Z]*)? ([a-z]*) \\[[a-z]* ([0-9]{1,2}), ([A-Z]*)\\]");

    private String mInputString;
    private VSEStructure mVSEStructure;

    private VSEStructureElement.Faculty mFaculty;
    private VSEStructureElement.StudyType mStudyType;
    private VSEStructureElement.Programme mProgramme;
    private VSEStructureElement mStudyField;
    private VSEStructureElement mSpecialization;
    private VSEStructureElement.Form mForm;
    private int mSemester;
    private String mStudyPlan;

    public static VSEStringParser parse(String inputString, VSEStructure data) throws ParseException {
        VSEStringParser parser = new VSEStringParser();
        parser.mInputString = inputString;
        parser.mVSEStructure = data;

        parser.doParse();

        return parser;
    }

    private void doParse() throws ParseException {
        Matcher matcher = pattern.matcher(mInputString);

        if (matcher.find()) {
            mFaculty = mVSEStructure.getByCode(matcher.group(1));
            mStudyType = mFaculty.types.getByCode(matcher.group(2));
            mProgramme = mStudyType.programmes.getByCode(matcher.group(3));
            mStudyField = mProgramme.fields.getByCode(matcher.group(4));

            String specializationText = matcher.group(5);
            if (!TextUtils.isEmpty(specializationText))
                mSpecialization = mStudyType.specializations.getByCode(specializationText);

            mForm = VSEStructureElement.Form.fromString(matcher.group(6));
            mSemester = Integer.valueOf(matcher.group(7));
            mStudyPlan = matcher.group(8);
        }
    }

    public VSEStructureElement.Faculty getFaculty() {
        return mFaculty;
    }

    public VSEStructureElement.StudyType getStudyType() {
        return mStudyType;
    }

    public VSEStructureElement.Programme getProgramme() {
        return mProgramme;
    }

    public VSEStructureElement getStudyField() {
        return mStudyField;
    }

    public VSEStructureElement getSpecialization() {
        return mSpecialization;
    }

    public VSEStructureElement.Form getForm() {
        return mForm;
    }

    public int getSemester() {
        return mSemester;
    }

    public String getStudyPlan() {
        return mStudyPlan;
    }
}
