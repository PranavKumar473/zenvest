package in.zenvest.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "zenvest")
@Validated
public class ZenvestProperties {

  private static final Logger log = LoggerFactory.getLogger(ZenvestProperties.class);

  private final Environment environment;
  private final Auth auth = new Auth();
  private final Email email = new Email();
  private final Funds funds = new Funds();

  @NotBlank private String publicUrl = "http://localhost:8080";

  @NotBlank private String consentPolicyVersion = "1.0";

  public ZenvestProperties(Environment environment) {
    this.environment = environment;
  }

  @PostConstruct
  public void validateProdSecrets() {
    boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");
    if (isProd) {
      log.info("Validating production configuration and secrets...");
      assertNotPlaceholder("zenvest.email.resend-api-key", email.getResendApiKey());
      assertNotPlaceholder("zenvest.auth.jwt.active-kid", auth.getJwt().getActiveKid());
    }
  }

  private void assertNotPlaceholder(String propertyName, String value) {
    if (value == null || value.isBlank() || "change-me".equalsIgnoreCase(value.trim())) {
      throw new IllegalStateException(
          "CRITICAL SECURITY FAILURE: Property '"
              + propertyName
              + "' must be explicitly set and cannot equal 'change-me' in the production profile.");
    }
  }

  public Auth getAuth() {
    return auth;
  }

  public Email getEmail() {
    return email;
  }

  public Funds getFunds() {
    return funds;
  }

  public String getPublicUrl() {
    return publicUrl;
  }

  public void setPublicUrl(String publicUrl) {
    this.publicUrl = publicUrl;
  }

  public String getConsentPolicyVersion() {
    return consentPolicyVersion;
  }

  public void setConsentPolicyVersion(String consentPolicyVersion) {
    this.consentPolicyVersion = consentPolicyVersion;
  }

  public static class Auth {
    private final Jwt jwt = new Jwt();
    private String cookieDomain = "localhost";
    private String corsAllowedOrigins = "http://localhost:5173";

    public Jwt getJwt() {
      return jwt;
    }

    public String getCookieDomain() {
      return cookieDomain;
    }

    public void setCookieDomain(String cookieDomain) {
      this.cookieDomain = cookieDomain;
    }

    public String getCorsAllowedOrigins() {
      return corsAllowedOrigins;
    }

    public void setCorsAllowedOrigins(String corsAllowedOrigins) {
      this.corsAllowedOrigins = corsAllowedOrigins;
    }

    public static class Jwt {
      private String privateKeyPath = "/path/to/private.pem";
      private String publicKeyPath = "/path/to/public.pem";
      private String activeKid = "key-2026-01";
      private String secondaryKid;
      private String secondaryPublicKeyPath;

      public String getPrivateKeyPath() {
        return privateKeyPath;
      }

      public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
      }

      public String getPublicKeyPath() {
        return publicKeyPath;
      }

      public void setPublicKeyPath(String publicKeyPath) {
        this.publicKeyPath = publicKeyPath;
      }

      public String getActiveKid() {
        return activeKid;
      }

      public void setActiveKid(String activeKid) {
        this.activeKid = activeKid;
      }

      public String getSecondaryKid() {
        return secondaryKid;
      }

      public void setSecondaryKid(String secondaryKid) {
        this.secondaryKid = secondaryKid;
      }

      public String getSecondaryPublicKeyPath() {
        return secondaryPublicKeyPath;
      }

      public void setSecondaryPublicKeyPath(String secondaryPublicKeyPath) {
        this.secondaryPublicKeyPath = secondaryPublicKeyPath;
      }
    }
  }

  public static class Email {
    private String resendApiKey = "change-me";
    private String from = "noreply@zenvest.in";

    public String getResendApiKey() {
      return resendApiKey;
    }

    public void setResendApiKey(String resendApiKey) {
      this.resendApiKey = resendApiKey;
    }

    public String getFrom() {
      return from;
    }

    public void setFrom(String from) {
      this.from = from;
    }
  }

  public static class Funds {
    private String amfiNavAllUrl = "https://www.amfiindia.com/spages/NAVAll.txt";
    private String mfapiBaseUrl = "https://api.mfapi.in";

    public String getAmfiNavAllUrl() {
      return amfiNavAllUrl;
    }

    public void setAmfiNavAllUrl(String amfiNavAllUrl) {
      this.amfiNavAllUrl = amfiNavAllUrl;
    }

    public String getMfapiBaseUrl() {
      return mfapiBaseUrl;
    }

    public void setMfapiBaseUrl(String mfapiBaseUrl) {
      this.mfapiBaseUrl = mfapiBaseUrl;
    }
  }
}
