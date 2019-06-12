package se.skl.tp.vp.httpheader;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import se.skl.tp.vp.util.TestLogAppender;

import static se.skl.tp.vp.constants.PropertyConstants.IP_WHITELIST;

@RunWith(SpringRunner.class)
@TestPropertySource("classpath:application.properties")
public class IPWhitelistHandlerTest {

  @Autowired
  Environment environment;

  @Value("${" + IP_WHITELIST + "}")
  private String whitelist;

  private String[] whiteListArray;

  IPWhitelistHandler ipWhitelistHandler;
  TestLogAppender testLogAppender = TestLogAppender.getInstance();

  private static final String LOG_CLASS = "se.skl.tp.vp.httpheader.IPWhitelistHandlerImpl";

  @Before
  public void beforeTest(){
    if(ipWhitelistHandler==null){
      ipWhitelistHandler = new IPWhitelistHandlerImpl(environment.getProperty(IP_WHITELIST));
    }
    whiteListArray = whitelist.split(",");
    testLogAppender.clearEvents();
  }

  @Test
  public void ipInWhitelistTest() {
    Assert.assertTrue(ipWhitelistHandler.isCallerOnWhiteList("1.2.3.4"));
    Assert.assertTrue(ipWhitelistHandler.isCallerOnWhiteList("5.6.7.8"));
  }

  @Test
  public void ipNotInWhitelistTest() {
    Assert.assertFalse(ipWhitelistHandler.isCallerOnWhiteList("127.0.0.2"));
    testLogMessage(1, "Caller was not on the white list of accepted IP-addresses. IP-address: 127.0.0.2, accepted IP-addresses in IP_WHITE_LIST:[" +
            whitelist + "]");
  }

  @Test
  public void whitelistMissingTest() {
    IPWhitelistHandler emptyIpWhitelistHandler = new IPWhitelistHandlerImpl(null);
    Assert.assertFalse(emptyIpWhitelistHandler.isCallerOnWhiteList("1.2.3.4"));
    testLogMessage(1, "A check against the ip address whitelist was requested, but the whitelist is configured empty");
  }

  @Test
  public void senderIDNullTest() {
    Assert.assertFalse(ipWhitelistHandler.isCallerOnWhiteList(null));
    testLogMessage(1, "A potential empty ip address from the caller, ip adress is: null.");
  }

  @Test
  public void senderIDEmptyTest() {
    Assert.assertFalse(ipWhitelistHandler.isCallerOnWhiteList(""));
    testLogMessage(1, "A potential empty ip address from the caller, ip adress is: .");
  }

  private void testLogMessage(int num, String message) {
    int i = testLogAppender.getNumEvents(LOG_CLASS);
    Assert.assertEquals(num, testLogAppender.getNumEvents(LOG_CLASS));
    Assert.assertTrue(testLogAppender.getEventMessage(LOG_CLASS, 0).contains(message));
  }
}