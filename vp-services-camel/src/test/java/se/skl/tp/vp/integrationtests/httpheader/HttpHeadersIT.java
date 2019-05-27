package se.skl.tp.vp.integrationtests.httpheader;

import static se.skl.tp.vp.integrationtests.httpheader.HeadersUtil.TEST_CONSUMER;
import static se.skl.tp.vp.integrationtests.httpheader.HeadersUtil.TEST_CORRELATION_ID;
import static se.skl.tp.vp.integrationtests.httpheader.HeadersUtil.TEST_SENDER;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import se.skl.tp.vp.TestBeanConfiguration;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.PropertyConstants;
import se.skl.tp.vp.integrationtests.utils.StartTakService;
import se.skl.tp.vp.util.soaprequests.TestSoapRequests;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = TestBeanConfiguration.class)
@TestPropertySource("classpath:application.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@StartTakService
public class HttpHeadersIT extends CamelTestSupport {

  @Value("${" + PropertyConstants.VP_HEADER_USER_AGENT + "}")
  private String vpHeaderUserAgent;

  @Value("${" + PropertyConstants.VP_HEADER_CONTENT_TYPE + "}")
  private String headerContentType;

  @Value("${" + PropertyConstants.VP_INSTANCE_ID + "}")
  private String vpInstanceId;

  @Value("${" + PropertyConstants.VP_HTTP_ROUTE_URL + "}")
  private String httpRoute;

  @EndpointInject(uri = "mock:result")
  protected MockEndpoint resultEndpoint;

  @Produce(uri = "direct:start")
  protected ProducerTemplate template;

  @Autowired private CamelContext camelContext;

  private static boolean isContextStarted = false;

  @Before
  public void setUp() throws Exception {
    if (!isContextStarted) {
      addConsumerRoute(camelContext);
      camelContext.start();
      isContextStarted = true;
    }
    resultEndpoint.reset();
  }

  @Test
  public void checkSoapActionSetTest() {
    // This param is mandatory for the request to pass.
    template.sendBodyAndHeaders(
        TestSoapRequests.GET_NO_CERT_HTTP_SOAP_REQUEST, HeadersUtil.getHttpHeadersWithoutMembers());
    String s =
        (String)
            resultEndpoint.getExchanges().get(0).getIn().getHeaders().get(HttpHeaders.SOAP_ACTION);
    assertEquals("action", s);
  }

  @Test
  public void checkHeadersSetByConfigTest() {
    // These params are partly set by configuration in HeaderProcessorImpl.java
    template.sendBodyAndHeaders(
        TestSoapRequests.GET_NO_CERT_HTTP_SOAP_REQUEST, HeadersUtil.getHttpHeadersWithoutMembers());
    assertEquals(
        vpInstanceId,
        resultEndpoint
            .getExchanges()
            .get(0)
            .getIn()
            .getHeaders()
            .get(HttpHeaders.X_VP_INSTANCE_ID));
    assertEquals(
        headerContentType,
        resultEndpoint
            .getExchanges()
            .get(0)
            .getIn()
            .getHeaders()
            .get(HttpHeaders.HEADER_CONTENT_TYPE));
    assertEquals(
        vpHeaderUserAgent,
        resultEndpoint
            .getExchanges()
            .get(0)
            .getIn()
            .getHeaders()
            .get(HttpHeaders.HEADER_USER_AGENT));
    assertEquals(
        TEST_SENDER,
        resultEndpoint.getExchanges().get(0).getIn().getHeaders().get(HttpHeaders.X_VP_SENDER_ID));
  }

  @Test
  public void checkCorrelationIdPropagatedWhenIncomingHeaderSetTest() {
    template.sendBodyAndHeaders(
        TestSoapRequests.GET_NO_CERT_HTTP_SOAP_REQUEST, HeadersUtil.getHttpHeadersWithMembers());
    assertEquals(
        TEST_CORRELATION_ID,
        resultEndpoint
            .getExchanges()
            .get(0)
            .getIn()
            .getHeaders()
            .get(HttpHeaders.X_SKLTP_CORRELATION_ID));
  }

  @Test
  public void checkXrivtaOriginalConsumerIdPropagatedWhenIncomingHeaderSetTest() {
    template.sendBodyAndHeaders(
        TestSoapRequests.GET_NO_CERT_HTTP_SOAP_REQUEST, HeadersUtil.getHttpHeadersWithMembers());
    assertEquals(
        TEST_CONSUMER,
        resultEndpoint
            .getExchanges()
            .get(0)
            .getIn()
            .getHeaders()
            .get(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID));
  }

  @Test
  public void checkCorrelationIdPropagatedWithoutIncomingHeaderSetTest() {
    template.sendBodyAndHeaders(
        TestSoapRequests.GET_NO_CERT_HTTP_SOAP_REQUEST, HeadersUtil.getHttpHeadersWithoutMembers());
    String s =
        (String)
            resultEndpoint
                .getExchanges()
                .get(0)
                .getIn()
                .getHeaders()
                .get(HttpHeaders.X_SKLTP_CORRELATION_ID);
    assertNotNull(s);
    assertNotEquals(TEST_CORRELATION_ID, s);
    assertTrue(s.length() > 20);
  }

  @Test
  public void checkXrivtaOriginalConsumerIdPropagatedWithoutIncomingHeaderSetTest() {
    template.sendBodyAndHeaders(
        TestSoapRequests.GET_NO_CERT_HTTP_SOAP_REQUEST, HeadersUtil.getHttpHeadersWithoutMembers());
    assertEquals(
        TEST_SENDER,
        resultEndpoint
            .getExchanges()
            .get(0)
            .getIn()
            .getHeaders()
            .get(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID));
  }

  private void addConsumerRoute(CamelContext camelContext) throws Exception {
    camelContext.addRoutes(
        new RouteBuilder() {
          @Override
          public void configure() {
            from("direct:start")
                .routeId("start")
                .routeDescription("consumer")
                .to("netty4-http:" + httpRoute);
            // Address below from tak-vagval-test.xml
            from("netty4-http:http://localhost:19000/vardgivare-b/tjanst2")
                .routeDescription("producer")
                .to("mock:result");
          }
        });
  }
}
