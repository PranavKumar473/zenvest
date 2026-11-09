package in.zenvest.shared.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AuditServiceImplTest {

  private final AuditLogRepository repository = mock(AuditLogRepository.class);
  private final AuditServiceImpl auditService = new AuditServiceImpl(repository);

  @Test
  @DisplayName("Saves audit log entry with all provided fields")
  void savesAuditEntry() {
    auditService.log("user-1", "LOGIN", "USER", "user-1", "127.0.0.1", "Mozilla/5.0", "details");

    ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
    verify(repository).save(captor.capture());

    AuditLog saved = captor.getValue();
    assertThat(saved.getUserId()).isEqualTo("user-1");
    assertThat(saved.getAction()).isEqualTo("LOGIN");
    assertThat(saved.getResourceType()).isEqualTo("USER");
    assertThat(saved.getResourceId()).isEqualTo("user-1");
    assertThat(saved.getIpAddress()).isEqualTo("127.0.0.1");
    assertThat(saved.getUserAgent()).isEqualTo("Mozilla/5.0");
    assertThat(saved.getDetails()).isEqualTo("details");
    assertThat(saved.getCreatedAt()).isNotNull();
  }
}
