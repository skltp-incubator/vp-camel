package se.skl.tp.vp.integrationtests.errorhandling;

import org.apache.camel.*;
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
import se.skl.tp.vp.util.soaprequests.TestSoapRequests;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static se.skl.tp.vp.VPRouter.DIRECT_PRODUCER_ROUTE;
import static se.skl.tp.vp.util.soaprequests.TestSoapRequests.RECEIVER_RIV20;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = TestBeanConfiguration.class)
@TestPropertySource("classpath:application.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@StartTakService
public class CheckCode500InResponseIT {

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
    String body = TestSoapRequests.createGetActivitiesRiv20Request(RECEIVER_RIV20);
    Map headers = HeadersUtil.getHttpHeadersWithoutMembers();
    body = body.replace("<urn2:extension>197404188888</urn2:extension>", "<urn2:extension></urn2:extension>");
    try {
      addHttpConsumerRoute(camelContext);
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
    assertTrue(content.contains("<faultcode>soap:Server</faultcode>"));
    assertTrue(content.contains("<faultstring>null</faultstring>"));
  }


  private void addHttpConsumerRoute(CamelContext camelContext) throws Exception {
    camelContext.addRoutes(
            new RouteBuilder() {
              @Override
              public void configure() {
                from("direct:start").routeId("start").routeDescription("consumer")
                .to("netty4-http:" + httpRoute);
                // Address below from tak-vagval-test.xml
                from("netty4-http:http://localhost:19000/GetActivitiesResponder").routeDescription("producer").to(DIRECT_PRODUCER_ROUTE)
                .to("mock:result");
              }
            });
  }
}
