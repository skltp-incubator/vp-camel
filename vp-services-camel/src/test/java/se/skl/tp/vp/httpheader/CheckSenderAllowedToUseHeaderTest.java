package se.skl.tp.vp.httpheader;


import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import se.skl.tp.vp.TestBeanConfiguration;
import se.skl.tp.vp.util.TestLogAppender;

import static se.skl.tp.vp.constants.PropertyConstants.SENDER_ID_ALLOWED_LIST;


@RunWith( CamelSpringBootRunner.class )
@ContextConfiguration(classes = TestBeanConfiguration.class)
@TestPropertySource("classpath:application.properties")
public class CheckSenderAllowedToUseHeaderTest {

  @Value("${" + SENDER_ID_ALLOWED_LIST + "}")
  private String allowedUsers;

  TestLogAppender testLogAppender = TestLogAppender.getInstance();

  private static String LOG_CLASS = "se.skl.tp.vp.httpheader.CheckSenderAllowedToUseHeaderImpl";

  @Autowired
  CheckSenderAllowedToUseHeader checkSenderIdAgainstList;

  @Autowired
  CheckSenderAllowedToUseHeader emptyCheckSenderIdAgainstList;

  @Before
  public void beforeTest(){
    testLogAppender.clearEvents();
  }

  @Test
  public void senderIdInListTest() {
    Assert.assertTrue(checkSenderIdAgainstList.isSenderIdAllowedToUseXrivtaOriginalConsumerIdHeader("127.0.0.1"));
    Assert.assertTrue(checkSenderIdAgainstList.isSenderIdAllowedToUseXrivtaOriginalConsumerIdHeader("127.0.0.2"));
    testLogMessage(2, "Caller 127.0.0.1 matches ip address/subdomain in " + SENDER_ID_ALLOWED_LIST);
  }

  @Test
  public void ipNotInWhitelistTest() {
    Assert.assertFalse(checkSenderIdAgainstList.isSenderIdAllowedToUseXrivtaOriginalConsumerIdHeader("1.2.3.4"));
    testLogMessage(1, "SenderId was not on the list " + SENDER_ID_ALLOWED_LIST + ". SenderId: 1.2.3.4, accepted senderId's in "
            + SENDER_ID_ALLOWED_LIST + ": <" + allowedUsers + ">");
  }

  @Test
  public void whitelistMissingTest() {
    Assert.assertFalse(emptyCheckSenderIdAgainstList.isSenderIdAllowedToUseXrivtaOriginalConsumerIdHeader("127.0.0.2"));
    testLogMessage(1, "sender.id.allowed.list was NULL, so nothing to compare sender 127.0.0.2 against. " +
            "HTTP header that caused checking: x-rivta-original-serviceconsumer-hsaid.");
  }

  @Test
  public void senderIDNullTest() {
    Assert.assertFalse(checkSenderIdAgainstList.isSenderIdAllowedToUseXrivtaOriginalConsumerIdHeader(null));
    testLogMessage(1, "The sender was null/empty. Could not check address in list " + SENDER_ID_ALLOWED_LIST +
            ". HTTP header that caused checking: x-rivta-original-serviceconsumer-hsaid.");
  }

  @Test
  public void senderIDEmptyTest() {
    Assert.assertFalse(checkSenderIdAgainstList.isSenderIdAllowedToUseXrivtaOriginalConsumerIdHeader(""));
    testLogMessage(1, "The sender was null/empty. Could not check address in list " + SENDER_ID_ALLOWED_LIST +
            ". HTTP header that caused checking: x-rivta-original-serviceconsumer-hsaid.");
  }

  private void testLogMessage(int num, String message) {
    Assert.assertEquals(num, testLogAppender.getNumEvents(LOG_CLASS));
    Assert.assertEquals(message, testLogAppender.getEventMessage(LOG_CLASS, 0));
  }

}
