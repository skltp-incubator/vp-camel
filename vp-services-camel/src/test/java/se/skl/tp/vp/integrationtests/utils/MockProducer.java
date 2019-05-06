package se.skl.tp.vp.integrationtests.utils;

import lombok.Data;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

@Data
public class MockProducer {
  public static final String NETTY4_HTTP = "netty4-http:";

  private Integer responseHttpStatus=200;
  private String responseBody="response text";
  private Integer timeout=0;

  public MockProducer(CamelContext camelContext, String producerAddress) throws Exception {
    camelContext.addRoutes(new RouteBuilder() {
      @Override
      public void configure() throws Exception {
        from(NETTY4_HTTP + producerAddress).routeDescription("Producer returning Soapfault VP007")
            .process((Exchange exchange) -> {
              exchange.getOut().setBody(responseBody);
              exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, responseHttpStatus);
              Thread.sleep(timeout);
            });
      }
    });

  }

}
