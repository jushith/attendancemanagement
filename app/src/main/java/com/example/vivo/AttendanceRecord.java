package com.example.vivo;

import java.util.List;

public class AttendanceRecord {
    private String course;
    private String studentClass; // Use "class" instead of "studentClass" since "class" is a reserved keyword in Java
    private List<String> presentStudents;

    // Default constructor required for Firestore
    public AttendanceRecord() {}

    // Parameterized constructor
    public AttendanceRecord(String course, String studentClass, List<String> presentStudents) {
        this.course = course;
        this.studentClass = studentClass;
        this.presentStudents = presentStudents;
    }

    // Getters and setters
    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getStudentClass() {
        return studentClass;
    }

    public void setStudentClass(String studentClass) {
        this.studentClass = studentClass;
    }

    public List<String> getPresentStudents() {
        return presentStudents;
    }

    public void setPresentStudents(List<String> presentStudents) {
        this.presentStudents = presentStudents;
    }
}
