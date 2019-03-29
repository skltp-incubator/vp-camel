package se.skl.tp.vp.certificate;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.util.soaprequests.TestSoapRequests;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class CertificateReaderTest {

  @Autowired
  CamelContext camelContext;

  @Autowired
  Environment env;

  @EndpointInject(uri = "mock:result")
  protected MockEndpoint resultEndpoint;

  @Produce(uri = "direct:start")
  protected ProducerTemplate template;

  @Autowired
  CertificateExtractorProcessor certificateExtractorProcessor;

  @Before
  public void setUp() throws Exception {
    createRoute(camelContext);
    camelContext.start();
    resultEndpoint.reset();
  }

  private void createRoute(CamelContext camelContext) throws Exception {
    camelContext.addRoutes(new RouteBuilder() {
      @Override
      public void configure() throws Exception {
        from("direct:start")
            .to("netty4-http:https://localhost:4433/vp?sslContextParameters=#outgoingSSLContextParameters&ssl=true");

        from(
            "netty4-http:https://localhost:4433/vp?sslContextParameters=#incomingSSLContextParameters&ssl=true&sslClientCertHeaders=true&needClientAuth=true")
            .process(certificateExtractorProcessor)
            .to("mock:result");
      }
    });
  }

  @Test
  public void testSendRivta20MessageHttps() throws Exception {
    resultEndpoint.expectedBodiesReceived(TestSoapRequests.GET_CERTIFICATE_TO_UNIT_TEST_SOAP_REQUEST);
    resultEndpoint.expectedPropertyReceived(VPExchangeProperties.SENDER_ID, "tp");

    template.sendBody(TestSoapRequests.GET_CERTIFICATE_TO_UNIT_TEST_SOAP_REQUEST);
    resultEndpoint.assertIsSatisfied();
  }

}
