package in.zenvest.shared.internal;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "audit_log",
    indexes = {
      @Index(name = "idx_audit_user_action", columnList = "userId, action"),
      @Index(name = "idx_audit_created_at", columnList = "createdAt")
    })
public class AuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id")
  private String userId;

  @Column(name = "action", nullable = false, length = 64)
  private String action;

  @Column(name = "resource_type", nullable = false, length = 64)
  private String resourceType;

  @Column(name = "resource_id")
  private String resourceId;

  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  @Column(name = "user_agent", length = 255)
  private String userAgent;

  @Column(name = "details", columnDefinition = "TEXT")
  private String details;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  public AuditLog() {}

  public AuditLog(
      String userId,
      String action,
      String resourceType,
      String resourceId,
      String ipAddress,
      String userAgent,
      String details) {
    this.userId = userId;
    this.action = action;
    this.resourceType = resourceType;
    this.resourceId = resourceId;
    this.ipAddress = ipAddress;
    this.userAgent = userAgent;
    this.details = details;
    this.createdAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public String getUserId() {
    return userId;
  }

  public String getAction() {
    return action;
  }

  public String getResourceType() {
    return resourceType;
  }

  public String getResourceId() {
    return resourceId;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public String getDetails() {
    return details;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
