package in.zenvest.shared.api;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ZenvestException.class)
  public ResponseEntity<ProblemDetail> handleZenvestException(ZenvestException ex) {
    log.warn("Business exception: code={}, message={}", ex.getCode(), ex.getMessage());
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
    problem.setType(URI.create("https://zenvest.in/errors/" + ex.getCode().toLowerCase()));
    problem.setTitle(ex.getStatus().getReasonPhrase());
    problem.setProperty("code", ex.getCode());
    problem.setProperty("traceId", getTraceId());
    return ResponseEntity.status(ex.getStatus()).body(problem);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleValidationException(
      MethodArgumentNotValidException ex) {
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
    problem.setType(URI.create("https://zenvest.in/errors/validation-failed"));
    problem.setTitle("Bad Request");
    problem.setProperty("code", "VALIDATION_FAILED");
    problem.setProperty("traceId", getTraceId());

    List<ValidationError> errors = new ArrayList<>();
    for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
      errors.add(new ValidationError(fieldError.getField(), fieldError.getDefaultMessage()));
    }
    problem.setProperty("errors", errors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleGenericException(Exception ex) {
    log.error("Unhandled internal server error: ", ex);
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected internal error occurred. Please contact support.");
    problem.setType(URI.create("https://zenvest.in/errors/internal-server-error"));
    problem.setTitle("Internal Server Error");
    problem.setProperty("code", "INTERNAL_SERVER_ERROR");
    problem.setProperty("traceId", getTraceId());

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
  }

  private String getTraceId() {
    String traceId = MDC.get("traceId");
    if (traceId == null || traceId.isBlank()) {
      traceId = MDC.get("requestId");
    }
    return traceId != null ? traceId : "unknown";
  }

  public record ValidationError(String field, String message) {}
}
