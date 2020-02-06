package se.skl.tp.vp.hawtioAuth;

import io.hawt.config.ConfigFacade;
import io.hawt.web.auth.AuthenticationConfiguration;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import se.skl.tp.vp.constants.PropertyConstants;

@SpringBootApplication
public class HawtioAuthSpringBootService {

  private static final Logger LOG = LoggerFactory.getLogger(HawtioAuthSpringBootService.class);
  private static final String JAVA_SECURITY_AUTH_LOGIN_CONFIG = "java.security.auth.login.config";

  @Value("${" + PropertyConstants.VP_INSTANCE_ID + "}")
  private String env;

  /**
   * Configure facade to use authentication.
   *
   * @return config
   * @throws Exception if an error occurs
   */
  @Bean(initMethod = "init")
  public ConfigFacade configFacade() throws Exception {
    if (env.equals("dev_env")) {
      System.setProperty(AuthenticationConfiguration.HAWTIO_AUTHENTICATION_ENABLED, "false");
    } else {
      final URL loginResource = this.getClass().getClassLoader().getResource("login.conf");
      if (loginResource != null) {
        setSystemPropertyIfNotSet(JAVA_SECURITY_AUTH_LOGIN_CONFIG, loginResource.toExternalForm());
      }
      LOG.info(
          "Using loginResource "
              + JAVA_SECURITY_AUTH_LOGIN_CONFIG
              + " : "
              + System.getProperty(JAVA_SECURITY_AUTH_LOGIN_CONFIG));

      final URL loginFile = this.getClass().getClassLoader().getResource("realm.properties");
      if (loginFile != null) {
        setSystemPropertyIfNotSet("login.file", loginFile.toExternalForm());
      }
      LOG.info("Using login.file : " + System.getProperty("login.file"));

      setSystemPropertyIfNotSet(AuthenticationConfiguration.HAWTIO_ROLES, "user");
      setSystemPropertyIfNotSet(AuthenticationConfiguration.HAWTIO_ROLES, "admin");
      setSystemPropertyIfNotSet(AuthenticationConfiguration.HAWTIO_REALM, "hawtio");
      setSystemPropertyIfNotSet(AuthenticationConfiguration.HAWTIO_REALM, "hawtioNtjp");
      setSystemPropertyIfNotSet(
          AuthenticationConfiguration.HAWTIO_ROLE_PRINCIPAL_CLASSES,
          "org.eclipse.jetty.jaas.JAASRole");
      if (!Boolean.getBoolean("debugMode")) {
        System.setProperty(AuthenticationConfiguration.HAWTIO_AUTHENTICATION_ENABLED, "true");
      }
    }
    return new ConfigFacade();
  }

  private void setSystemPropertyIfNotSet(final String key, final String value) {
    if (System.getProperty(key) == null) {
      System.setProperty(key, value);
    }
  }
}
