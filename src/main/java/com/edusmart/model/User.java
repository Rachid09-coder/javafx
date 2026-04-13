package com.edusmart.model;

/**
 * User model - represents an EduSmart platform user (student or teacher).
 */
public class User {

    public enum Role {
        STUDENT, TEACHER, ADMIN
    }

    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String passwordHash;
    private Role role;
    private String avatarUrl;

    public User() {}

    public User(int id, String firstName, String lastName, String email, Role role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
    }

    // Getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getFullName() { return firstName + " " + lastName; }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + getFullName() + "', email='" + email + "', role=" + role + "}";
    }
}
