package se.skl.tp.vp.inneTest.httpheader;

import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.skl.tp.vp.httpheader.IPWhitelistHandler;
import se.skl.tp.vp.inneTest.TestBeanConfiguration;

@RunWith( CamelSpringBootRunner.class )
@ContextConfiguration(classes = TestBeanConfiguration.class)
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