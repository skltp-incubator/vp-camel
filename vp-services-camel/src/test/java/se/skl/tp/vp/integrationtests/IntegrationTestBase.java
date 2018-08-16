package se.skl.tp.vp.integrationtests;

import java.util.Map;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.util.jsse.KeyManagersParameters;
import org.apache.camel.util.jsse.KeyStoreParameters;
import org.apache.camel.util.jsse.SSLContextParameters;
import org.apache.camel.util.jsse.TrustManagersParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import se.skl.tp.vp.constants.ApplicationProperties;

public class IntegrationTestBase extends CamelTestSupport {

  public static final String DIRECT_START_HTTP = "direct:start_http";
  public static final String DIRECT_START_HTTPS = "direct:start_https";

  @Autowired
  Environment env;

  protected String sendHttpRequestToVP(String message, Map<String, Object> headers){
    return template.requestBodyAndHeaders(
        DIRECT_START_HTTP,
        message,
        headers, String.class
    );
  }

  protected String sendHttpsRequestToVP(String message, Map<String, Object> headers){
    return template.requestBodyAndHeaders(
        DIRECT_START_HTTPS,
        message,
        headers, String.class
    );
  }

  @Override
  protected RouteBuilder[] createRouteBuilders() throws Exception {

    return new RouteBuilder[]{/*new VPRouter(),*/
        new RouteBuilder() {
          @Override
          public void configure() throws Exception {
            from(DIRECT_START_HTTP)
                .to("netty4-http:http://localhost:12312/vp");

            from(DIRECT_START_HTTPS)
                .to("netty4-http:https://localhost:443/vp?sslContextParameters=#outgoingSSLContextParameters&ssl=true");

          }
        }
//       , new TakMockServerRoute()
    };
  }

    @Override
  protected JndiRegistry createRegistry() throws Exception {
    KeyStoreParameters incomingksp = new KeyStoreParameters();
    incomingksp.setResource(env.getProperty(ApplicationProperties.TP_TLS_STORE_LOCATION) + env.getProperty(ApplicationProperties.TP_TLS_STORE_PRODUCER_FILE));
    incomingksp.setPassword(env.getProperty(ApplicationProperties.TP_TLS_STORE_PRODUCER_PASSWORD));
    KeyManagersParameters incomingkmp = new KeyManagersParameters();
    incomingkmp.setKeyPassword(env.getProperty(ApplicationProperties.TP_TLS_STORE_PRODUCER_KEY_PASSWORD));
    incomingkmp.setKeyStore(incomingksp);
    KeyStoreParameters incomingtsp = new KeyStoreParameters();
    incomingtsp.setResource(env.getProperty(ApplicationProperties.TP_TLS_STORE_LOCATION) + env.getProperty(ApplicationProperties.TP_TLS_STORE_TRUSTSTORE_FILE));
    incomingtsp.setPassword(env.getProperty(ApplicationProperties.TP_TLS_STORE_TRUSTSTORE_PASSWORD));
    TrustManagersParameters incomingtmp = new TrustManagersParameters();
    incomingtmp.setKeyStore(incomingtsp);
    SSLContextParameters incomingsslContextParameters = new SSLContextParameters();
    incomingsslContextParameters.setKeyManagers(incomingkmp);
    incomingsslContextParameters.setTrustManagers(incomingtmp);

    KeyStoreParameters outgoingksp = new KeyStoreParameters();
    outgoingksp.setResource(env.getProperty(ApplicationProperties.TP_TLS_STORE_LOCATION) + env.getProperty(ApplicationProperties.TP_TLS_STORE_CONSUMER_FILE));
    outgoingksp.setPassword(env.getProperty(ApplicationProperties.TP_TLS_STORE_CONSUMER_PASSWORD));
    KeyManagersParameters outgoingkmp = new KeyManagersParameters();
    outgoingkmp.setKeyPassword(env.getProperty(ApplicationProperties.TP_TLS_STORE_CONSUMER_KEY_PASSWORD));
    outgoingkmp.setKeyStore(outgoingksp);
    KeyStoreParameters outgoingtsp = new KeyStoreParameters();
    outgoingtsp.setResource(env.getProperty(ApplicationProperties.TP_TLS_STORE_LOCATION) + env.getProperty(ApplicationProperties.TP_TLS_STORE_TRUSTSTORE_FILE));
    outgoingtsp.setPassword(env.getProperty(ApplicationProperties.TP_TLS_STORE_TRUSTSTORE_PASSWORD));
    TrustManagersParameters outgoingtmp = new TrustManagersParameters();
    outgoingtmp.setKeyStore(outgoingtsp);
    SSLContextParameters outgoingsslContextParameters = new SSLContextParameters();
    outgoingsslContextParameters.setKeyManagers(outgoingkmp);
    outgoingsslContextParameters.setTrustManagers(outgoingtmp);

    JndiRegistry registry = super.createRegistry();
    registry.bind("incomingSSLContextParameters", incomingsslContextParameters);
    registry.bind("outgoingSSLContextParameters", outgoingsslContextParameters);

    return registry;
  }

}
