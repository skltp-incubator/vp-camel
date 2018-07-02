package se.skl.tp.vp.vagval.handlers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.skl.tp.hsa.cache.HsaCache;
import se.skl.tp.vp.Application;
import se.skltp.takcache.TakCache;

import java.net.URL;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static se.skl.tp.vp.util.takcache.TestTakDataDefines.*;

@RunWith( SpringJUnit4ClassRunner.class )
@SpringBootTest(classes = Application.class)
public class BehorighetHandlerTest {

    @Value("${vagvalrouter.default.routing.address.delimiter}")
    private String defaultRoutingDelimiter;

    @Autowired
    HsaCache hsaCache;

    @Mock
    TakCache takCache;

    BehorighetHandler behorighetHandler;

    @Before
    public void beforeTest()  {
        MockitoAnnotations.initMocks(this);
        URL url = getClass().getClassLoader().getResource("hsacache.xml");
        URL urlHsaRoot = getClass().getClassLoader().getResource("hsacachecomplementary.xml");
        hsaCache.init(url.getFile(), urlHsaRoot.getFile());
    }

    @Test
    public void testSimpleAuthorizon() throws Exception {

        Mockito.when(takCache.isAuthorized(SENDER_1, NAMNRYMD_1, RECEIVER_1 )).thenReturn(true);
        Mockito.when(takCache.isAuthorized(SENDER_2, NAMNRYMD_1, RECEIVER_1 )).thenReturn(false);

        behorighetHandler = new BehorighetHandler(hsaCache, takCache, defaultRoutingDelimiter );

        assertTrue( behorighetHandler.isAuthorized(SENDER_1, NAMNRYMD_1, RECEIVER_1));
        assertFalse( behorighetHandler.isAuthorized(SENDER_2, NAMNRYMD_1, RECEIVER_1));
    }

    @Test
    public void testIsAuthorizedByHsaTreeClimbing() throws Exception {
        Mockito.when(takCache.isAuthorized(anyString(), anyString(), eq(AUTHORIZED_RECEIVER_IN_HSA_TREE) )).thenReturn(true);
        Mockito.when(takCache.isAuthorized(anyString(), anyString(), AdditionalMatchers.not(eq(AUTHORIZED_RECEIVER_IN_HSA_TREE)) )).thenReturn(false);

        behorighetHandler = new BehorighetHandler(hsaCache, takCache, defaultRoutingDelimiter );

        assertTrue( behorighetHandler.isAuthorized(SENDER_1, NAMNRYMD_1, AUTHORIZED_RECEIVER_IN_HSA_TREE));
        assertTrue( behorighetHandler.isAuthorized(SENDER_1, NAMNRYMD_1, CHILD_OF_AUTHORIZED_RECEIVER_IN_HSA_TREE));
        assertFalse( behorighetHandler.isAuthorized(SENDER_1, NAMNRYMD_1, PARENT_OF_AUTHORIZED_RECEIVER_IN_HSA_TREE));
    }

    @Test
    public void testIsAuthorizedByOldStyleDefaultRouting() throws Exception {
        Mockito.when(takCache.isAuthorized(anyString(), anyString(), eq(RECEIVER_2) )).thenReturn(true);
        Mockito.when(takCache.isAuthorized(anyString(), anyString(),  AdditionalMatchers.not(eq(RECEIVER_2)) )).thenReturn(false);

        behorighetHandler = new BehorighetHandler(hsaCache, takCache, defaultRoutingDelimiter );

        assertTrue( behorighetHandler.isAuthorized(SENDER_1, NAMNRYMD_1, RECEIVER_1_DEFAULT_RECEIVER_2));
        assertTrue( behorighetHandler.isAuthorized(SENDER_1, NAMNRYMD_1, RECEIVER_2_DEFAULT_RECEIVER_3));
        assertFalse( behorighetHandler.isAuthorized(SENDER_1, NAMNRYMD_1, RECEIVER_3_DEFAULT_RECEIVER_4));
    }

    @Test
    public void testIsNotAuthorizedByOldStyleDefaultRoutingWhenItsDisabled() throws Exception {
        Mockito.when(takCache.isAuthorized(anyString(), anyString(), eq(RECEIVER_2) )).thenReturn(true);
        Mockito.when(takCache.isAuthorized(anyString(), anyString(),  AdditionalMatchers.not(eq(RECEIVER_2)) )).thenReturn(false);

        behorighetHandler = new BehorighetHandler(hsaCache, takCache, "" );

        assertFalse( behorighetHandler.isAuthorized(SENDER_1, NAMNRYMD_1, RECEIVER_1_DEFAULT_RECEIVER_2));
    }


}