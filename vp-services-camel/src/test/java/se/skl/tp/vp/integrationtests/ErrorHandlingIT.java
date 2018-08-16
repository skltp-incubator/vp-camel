package se.skl.tp.vp.integrationtests;

import static se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum.VP002;
import static se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum.VP004;
import static se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum.VP007;
import static se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum.VP009;
import static se.skl.tp.vp.util.soaprequests.TestSoapRequests.RECEIVER_NOT_AUHORIZED;
import static se.skl.tp.vp.util.soaprequests.TestSoapRequests.RECEIVER_WITH_NO_VAGVAL;
import static se.skl.tp.vp.util.soaprequests.TestSoapRequests.TJANSTEKONTRAKT_GET_CERTIFICATE_KEY;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.soap.SOAPBody;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import se.skl.tp.vp.Application;
import se.skl.tp.vp.integrationtests.utils.TakMockWebService;
import se.skl.tp.vp.util.soaprequests.SoapUtils;
import se.skl.tp.vp.util.soaprequests.TestSoapRequests;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class ErrorHandlingIT extends IntegrationTestBase {


  @SuppressWarnings("unchecked")
  @BeforeClass
  public static void beforeClass() throws IOException {

    // Seems to be som kind of conflict with JAXB camel dependencies and JDK8 Jaxb impl.
    // https://stackoverflow.com/questions/42499436/classcastexception-cannot-be-cast-to-com-sun-xml-internal-bind-v2-runtime-refle
//    System.setProperty( "com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");

    //TODO Use dynamic ports and also set TAK address used by takcache (Override "takcache.endpoint.address" property)
    TakMockWebService takMockWebService = new TakMockWebService("http://localhost:8086/tak-services/SokVagvalsInfo/v2");
    takMockWebService.start();
//    System.setProperty("takcache.endpoint.address", String.format("http://localhost:%d/tak-services/SokVagvalsInfo/v2", DYNAMIC_PORT));
  }

  @Test
  public void shouldGetVP002WhenNoCertificateInHTTPCall() throws Exception {
    Map<String, Object> headers = new HashMap<>();

    String result = sendHttpRequestToVP(TestSoapRequests.GET_CERTIFICATE_TO_UNIT_TEST_SOAP_REQUEST, headers);

    SOAPBody soapBody = SoapUtils.getSoapBody(result);
    assertNotNull("Expected a SOAP message", soapBody);
    assertNotNull("Expected a SOAPFault", soapBody.hasFault());

    System.out.printf("Code:%s FaultString:%s\n", soapBody.getFault().getFaultCode(),
        soapBody.getFault().getFaultString());
    assertStringContains(soapBody.getFault().getFaultString(), VP002.getCode());

  }

  @Test
  public void shouldGetVP004WhenRecieverNotInVagval() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    String result = sendHttpsRequestToVP(TestSoapRequests.GET_CERTIFICATE_NO_VAGVAL_IN_TAK, headers);

    SOAPBody soapBody = SoapUtils.getSoapBody(result);
    assertNotNull("Expected a SOAP message", soapBody);
    assertNotNull("Expected a SOAPFault", soapBody.hasFault());

    System.out.printf("Code:%s FaultString:%s\n", soapBody.getFault().getFaultCode(),
        soapBody.getFault().getFaultString());
    assertStringContains(soapBody.getFault().getFaultString(), VP004.getCode());
    assertStringContains(soapBody.getFault().getFaultString(),
        " No receiverId (logical address) found for");
    assertStringContains(soapBody.getFault().getFaultString(),
        RECEIVER_WITH_NO_VAGVAL);
    assertStringContains(soapBody.getFault().getFaultString(),
        TJANSTEKONTRAKT_GET_CERTIFICATE_KEY);

  }

  @Test
  public void shouldGetVP007WhenRecieverNotAuhtorized() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    String result = sendHttpsRequestToVP(TestSoapRequests.GET_CERTIFICATE_NOT_AUTHORIZED_IN_TAK, headers);

    SOAPBody soapBody = SoapUtils.getSoapBody(result);
    assertNotNull("Expected a SOAP message", soapBody);
    assertNotNull("Expected a SOAPFault", soapBody.hasFault());

    System.out.printf("Code:%s FaultString:%s\n", soapBody.getFault().getFaultCode(),
        soapBody.getFault().getFaultString());
    assertStringContains(soapBody.getFault().getFaultString(), VP007.getCode());
    assertStringContains(soapBody.getFault().getFaultString(),
        "Authorization missing for");
    assertStringContains(soapBody.getFault().getFaultString(),
        RECEIVER_NOT_AUHORIZED);
    assertStringContains(soapBody.getFault().getFaultString(),
        TJANSTEKONTRAKT_GET_CERTIFICATE_KEY);

  }

  @Test
  public void shouldGetVP009WhenProducerNotAvailable() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    String result = sendHttpsRequestToVP(TestSoapRequests.GET_CERTIFICATE_NO_PRODUCER_NOT_AVAILABLE_, headers);

    SOAPBody soapBody = SoapUtils.getSoapBody(result);
    assertNotNull("Expected a SOAP message", soapBody);
    assertNotNull("Expected a SOAPFault", soapBody.hasFault());

    System.out.printf("Code:%s FaultString:%s\n", soapBody.getFault().getFaultCode(),
        soapBody.getFault().getFaultString());
    assertStringContains(soapBody.getFault().getFaultString(), VP009.getCode());
  }

}
