package se.skl.tp.vp.integrationtests.errorhandling;

import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.httpheader.OriginalConsumerIdProcessorImpl;
import se.skl.tp.vp.integrationtests.utils.StartTakService;
import se.skl.tp.vp.integrationtests.utils.TestConsumer;
import se.skl.tp.vp.logging.MessageInfoLogger;
import se.skl.tp.vp.util.TestLogAppender;
import se.skl.tp.vp.util.soaprequests.SoapUtils;

import javax.xml.soap.SOAPBody;
import java.util.HashMap;
import java.util.Map;

import static org.apache.camel.test.junit4.TestSupport.assertStringContains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum.VP013;
import static se.skl.tp.vp.integrationtests.httpheader.HeadersUtil.TEST_CONSUMER;
import static se.skl.tp.vp.util.soaprequests.TestSoapRequests.RECEIVER_NO_PRODUCER_AVAILABLE;
import static se.skl.tp.vp.util.soaprequests.TestSoapRequests.createGetCertificateRequest;

/**
 * Vp-camel can be configured, via property approve.the.use.of.header.original.consumer, to enforce a check against a
 * list of approved senderId's, that have the right to set header HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID.
 * All other senderId's should get an error if they set that header.
 */
@RunWith(CamelSpringBootRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@StartTakService
public class CheckSenderAllowedToUseHeaderIT {

  @Autowired
  TestConsumer testConsumer;

  TestLogAppender testLogAppender = TestLogAppender.getInstance();

  private String errorString = "VP013" + OriginalConsumerIdProcessorImpl.MESSAGE;

  @Before
  public void beforeTest(){
    testLogAppender.clearEvents();
  }

  @Test
  public void shouldGetVP013WhenIllegalSender() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    headers.put(HttpHeaders.X_VP_SENDER_ID, "1.2.3.4"); //Not on list ip.consumer.list
    headers.put(HttpHeaders.X_VP_INSTANCE_ID, "dev_env");
    headers.put(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, TEST_CONSUMER);
    String result = testConsumer.sendHttpRequestToVP(createGetCertificateRequest(RECEIVER_NO_PRODUCER_AVAILABLE), headers);

    SOAPBody soapBody = SoapUtils.getSoapBody(result);
    assertNotNull("Expected a SOAP message", soapBody);
    assertNotNull("Expected a SOAPFault", soapBody.hasFault());

    assertStringContains(soapBody.getFault().getFaultString(), VP013.getCode());
    assertStringContains(soapBody.getFault().getFaultString(), errorString + "1.2.3.4");

    assertEquals(1,testLogAppender.getNumEvents(MessageInfoLogger.REQ_ERROR));
    String errorLogMsg = testLogAppender.getEventMessage(MessageInfoLogger.REQ_ERROR,0);
    assertStringContains(errorLogMsg, "-errorCode=VP013");
    assertStringContains(errorLogMsg, errorString + "1.2.3.4");
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
    assertEquals(soapBody.getFault().getFaultString(), errorString);

    assertEquals(1,testLogAppender.getNumEvents(MessageInfoLogger.REQ_ERROR));
    String errorLogMsg = testLogAppender.getEventMessage(MessageInfoLogger.REQ_ERROR,0);
    assertStringContains(errorLogMsg, VP013.getCode());
    assertStringContains(errorLogMsg, errorString);
  }
}
