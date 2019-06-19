package se.skl.tp.vp.integrationtests;

import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.integrationtests.utils.MockProducer;
import se.skl.tp.vp.integrationtests.utils.StartTakService;
import se.skl.tp.vp.integrationtests.utils.TakMockWebService;
import se.skl.tp.vp.integrationtests.utils.TestConsumer;
import se.skl.tp.vp.logging.MessageInfoLogger;
import se.skl.tp.vp.util.TestLogAppender;

import java.util.HashMap;
import java.util.Map;

import static org.apache.camel.test.junit4.TestSupport.assertStringContains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static se.skl.tp.vp.util.soaprequests.TestSoapRequests.createGetActivitiesRiv21Request;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest
@StartTakService
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class FullServiceDefaultVagvalIT {

  public static final String ANSWER_FROM_DEFAULT_PRODUCER = "<Answer from default producer>";

  @Autowired
  TestConsumer testConsumer;

  @Autowired
  MockProducer defaultRoutedProducer;

  @Autowired
  MockProducer explicedRoutedProducer;

  @Autowired
  MockProducer hsaTreeRoutedProducer;

  @Autowired
  TakMockWebService takMockWebService;

  @Value("${vp.instance.id}")
  String vpInstanceId;

  TestLogAppender testLogAppender = TestLogAppender.getInstance();

  @Before
  public void before() throws Exception {
    defaultRoutedProducer.start("http://localhost:1900/default/GetActivitiesResponder");
    explicedRoutedProducer.start("http://localhost:1900/explicit/GetActivitiesResponder");
    testLogAppender.clearEvents();
  }

  @Test
  public void testDefaultVagvalWithDelimiterInReceiver() throws Exception {
    defaultRoutedProducer.setResponseBody(ANSWER_FROM_DEFAULT_PRODUCER);
    Map<String, Object> headers = new HashMap<>();
    headers.put(HttpHeaders.X_VP_INSTANCE_ID, vpInstanceId);
    headers.put(HttpHeaders.X_VP_SENDER_ID,"SenderWithDefaultBehorighet");
    String response = testConsumer.sendHttpRequestToVP(createGetActivitiesRiv21Request("FirstReceiverRiv21#SecondReceiverRiv21"), headers);
    assertEquals(ANSWER_FROM_DEFAULT_PRODUCER, response);
    assertLogMessage("FirstReceiverRiv21#SecondReceiverRiv21", "SecondReceiverRiv21");
  }

  @Test
  public void testDefaultVagvalWithDelimiterInReceiverReversed() throws Exception {
    defaultRoutedProducer.setResponseBody(ANSWER_FROM_DEFAULT_PRODUCER);
    Map<String, Object> headers = new HashMap<>();
    headers.put(HttpHeaders.X_VP_INSTANCE_ID, vpInstanceId);
    headers.put(HttpHeaders.X_VP_SENDER_ID,"SenderWithDefaultBehorighet");
    String response = testConsumer.sendHttpRequestToVP(createGetActivitiesRiv21Request("SecondReceiverRiv21#FirstReceiverRiv21"), headers);
    assertEquals(ANSWER_FROM_DEFAULT_PRODUCER, response);
    assertLogMessage("SecondReceiverRiv21#FirstReceiverRiv21", "FirstReceiverRiv21");
  }

  @Test
  public void testDefaultVagvalWithDelimiterInReceiverJustOneValid() throws Exception {
    defaultRoutedProducer.setResponseBody(ANSWER_FROM_DEFAULT_PRODUCER);
    Map<String, Object> headers = new HashMap<>();
    headers.put(HttpHeaders.X_VP_INSTANCE_ID, vpInstanceId);
    headers.put(HttpHeaders.X_VP_SENDER_ID,"SenderWithDefaultBehorighet");
    String response = testConsumer.sendHttpRequestToVP(createGetActivitiesRiv21Request("SecondReceiverRiv21#NotValidReceiver"), headers);
    assertEquals(ANSWER_FROM_DEFAULT_PRODUCER, response);
    assertLogMessage("SecondReceiverRiv21#NotValidReceiver", "NotValidReceiver,SecondReceiverRiv21");
  }

  @Test
  public void testDefaultVagvalWithDelimiterAndTooManyReceivers() throws Exception {
    defaultRoutedProducer.setResponseBody(ANSWER_FROM_DEFAULT_PRODUCER);
    Map<String, Object> headers = new HashMap<>();
    headers.put(HttpHeaders.X_VP_INSTANCE_ID, vpInstanceId);
    headers.put(HttpHeaders.X_VP_SENDER_ID,"SenderWithDefaultBehorighet");
    String response = testConsumer.sendHttpRequestToVP(createGetActivitiesRiv21Request("TooManyReceivers#SecondReceiverRiv21#NotValidReceiver"), headers);
    assertTrue(response.contains("VP007 Authorization missing for serviceNamespace: urn:riv:clinicalprocess:activity:actions:GetActivitiesResponder:1, " +
                    "receiverId: TooManyReceivers#SecondReceiverRiv21#NotValidReceiver, senderId: SenderWithDefaultBehorighet"));
  }

  private void assertLogMessage(String receiver, String trace) {
    String respOutLogMsg = testLogAppender.getEventMessage(MessageInfoLogger.RESP_OUT, 0);
    assertStringContains(respOutLogMsg, "LogMessage=resp-out");
    assertStringContains(respOutLogMsg, "-senderid=SenderWithDefaultBehorighet");
    assertStringContains(respOutLogMsg, "-endpoint_url=http://localhost:1900/default/GetActivitiesResponder");
    assertStringContains(respOutLogMsg, "-receiverid=" + receiver);
    assertStringContains(respOutLogMsg, "-routerVagvalTrace=(leaf)" + trace);
    assertStringContains(respOutLogMsg, "-routerBehorighetTrace=(leaf)" + trace);
  }
}




























