package in.zenvest.shared.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  @DisplayName("Handles ZenvestException returning RFC 9457 ProblemDetail")
  void handlesZenvestException() {
    ZenvestException ex =
        new ZenvestException(
            "DEMO_READ_ONLY", "Demo user cannot perform writes", HttpStatus.FORBIDDEN);
    ResponseEntity<ProblemDetail> response = handler.handleZenvestException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    ProblemDetail body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getDetail()).isEqualTo("Demo user cannot perform writes");
    assertThat(body.getProperties()).containsEntry("code", "DEMO_READ_ONLY");
    assertThat(body.getProperties()).containsKey("traceId");
  }

  @Test
  @DisplayName("Handles generic exception without exposing internal details or stack trace")
  void handlesGenericExceptionSanitized() {
    Exception ex = new NullPointerException("Secret internal database password leaked");
    ResponseEntity<ProblemDetail> response = handler.handleGenericException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    ProblemDetail body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getDetail()).doesNotContain("password");
    assertThat(body.getDetail())
        .isEqualTo("An unexpected internal error occurred. Please contact support.");
    assertThat(body.getProperties()).containsEntry("code", "INTERNAL_SERVER_ERROR");
  }
}
