package se.skl.tp.vp.integrationtests.utils;

import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import se.skl.tp.vp.constants.PropertyConstants;

@Component
public class TestConsumer {
  public static final String DIRECT_START_HTTP = "direct:start_http";
  public static final String DIRECT_START_HTTPS = "direct:start_https";
  private static final String NETTY_PREFIX = "netty4-http:";
  private String httpConsumerRouteUrl;
  private String httpsConsumerRouteUrl;

  @Produce(uri = "direct:start")
  protected ProducerTemplate template;

  private Environment env;

  @Autowired
  public TestConsumer(CamelContext camelContext, Environment env) throws Exception {
    this.env = env;
    createConsumerRouteUrls();
    createConsumerRoutes(camelContext);
  }

  public String sendHttpRequestToVP(String message, Map<String, Object> headers){
    return template.requestBodyAndHeaders(
            DIRECT_START_HTTP,
            message,
            headers, String.class
    );
  }

  public String sendHttpsRequestToVP(String message, Map<String, Object> headers){
    return template.requestBodyAndHeaders(
            DIRECT_START_HTTPS,
            message,
            headers, String.class
    );
  }

  private void createConsumerRouteUrls(){
    String vpHttpBaseUrl = env.getProperty(PropertyConstants.VP_HTTP_ROUTE_URL);
    String vpHttpsBaseUrl = env.getProperty(PropertyConstants.VP_HTTPS_ROUTE_URL);
    httpConsumerRouteUrl = NETTY_PREFIX + vpHttpBaseUrl;
    httpsConsumerRouteUrl = NETTY_PREFIX + vpHttpsBaseUrl + "?sslContextParameters=#outgoingSSLContextParameters&ssl=true";
  }

  private void createConsumerRoutes(CamelContext camelContext) throws Exception {
    camelContext.addRoutes(new RouteBuilder() {
      @Override
      public void configure() throws Exception {
        from(DIRECT_START_HTTP)
                .to(httpConsumerRouteUrl);

        from(DIRECT_START_HTTPS)
                .to(httpsConsumerRouteUrl);
      }
    });
  }

}
