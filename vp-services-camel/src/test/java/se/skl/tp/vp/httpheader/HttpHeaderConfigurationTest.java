package se.skl.tp.vp.httpheader;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import se.skl.tp.vp.TestBeanConfiguration;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.integrationtests.httpheader.HeadersUtil;
import java.util.Map;

import static se.skl.tp.vp.integrationtests.httpheader.HeadersUtil.*;

/**
 * Testing transformation of headers in class HeaderConfigurationProcessor.java.
 * If a consumerId is present in the request, then just forward it. Otherwise
 * set the senderId as consumerId.
 * The correlationId is used with http requests AND https if that is configured.
 * If it is present, then forward, otherwise create one and forward that one.
 */
@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = TestBeanConfiguration.class)
@TestPropertySource("classpath:application.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class HttpHeaderConfigurationTest extends CamelTestSupport {

  private static boolean isContextStarted = false;

  @Autowired
  private CamelContext camelContext;

  @EndpointInject(uri = "mock:result")
  protected MockEndpoint resultEndpoint;

  @Produce(uri = "direct:start")
  protected ProducerTemplate template;

  @Autowired
  HeaderConfigurationProcessorImpl headerConfigurationProcessor;

  @Before
  public void setUp() throws Exception {
    if (!isContextStarted) {
      createRoute(camelContext);
      camelContext.start();
      isContextStarted = true;
    }
    resultEndpoint.reset();
  }

  @Test
  public void positiveCorrelationIdTest() {
    Map headers = createHeaders();
    headers.put(HttpHeaders.X_SKLTP_CORRELATION_ID, TEST_CORRELATION_ID);
    template.sendBodyAndHeaders(TEST_BODY, headers);
    assert (TEST_CORRELATION_ID
        .equals(resultEndpoint.getReceivedExchanges().get(0).getIn().getHeaders().get(HttpHeaders.X_SKLTP_CORRELATION_ID)));
  }

  @Test
  public void negativeCorrelationIdTest() {
    Map headers = createHeaders();
    template.sendBodyAndHeaders(TEST_BODY, headers);
    String correlation = (String) resultEndpoint.getReceivedExchanges().get(0).getIn().getHeaders()
            .get(HttpHeaders.X_SKLTP_CORRELATION_ID);
    //If no correlationId was present in the request, it should have been generated.
    assertNotNull(correlation);
    assertNotEquals(TEST_CORRELATION_ID, correlation);
    assert (correlation.length() > 35);
  }

  @Test
  public void positiveOriginalConsumerTest() {
    Map headers = createHeaders();
    headers.put(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, TEST_CONSUMER);
    template.sendBodyAndHeaders(TEST_BODY, headers);
    assert (TEST_CONSUMER.equals(resultEndpoint.getReceivedExchanges().get(0).getIn().getHeaders()
        .get(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID)));
  }

  @Test
  public void negativeOriginalConsumerTest() {
    Map headers = createHeaders();
    template.sendBodyAndHeaders(TEST_BODY, headers);
    //The senderId should be used as originalConsumerId, if originalConsumerId isn't present in request.
    String s = (String) resultEndpoint.getReceivedExchanges().get(0).getIn().getHeaders().get(HttpHeaders.X_VP_SENDER_ID);
    assert (s.equals(resultEndpoint.getReceivedExchanges().get(0).getIn().getHeaders()
        .get(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID)));
  }

  private void createRoute(CamelContext camelContext) {
    try {
      camelContext.addRoutes(new RouteBuilder() {
        @Override
        public void configure() {
          from("direct:start").routeDescription("Consumer").id("Consumer")
              .setProperty(VPExchangeProperties.IS_HTTPS, constant(false))
              .setProperty(VPExchangeProperties.SENDER_ID, constant("tp"))
              .process(headerConfigurationProcessor)
              .to("mock:result");
        }
      });
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  public Map createHeaders() {
    return HeadersUtil.getHttpHeadersWithoutMembers();
  }
}
