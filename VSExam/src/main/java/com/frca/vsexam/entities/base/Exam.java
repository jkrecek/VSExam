package com.frca.vsexam.entities.base;

import android.content.Context;

import com.frca.vsexam.entities.lists.ClassmateList;
import com.frca.vsexam.helper.RegisteringService;

import java.util.Date;

public class Exam extends ParentEntity {

    public enum Group {
        IS_REGISTERED(0),
        CAN_REGISTER(1),
        CAN_NOT_REGISTER(2);

        private int id;
        private Group(int id) {
            this.id = id;
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

    private int registeredOnId;
    private int studyId;
    private int periodId;
    private int authorId;
    private int currentCapacity;
    private int maxCapacity;
    private String courseCode;
    private String courseName;
    private String location;
    private String type;
    private String authorName;
    private Date examDate;
    private Date registerStart;
    private Date registerEnd;
    private Date unregisterEnd;
    private Group group;

    private ClassmateList classmates;
    private boolean registerOnTime;

    static {
        fieldNamesToIgnore = new String[] { "classmates" } ;
    }

    public Exam() {
        registerOnTime = false;
        registeredOnId = 0;
    }


    public ClassmateList getClassmates() {
        return classmates;
    }

    public void setClassmates(ClassmateList classmates) {
        this.classmates = classmates;
    }

    public void setRegistered() {
        group = Group.IS_REGISTERED;
    }

    public boolean isRegistered() {
        return group == Group.IS_REGISTERED;
    }

    public void setRegisterOnTime(Context context, boolean registerOnTime) {
        if (this.registerOnTime != registerOnTime) {
            if (registerOnTime) {
                saveToFile(context);
                RegisteringService.setExamRegister(context, this);
            } else {
                deleteFile(context);
                RegisteringService.cancelExamRegister(context, this);
            }
        }

        this.registerOnTime = registerOnTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public boolean isRegisterOnTime() {
        return registerOnTime;
    }

}
