package com.edusmart.model;

/**
 * Grade model - represents a student's grade for a subject/course (used in Bulletin).
 */
public class Grade {

    private int id;
    private int studentId;
    private int courseId;
    private double note;
    private Double coefficient;
    private String session; // e.g. "Principale"
    private String semester;
    private String academicYear;
    private Integer moduleId;

    public Grade() {}

    public Grade(int id, int studentId, int courseId, double note, String semester, String academicYear) {
        this.id = id;
        this.studentId = studentId;
        this.courseId = courseId;
        this.note = note;
        this.semester = semester;
        this.academicYear = academicYear;
    }

    // Getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public double getNote() { return note; }
    public void setNote(double note) { this.note = note; }

    public Double getCoefficient() { return coefficient; }
    public void setCoefficient(Double coefficient) { this.coefficient = coefficient; }

    public String getSession() { return session; }
    public void setSession(String session) { this.session = session; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public Integer getModuleId() { return moduleId; }
    public void setModuleId(Integer moduleId) { this.moduleId = moduleId; }

    @Override
    public String toString() {
        return "Grade{id=" + id + ", studentId=" + studentId + ", note=" + note + "}";
    }
}
