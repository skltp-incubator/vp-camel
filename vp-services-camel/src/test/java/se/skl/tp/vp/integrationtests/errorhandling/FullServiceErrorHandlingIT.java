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
    assertSoapFault(soapBody, VP002.getCode(), "");

    System.out.printf("Code:%s FaultString:%s\n", soapBody.getFault().getFaultCode(),
        soapBody.getFault().getFaultString());

    assertErrorLog(VP002.getCode(), "Stacktrace=se.skl.tp.vp.exceptions.VpSemanticException: VP002");
    assertRespOutLog("VP002 No certificate found in httpheader x-vp-auth-cert");
  }

  @Test
  public void shouldGetVP003WhenNoReceieverExist() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    String result = testConsumer.sendHttpsRequestToVP(createGetCertificateRequest(""), headers);

    SOAPBody soapBody = SoapUtils.getSoapBody(result);
    assertSoapFault(soapBody, VP003.getCode(), "");

    System.out.printf("Code:%s FaultString:%s\n", soapBody.getFault().getFaultCode(),
        soapBody.getFault().getFaultString());

    assertErrorLog(VP003.getCode(), "Stacktrace=se.skl.tp.vp.exceptions.VpSemanticException: VP003");
    assertRespOutLog("VP003 No receiverId (logical address) found in message header. null");
  }

  @Test
  public void shouldGetVP004WhenRecieverNotInVagval() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    String result = testConsumer.sendHttpsRequestToVP(createGetCertificateRequest(RECEIVER_WITH_NO_VAGVAL), headers);

    SOAPBody soapBody = SoapUtils.getSoapBody(result);
    assertSoapFault(soapBody, VP004.getCode(), " No receiverId (logical address) found for",
            RECEIVER_WITH_NO_VAGVAL, TJANSTEKONTRAKT_GET_CERTIFICATE_KEY);
    System.out.printf("Code:%s FaultString:%s\n", soapBody.getFault().getFaultCode(),
        soapBody.getFault().getFaultString());

    assertErrorLog(VP004.getCode(), "Stacktrace=se.skl.tp.vp.exceptions.VpSemanticException: VP004");
    assertRespOutLog("VP004 No receiverId (logical address) found for serviceNamespace: " +
            "urn:riv:insuranceprocess:healthreporting:GetCertificateResponder:1, receiverId: NoVagvalReceiver");
  }

  @Test
  public void shouldGetVP005WhenUnkownRivVersionInTAK() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    String result = testConsumer.sendHttpsRequestToVP(createGetCertificateRequest(RECEIVER_UNKNOWN_RIVVERSION), headers);
    SOAPBody soapBody = SoapUtils.getSoapBody(result);
    assertSoapFault(soapBody, VP005.getCode(), "rivtabp20");

    System.out.printf("Code:%s FaultString:%s\n", soapBody.getFault().getFaultCode(),
        soapBody.getFault().getFaultString());

    assertErrorLog(VP005.getCode(), "Stacktrace=se.skl.tp.vp.exceptions.VpSemanticException: VP005");
    assertRespOutLog("VP005 No receiverId (logical address) with matching Riv-version found for rivtabp20");
  }

  @Test
  public void shouldGetVP006WhenMultipleVagvalExist() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    String result = testConsumer.sendHttpsRequestToVP(createGetCertificateRequest(RECEIVER_MULTIPLE_VAGVAL), headers);

    SOAPBody soapBody = SoapUtils.getSoapBody(result);
    assertSoapFault(soapBody, VP006.getCode(), "RecevierMultipleVagval");

    System.out.printf("Code:%s FaultString:%s\n", soapBody.getFault().getFaultCode(),
        soapBody.getFault().getFaultString());

    assertErrorLog(VP006.getCode(), "Stacktrace=se.skl.tp.vp.exceptions.VpSemanticException: VP006");
    assertRespOutLog("VP006 More than one receiverId (logical address) with matching Riv-version found for serviceNamespace: " +
            "urn:riv:insuranceprocess:healthreporting:GetCertificateResponder:1");
  }

  @Test
  public void shouldGetVP007WhenRecieverNotAuhtorized() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    String result = testConsumer.sendHttpsRequestToVP(createGetCertificateRequest(RECEIVER_NOT_AUHORIZED), headers);

    SOAPBody soapBody = SoapUtils.getSoapBody(result);
    assertSoapFault(soapBody, VP007.getCode(), "Authorization missing for", RECEIVER_NOT_AUHORIZED, TJANSTEKONTRAKT_GET_CERTIFICATE_KEY);

    System.out.printf("Code:%s FaultString:%s\n", soapBody.getFault().getFaultCode(),
        soapBody.getFault().getFaultString());

    assertErrorLog(VP007.getCode(), "Stacktrace=se.skl.tp.vp.exceptions.VpSemanticException: VP007");
    assertRespOutLog("VP007 Authorization missing for serviceNamespace: urn:riv:insuranceprocess:healthreporting:GetCertificateResponder:1");
  }

  @Test
  public void shouldGetVP009WhenProducerNotAvailable() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    String result = testConsumer.sendHttpsRequestToVP(createGetCertificateRequest(RECEIVER_NO_PRODUCER_AVAILABLE), headers);

    SOAPBody soapBody = SoapUtils.getSoapBody(result);
    assertSoapFault(soapBody, VP009.getCode(), "");

    System.out.printf("Code:%s FaultString:%s\n", soapBody.getFault().getFaultCode(),
        soapBody.getFault().getFaultString());

    assertErrorLog(VP009.getCode(), "Stacktrace=java.net.ConnectException: Cannot connect to");

    String respOutLogMsg = getAndAssertRespOutLog();
    assertStringContains(respOutLogMsg, "LogMessage=resp-out");
    assertStringContains(respOutLogMsg, "ComponentId=vp-services");
    assertExtraInfoLog(respOutLogMsg, RECEIVER_NO_PRODUCER_AVAILABLE, "https://localhost:1974/Im/not/available");
  }

  @Test
  public void shouldGetVP010WhenPhysicalAdressEmptyInVagval() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    String result = testConsumer.sendHttpsRequestToVP(createGetCertificateRequest(RECEIVER_NO_PHYSICAL_ADDRESS), headers);

    SOAPBody soapBody = SoapUtils.getSoapBody(result);
    assertSoapFault(soapBody, VP010.getCode(), "RecevierNoPhysicalAddress");
    assertErrorLog(VP010.getCode(), "Stacktrace=se.skl.tp.vp.exceptions.VpSemanticException: VP010");
    assertRespOutLog("VP010 Physical Address field is empty in Service Producer for serviceNamespace: " +
            "urn:riv:insuranceprocess:healthreporting:GetCertificateResponder:1, receiverId: RecevierNoPhysicalAddress");
    System.out.printf("Code:%s FaultString:%s\n", soapBody.getFault().getFaultCode(),
        soapBody.getFault().getFaultString());

  }

  @Test
  public void   shouldGetVP011ifIpAddressIsNotWhitelisted() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    headers.put(HttpHeaders.X_VP_SENDER_ID, "Urken");
    headers.put(HttpHeaders.X_VP_INSTANCE_ID, "dev_env");
    headers.put("X-Forwarded-For", "10.20.30.40");
    String result = testConsumer.sendHttpRequestToVP(createGetCertificateRequest(RECEIVER_NO_PRODUCER_AVAILABLE), headers);

    SOAPBody soapBody = SoapUtils.getSoapBody(result);
    assertSoapFault(soapBody, VP011.getCode(), "10.20.30.40");
    assertErrorLog(VP011.getCode(), "Stacktrace=se.skl.tp.vp.exceptions.VpSemanticException: VP011");
    assertRespOutLog("VP011 Caller was not on the white list of accepted IP-addresses.  IP-address: 10.20.30.40. " +
            "HTTP header that caused checking: X-Forwarded-For");
  }

  @Test
  public void shouldGetVP013WhenIllegalSender() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    headers.put(HttpHeaders.X_VP_SENDER_ID, "SENDER3"); //Not on list sender.id.allowed.list
    headers.put(HttpHeaders.X_VP_INSTANCE_ID, "dev_env");
    headers.put(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, TEST_CONSUMER);
    String result = testConsumer.sendHttpRequestToVP(createGetCertificateRequest(RECEIVER_NO_PRODUCER_AVAILABLE), headers);

    SOAPBody soapBody = SoapUtils.getSoapBody(result);
    assertSoapFault(soapBody,VP013.getCode(), msgVP013);
    assertErrorLog(VP013.getCode(), msgVP013);
    assertRespOutLog("VP013 Sender is not approved to set header x-rivta-original-serviceconsumer-hsaid.");
  }

  @Test
  public void shouldGetVP013WhenEmptySender() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    headers.put(HttpHeaders.X_VP_SENDER_ID, ""); //Not on list sender.id.allowed.list
    headers.put(HttpHeaders.X_VP_INSTANCE_ID, "dev_env");
    headers.put(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, TEST_CONSUMER);
    String result = testConsumer.sendHttpRequestToVP(createGetCertificateRequest(RECEIVER_NO_PRODUCER_AVAILABLE), headers);

    SOAPBody soapBody = SoapUtils.getSoapBody(result);
    assertSoapFault(soapBody,VP013.getCode(),msgVP013);
    assertErrorLog(VP013.getCode(), msgVP013);
    assertRespOutLog("VP013 Sender is not approved to set header x-rivta-original-serviceconsumer-hsaid.");
  }

  private void assertSoapFault(SOAPBody soapBody, String code, String message) {
    assertNotNull("Expected a SOAP message", soapBody);
    assertNotNull("Expected a SOAPFault", soapBody.hasFault());
    assertStringContains(soapBody.getFault().getFaultString(), code);
    assertStringContains(soapBody.getFault().getFaultString(), message);
  }

  private void assertSoapFault(SOAPBody soapBody, String code, String mess1, String mess2, String mess3) {
    assertNotNull("Expected a SOAP message", soapBody);
    assertNotNull("Expected a SOAPFault", soapBody.hasFault());
    assertStringContains(soapBody.getFault().getFaultString(), code);
    assertStringContains(soapBody.getFault().getFaultString(), mess1);
    assertStringContains(soapBody.getFault().getFaultString(), mess2);
    assertStringContains(soapBody.getFault().getFaultString(), mess3);
  }

  private void assertErrorLog(String code, String message) {
    assertEquals(1, testLogAppender.getNumEvents(MessageInfoLogger.REQ_ERROR));
    String errorLogMsg = testLogAppender.getEventMessage(MessageInfoLogger.REQ_ERROR,0);
    assertStringContains(errorLogMsg, code);
    assertStringContains(errorLogMsg, message);
  }

  private void assertRespOutLog(String msg) {
    assertEquals(1, testLogAppender.getNumEvents(MessageInfoLogger.RESP_OUT));
    String respOutLogMsg = testLogAppender.getEventMessage(MessageInfoLogger.RESP_OUT,0);
    assertStringContains(respOutLogMsg, msg);
    assertStringContains(respOutLogMsg,"-Headers={CamelHttpResponseCode=500}");
  }

  private String getAndAssertRespOutLog() {
    assertEquals(1, testLogAppender.getNumEvents(MessageInfoLogger.RESP_OUT));
    return testLogAppender.getEventMessage(MessageInfoLogger.RESP_OUT,0);
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
