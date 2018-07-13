package se.skl.tp.vp.constants;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:vp-security.properties")
@ConfigurationProperties(prefix = "tp.tls")
public class SecurityProperies {

  private String allowedIncomingProtocols;
  private String allowedOutgoingProtocols;

  private Store store;

  public static class Store {
    private String location;

    private Producer producer;
    private Consumer consumer;
    private Truststore truststore;

    public static class Producer {
      private String type;
      private String file;
      private String password;
      private String keyPassword;

      public String getType() {
        return type;
      }

      public void setType(String type) {
        this.type = type;
      }

      public String getFile() {
        return file;
      }

      public void setFile(String file) {
        this.file = file;
      }

      public String getPassword() {
        return password;
      }

      public void setPassword(String password) {
        this.password = password;
      }

      public String getKeyPassword() {
        return keyPassword;
      }

      public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
      }
    }

    public static class Consumer {
      private String type;
      private String file;
      private String password;
      private String keyPassword;

      public String getType() {
        return type;
      }

      public void setType(String type) {
        this.type = type;
      }

      public String getFile() {
        return file;
      }

      public void setFile(String file) {
        this.file = file;
      }

      public String getPassword() {
        return password;
      }

      public void setPassword(String password) {
        this.password = password;
      }

      public String getKeyPassword() {
        return keyPassword;
      }

      public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
      }
    }

    public static class Truststore {
      private String type;
      private String file;
      private String password;

      public String getType() {
        return type;
      }

      public void setType(String type) {
        this.type = type;
      }

      public String getFile() {
        return file;
      }

      public void setFile(String file) {
        this.file = file;
      }

      public String getPassword() {
        return password;
      }

      public void setPassword(String password) {
        this.password = password;
      }
    }

    public String getLocation() {
      return location;
    }

    public void setLocation(String location) {
      this.location = location;
    }

    public Producer getProducer() {
      return producer;
    }

    public void setProducer(Producer producer) {
      this.producer = producer;
    }

    public Consumer getConsumer() {
      return consumer;
    }

    public void setConsumer(Consumer consumer) {
      this.consumer = consumer;
    }

    public Truststore getTruststore() {
      return truststore;
    }

    public void setTruststore(Truststore truststore) {
      this.truststore = truststore;
    }
  }

  public String getAllowedIncomingProtocols() {
    return allowedIncomingProtocols;
  }

  public void setAllowedIncomingProtocols(String allowedIncomingProtocols) {
    this.allowedIncomingProtocols = allowedIncomingProtocols;
  }

  public String getAllowedOutgoingProtocols() {
    return allowedOutgoingProtocols;
  }

  public void setAllowedOutgoingProtocols(String allowedOutgoingProtocols) {
    this.allowedOutgoingProtocols = allowedOutgoingProtocols;
  }

  public Store getStore() {
    return store;
  }

  public void setStore(Store store) {
    this.store = store;
  }
}
