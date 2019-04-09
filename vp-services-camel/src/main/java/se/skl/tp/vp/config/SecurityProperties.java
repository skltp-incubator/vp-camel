package se.skl.tp.vp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@Configuration
//@PropertySource("classpath:application-security.properties")
@ConfigurationProperties(prefix = "tp.tls")
public class SecurityProperties {

  private String allowedIncomingProtocols;
  private String allowedOutgoingProtocols;
  private Store store;

  @Data
  public static class Store {
    private String location;
    private Producer producer;
    private Consumer consumer;
    private Truststore truststore;

    @Data
    public static class Producer {
      private String type;
      private String file;
      private String password;
      private String keyPassword;
    }

    @Data
    public static class Consumer {
      private String type;
      private String file;
      private String password;
      private String keyPassword;
    }

    @Data
    public static class Truststore {
      private String type;
      private String file;
      private String password;
    }

  }

}
