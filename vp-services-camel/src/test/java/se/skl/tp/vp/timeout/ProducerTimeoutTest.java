package se.skl.tp.vp.timeout;

import static se.skl.tp.vp.util.soaprequests.RoutingInfoUtil.createRoutingInfo;
import static se.skl.tp.vp.util.soaprequests.TestSoapRequests.RECEIVER_UNIT_TEST;
import static se.skl.tp.vp.util.soaprequests.TestSoapRequests.createGetCertificateRequest;
import static se.skl.tp.vp.util.takcache.TakCacheMockUtil.createTakCacheLogOk;
import static se.skl.tp.vp.util.takcache.TestTakDataDefines.RIV20;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import se.skl.tp.vp.TestBeanConfiguration;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.httpheader.SenderIpExtractor;
import se.skl.tp.vp.logging.MessageInfoLogger;
import se.skl.tp.vp.service.TakCacheService;
import se.skl.tp.vp.util.TestLogAppender;
import se.skltp.takcache.RoutingInfo;
import se.skltp.takcache.TakCache;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestBeanConfiguration.class)
@TestPropertySource("classpath:application.properties")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class ProducerTimeoutTest extends CamelTestSupport {

  @EndpointInject(uri = "mock:result")
  protected MockEndpoint resultEndpoint;

  @Produce(uri = "direct:start")
  protected ProducerTemplate template;

  @Autowired SenderIpExtractor senderIpExtractor;

  @Autowired CamelContext camelContext;

  @MockBean TakCache takCache;

  @Autowired TimeoutConfiguration timeoutConfiguration;

  @Autowired TakCacheService takCacheService;

  TestLogAppender testLogAppender = TestLogAppender.getInstance();

  private static boolean isContextStarted = false;

  @Before
  public void setUp() throws Exception {
    if (!isContextStarted) {
      createRoute(camelContext);
      camelContext.start();
      Mockito.when(takCache.refresh()).thenReturn(createTakCacheLogOk());
      takCacheService.refresh();
    }
    resultEndpoint.reset();
    testLogAppender.clearEvents();
    isContextStarted = true;
  }

  @Test
  public void configSetForTjanstekontraktTest() throws Exception {
    runTimeoutTestWithDifferentConfig(false);
  }

  @Test
  public void noConfigSetForTjanstekontraktTest() throws Exception {
    runTimeoutTestWithDifferentConfig(true);
  }

  private void runTimeoutTestWithDifferentConfig(boolean onlyDefaultTimeoutInConfig) throws Exception {
    timeoutConfiguration.setMapOnTjansteKontrakt(getConfigMap(onlyDefaultTimeoutInConfig));
    List<RoutingInfo> list = new ArrayList<>();
    list.add(createRoutingInfo("http://localhost:12123/vp", RIV20));
    Mockito.when(takCache.getRoutingInfo("urn:riv:insuranceprocess:healthreporting:GetCertificateResponder:1",
            "UnitTest")).thenReturn(list);
    Mockito.when(takCache.isAuthorized("UnitTest","urn:riv:insuranceprocess:healthreporting:GetCertificateResponder:1",
                "UnitTest")).thenReturn(true);
    template.sendBody(createGetCertificateRequest(RECEIVER_UNIT_TEST));

    resultEndpoint.expectedHeaderReceived(Exchange.HTTP_RESPONSE_CODE, 500);
    String resultBody = resultEndpoint.getExchanges().get(0).getIn().getBody(String.class);
    assertStringContains(resultBody, "Timeout");
    resultEndpoint.assertIsSatisfied();

    assertEquals(1, testLogAppender.getNumEvents(MessageInfoLogger.REQ_ERROR));
    String errorLogMsg = testLogAppender.getEventMessage(MessageInfoLogger.REQ_ERROR, 0);
    assertStringContains(errorLogMsg, "-errorCode=VP009");
    assertStringContains(errorLogMsg, "Stacktrace=io.netty.handler.timeout.ReadTimeoutException:");

    assertEquals(1, testLogAppender.getNumEvents(MessageInfoLogger.REQ_OUT));
    String reqOutLogMsg = testLogAppender.getEventMessage(MessageInfoLogger.REQ_OUT, 0);
    if (onlyDefaultTimeoutInConfig) {
      assertStringContains(reqOutLogMsg, "CamelNettyRequestTimeout=500");
    } else {
      assertStringContains(reqOutLogMsg, "CamelNettyRequestTimeout=460");
    }
  }

  private HashMap<String, TimeoutConfig> getConfigMap(boolean defaultOnly) {
    HashMap<String, TimeoutConfig> map = new HashMap<>();
    TimeoutConfig timeoutConfig = new TimeoutConfig();
    timeoutConfig.setTjanstekontrakt("default_timeouts");
    timeoutConfig.setProducertimeout(500);
    map.put("default_timeouts", timeoutConfig);
    if (!defaultOnly) {
      timeoutConfig = new TimeoutConfig();
      timeoutConfig.setTjanstekontrakt("urn:riv:insuranceprocess:healthreporting:GetCertificateResponder:1");
      timeoutConfig.setProducertimeout(460);
      map.put("urn:riv:insuranceprocess:healthreporting:GetCertificateResponder:1", timeoutConfig);
    }
    return map;
  }

  private void createRoute(CamelContext camelContext) throws Exception {
    camelContext.addRoutes( new RouteBuilder() {
          @Override
          public void configure() throws Exception {
            from("direct:start")
                .setHeader(HttpHeaders.X_VP_SENDER_ID, constant("UnitTest"))
                .setHeader(HttpHeaders.X_VP_INSTANCE_ID, constant("dev_env"))
                .setHeader("X-Forwarded-For", constant("1.2.3.4"))
                .to("http4://localhost:12312/vp?throwExceptionOnFailure=false")
                .to("mock:result");
            ;
            from("netty4-http:http://localhost:12123/vp")
                .process(
                    (Exchange exchange) -> {
                      Thread.sleep(1000);
                    });
          }
        });
  }
}
