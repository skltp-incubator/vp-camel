package se.skl.tp.vp.integrationtests.utils;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Data
@Component
@Log4j2
public class MockProducer {
  public static final String NETTY4_HTTP = "netty4-http:";

  private Integer responseHttpStatus=200;
  private String responseBody="response text";
  private Integer timeout=0;

  private CamelContext camelContext;

  String inBody;
  Map<String, Object> inHeaders = new HashMap<>();

  @Autowired
  public MockProducer(CamelContext camelContext){
    this.camelContext = camelContext;
  }

  public MockProducer(CamelContext camelContext, String producerAddress) throws Exception {
    this.camelContext = camelContext;
    start(producerAddress);
  }


  public void start(String producerAddress) throws Exception {
    inHeaders.clear();
    inBody=null;
    Route route = camelContext.getRoute(producerAddress);
    if(route!=null){
      log.info("Producer route with address '{}' already started", producerAddress);
      return;
    }

    camelContext.addRoutes(new RouteBuilder() {
      @Override
      public void configure() throws Exception {
        from(NETTY4_HTTP + producerAddress).id(producerAddress).routeDescription("Producer")
            .streamCaching()
            .process((Exchange exchange) -> {
              inHeaders.putAll(exchange.getIn().getHeaders());
              inBody = exchange.getIn().getBody(String.class);
              exchange.getOut().setBody(responseBody);
              exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, responseHttpStatus);
              Thread.sleep(timeout);
            });
      }
    });
  }

}
