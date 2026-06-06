package lims.models;

import java.time.LocalDateTime;

public class User {

    private int id;
    private String fullName;
    private String email;
    private String passwordHash;
    private String role;
    private boolean emailVerified;
    private boolean forcePasswordChange;
    private LocalDateTime createdAt;

    public User() {
    }

    public User(int id, String fullName, String email, String role,
                boolean emailVerified, boolean forcePasswordChange, LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.emailVerified = emailVerified;
        this.forcePasswordChange = forcePasswordChange;
        this.createdAt = createdAt;
    }

    public User(int id, String fullName, String email, String passwordHash, String role,
                boolean emailVerified, boolean forcePasswordChange, LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.emailVerified = emailVerified;
        this.forcePasswordChange = forcePasswordChange;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }


    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }


    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }


    public boolean isForcePasswordChange() {
        return forcePasswordChange;
    }

    public void setForcePasswordChange(boolean forcePasswordChange) {
        this.forcePasswordChange = forcePasswordChange;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}