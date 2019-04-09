package se.skl.tp.vp.integrationtests.errorhandling;

import static org.apache.camel.test.junit4.TestSupport.assertStringContains;
import static org.junit.Assert.assertNotNull;
import static se.skl.tp.vp.VPRouter.VAGVAL_PROCESSOR_ID;
import static se.skl.tp.vp.VPRouter.VP_ROUTE;

import java.util.HashMap;
import java.util.Map;
import javax.xml.soap.SOAPBody;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import se.skl.tp.vp.integrationtests.utils.TestConsumer;
import se.skl.tp.vp.util.soaprequests.SoapUtils;
import se.skl.tp.vp.util.soaprequests.TestSoapRequests;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest
public class SoapFaultIT {

  public static final String TEST_EXCEPTION_MESSAGE = "Test exception message!";
  @EndpointInject(uri = "mock:vagvalprocessor")
  protected MockEndpoint resultEndpoint;

  @Autowired
  TestConsumer testConsumer;

  @Autowired
  private CamelContext camelContext;


  @Before
  public void mockVagvalProcessor() throws Exception {
    replaceVagvalProcessor();
    makeMockVagvalProcessorThrowException();
    camelContext.start();
  }

  @Test
  public void unexpectedExceptionInRouteShouldResultInSoapFault() throws Exception {

    Map<String, Object> headers = new HashMap<>();
    String result = testConsumer.sendHttpsRequestToVP(TestSoapRequests.GET_CERTIFICATE_NO_PRODUCER_NOT_AVAILABLE_, headers);

    SOAPBody soapBody = SoapUtils.getSoapBody(result);
    assertNotNull("Expected a SOAP message", soapBody);
    assertNotNull("Expected a SOAPFault", soapBody.hasFault());

    assertStringContains(soapBody.getFault().getFaultString(), TEST_EXCEPTION_MESSAGE);
  }

  private void replaceVagvalProcessor() throws Exception {
    AdviceWithRouteBuilder mockNetty = new AdviceWithRouteBuilder() {

      @Override
      public void configure() throws Exception {
        weaveById(VAGVAL_PROCESSOR_ID)
            .replace().to("mock:vagvalprocessor");
      }
    };
    camelContext.getRouteDefinition(VP_ROUTE).adviceWith(camelContext, mockNetty);
  }

  private void makeMockVagvalProcessorThrowException() {
    resultEndpoint.whenAnyExchangeReceived(new Processor() {
      @Override
      public void process(Exchange exchange) throws Exception {
        throw new NullPointerException(TEST_EXCEPTION_MESSAGE);
      }
    });
  }

}
