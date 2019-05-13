package se.skl.tp.vp.httpheader;

import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import se.skl.tp.vp.TestBeanConfiguration;

@RunWith( CamelSpringBootRunner.class )
@ContextConfiguration(classes = TestBeanConfiguration.class)
@TestPropertySource("classpath:application.properties")
public class IPWhitelistHandlerTest {

    @Autowired
    IPWhitelistHandler ipWhitelistHandler;

    @Autowired
    IPWhitelistHandler emptyIpWhitelistHandler;

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