package se.skl.tp.vp.integrationtests.httpheader;

import static se.skl.tp.vp.constants.HttpHeaders.*;
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
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import se.skl.tp.vp.TestBeanConfiguration;
import se.skl.tp.vp.constants.PropertyConstants;
import se.skl.tp.vp.httpheader.OutHeaderProcessorImpl;
import se.skl.tp.vp.integrationtests.utils.StartTakService;
import se.skl.tp.vp.logging.LogExtraInfoBuilder;
import se.skl.tp.vp.logging.MessageInfoLogger;
import se.skl.tp.vp.util.TestLogAppender;
import se.skl.tp.vp.util.soaprequests.TestSoapRequests;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = TestBeanConfiguration.class)
@StartTakService
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class HttpsHeadersIT extends CamelTestSupport {

  @Value("${" + PropertyConstants.PROPAGATE_CORRELATION_ID_FOR_HTTPS + "}")
  private Boolean propagateCorrIdForHttps;

  @Value("${" + PropertyConstants.VP_HTTPS_ROUTE_URL + "}")
  private String httpsRoute;

  @Value("${" + PropertyConstants.VP_HEADER_USER_AGENT + "}")
  private String vpHeaderUserAgent;

  @EndpointInject(uri = "mock:result")
  protected MockEndpoint resultEndpoint;

  @Produce(uri = "direct:start")
  protected ProducerTemplate template;

  @Autowired private OutHeaderProcessorImpl headerProcessor;

  private boolean oldCorrelation;

  @Autowired private CamelContext camelContext;

  private static boolean isContextStarted = false;

  TestLogAppender testLogAppender = TestLogAppender.getInstance();

  @Before
  public void setUp() throws Exception {
    if (!isContextStarted) {
      addConsumerRoute(camelContext);
      camelContext.start();
      isContextStarted = true;
    }
    resultEndpoint.reset();
    oldCorrelation = headerProcessor.getPropagateCorrelationIdForHttps();
    testLogAppender.clearEvents();
  }

  @After
  public void after() {
    headerProcessor.setPropagateCorrelationIdForHttps(oldCorrelation);
  }

  @Test
  public void checkSoapActionSetTest() {
    template.sendBodyAndHeaders(TestSoapRequests.GET_CERT_HTTPS_REQUEST, HeadersUtil.getHttpsHeadersWithoutMembers());
    String s = (String) resultEndpoint.getExchanges().get(0).getIn().getHeader(SOAP_ACTION);
    assertFalse(StringUtils.isEmpty(s));
    testPositiveLogMessage(MessageInfoLogger.RESP_OUT, "LogMessage=resp-out", SOAP_ACTION + "=action");
  }

  // CorrelationId...passCorrelationId set to false.
  @Test // with headers set.
  public void checkCorrelationIdPropagatedWithIncomingHeaderSetAndPropagateCorrelationSetFalseTest() {
    headerProcessor.setPropagateCorrelationIdForHttps(false);
    template.sendBodyAndHeaders(TestSoapRequests.GET_CERT_HTTPS_REQUEST, HeadersUtil.getHttpsHeadersWithMembers());
    assertNull(resultEndpoint.getExchanges().get(0).getIn().getHeader(X_SKLTP_CORRELATION_ID));
    testPositiveLogMessage(MessageInfoLogger.REQ_IN, "LogMessage=req-in", "BusinessCorrelationId=");
    String respOutLogMsg = testLogAppender.getEventMessage(MessageInfoLogger.RESP_OUT, 0);
    assertStringContains(respOutLogMsg, "LogMessage=resp-out");
    assertFalse(respOutLogMsg.contains(X_SKLTP_CORRELATION_ID));

  }

  @Test // Without headers
  public void checkCorrelationIdPropagatedWithoutIncomingHeaderSetAndPropagateCorrelationSetFalseTest() {
    headerProcessor.setPropagateCorrelationIdForHttps(false);
    template.sendBodyAndHeaders(TestSoapRequests.GET_CERT_HTTPS_REQUEST, HeadersUtil.getHttpsHeadersWithoutMembers());
    assertNull(resultEndpoint.getExchanges().get(0).getIn().getHeader(X_SKLTP_CORRELATION_ID));
    testPositiveLogMessage(MessageInfoLogger.REQ_IN, "LogMessage=req-in", "BusinessCorrelationId=");

    String respOutLogMsg = testLogAppender.getEventMessage(MessageInfoLogger.RESP_OUT, 0);
    assertStringContains(respOutLogMsg, "LogMessage=resp-out");
    assertFalse(respOutLogMsg.contains(X_SKLTP_CORRELATION_ID));
  }

  // CorrelationId...passCorrelationId set to true.
  @Test // With headers set
  public void checkCorrelationIdPropagatedWhenIncomingHeaderSetAndPropagateCorrelationSetTrueTest() {
    headerProcessor.setPropagateCorrelationIdForHttps(true);
    template.sendBodyAndHeaders(TestSoapRequests.GET_CERT_HTTPS_REQUEST, HeadersUtil.getHttpsHeadersWithMembers());
    assertEquals(TEST_CORRELATION_ID, resultEndpoint.getExchanges().get(0).getIn().getHeader(X_SKLTP_CORRELATION_ID));
    testPositiveLogMessage(MessageInfoLogger.REQ_IN, "LogMessage=req-in", "BusinessCorrelationId=" + TEST_CORRELATION_ID);
    testPositiveLogMessage(MessageInfoLogger.RESP_OUT, "LogMessage=resp-out", X_SKLTP_CORRELATION_ID + "=" + TEST_CORRELATION_ID);
  }

  @Test // Without headers
  public void checkCorrelationIdPropagatedWhenNoIncomingHeaderSetAndPropagateCorrelationSetTrueTest() {
    headerProcessor.setPropagateCorrelationIdForHttps(true);
    template.sendBodyAndHeaders(TestSoapRequests.GET_CERT_HTTPS_REQUEST, HeadersUtil.getHttpsHeadersWithoutMembers());
    String s = (String) resultEndpoint.getExchanges().get(0).getIn().getHeader(X_SKLTP_CORRELATION_ID);
    assertNotNull(s);
    assertNotEquals(TEST_CORRELATION_ID, s);
    assertTrue(s.length() > 20);
    testPositiveLogMessage(MessageInfoLogger.REQ_IN, "LogMessage=req-in", "BusinessCorrelationId=" + s);
    testPositiveLogMessage(MessageInfoLogger.RESP_OUT, "LogMessage=resp-out", X_SKLTP_CORRELATION_ID + "=" + s);
  }

  // OriginalConsumerId
  @Test // With headers set.
  public void checkXrivtaOriginalConsumerIdPropagatedWhenIncomingHeaderSet() {
    template.sendBodyAndHeaders(TestSoapRequests.GET_CERT_HTTPS_REQUEST, HeadersUtil.getHttpsHeadersWithMembers());
    assertEquals(TEST_CONSUMER, resultEndpoint.getExchanges().get(0).getIn().getHeader(X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID));
    testPositiveLogMessage(MessageInfoLogger.REQ_IN, "LogMessage=req-in", LogExtraInfoBuilder.IN_ORIGINAL_SERVICE_CONSUMER_HSA_ID + "=" + TEST_CONSUMER);
    testPositiveLogMessage(MessageInfoLogger.RESP_OUT, "LogMessage=resp-out", X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID + "=" + TEST_CONSUMER);
  }

  @Test // Without headers.
  public void checkXrivtaConsumerIdPropagatedWhenNoIncomingHeaderTest() {
    template.sendBodyAndHeaders(TestSoapRequests.GET_CERT_HTTPS_REQUEST, HeadersUtil.getHttpsHeadersWithoutMembers());
    assertEquals(TEST_SENDER, resultEndpoint.getExchanges().get(0).getIn().getHeader(X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID));

    String reqInLogMsg = testLogAppender.getEventMessage(MessageInfoLogger.REQ_IN, 0);
    assertStringContains(reqInLogMsg, "LogMessage=req-in");
    boolean b = reqInLogMsg.contains(LogExtraInfoBuilder.IN_ORIGINAL_SERVICE_CONSUMER_HSA_ID + "=" + TEST_SENDER);
    assertFalse(b);

    testPositiveLogMessage(MessageInfoLogger.RESP_OUT, "LogMessage=resp-out", X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID + "=" +TEST_SENDER);
  }

  @Test
  public void checkUserAgentGetPropagated() {
    template.sendBodyAndHeaders(TestSoapRequests.GET_CERT_HTTPS_REQUEST, HeadersUtil.getHttpsHeadersWithoutMembers());
    String s = (String) resultEndpoint.getExchanges().get(0).getIn().getHeader(HEADER_USER_AGENT);
    assertEquals(s, vpHeaderUserAgent);
    testPositiveLogMessage(MessageInfoLogger.RESP_OUT, "LogMessage=resp-out", HEADER_USER_AGENT + "=" + vpHeaderUserAgent);
  }

  private void testPositiveLogMessage(String logger, String msg1, String msg2) {
    String logMessage = testLogAppender.getEventMessage(logger, 0);
    assertStringContains(logMessage, msg1);
    assertStringContains(logMessage, msg2);
  }

  @Test
  public void testInstanceIdDontGetPropagatedForHttps () {
    template.sendBodyAndHeaders(TestSoapRequests.GET_CERT_HTTPS_REQUEST, HeadersUtil.getHttpHeadersWithoutMembers());
    negativeHeaderAndLogTest(X_VP_INSTANCE_ID);
  }

  @Test
  public void testXvPSenderIdDontGetPropagatedForHttps () {
    template.sendBodyAndHeaders(TestSoapRequests.GET_CERT_HTTPS_REQUEST, HeadersUtil.getHttpHeadersWithoutMembers());
    negativeHeaderAndLogTest(X_VP_SENDER_ID);
  }

  private void negativeHeaderAndLogTest(String header) {
    assertNull(resultEndpoint.getExchanges().get(0).getIn().getHeader(header));
    String respOutLogMsg = testLogAppender.getEventMessage(MessageInfoLogger.RESP_OUT, 0);
    assertStringContains(respOutLogMsg, "LogMessage=resp-out");
    assertFalse(respOutLogMsg.contains(header));
  }

  private void addConsumerRoute(CamelContext camelContext) throws Exception {
    camelContext.addRoutes(
        new RouteBuilder() {
          @Override
          public void configure() {
            from("direct:start")
                .routeId("start")
                .to(
                    "netty4-http:"
                        + httpsRoute
                        + "?sslContextParameters=#incomingSSLContextParameters&ssl=true&"
                        + "sslClientCertHeaders=true&needClientAuth=true&matchOnUriPrefix=true");
            // Address below from tak-vagval-test.xml
            from("netty4-http:https://localhost:19001/vardgivare-b/tjanst2?sslContextParameters=#outgoingSSLContextParameters&ssl=true")
                .to("mock:result");
          }
        });
  }
}
