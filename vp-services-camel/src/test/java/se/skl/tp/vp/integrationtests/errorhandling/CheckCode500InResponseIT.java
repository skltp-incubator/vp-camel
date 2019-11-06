package se.skl.tp.vp.integrationtests.errorhandling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static se.skl.tp.vp.VPRouter.DIRECT_PRODUCER_ROUTE;

import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.netty4.http.NettyHttpOperationFailedException;
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
import se.skl.tp.vp.constants.PropertyConstants;
import se.skl.tp.vp.errorhandling.HandleProducerExceptionProcessor;
import se.skl.tp.vp.integrationtests.httpheader.HeadersUtil;
import se.skl.tp.vp.integrationtests.utils.StartTakService;
import se.skl.tp.vp.util.LeakDetectionBaseTest;
import se.skl.tp.vp.util.soaprequests.TestSoapRequests;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = TestBeanConfiguration.class)
@TestPropertySource("classpath:application.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@StartTakService
public class CheckCode500InResponseIT extends LeakDetectionBaseTest {

  @Value("${" + PropertyConstants.VP_HTTP_ROUTE_URL + "}")
  private String httpRoute;

  @Value("${" + PropertyConstants.VP_HTTPS_ROUTE_URL + "}")
  private String httpsRoute;

  @EndpointInject(uri = "mock:result")
  protected MockEndpoint resultEndpoint;

  @Produce(uri = "direct:start")
  protected ProducerTemplate template;

  @Autowired
  private CamelContext camelContext;

  @Autowired
  HandleProducerExceptionProcessor handleProducerExceptionProcessor;

  private static boolean isContextStarted = false;

  @Before
  public void setUp() throws Exception {
    if (!isContextStarted) {
      camelContext.start();
      isContextStarted = true;
    }
    resultEndpoint.reset();
  }

  @Test
  public void checkCode500InHttpResponseTest() {
    String body = TestSoapRequests.GET_NO_CERT_HTTP_SOAP_REQUEST_NO_VAGVAL_RECEIVER;
    Map headers = HeadersUtil.createHttpHeaders();

    try {
      addConsumerRoute(camelContext);
      template.sendBodyAndHeaders(body, headers);
    } catch (Exception e) {
      assertExceptionContent(e);
    }
  }

  @Test
  public void checkCode500InHttpsResponseTest() {
    String body = TestSoapRequests.GET_CERT_HTTPS_REQUEST;
    Map headers = HeadersUtil.createHttpsHeaders();

    try {
      addHttpsConsumerRoute(camelContext);
      template.sendBodyAndHeaders(body, headers);
    } catch (Exception e) {
      assertExceptionContent(e);
    }
  }

  public void assertExceptionContent(Exception e) {
    String content = null;
    int code = -1;
    Throwable t = e.getCause();
    if (t instanceof NettyHttpOperationFailedException)  {
      NettyHttpOperationFailedException n = (NettyHttpOperationFailedException) t;
      content = n.getContentAsString();
      code = n.getStatusCode();
    }
    assertEquals(500, code);
    assertTrue(content.contains("VP004 No receiverId (logical address) found for serviceNamespace: " +
            "urn:riv:insuranceprocess:healthreporting:GetCertificateResponder:1, receiverId: NoVagvalReceiver"));
  }

  private void addHttpsConsumerRoute(CamelContext camelContext) throws Exception {
    camelContext.addRoutes(
            new RouteBuilder() {
              @Override
              public void configure() {from("direct:start").routeId("start")
                .to("netty4-http:" + httpsRoute + "?sslContextParameters=#incomingSSLContextParameters&ssl=true&"
                  + "sslClientCertHeaders=true&needClientAuth=true&matchOnUriPrefix=true");
                // Address below from tak-vagval-test.xml
                from("netty4-http:https://localhost:19001/vardgivare-b/tjanst2?sslContextParameters=#outgoingSSLContextParameters&ssl=true")
                .to("mock:result");
              }
            });
  }

  private void addConsumerRoute(CamelContext camelContext) throws Exception {
    camelContext.addRoutes(
            new RouteBuilder() {
              @Override
              public void configure() {
                from("direct:start").routeId("start").routeDescription("consumer")
                .to("netty4-http:" + httpRoute);
                // Address below from tak-vagval-test.xml
                from("netty4-http:http://localhost:19000/vardgivare-b/tjanst2").routeDescription("producer").to(DIRECT_PRODUCER_ROUTE)
                .to("mock:result");
              }
            });
  }
}
