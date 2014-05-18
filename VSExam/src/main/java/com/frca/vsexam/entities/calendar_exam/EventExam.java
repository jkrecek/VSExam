package com.frca.vsexam.entities.calendar_exam;

public class EventExam {
    private final long mExamId;
    private final long mEventId;

    private static final char SPLIT_CHAR = ':';

    public EventExam(long examId, long eventId) {
        mExamId = examId;
        mEventId = eventId;
    }

    public long getExamId() {
        return mExamId;
    }

    public long getEventId() {
        return mEventId;
    }

    public String asString() {
        return String.valueOf(mExamId) + SPLIT_CHAR + String.valueOf(mEventId);
    }

    public static EventExam fromString(String string) {
        int idx = string.indexOf(SPLIT_CHAR);
        int examId = Integer.parseInt(string.substring(0, idx));
        int eventId = Integer.parseInt(string.substring(idx+1));
        return new EventExam(examId, eventId);
    }
}
