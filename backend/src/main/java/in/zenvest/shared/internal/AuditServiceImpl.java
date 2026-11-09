package in.zenvest.shared.internal;

import in.zenvest.shared.api.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditServiceImpl implements AuditService {

  private final AuditLogRepository auditLogRepository;

  public AuditServiceImpl(AuditLogRepository auditLogRepository) {
    this.auditLogRepository = auditLogRepository;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void log(
      String userId,
      String action,
      String resourceType,
      String resourceId,
      String ipAddress,
      String userAgent,
      String details) {
    AuditLog audit =
        new AuditLog(userId, action, resourceType, resourceId, ipAddress, userAgent, details);
    auditLogRepository.save(audit);
  }
}
