package com.frca.vsexam;


import com.frca.vsexam.helper.Helper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by KillerFrca on 5.10.13.
 */
public class ExamList extends ArrayList<Exam> {

    public List<String> getCourseNames() {
        return Helper.extractObjectValues(this, "courseName");
    }
}
