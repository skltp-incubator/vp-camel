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
import static se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum.VP013;
import static se.skl.tp.vp.integrationtests.httpheader.HeadersUtil.TEST_CONSUMER;
import static se.skl.tp.vp.util.soaprequests.TestSoapRequests.RECEIVER_MULTIPLE_VAGVAL;
import static se.skl.tp.vp.util.soaprequests.TestSoapRequests.RECEIVER_NOT_AUHORIZED;
import static se.skl.tp.vp.util.soaprequests.TestSoapRequests.RECEIVER_NO_PHYSICAL_ADDRESS;
import static se.skl.tp.vp.util.soaprequests.TestSoapRequests.RECEIVER_NO_PRODUCER_AVAILABLE;
import static se.skl.tp.vp.util.soaprequests.TestSoapRequests.RECEIVER_UNIT_TEST;
import static se.skl.tp.vp.util.soaprequests.TestSoapRequests.RECEIVER_UNKNOWN_RIVVERSION;
import static se.skl.tp.vp.util.soaprequests.TestSoapRequests.RECEIVER_WITH_NO_VAGVAL;
import static se.skl.tp.vp.util.soaprequests.TestSoapRequests.TJANSTEKONTRAKT_GET_CERTIFICATE_KEY;
import static se.skl.tp.vp.util.soaprequests.TestSoapRequests.createGetCertificateRequest;

import java.util.HashMap;
import java.util.Map;
import javax.xml.soap.SOAPBody;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.integrationtests.utils.StartTakService;
import se.skl.tp.vp.integrationtests.utils.TestConsumer;
import se.skl.tp.vp.logging.MessageInfoLogger;
import se.skl.tp.vp.util.TestLogAppender;
import se.skl.tp.vp.util.soaprequests.SoapUtils;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest
@TestPropertySource(locations = {"classpath:application.properties","classpath:vp-messages.properties"})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@StartTakService
public class FullServiceErrorHandlingIT {

  @Autowired
  TestConsumer testConsumer;

  TestLogAppender testLogAppender = TestLogAppender.getInstance();

  @Value("VP013")
  String msgVP013;

  @Before
  public void beforeTest(){
    testLogAppender.clearEvents();
  }

  @Test
  public void shouldGetVP002WhenNoCertificateInHTTPCall() throws Exception {
    Map<String, Object> headers = new HashMap<>();

    String result = testConsumer.sendHttpRequestToVP(createGetCertificateRequest(RECEIVER_UNIT_TEST), headers);

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
  public void shouldGetVP003WhenNoReceieverExist() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    String result = testConsumer.sendHttpsRequestToVP(createGetCertificateRequest(""), headers);

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
    String result = testConsumer.sendHttpsRequestToVP(createGetCertificateRequest(RECEIVER_WITH_NO_VAGVAL), headers);

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
    String result = testConsumer.sendHttpsRequestToVP(createGetCertificateRequest(RECEIVER_UNKNOWN_RIVVERSION), headers);
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
    String result = testConsumer.sendHttpsRequestToVP(createGetCertificateRequest(RECEIVER_MULTIPLE_VAGVAL), headers);

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
    String result = testConsumer.sendHttpsRequestToVP(createGetCertificateRequest(RECEIVER_NOT_AUHORIZED), headers);

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
    String result = testConsumer.sendHttpsRequestToVP(createGetCertificateRequest(RECEIVER_NO_PRODUCER_AVAILABLE), headers);

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

    assertEquals(1,testLogAppender.getNumEvents(MessageInfoLogger.RESP_OUT));
    String respOutLogMsg = testLogAppender.getEventMessage(MessageInfoLogger.RESP_OUT, 0);
    assertStringContains(respOutLogMsg, "LogMessage=resp-out");
    assertStringContains(respOutLogMsg, "ComponentId=vp-services");
    assertExtraInfoLog(respOutLogMsg, RECEIVER_NO_PRODUCER_AVAILABLE, "https://localhost:1974/Im/not/available");
  }

  @Test
  public void shouldGetVP010WhenPhysicalAdressEmptyInVagval() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    String result = testConsumer.sendHttpsRequestToVP(createGetCertificateRequest(RECEIVER_NO_PHYSICAL_ADDRESS), headers);

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
    String result = testConsumer.sendHttpRequestToVP(createGetCertificateRequest(RECEIVER_NO_PRODUCER_AVAILABLE), headers);

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

  @Test
  public void shouldGetVP013WhenIllegalSender() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    headers.put(HttpHeaders.X_VP_SENDER_ID, "SENDER3"); //Not on list ip.consumer.list
    headers.put(HttpHeaders.X_VP_INSTANCE_ID, "dev_env");
    headers.put(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, TEST_CONSUMER);
    String result = testConsumer.sendHttpRequestToVP(createGetCertificateRequest(RECEIVER_NO_PRODUCER_AVAILABLE), headers);

    SOAPBody soapBody = SoapUtils.getSoapBody(result);
    assertNotNull("Expected a SOAP message", soapBody);
    assertNotNull("Expected a SOAPFault", soapBody.hasFault());

    assertStringContains(soapBody.getFault().getFaultString(), VP013.getCode());
    assertStringContains(soapBody.getFault().getFaultString(), msgVP013);

    assertEquals(1,testLogAppender.getNumEvents(MessageInfoLogger.REQ_ERROR));
    String errorLogMsg = testLogAppender.getEventMessage(MessageInfoLogger.REQ_ERROR,0);
    assertStringContains(errorLogMsg, "-errorCode=VP013");
    assertStringContains(errorLogMsg, msgVP013);
  }

  @Test
  public void shouldGetVP013WhenEmptySender() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    headers.put(HttpHeaders.X_VP_SENDER_ID, ""); //Not on list ip.consumer.list
    headers.put(HttpHeaders.X_VP_INSTANCE_ID, "dev_env");
    headers.put(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, TEST_CONSUMER);
    String result = testConsumer.sendHttpRequestToVP(createGetCertificateRequest(RECEIVER_NO_PRODUCER_AVAILABLE), headers);

    SOAPBody soapBody = SoapUtils.getSoapBody(result);
    assertNotNull("Expected a SOAP message", soapBody);
    assertNotNull("Expected a SOAPFault", soapBody.hasFault());

    assertStringContains(soapBody.getFault().getFaultString(), VP013.getCode());
    assertStringContains(soapBody.getFault().getFaultString(), msgVP013);

    assertEquals(1,testLogAppender.getNumEvents(MessageInfoLogger.REQ_ERROR));
    String errorLogMsg = testLogAppender.getEventMessage(MessageInfoLogger.REQ_ERROR,0);
    assertStringContains(errorLogMsg, VP013.getCode());
    assertStringContains(errorLogMsg, msgVP013);
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
    assertStringContains(respOutLogMsg, "-originalServiceconsumerHsaid=tp");
    assertStringContains(respOutLogMsg, "-rivversion=rivtabp20");
    assertStringContains(respOutLogMsg, "-routerBehorighetTrace=" + expectedReceiverId);
  }
}
