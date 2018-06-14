package se.skl.tp.vp.httpheader;

import org.junit.Assert;
import org.junit.Test;

public class IPWhitelistHandlerTest {

    @Test
    public void ipInWhitelistTest() {
        IPWhitelistHandler ipWhitelistHandler = new IPWhitelistHandlerImpl("1.2.3.4,5.6.7.8");
        Assert.assertTrue(ipWhitelistHandler.isCallerOnWhiteList("5.6.7.8"));
    }

    @Test
    public void ipNotInWhitelistTest() {
        IPWhitelistHandler ipWhitelistHandler = new IPWhitelistHandlerImpl("1.2.3.4,5.6.7.8");
        Assert.assertFalse(ipWhitelistHandler.isCallerOnWhiteList("127.0.0.1"));
    }

    @Test
    public void whitelistMissingTest() {
        IPWhitelistHandler ipWhitelistHandler = new IPWhitelistHandlerImpl(null);
        Assert.assertFalse(ipWhitelistHandler.isCallerOnWhiteList("127.0.0.1"));
    }

    @Test
    public void senderIDNullTest() {
        IPWhitelistHandler ipWhitelistHandler = new IPWhitelistHandlerImpl("1.2.3.4,5.6.7.8");
        Assert.assertFalse(ipWhitelistHandler.isCallerOnWhiteList(null));
    }

    @Test
    public void senderIDEmptyTest() {
        IPWhitelistHandler ipWhitelistHandler = new IPWhitelistHandlerImpl("1.2.3.4,5.6.7.8");
        Assert.assertFalse(ipWhitelistHandler.isCallerOnWhiteList(""));
    }
}