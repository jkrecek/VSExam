package com.frca.vsexam.entities.exam;

import android.content.Context;

import com.frca.vsexam.R;
import com.frca.vsexam.entities.base.ParentEntity;
import com.frca.vsexam.entities.classmate.ClassmateList;
import com.frca.vsexam.helper.Helper;
import com.frca.vsexam.helper.RegisteringService;

import java.util.Date;

public class Exam extends ParentEntity {

    public enum Group {
        IS_REGISTERED(0, R.string.group_title_is_registered),
        CAN_REGISTER(1, R.string.group_title_can_register),
        CAN_NOT_REGISTER(2, R.string.group_title_can_not_register);

        private int id;
        private int titleRes;
        private Group(int id, int titleRes) {
            this.id = id;
            this.titleRes = titleRes;
        }

        public int getTitleRes() {
            return titleRes;
        }
        public int toInt() {
            return id;
        }

        public static Group fromInt(int id) {
            for (Group group : Group.values())
                if (group.id == id)
                    return group;

            return null;
        }

    }

    int registeredOnId;
    int studyId;
    int periodId;
    int authorId;
    int currentCapacity;
    int maxCapacity;
    int courseId;
    String courseCode;
    String courseName;
    String location;
    String type;
    String authorName;
    Date examDate;
    Date registerStart;
    Date registerEnd;
    Date unregisterEnd;
    Group group;

    ClassmateList classmates;
    private boolean toBeRegistered;

    Exam(int id) {
        super(id);
        toBeRegistered = false;
        registeredOnId = 0;
    }

    public void setRegistered(boolean apply) {
        if (apply)
            group = Group.IS_REGISTERED;
        else
            group = Group.CAN_REGISTER;
    }

    public boolean isRegistered() {
        return group == Group.IS_REGISTERED;
    }

    public void setToBeRegistered(Context context, boolean toBeRegistered) {
        if (this.toBeRegistered != toBeRegistered) {
            Helper.appendLog("Exam ROT is set to " + String.valueOf(toBeRegistered));
            if (toBeRegistered) {
                saveToFile(context);
                RegisteringService.setExamRegister(context, this);
            } else {
                deleteFile(context);
                RegisteringService.cancelExamRegister(context, this);
            }
        }

        this.toBeRegistered = toBeRegistered;
    }

    @Override
    protected void removeUnsavedValues() {
        classmates = null;
    }

    public int getRegisteredOnId() {
        return registeredOnId;
    }

    public void setRegisteredOnId(int registeredOnId) {
        this.registeredOnId = registeredOnId;
    }

    public int getStudyId() {
        return studyId;
    }

    public void setStudyId(int studyId) {
        this.studyId = studyId;
    }

    public int getPeriodId() {
        return periodId;
    }

    public void setPeriodId(int periodId) {
        this.periodId = periodId;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public int getCurrentCapacity() {
        return currentCapacity;
    }

    public void setCurrentCapacity(int currentCapacity) {
        this.currentCapacity = currentCapacity;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public Date getExamDate() {
        return examDate;
    }

    public void setExamDate(Date examDate) {
        this.examDate = examDate;
    }

    public Date getRegisterStart() {
        return registerStart;
    }

    public void setRegisterStart(Date registerStart) {
        this.registerStart = registerStart;
    }

    public Date getRegisterEnd() {
        return registerEnd;
    }

    public void setRegisterEnd(Date registerEnd) {
        this.registerEnd = registerEnd;
    }

    public Date getUnregisterEnd() {
        return unregisterEnd;
    }

    public void setUnregisterEnd(Date unregisterEnd) {
        this.unregisterEnd = unregisterEnd;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public boolean isToBeRegistered() {
        return toBeRegistered;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public ClassmateList getClassmates() {
        return classmates;
    }

    public void setClassmates(ClassmateList classmates) {
        this.classmates = classmates;
    }


}