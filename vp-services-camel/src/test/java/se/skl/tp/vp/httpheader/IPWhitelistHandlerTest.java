package se.skl.tp.vp.httpheader;

import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration(classes = se.skl.tp.vp.BeansConfiguration.class)
@TestPropertySource("classpath:application.properties")
public class IPWhitelistHandlerTest {

    @Autowired
    IPWhitelistHandler ipWhitelistHandler;

    @Test
    public void ipInWhitelistTest() {

        Assert.assertTrue(ipWhitelistHandler.isCallerOnWhiteList("5.6.7.8"));
    }

    @Test
    public void ipNotInWhitelistTest() {

        Assert.assertFalse(ipWhitelistHandler.isCallerOnWhiteList("127.0.0.1"));
    }

    @Test
    public void whitelistMissingTest() {

        Assert.assertFalse(ipWhitelistHandler.isCallerOnWhiteList("127.0.0.1"));
    }

    /*@Test
    public void senderIDNullTest() {
        IPWhitelistHandler ipWhitelistHandler = new IPWhitelistHandlerImpl(env);
        Assert.assertFalse(ipWhitelistHandler.isCallerOnWhiteList(null));
    }*/

    @Test
    public void senderIDEmptyTest() {

        Assert.assertFalse(ipWhitelistHandler.isCallerOnWhiteList(""));
    }
}