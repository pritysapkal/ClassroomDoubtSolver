package com.example.classroomdoubt.models;

public class Answer {
    private String answerId;
    private String doubtId;
    private String facultyId;
    private String facultyName;
    private String facultyDept;
    private String answerText;
    private float studentRating; // student rates the answer
    private boolean isBestAnswer;
    private long createdAt;

    public Answer() {}

    public Answer(String doubtId, String facultyId, String facultyName,
                  String facultyDept, String answerText) {
        this.doubtId = doubtId;
        this.facultyId = facultyId;
        this.facultyName = facultyName;
        this.facultyDept = facultyDept;
        this.answerText = answerText;
        this.studentRating = 0;
        this.isBestAnswer = false;
        this.createdAt = System.currentTimeMillis();
    }

    public String getAnswerId() { return answerId; }
    public void setAnswerId(String answerId) { this.answerId = answerId; }
    public String getDoubtId() { return doubtId; }
    public void setDoubtId(String doubtId) { this.doubtId = doubtId; }
    public String getFacultyId() { return facultyId; }
    public void setFacultyId(String facultyId) { this.facultyId = facultyId; }
    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }
    public String getFacultyDept() { return facultyDept; }
    public void setFacultyDept(String facultyDept) { this.facultyDept = facultyDept; }
    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }
    public float getStudentRating() { return studentRating; }
    public void setStudentRating(float studentRating) { this.studentRating = studentRating; }
    public boolean isBestAnswer() { return isBestAnswer; }
    public void setBestAnswer(boolean bestAnswer) { isBestAnswer = bestAnswer; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}