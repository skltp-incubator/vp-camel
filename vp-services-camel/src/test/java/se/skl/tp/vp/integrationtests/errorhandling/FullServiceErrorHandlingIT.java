package se.skl.tp.vp.integrationtests.errorhandling;

import static org.apache.camel.test.junit4.TestSupport.assertStringContains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum.VP002;
import static se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum.VP003;
import static se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum.VP004;
import static se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum.VP005;
import static se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum.VP006;
import static se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum.VP007;
import static se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum.VP009;
import static se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum.VP010;
import static se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum.VP011;
import static se.skl.tp.vp.util.soaprequests.TestSoapRequests.RECEIVER_NOT_AUHORIZED;
import static se.skl.tp.vp.util.soaprequests.TestSoapRequests.RECEIVER_WITH_NO_VAGVAL;
import static se.skl.tp.vp.util.soaprequests.TestSoapRequests.TJANSTEKONTRAKT_GET_CERTIFICATE_KEY;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.soap.SOAPBody;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.logging.log4j.core.LogEvent;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.integrationtests.utils.TakMockWebService;
import se.skl.tp.vp.integrationtests.utils.TestConsumer;
import se.skl.tp.vp.logging.MessageInfoLogger;
import se.skl.tp.vp.util.TestLogAppender;
import se.skl.tp.vp.util.soaprequests.SoapUtils;
import se.skl.tp.vp.util.soaprequests.TestSoapRequests;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class FullServiceErrorHandlingIT {

  @Autowired
  TestConsumer testConsumer;

  static TakMockWebService takMockWebService;

  TestLogAppender testLogAppender = TestLogAppender.getInstance();

  @SuppressWarnings("unchecked")
  @BeforeClass
  public static void beforeClass() throws IOException {

    // Seems to be som kind of conflict with JAXB camel dependencies and JDK8 Jaxb impl.
    // https://stackoverflow.com/questions/42499436/classcastexception-cannot-be-cast-to-com-sun-xml-internal-bind-v2-runtime-refle
//    System.setProperty( "com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");

    //TODO Use dynamic ports and also set TAK address used by takcache (Override "takcache.endpoint.address" property)
    takMockWebService = new TakMockWebService("http://localhost:8086/tak-services/SokVagvalsInfo/v2");
    takMockWebService.start();

//    System.setProperty("takcache.endpoint.address", String.format("http://localhost:%d/tak-services/SokVagvalsInfo/v2", DYNAMIC_PORT));
  }

  @AfterClass
  public static void afterClass(){
    if(takMockWebService!=null){
      takMockWebService.stop();
    }
  }

  @Before
  public void beforeTest(){
    testLogAppender.clearEvents();
  }

  @Test
  public void shouldGetVP002WhenNoCertificateInHTTPCall() throws Exception {
    Map<String, Object> headers = new HashMap<>();

    String result = testConsumer.sendHttpRequestToVP(TestSoapRequests.GET_CERTIFICATE_TO_UNIT_TEST_SOAP_REQUEST, headers);

    SOAPBody soapBody = SoapUtils.getSoapBody(result);
    assertNotNull("Expected a SOAP message", soapBody);
    assertNotNull("Expected a SOAPFault", soapBody.hasFault());

    System.out.printf("Code:%s FaultString:%s\n", soapBody.getFault().getFaultCode(),
        soapBody.getFault().getFaultString());
    assertStringContains(soapBody.getFault().getFaultString(), VP002.getCode());

    assertEquals(1,testLogAppender.getNumEvents(MessageInfoLogger.REQ_ERROR));
    String errorLogMsg = testLogAppender.getEventMessage(MessageInfoLogger.REQ_ERROR,0);
    assertStringContains(errorLogMsg, "-errorCode=VP002");
    assertStringContains(errorLogMsg, "Stacktrace=se.skl.tp.vp.exceptions.VpSemanticException: VP002");

  }

  @Test
  public void shouldGetVP003WhenNoReveieverExist() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    String result = testConsumer.sendHttpsRequestToVP(TestSoapRequests.GET_CERTIFICATE_NO_RECEIVER, headers);

    SOAPBody soapBody = SoapUtils.getSoapBody(result);
    assertNotNull("Expected a SOAP message", soapBody);
    assertNotNull("Expected a SOAPFault", soapBody.hasFault());

    System.out.printf("Code:%s FaultString:%s\n", soapBody.getFault().getFaultCode(),
        soapBody.getFault().getFaultString());
    assertStringContains(soapBody.getFault().getFaultString(), VP003.getCode());

    assertEquals(1,testLogAppender.getNumEvents(MessageInfoLogger.REQ_ERROR));
    String errorLogMsg = testLogAppender.getEventMessage(MessageInfoLogger.REQ_ERROR,0);
    assertStringContains(errorLogMsg, "-errorCode=VP003");
    assertStringContains(errorLogMsg, "Stacktrace=se.skl.tp.vp.exceptions.VpSemanticException: VP003");

  }

  @Test
  public void shouldGetVP004WhenRecieverNotInVagval() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    String result = testConsumer.sendHttpsRequestToVP(TestSoapRequests.GET_CERTIFICATE_NO_VAGVAL_IN_TAK, headers);

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

    assertEquals(1,testLogAppender.getNumEvents(MessageInfoLogger.REQ_ERROR));
    String errorLogMsg = testLogAppender.getEventMessage(MessageInfoLogger.REQ_ERROR,0);
    assertStringContains(errorLogMsg, "-errorCode=VP004");
    assertStringContains(errorLogMsg, "Stacktrace=se.skl.tp.vp.exceptions.VpSemanticException: VP004");
  }

  @Test
  public void shouldGetVP005WhenUnkownRivVersionInTAK() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    String result = testConsumer.sendHttpsRequestToVP(TestSoapRequests.GET_CERTIFICATE_UNKNOWN_RIVVERSION_, headers);

    SOAPBody soapBody = SoapUtils.getSoapBody(result);
    assertNotNull("Expected a SOAP message", soapBody);
    assertNotNull("Expected a SOAPFault", soapBody.hasFault());

    System.out.printf("Code:%s FaultString:%s\n", soapBody.getFault().getFaultCode(),
        soapBody.getFault().getFaultString());
    assertStringContains(soapBody.getFault().getFaultString(), VP005.getCode());
    assertStringContains(soapBody.getFault().getFaultString(), "rivtabp20");

    assertEquals(1,testLogAppender.getNumEvents(MessageInfoLogger.REQ_ERROR));
    String errorLogMsg = testLogAppender.getEventMessage(MessageInfoLogger.REQ_ERROR,0);
    assertStringContains(errorLogMsg, "-errorCode=VP005");
    assertStringContains(errorLogMsg, "Stacktrace=se.skl.tp.vp.exceptions.VpSemanticException: VP005");
  }

  @Test
  public void shouldGetVP006WhenMultipleVagvalExist() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    String result = testConsumer.sendHttpsRequestToVP(TestSoapRequests.GET_CERTIFICATE_MULTIPLE_VAGVAL, headers);

    SOAPBody soapBody = SoapUtils.getSoapBody(result);
    assertNotNull("Expected a SOAP message", soapBody);
    assertNotNull("Expected a SOAPFault", soapBody.hasFault());

    System.out.printf("Code:%s FaultString:%s\n", soapBody.getFault().getFaultCode(),
        soapBody.getFault().getFaultString());
    assertStringContains(soapBody.getFault().getFaultString(), VP006.getCode());
    assertStringContains(soapBody.getFault().getFaultString(), "RecevierMultipleVagval");

    assertEquals(1,testLogAppender.getNumEvents(MessageInfoLogger.REQ_ERROR));
    String errorLogMsg = testLogAppender.getEventMessage(MessageInfoLogger.REQ_ERROR,0);
    assertStringContains(errorLogMsg, "-errorCode=VP006");
    assertStringContains(errorLogMsg, "Stacktrace=se.skl.tp.vp.exceptions.VpSemanticException: VP006");
  }

  @Test
  public void shouldGetVP007WhenRecieverNotAuhtorized() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    String result = testConsumer.sendHttpsRequestToVP(TestSoapRequests.GET_CERTIFICATE_NOT_AUTHORIZED_IN_TAK, headers);

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

    assertEquals(1,testLogAppender.getNumEvents(MessageInfoLogger.REQ_ERROR));
    String errorLogMsg = testLogAppender.getEventMessage(MessageInfoLogger.REQ_ERROR,0);
    assertStringContains(errorLogMsg, "-errorCode=VP007");
    assertStringContains(errorLogMsg, "Stacktrace=se.skl.tp.vp.exceptions.VpSemanticException: VP007");

  }

  @Test
  public void shouldGetVP009WhenProducerNotAvailable() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    String result = testConsumer.sendHttpsRequestToVP(TestSoapRequests.GET_CERTIFICATE_NO_PRODUCER_NOT_AVAILABLE_, headers);

    SOAPBody soapBody = SoapUtils.getSoapBody(result);
    assertNotNull("Expected a SOAP message", soapBody);
    assertNotNull("Expected a SOAPFault", soapBody.hasFault());

    System.out.printf("Code:%s FaultString:%s\n", soapBody.getFault().getFaultCode(),
        soapBody.getFault().getFaultString());
    assertStringContains(soapBody.getFault().getFaultString(), VP009.getCode());

    assertEquals(1,testLogAppender.getNumEvents(MessageInfoLogger.REQ_ERROR));
    String errorLogMsg = testLogAppender.getEventMessage(MessageInfoLogger.REQ_ERROR,0);
    assertStringContains(errorLogMsg, "-errorCode=VP009");
    assertStringContains(errorLogMsg, "Stacktrace=java.net.ConnectException: Cannot connect to");
  }

  @Test
  public void shouldGetVP010WhenPhysicalAdressEmptyInVagval() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    String result = testConsumer.sendHttpsRequestToVP(TestSoapRequests.GET_CERTIFICATE_NO_PHYSICAL_ADDRESS, headers);

    SOAPBody soapBody = SoapUtils.getSoapBody(result);
    assertNotNull("Expected a SOAP message", soapBody);
    assertNotNull("Expected a SOAPFault", soapBody.hasFault());

    System.out.printf("Code:%s FaultString:%s\n", soapBody.getFault().getFaultCode(),
        soapBody.getFault().getFaultString());
    assertStringContains(soapBody.getFault().getFaultString(), VP010.getCode());
    assertStringContains(soapBody.getFault().getFaultString(), "RecevierNoPhysicalAddress");

    assertEquals(1,testLogAppender.getNumEvents(MessageInfoLogger.REQ_ERROR));
    String errorLogMsg = testLogAppender.getEventMessage(MessageInfoLogger.REQ_ERROR,0);
    assertStringContains(errorLogMsg, "-errorCode=VP010");
    assertStringContains(errorLogMsg, "Stacktrace=se.skl.tp.vp.exceptions.VpSemanticException: VP010");
  }

  @Test
  public void shouldGetVP011ifIpAddressIsNotWhitelisted() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    headers.put(HttpHeaders.X_VP_SENDER_ID, "Urken");
    headers.put(HttpHeaders.X_VP_INSTANCE_ID, "dev_env");
    headers.put("X-Forwarded-For", "10.20.30.40");
    String result = testConsumer.sendHttpRequestToVP(TestSoapRequests.GET_CERTIFICATE_NO_PRODUCER_NOT_AVAILABLE_, headers);

    SOAPBody soapBody = SoapUtils.getSoapBody(result);
    assertNotNull("Expected a SOAP message", soapBody);
    assertNotNull("Expected a SOAPFault", soapBody.hasFault());

    assertStringContains(soapBody.getFault().getFaultString(), VP011.getCode());
    assertStringContains(soapBody.getFault().getFaultString(), "10.20.30.40");

    assertEquals(1,testLogAppender.getNumEvents(MessageInfoLogger.REQ_ERROR));
    String errorLogMsg = testLogAppender.getEventMessage(MessageInfoLogger.REQ_ERROR,0);
    assertStringContains(errorLogMsg, "-errorCode=VP011");
    assertStringContains(errorLogMsg, "Stacktrace=se.skl.tp.vp.exceptions.VpSemanticException: VP011");
  }

}
