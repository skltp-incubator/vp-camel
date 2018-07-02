package se.skl.tp.vp.vagval;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Before;
import org.junit.Test;
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
import se.skltp.takcache.RoutingInfo;
import se.skltp.takcache.TakCache;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static se.skl.tp.vp.exceptions.VpSemanticErrorCodeEnum.*;
import static se.skl.tp.vp.util.takcache.TestTakDataDefines.*;

@RunWith( SpringJUnit4ClassRunner.class )
@SpringBootTest(classes = Application.class)
@DirtiesContext
public class VagvalProcessorTest {
    @Autowired
    VagvalProcessor vagvalProcessor;

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
    public void testVagvalFound() throws Exception {

        List<RoutingInfo> list = new ArrayList<>();
        list.add(new RoutingInfo(ADDRESS_1,RIV20));
        Mockito.when(takCache.getRoutingInfo(NAMNRYMD_1, RECEIVER_1 )).thenReturn(list);

        Exchange ex = createExchangeWithProperties( NAMNRYMD_1, RECEIVER_1);
        vagvalProcessor.process(ex);

        assertEquals(ADDRESS_1, ex.getProperty(VPExchangeProperties.VAGVAL));
        assertEquals(RIV20, ex.getProperty(VPExchangeProperties.RIV_VERSION_OUT));

    }

    @Test
    public void testNoLogicaAddressInRequestShouldThrowVP003Exception() throws Exception {

        List<RoutingInfo> list = new ArrayList<>();
        list.add(new RoutingInfo(ADDRESS_1,RIV20));
        Mockito.when(takCache.getRoutingInfo(NAMNRYMD_1, RECEIVER_1 )).thenReturn(list);

        try {
            Exchange ex = createExchangeWithProperties( NAMNRYMD_1, null);
            vagvalProcessor.process(ex);
            fail("Förväntade ett VP003 SemanticException");
        }catch(VpSemanticException vpSemanticException){
            assertEquals(VP003, vpSemanticException.getErrorCode());
            // TODO assert that message contain good information
        }
    }

    @Test
    public void testNoVagvalFoundShouldThrowVP004Exception() throws Exception {

        Mockito.when(takCache.getRoutingInfo(NAMNRYMD_1, RECEIVER_1 )).thenReturn(Collections.emptyList());

        try {
            Exchange ex = createExchangeWithProperties( NAMNRYMD_1, RECEIVER_1);
            vagvalProcessor.process(ex);
            fail("Förväntade ett VP004 SemanticException");
        }catch(VpSemanticException vpSemanticException){
            assertEquals(VP004, vpSemanticException.getErrorCode());
            // TODO assert that message contain good information
        }
    }

    @Test
    public void testTooManyVagvalFoundShouldThrowVP006Exception() throws Exception {

        List<RoutingInfo> list = new ArrayList<>();
        list.add(new RoutingInfo(ADDRESS_1,RIV20));
        list.add(new RoutingInfo(ADDRESS_1,RIV21));
        Mockito.when(takCache.getRoutingInfo(NAMNRYMD_1, RECEIVER_1 )).thenReturn(list);

        try {
            Exchange ex = createExchangeWithProperties( NAMNRYMD_1, RECEIVER_1);
            vagvalProcessor.process(ex);
            fail("Förväntade ett VP006 SemanticException");
        }catch(VpSemanticException vpSemanticException){
            assertEquals(VP006, vpSemanticException.getErrorCode());
            // TODO assert that message contain good information
        }
    }

    @Test
    public void testIfFoundVagvalAddressIsEmptyItShouldThrowVP010Exception() throws Exception {

        List<RoutingInfo> list = new ArrayList<>();
        list.add(new RoutingInfo("",RIV20));
        Mockito.when(takCache.getRoutingInfo(NAMNRYMD_1, RECEIVER_1 )).thenReturn(list);

        try {
            Exchange ex = createExchangeWithProperties( NAMNRYMD_1, RECEIVER_1);
            vagvalProcessor.process(ex);
            fail("Förväntade ett VP010 SemanticException");
        }catch(VpSemanticException vpSemanticException){
            assertEquals(VP010, vpSemanticException.getErrorCode());
            // TODO assert that message contain good information
        }
    }

    private Exchange createExchangeWithProperties(String nameSpace, String receiver) {
        CamelContext ctx = new DefaultCamelContext();
        Exchange ex = new DefaultExchange(ctx);
        ex.setProperty(VPExchangeProperties.SERVICECONTRACT_NAMESPACE, nameSpace);
        ex.setProperty(VPExchangeProperties.RECEIVER_ID, receiver );
        return ex;
    }
}
