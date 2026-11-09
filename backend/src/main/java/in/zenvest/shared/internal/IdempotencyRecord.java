package in.zenvest.shared.internal;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "idempotency_keys",
    indexes = {
      @Index(
          name = "idx_idempotency_user_key",
          columnList = "userId, idempotencyKey",
          unique = true)
    })
public class IdempotencyRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private String userId;

  @Column(name = "idempotency_key", nullable = false, length = 128)
  private String idempotencyKey;

  @Column(name = "request_hash", nullable = false, length = 64)
  private String requestHash;

  @Column(name = "response_status", nullable = false)
  private int responseStatus;

  @Column(name = "response_body", columnDefinition = "TEXT")
  private String responseBody;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  public IdempotencyRecord() {}

  public IdempotencyRecord(
      String userId,
      String idempotencyKey,
      String requestHash,
      int responseStatus,
      String responseBody) {
    this.userId = userId;
    this.idempotencyKey = idempotencyKey;
    this.requestHash = requestHash;
    this.responseStatus = responseStatus;
    this.responseBody = responseBody;
    this.createdAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public String getUserId() {
    return userId;
  }

  public String getIdempotencyKey() {
    return idempotencyKey;
  }

  public String getRequestHash() {
    return requestHash;
  }

  public int getResponseStatus() {
    return responseStatus;
  }

  public String getResponseBody() {
    return responseBody;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
