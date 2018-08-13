package se.skl.tp.vp.vagval;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum.VP002;
import static se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum.VP003;
import static se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum.VP007;
import static se.skl.tp.vp.util.takcache.TestTakDataDefines.AUTHORIZED_RECEIVER_IN_HSA_TREE;
import static se.skl.tp.vp.util.takcache.TestTakDataDefines.CHILD_OF_AUTHORIZED_RECEIVER_IN_HSA_TREE;
import static se.skl.tp.vp.util.takcache.TestTakDataDefines.NAMNRYMD_1;
import static se.skl.tp.vp.util.takcache.TestTakDataDefines.RECEIVER_1;
import static se.skl.tp.vp.util.takcache.TestTakDataDefines.RECEIVER_1_DEFAULT_RECEIVER_2;
import static se.skl.tp.vp.util.takcache.TestTakDataDefines.RECEIVER_2;
import static se.skl.tp.vp.util.takcache.TestTakDataDefines.SENDER_1;

import java.net.URL;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import se.skl.tp.hsa.cache.HsaCache;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.exceptions.VpSemanticException;
import se.skltp.takcache.TakCache;

@RunWith( CamelSpringBootRunner.class )
@SpringBootTest(classes = VagvalTestConfiguration.class)
public class BehorighetProcessorTest  extends CamelTestSupport {

    @Autowired
    BehorighetProcessor behorighetProcessor;

    @Autowired
    HsaCache hsaCache;

    @MockBean
    TakCache takCache;

    @Before
    public void beforeTest()  {
        URL url = getClass().getClassLoader().getResource("hsacache.xml");
        URL urlHsaRoot = getClass().getClassLoader().getResource("hsacachecomplementary.xml");
        hsaCache.init(url.getFile(), urlHsaRoot.getFile());
    }

    @Test
    public void testAuthorizonIsOk() throws Exception {

        Mockito.when(takCache.isAuthorized(anyString(),anyString(),anyString())).thenReturn(true);

        Exchange ex = createExchangeWithProperties(SENDER_1, NAMNRYMD_1, RECEIVER_1);
        behorighetProcessor.process(ex);
    }

    @Test
    public void testAuthorizonByClimbingHsaTree() throws Exception {

        Mockito.when(takCache.isAuthorized(anyString(), anyString(), eq(AUTHORIZED_RECEIVER_IN_HSA_TREE) )).thenReturn(true);
        Mockito.when(takCache.isAuthorized(anyString(), anyString(), AdditionalMatchers.not(eq(AUTHORIZED_RECEIVER_IN_HSA_TREE)) )).thenReturn(false);

        Exchange ex = createExchangeWithProperties(SENDER_1, NAMNRYMD_1, CHILD_OF_AUTHORIZED_RECEIVER_IN_HSA_TREE);
        behorighetProcessor.process(ex);
    }

    @Test
    public void testAuthorizonByDefaultRouting() throws Exception {

        Mockito.when(takCache.isAuthorized(anyString(), anyString(), eq(RECEIVER_2) )).thenReturn(true);
        Mockito.when(takCache.isAuthorized(anyString(), anyString(),  AdditionalMatchers.not(eq(RECEIVER_2)) )).thenReturn(false);

        Exchange ex = createExchangeWithProperties(SENDER_1, NAMNRYMD_1, RECEIVER_1_DEFAULT_RECEIVER_2);
        behorighetProcessor.process(ex);
    }

    @Test
    public void testNoSenderIdShouldThrowVP002Exception() throws Exception {

        Mockito.when(takCache.isAuthorized(anyString(),anyString(),anyString())).thenReturn(true);

        try {
            Exchange ex = createExchangeWithProperties(null, NAMNRYMD_1, RECEIVER_1);
            behorighetProcessor.process(ex);
            fail("Förväntade ett VP002 SemanticException");
        }catch(VpSemanticException vpSemanticException){
            assertEquals(VP002, vpSemanticException.getErrorCode());
            assertTrue(vpSemanticException.getMessage().contains(   "No sender ID (SERIALNUMBER) found in certificate"));
            assertTrue(vpSemanticException.getMessage().contains( NAMNRYMD_1));
            assertTrue(vpSemanticException.getMessage().contains( RECEIVER_1));
        }
    }

    @Test
    public void testNoLogicalAddressShouldThrowVP003Exception() throws Exception {

        Mockito.when(takCache.isAuthorized(anyString(),anyString(),anyString())).thenReturn(true);

        try {
            Exchange ex = createExchangeWithProperties(SENDER_1, NAMNRYMD_1, null);
            behorighetProcessor.process(ex);
            fail("Förväntade ett VP003 SemanticException");
        }catch(VpSemanticException vpSemanticException){
            assertEquals(VP003, vpSemanticException.getErrorCode());
            assertTrue(vpSemanticException.getMessage().contains( "No receiverId (logical address) found in message header"));
            assertTrue(vpSemanticException.getMessage().contains( NAMNRYMD_1));
            assertTrue(vpSemanticException.getMessage().contains( SENDER_1));
        }
    }

    @Test
    public void testNotAuthorizedShouldThrowVP007Exception() throws Exception {

        Mockito.when(takCache.isAuthorized(anyString(),anyString(),anyString())).thenReturn(false);

        try {
            Exchange ex = createExchangeWithProperties(SENDER_1, NAMNRYMD_1, RECEIVER_1);
            behorighetProcessor.process(ex);
            fail("Förväntade ett VP007 SemanticException");
        }catch(VpSemanticException vpSemanticException){
            assertEquals(VP007, vpSemanticException.getErrorCode());
            assertTrue(vpSemanticException.getMessage().contains( "Authorization missing for"));
            assertTrue(vpSemanticException.getMessage().contains( NAMNRYMD_1));
            assertTrue(vpSemanticException.getMessage().contains( SENDER_1));
            assertTrue(vpSemanticException.getMessage().contains( RECEIVER_1));
        }
    }

    private Exchange createExchangeWithProperties(String senderId, String nameSpace, String receiver) {
        CamelContext ctx = new DefaultCamelContext();
        Exchange ex = new DefaultExchange(ctx);
        ex.setProperty(VPExchangeProperties.SENDER_ID, senderId );
        ex.setProperty(VPExchangeProperties.SERVICECONTRACT_NAMESPACE, nameSpace);
        ex.setProperty(VPExchangeProperties.RECEIVER_ID, receiver );
        return ex;
    }

}