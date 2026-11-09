package in.zenvest.shared.api;

public interface AuditService {
  void log(
      String userId,
      String action,
      String resourceType,
      String resourceId,
      String ipAddress,
      String userAgent,
      String details);
}
