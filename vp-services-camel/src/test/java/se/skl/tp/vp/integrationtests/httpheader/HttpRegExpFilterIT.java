package se.skl.tp.vp.integrationtests.httpheader;
import static junit.framework.TestCase.assertTrue;
import static se.skl.tp.vp.util.soaprequests.RoutingInfoUtil.createRoutingInfo;
import static se.skl.tp.vp.util.takcache.TakCacheMockUtil.createTakCacheLogOk;
import static se.skl.tp.vp.util.takcache.TestTakDataDefines.RIV20;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.netty4.http.NettyHttpMessage;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import se.skl.tp.vp.TestBeanConfiguration;
import se.skl.tp.vp.constants.HeadersTestData;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.service.TakCacheService;
import se.skl.tp.vp.util.soaprequests.TestSoapRequests;
import se.skltp.takcache.RoutingInfo;
import se.skltp.takcache.TakCache;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = TestBeanConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class HttpRegExpFilterIT implements RouteProcessEventListener {
  private static final String MOCK_PRODUCER_ADDRESS = "http://localhost:12126/vp";
  private static final String VP_ADDRESS = "http://localhost:12312/vp";
  private static boolean isContextStarted = false;
  private Map<String, Object> lastCopyOfReceivedHeadersAtMockProducer = new HashMap<>();

  private static MockFaultyHeadersProducer mockProducer;

  @MockBean
  TakCache takCache;

  @Autowired
  TakCacheService takCacheService;

  @Autowired
  private CamelContext camelContext;

  private HeadersTestData testData;

  @EndpointInject(uri = "mock:result")
  protected MockEndpoint resultEndpoint;

  @Produce(uri = "direct:start")
  protected ProducerTemplate template;

  @Before
  public void init() throws Exception {
    testData = new HeadersTestData();

    if (!isContextStarted) {
      mockProducer = new MockFaultyHeadersProducer(camelContext, MOCK_PRODUCER_ADDRESS, this);
      addMyRoute(camelContext);
      camelContext.start();
      isContextStarted = true;
    }

    resultEndpoint.reset();
    Mockito.when(takCache.refresh()).thenReturn(createTakCacheLogOk());
    takCacheService.refresh();
  }

  @Test
  public void checkMockProducerHeaderTest() {

    mockProducer.setHeaders(testData.getAllTestHeaders());

    List<RoutingInfo> list = new ArrayList<>();

    list.add(createRoutingInfo(MOCK_PRODUCER_ADDRESS, RIV20));

    setTakCacheMockResult(list);

    template.sendBody(TestSoapRequests.GET_CERTIFICATE_TO_UNIT_TEST_SOAP_REQUEST);


    boolean expected;
    for (String key : lastCopyOfReceivedHeadersAtMockProducer.keySet()) {
      expected = testData.getExpectedHeadersIgnoreCase().contains(key);
      assertTrue(
          "A \"new\" http header: "
              + key
              + " was detected. If This header is supposed to "
              + "bee forwarded to the producer please add this header in HeadersTestData.EXPECTED_HEADERS. "
              + "Otherwise please edit the application.properties.headers.reg.exp.removeRegExp",
          expected);
    }

  }

  private void addMyRoute(CamelContext camelContext) throws Exception {
    camelContext.addRoutes(
        new RouteBuilder() {
          @Override
          public void configure()  {

            from("direct:start")
                .routeDescription("Consumer")
                .id("Consumer")
                .setHeader(HttpHeaders.X_VP_SENDER_ID, constant("UnitTest"))
                .setHeader(HttpHeaders.X_VP_INSTANCE_ID, constant("dev_env"))
                .setHeader("X-Forwarded-For", constant("1.2.3.4"))
                .to("netty4-http:" + VP_ADDRESS)
                .to("mock:result");
          }
        });
  }

  private void setTakCacheMockResult(List<RoutingInfo> list) {
    Mockito.when(
        takCache.getRoutingInfo(
            "urn:riv:insuranceprocess:healthreporting:GetCertificateResponder:1", "UnitTest"))
        .thenReturn(list);
    Mockito.when(
        takCache.isAuthorized(
            "UnitTest",
            "urn:riv:insuranceprocess:healthreporting:GetCertificateResponder:1",
            "UnitTest"))
        .thenReturn(true);
  }

  @Override
  public void OnBeforeProcess(Exchange exchange) {
    List<Entry<String, String>> entries =
        ((NettyHttpMessage) exchange.getIn()).getHttpRequest().headers().entries();
    for (Entry<String, String> e : entries) {
      lastCopyOfReceivedHeadersAtMockProducer.put(e.getKey(), e.getValue());
    }
  }
}
