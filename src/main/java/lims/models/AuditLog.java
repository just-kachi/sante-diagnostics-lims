package lims.models;

import java.time.LocalDateTime;

public class AuditLog {

    private int id;
    private int userId;
    private String userEmail;
    private String userRole;
    private String action;
    private String entityType;
    private int entityId;
    private String description;
    private LocalDateTime createdAt;

    public AuditLog() {
    }

    public AuditLog(int id, int userId, String userEmail, String userRole,
                    String action, String entityType, int entityId,
                    String description, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userRole = userRole;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.description = description;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserRole() {
        return userRole;
    }

    public String getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public int getEntityId() {
        return entityId;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}