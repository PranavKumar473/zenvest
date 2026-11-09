package in.zenvest.shared.internal;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class RequestTracingFilterTest {

  private final RequestTracingFilter filter = new RequestTracingFilter();

  @Test
  @DisplayName("Propagates incoming X-Request-Id header to MDC and response")
  void propagatesIncomingRequestId() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(RequestTracingFilter.REQUEST_ID_HEADER, "req-12345");
    MockHttpServletResponse response = new MockHttpServletResponse();

    FilterChain chain =
        (req, res) -> {
          assertThat(MDC.get(RequestTracingFilter.MDC_REQUEST_ID)).isEqualTo("req-12345");
          assertThat(MDC.get(RequestTracingFilter.MDC_TRACE_ID)).isEqualTo("req-12345");
        };

    filter.doFilter(request, response, chain);

    assertThat(response.getHeader(RequestTracingFilter.REQUEST_ID_HEADER)).isEqualTo("req-12345");
    assertThat(MDC.get(RequestTracingFilter.MDC_REQUEST_ID)).isNull();
  }

  @Test
  @DisplayName("Generates new UUID if X-Request-Id header is missing")
  void generatesNewRequestIdWhenMissing() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, (req, res) -> {});

    assertThat(response.getHeader(RequestTracingFilter.REQUEST_ID_HEADER)).isNotNull();
  }

  @Test
  @DisplayName("Captures authenticated user in MDC")
  void capturesAuthenticatedUser() throws ServletException, IOException {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken("user-abc", null, java.util.List.of()));

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    try {
      filter.doFilter(
          request,
          response,
          (req, res) -> {
            assertThat(MDC.get(RequestTracingFilter.MDC_USER_ID)).isEqualTo("user-abc");
          });
    } finally {
      SecurityContextHolder.clearContext();
    }
  }
}
