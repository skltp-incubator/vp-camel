package se.skl.tp.vp.integrationtests;

import static org.apache.camel.test.junit4.TestSupport.assertStringContains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static se.skl.tp.vp.util.soaprequests.TestSoapRequests.RECEIVER_HTTP;
import static se.skl.tp.vp.util.soaprequests.TestSoapRequests.RECEIVER_HTTPS;
import static se.skl.tp.vp.util.soaprequests.TestSoapRequests.createGetCertificateRequest;

import java.util.HashMap;
import java.util.Map;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.integrationtests.utils.MockProducer;
import se.skl.tp.vp.integrationtests.utils.StartTakService;
import se.skl.tp.vp.integrationtests.utils.TestConsumer;
import se.skl.tp.vp.logging.MessageInfoLogger;
import se.skl.tp.vp.util.TestLogAppender;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@StartTakService
public class FullServiceTestIT {

  public static final String HTTP_PRODUCER_URL = "http://localhost:19000/vardgivare-b/tjanst2";
  public static final String HTTPS_PRODUCER_URL = "https://localhost:19001/vardgivare-b/tjanst2";

  @Autowired
  TestConsumer testConsumer;

  @Autowired
  MockProducer mockProducer;

  @Autowired
  MockProducer mockHttpsProducer;

  @Value("${vp.http.route.url}")
  String vpHttpUrl;

  @Value("${vp.https.route.url}")
  String vpHttpsUrl;

  @Value("${vp.instance.id}")
  String vpInstanceId;

  @Value("${http.forwarded.header.for}")
  String forwardedHeaderFor;

  @Value("${http.forwarded.header.host}")
  String forwardedHeaderHost;

  @Value("${http.forwarded.header.port}")
  String forwardedHeaderPort;

  @Value("${http.forwarded.header.proto}")
  String forwardedHeaderProto;

  TestLogAppender testLogAppender = TestLogAppender.getInstance();

  @Before
  public void before() {
    try {
      mockProducer.start(HTTP_PRODUCER_URL);
      mockHttpsProducer
          .start(HTTPS_PRODUCER_URL +"?sslContextParameters=#outgoingSSLContextParameters&ssl=true");
    } catch (Exception e) {
      e.printStackTrace();
    }
    testLogAppender.clearEvents();
  }

  @Test
  public void callHttpsVPEndpoint2HttpProducerHappyDays() {
    mockProducer.setResponseBody("<mocked answer/>");

    Map<String, Object> headers = new HashMap<>();
    headers.put(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID,"originalid");
    String response = testConsumer.sendHttpsRequestToVP(createGetCertificateRequest(RECEIVER_HTTP), headers);
    assertEquals("<mocked answer/>", response);

    assertMessageLogsExists();

    String respOutLogMsg = testLogAppender.getEventMessage(MessageInfoLogger.RESP_OUT, 0);
    assertStringContains(respOutLogMsg, "skltp-messages");
    assertStringContains(respOutLogMsg, "LogMessage=resp-out");
    assertStringContains(respOutLogMsg, "ComponentId=vp-services");
    assertStringContains(respOutLogMsg, "Endpoint="+vpHttpsUrl);
    assertExtraInfoLog(respOutLogMsg, RECEIVER_HTTP, HTTP_PRODUCER_URL);
    assertStringContains(respOutLogMsg, "-originalServiceconsumerHsaid_in=originalid");
    assertStringContains(respOutLogMsg, "-originalServiceconsumerHsaid=originalid");


  }

  @Test
  public void callHttpVPEndpoint2HttpsProducerHappyDays() {
    mockProducer.setResponseBody("<mocked answer/>");

    Map<String, Object> headers = new HashMap<>();
    headers.put(HttpHeaders.X_VP_INSTANCE_ID,vpInstanceId);
    headers.put(HttpHeaders.X_VP_SENDER_ID,"tp");
    String response = testConsumer.sendHttpRequestToVP(createGetCertificateRequest(RECEIVER_HTTPS), headers);

    assertEquals("<mocked answer/>", response);

    assertMessageLogsExists();

    String respOutLogMsg = testLogAppender.getEventMessage(MessageInfoLogger.RESP_OUT, 0);
    assertStringContains(respOutLogMsg, "LogMessage=resp-out");
    assertStringContains(respOutLogMsg, "ComponentId=vp-services");
    assertStringContains(respOutLogMsg, "Endpoint="+vpHttpUrl);
    assertExtraInfoLog(respOutLogMsg, RECEIVER_HTTPS, HTTPS_PRODUCER_URL);
    assertStringContains(respOutLogMsg, "-originalServiceconsumerHsaid=tp");
    assertTrue(!respOutLogMsg.contains("-originalServiceconsumerHsaid_in"));
  }

  /**
   * Test for scenario where a reverse-proxy/loadbalancer sits in front of VP
   * and is required to forward original request info to VP for:
   * <ol>
   * <li>X-Forwarded-Proto</li>
   * <li>X-Forwarded-Host</li>
   * <li>X-Forwarded-Port</li>
   * <li>X-Forwarded-For</li>
   * </ol>
   * <p>The information is needed for:
   * <ul>
   * <li>re-writing URL's in WSDL's returned for WSDL lookups using ?wsdl</li>
   * <li>logging/tracing</li>
   * </ul>
   */
  @Test
  public void testLoadBalancerXForwardedInfo() throws Exception {

    mockProducer.setResponseBody("<mocked answer/>");

    Map<String, Object> headers = new HashMap<>();
    headers.put(HttpHeaders.X_VP_INSTANCE_ID,vpInstanceId);
    headers.put(HttpHeaders.X_VP_SENDER_ID,"tp");
    headers.put(forwardedHeaderFor,"1.2.3.4");
    headers.put(forwardedHeaderProto,"https");
    headers.put(forwardedHeaderHost,"skltp-lb.example.org");
    headers.put(forwardedHeaderPort,"443");

    String response = testConsumer.sendHttpRequestToVP(createGetCertificateRequest(RECEIVER_HTTPS), headers);
    assertEquals("<mocked answer/>", response);

    assertMessageLogsExists();

    String reqInLogMsg = testLogAppender.getEventMessage(MessageInfoLogger.REQ_IN, 0);
    assertStringContains(reqInLogMsg, "-senderIpAdress=1.2.3.4");
    assertStringContains(reqInLogMsg, "-httpXForwardedProto=https");
    assertStringContains(reqInLogMsg, "-httpXForwardedHost=skltp-lb.example.org");
    assertStringContains(reqInLogMsg, "-httpXForwardedPort=443");

  }

  private void assertMessageLogsExists() {
    assertEquals(0, testLogAppender.getNumEvents(MessageInfoLogger.REQ_ERROR));
    assertEquals(1, testLogAppender.getNumEvents(MessageInfoLogger.REQ_IN));
    assertEquals(1, testLogAppender.getNumEvents(MessageInfoLogger.REQ_OUT));
    assertEquals(1, testLogAppender.getNumEvents(MessageInfoLogger.RESP_IN));
    assertEquals(1, testLogAppender.getNumEvents(MessageInfoLogger.RESP_OUT));
  }

  private void assertExtraInfoLog(String respOutLogMsg, String expectedReceiverId, String expectedProducerUrl) {
    assertStringContains(respOutLogMsg, "-senderIpAdress=");
    assertStringContains(respOutLogMsg,
        "-servicecontract_namespace=urn:riv:insuranceprocess:healthreporting:GetCertificateResponder:1");
    assertStringContains(respOutLogMsg, "-senderid=tp");
    assertStringContains(respOutLogMsg, "-receiverid=" + expectedReceiverId);
    assertStringContains(respOutLogMsg, "-endpoint_url="+expectedProducerUrl);
    assertStringContains(respOutLogMsg, "-routerVagvalTrace=" + expectedReceiverId);
    assertStringContains(respOutLogMsg, "-wsdl_namespace=urn:riv:insuranceprocess:healthreporting:GetCertificate:1:rivtabp20");
    assertStringContains(respOutLogMsg, "-rivversion=rivtabp20");
    assertStringContains(respOutLogMsg, "-time.producer=");
    assertStringContains(respOutLogMsg, "-routerBehorighetTrace=" + expectedReceiverId);
  }
}
