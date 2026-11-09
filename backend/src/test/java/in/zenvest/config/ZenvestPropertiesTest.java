package in.zenvest.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class ZenvestPropertiesTest {

  @Test
  @DisplayName("In prod profile, throws IllegalStateException when secrets are 'change-me'")
  void throwsInProdWhenSecretIsPlaceholder() {
    MockEnvironment env = new MockEnvironment();
    env.setActiveProfiles("prod");

    ZenvestProperties props = new ZenvestProperties(env);
    props.getEmail().setResendApiKey("change-me");

    assertThatThrownBy(props::validateProdSecrets)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("CRITICAL SECURITY FAILURE");
  }

  @Test
  @DisplayName("In dev profile, does not throw when secrets are 'change-me'")
  void doesNotThrowInDev() {
    MockEnvironment env = new MockEnvironment();
    env.setActiveProfiles("dev");

    ZenvestProperties props = new ZenvestProperties(env);
    props.getEmail().setResendApiKey("change-me");

    assertThatCode(props::validateProdSecrets).doesNotThrowAnyException();
  }
}
