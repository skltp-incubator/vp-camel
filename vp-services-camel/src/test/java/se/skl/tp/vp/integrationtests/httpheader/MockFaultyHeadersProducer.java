package se.skl.tp.vp.integrationtests.httpheader;

import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

public class MockFaultyHeadersProducer {

  private static final String NETTY4_HTTP = "netty4-http:";

  public Map<String, Object> getHeaders() {
    return headers;
  }


  void setHeaders(Map<String, Object> headers) {
    this.headers = headers;
    headers.put("http.status",200);
  }

  private Map<String, Object> headers;


  MockFaultyHeadersProducer(
      CamelContext camelContext, String producerAddress, RouteProcessEventListener listener) throws Exception {
    camelContext.addRoutes(
        new RouteBuilder() {
          @Override
          public void configure()  {
            from(NETTY4_HTTP + producerAddress)
                .routeDescription("Route To \"A Process\" that add's a lot of headers")
                .routeId("headers-test")
                .process(
                    (Exchange exchange) -> {
                      listener.OnBeforeProcess(exchange);
                      exchange.getOut().setBody("<any-body/>");
                      exchange.getOut().setHeaders(headers);
                    });
          }
        });

  }


}
