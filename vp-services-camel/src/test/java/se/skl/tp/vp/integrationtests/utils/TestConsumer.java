package se.skl.tp.vp.integrationtests.utils;

import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestConsumer {
  public static final String DIRECT_START_HTTP = "direct:start_http";
  public static final String DIRECT_START_HTTPS = "direct:start_https";

  @Produce(uri = "direct:start")
  protected ProducerTemplate template;

  @Autowired
  public TestConsumer(CamelContext camelContext) throws Exception {
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

  private void createConsumerRoutes(CamelContext camelContext) throws Exception {
    camelContext.addRoutes(new RouteBuilder() {
      @Override
      public void configure() throws Exception {
        from(DIRECT_START_HTTP)
            .to("netty4-http:http://localhost:12312/vp");

        from(DIRECT_START_HTTPS)
            .to("netty4-http:https://localhost:443/vp?sslContextParameters=#outgoingSSLContextParameters&ssl=true");
      }
    });
  }

}
