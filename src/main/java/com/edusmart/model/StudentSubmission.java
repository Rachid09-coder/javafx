package com.edusmart.model;

public class StudentSubmission {
    private int studentId;
    private String name;
    private String prenom;
    private String email;
    private String role;
    private String filePath;

    public StudentSubmission() {}

    public StudentSubmission(int studentId, String name, String prenom, String email, String role, String filePath) {
        this.studentId = studentId;
        this.name = name;
        this.prenom = prenom;
        this.email = email;
        this.role = role;
        this.filePath = filePath;
    }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFullName() {
        return (prenom != null ? prenom : "") + " " + (name != null ? name : "");
    }
}
