package com.edusmart.model;

import java.time.LocalDateTime;

/**
 * User model — maps to the {@code user} table ({@code name} = nom, {@code prenom} = prénom).
 */
public class User {

    public enum Role {
        STUDENT, TEACHER, ADMIN
    }

    private int id;
    /** Maps to column {@code prenom}. */
    private String firstName;
    /** Maps to column {@code name} (nom de famille). */
    private String lastName;
    private String email;
    /** Maps to column {@code password}. */
    private String password;
    private Role role;
    /** DB {@code role} varchar — kept in sync with {@link #role}. */
    private String roleValue;
    private String numtel;
    private boolean active;
    private String resetToken;
    private LocalDateTime resetTokenExpiresAt;
    private String googleId;
    private String faceDescriptor;
    private String emailAssoc;
    private String signaturePath;

    /** Legacy alias used by older code paths. */
    private String avatarUrl;

    public User() {}

    public User(int id, String firstName, String lastName, String email, Role role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.roleValue = role != null ? role.name() : null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /** @deprecated use {@link #getPassword()} — same column */
    @Deprecated
    public String getPasswordHash() {
        return password;
    }

    /** @deprecated use {@link #setPassword(String)} */
    @Deprecated
    public void setPasswordHash(String passwordHash) {
        this.password = passwordHash;
    }

    public Role getRole() {
        if (role == null && roleValue != null) {
            try {
                role = Role.valueOf(roleValue.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
        this.roleValue = role != null ? role.name() : null;
    }

    public String getRoleValue() {
        return roleValue != null ? roleValue : (role != null ? role.name() : null);
    }

    public void setRoleValue(String roleValue) {
        this.roleValue = roleValue;
        if (roleValue != null && !roleValue.isBlank()) {
            try {
                this.role = Role.valueOf(roleValue.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                this.role = null;
            }
        } else {
            this.role = null;
        }
    }

    public String getNumtel() {
        return numtel;
    }

    public void setNumtel(String numtel) {
        this.numtel = numtel;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public LocalDateTime getResetTokenExpiresAt() {
        return resetTokenExpiresAt;
    }

    public void setResetTokenExpiresAt(LocalDateTime resetTokenExpiresAt) {
        this.resetTokenExpiresAt = resetTokenExpiresAt;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getFaceDescriptor() {
        return faceDescriptor;
    }

    public void setFaceDescriptor(String faceDescriptor) {
        this.faceDescriptor = faceDescriptor;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getEmailAssoc() {
        return emailAssoc;
    }

    public void setEmailAssoc(String emailAssoc) {
        this.emailAssoc = emailAssoc;
    }

    public String getSignaturePath() {
        return signaturePath;
    }

    public void setSignaturePath(String signaturePath) {
        this.signaturePath = signaturePath;
    }

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + getFullName() + "', email='" + email + "', role=" + getRoleValue() + "}";
    }
}
