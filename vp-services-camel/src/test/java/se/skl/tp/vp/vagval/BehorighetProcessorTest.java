package se.skl.tp.vp.vagval;

import org.apache.camel.*;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.skl.tp.hsa.cache.HsaCache;
import se.skl.tp.vp.Application;
import se.skl.tp.vp.constants.VPExchangeProperties;
import se.skl.tp.vp.exceptions.VpSemanticException;
import se.skltp.takcache.TakCache;
import se.skltp.takcache.services.TakService;

import java.net.URL;

import static org.mockito.ArgumentMatchers.anyString;
import static se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum.VP007;
import static se.skl.tp.vp.util.takcache.TestTakDataDefines.*;

@RunWith( SpringJUnit4ClassRunner.class )
@SpringBootTest(classes = Application.class)
@DirtiesContext
public class BehorighetProcessorTest  extends CamelTestSupport {

    @Autowired
    BehorighetProcessor behorighetProcessor;

    @Autowired
    HsaCache hsaCache;

    @MockBean
    TakCache takCache;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void beforeTest()  {
//        MockitoAnnotations.initMocks(this);
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
    public void testNotAuthorizedThrowsAException() throws Exception {

        Mockito.when(takCache.isAuthorized(anyString(),anyString(),anyString())).thenReturn(false);

        try {
            Exchange ex = createExchangeWithProperties(SENDER_1, NAMNRYMD_1, RECEIVER_1);
            behorighetProcessor.process(ex);
            fail("Förväntade ett VP007 SemanticException");
        }catch(VpSemanticException vpSemanticException){
            assertEquals(VP007, vpSemanticException.getErrorCode());
            // TODO assert that message contain good information
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