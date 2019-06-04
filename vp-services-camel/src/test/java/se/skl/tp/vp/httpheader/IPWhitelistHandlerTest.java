package se.skl.tp.vp.httpheader;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import se.skl.tp.vp.constants.PropertyConstants;

@RunWith(SpringRunner.class)
@TestPropertySource("classpath:application.properties")
public class IPWhitelistHandlerTest {

  @Autowired
  Environment environment;

  IPWhitelistHandler ipWhitelistHandler;

  @Before
  public void beforeTest(){
    if(ipWhitelistHandler==null){
      ipWhitelistHandler = new IPWhitelistHandlerImpl(environment.getProperty(PropertyConstants.IP_WHITELIST));
    }
  }

  @Test
  public void ipInWhitelistTest() {

    Assert.assertTrue(ipWhitelistHandler.isCallerOnWhiteList("1.2.3.4"));
    Assert.assertTrue(ipWhitelistHandler.isCallerOnWhiteList("5.6.7.8"));
  }

  @Test
  public void ipNotInWhitelistTest() {
    Assert.assertFalse(ipWhitelistHandler.isCallerOnWhiteList("127.0.0.2"));
  }

  @Test
  public void whitelistMissingTest() {
    IPWhitelistHandler emptyIpWhitelistHandler = new IPWhitelistHandlerImpl(null);
    Assert.assertFalse(emptyIpWhitelistHandler.isCallerOnWhiteList("1.2.3.4"));
  }

  @Test
  public void senderIDNullTest() {
    Assert.assertFalse(ipWhitelistHandler.isCallerOnWhiteList(null));
  }

  @Test
  public void senderIDEmptyTest() {

    Assert.assertFalse(ipWhitelistHandler.isCallerOnWhiteList(""));
  }
}