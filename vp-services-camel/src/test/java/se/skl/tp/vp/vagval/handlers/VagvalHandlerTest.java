package se.skl.tp.vp.vagval.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static se.skl.tp.vp.util.takcache.TestTakDataDefines.ADDRESS_1;
import static se.skl.tp.vp.util.takcache.TestTakDataDefines.ADDRESS_2;
import static se.skl.tp.vp.util.takcache.TestTakDataDefines.AUTHORIZED_RECEIVER_IN_HSA_TREE;
import static se.skl.tp.vp.util.takcache.TestTakDataDefines.CHILD_OF_AUTHORIZED_RECEIVER_IN_HSA_TREE;
import static se.skl.tp.vp.util.takcache.TestTakDataDefines.NAMNRYMD_1;
import static se.skl.tp.vp.util.takcache.TestTakDataDefines.PARENT_OF_AUTHORIZED_RECEIVER_IN_HSA_TREE;
import static se.skl.tp.vp.util.takcache.TestTakDataDefines.RECEIVER_1;
import static se.skl.tp.vp.util.takcache.TestTakDataDefines.RECEIVER_1_DEFAULT_RECEIVER_2;
import static se.skl.tp.vp.util.takcache.TestTakDataDefines.RECEIVER_2;
import static se.skl.tp.vp.util.takcache.TestTakDataDefines.RECEIVER_2_DEFAULT_RECEIVER_3;
import static se.skl.tp.vp.util.takcache.TestTakDataDefines.RECEIVER_3_DEFAULT_RECEIVER_4;
import static se.skl.tp.vp.util.takcache.TestTakDataDefines.RIV20;
import static se.skl.tp.vp.util.takcache.TestTakDataDefines.RIV21;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import se.skl.tp.hsa.cache.HsaCache;
import se.skl.tp.vp.logging.ThreadContextLogTrace;
import se.skl.tp.vp.vagval.VagvalTestConfiguration;
import se.skltp.takcache.RoutingInfo;
import se.skltp.takcache.TakCache;

@RunWith( SpringRunner.class )
@SpringBootTest
public class VagvalHandlerTest {
    @Value("${vagvalrouter.default.routing.address.delimiter}")
    private String defaultRoutingDelimiter;

    @Autowired
    HsaCache hsaCache;

    @Mock
    TakCache takCache;

    VagvalHandler vagvalHandler;

    @Before
    public void beforeTest()  {
        MockitoAnnotations.initMocks(this);
        URL url = getClass().getClassLoader().getResource("hsacache.xml");
        URL urlHsaRoot = getClass().getClassLoader().getResource("hsacachecomplementary.xml");
        hsaCache.init(url.getFile(), urlHsaRoot.getFile());
    }

    @Test
    public void testOneRoutingInfoFound() throws Exception {
        List<RoutingInfo> list = new ArrayList<>();
        list.add(new RoutingInfo(ADDRESS_1,RIV20));
        Mockito.when(takCache.getRoutingInfo(NAMNRYMD_1, RECEIVER_1 )).thenReturn(list);

        vagvalHandler = new VagvalHandler(hsaCache, takCache, defaultRoutingDelimiter );

        List<RoutingInfo> routingInfoList = vagvalHandler.getRoutingInfo(NAMNRYMD_1, RECEIVER_1);
        assertEquals(1, routingInfoList.size());
        assertEquals(ADDRESS_1, routingInfoList.get(0).getAddress());
        assertEquals(RIV20, routingInfoList.get(0).getRivProfile());
    }

    @Test
    public void testTraceLoggingOneRoutingInfoFound() throws Exception {
        List<RoutingInfo> list = new ArrayList<>();
        list.add(new RoutingInfo(ADDRESS_1,RIV20));
        Mockito.when(takCache.getRoutingInfo(NAMNRYMD_1, RECEIVER_1 )).thenReturn(list);

        vagvalHandler = new VagvalHandler(hsaCache, takCache, defaultRoutingDelimiter );

        List<RoutingInfo> routingInfoList = vagvalHandler.getRoutingInfo(NAMNRYMD_1, RECEIVER_1);
        assertEquals(1, routingInfoList.size());
        assertEquals(RECEIVER_1,  ThreadContextLogTrace.get(ThreadContextLogTrace.ROUTER_RESOLVE_VAGVAL_TRACE));
    }

    @Test
    public void testTwoRoutingInfoFound() throws Exception {
        List<RoutingInfo> list = new ArrayList<>();
        list.add(new RoutingInfo(ADDRESS_1,RIV20));
        list.add(new RoutingInfo(ADDRESS_2,RIV21));

        Mockito.when(takCache.getRoutingInfo(NAMNRYMD_1, RECEIVER_1 )).thenReturn(list);

        vagvalHandler = new VagvalHandler(hsaCache, takCache, defaultRoutingDelimiter );

        List<RoutingInfo> routingInfoList = vagvalHandler.getRoutingInfo(NAMNRYMD_1, RECEIVER_1);
        assertEquals(2, routingInfoList.size());
        assertEquals(ADDRESS_1, routingInfoList.get(0).getAddress());
        assertEquals(RIV20, routingInfoList.get(0).getRivProfile());
        assertEquals(ADDRESS_2, routingInfoList.get(1).getAddress());
        assertEquals(RIV21, routingInfoList.get(1).getRivProfile());
    }

    @Test
    public void testNoRoutingInfoFound() throws Exception {
        Mockito.when(takCache.getRoutingInfo(NAMNRYMD_1, RECEIVER_1 )).thenReturn(Collections.<RoutingInfo>emptyList());

        vagvalHandler = new VagvalHandler(hsaCache, takCache, defaultRoutingDelimiter );

        List<RoutingInfo> routingInfoList = vagvalHandler.getRoutingInfo(NAMNRYMD_1, RECEIVER_1);
        assertTrue(routingInfoList.isEmpty());
    }

    @Test
    public void testTraceLoggingNoRoutingInfoFound() throws Exception {
        Mockito.when(takCache.getRoutingInfo(NAMNRYMD_1, RECEIVER_1 )).thenReturn(Collections.<RoutingInfo>emptyList());

        vagvalHandler = new VagvalHandler(hsaCache, takCache, defaultRoutingDelimiter );

        List<RoutingInfo> routingInfoList = vagvalHandler.getRoutingInfo(NAMNRYMD_1, RECEIVER_1);
        assertTrue(routingInfoList.isEmpty());
        assertEquals("receiver-1,(parent)SE",  ThreadContextLogTrace.get(ThreadContextLogTrace.ROUTER_RESOLVE_VAGVAL_TRACE));
    }

    @Test
    public void testRoutingShouldBeFoundOnChildInHsaTree() throws Exception {
        List<RoutingInfo> list = new ArrayList<>();
        list.add(new RoutingInfo(ADDRESS_2, RIV21));

        Mockito.when(takCache.getRoutingInfo(anyString(), eq(AUTHORIZED_RECEIVER_IN_HSA_TREE) )).thenReturn(list);
        Mockito.when(takCache.getRoutingInfo(anyString(),AdditionalMatchers.not(eq(AUTHORIZED_RECEIVER_IN_HSA_TREE)) )).thenReturn(Collections.<RoutingInfo>emptyList());

        vagvalHandler = new VagvalHandler(hsaCache, takCache, defaultRoutingDelimiter );

        List<RoutingInfo> routingInfoList = vagvalHandler.getRoutingInfo(NAMNRYMD_1, CHILD_OF_AUTHORIZED_RECEIVER_IN_HSA_TREE);
        assertEquals(1, routingInfoList.size());
        assertEquals(ADDRESS_2, routingInfoList.get(0).getAddress());
        assertEquals(RIV21, routingInfoList.get(0).getRivProfile());
    }


    @Test
    public void testTraceLoggingWhenRoutingFoundOnChildInHsaTree() throws Exception {
        List<RoutingInfo> list = new ArrayList<>();
        list.add(new RoutingInfo(ADDRESS_2, RIV21));

        Mockito.when(takCache.getRoutingInfo(anyString(), eq(AUTHORIZED_RECEIVER_IN_HSA_TREE) )).thenReturn(list);
        Mockito.when(takCache.getRoutingInfo(anyString(),AdditionalMatchers.not(eq(AUTHORIZED_RECEIVER_IN_HSA_TREE)) )).thenReturn(Collections.<RoutingInfo>emptyList());

        vagvalHandler = new VagvalHandler(hsaCache, takCache, defaultRoutingDelimiter );

        List<RoutingInfo> routingInfoList = vagvalHandler.getRoutingInfo(NAMNRYMD_1, CHILD_OF_AUTHORIZED_RECEIVER_IN_HSA_TREE);
        assertEquals(1, routingInfoList.size());
        String logResult =  ThreadContextLogTrace.get(ThreadContextLogTrace.ROUTER_RESOLVE_VAGVAL_TRACE);
        assertEquals("SE0000000001-1234,(parent)SE0000000002-1234,SE0000000003-1234", logResult);
    }

    @Test
    public void testRoutingShouldNotBeFoundOnParentInHsaTree() throws Exception {
        List<RoutingInfo> list = new ArrayList<>();
        list.add(new RoutingInfo(ADDRESS_2, RIV21));

        Mockito.when(takCache.getRoutingInfo(anyString(), eq(AUTHORIZED_RECEIVER_IN_HSA_TREE) )).thenReturn(list);
        Mockito.when(takCache.getRoutingInfo(anyString(),AdditionalMatchers.not(eq(AUTHORIZED_RECEIVER_IN_HSA_TREE)) )).thenReturn(Collections.<RoutingInfo>emptyList());

        vagvalHandler = new VagvalHandler(hsaCache, takCache, defaultRoutingDelimiter );

        List<RoutingInfo> routingInfoList = vagvalHandler.getRoutingInfo(NAMNRYMD_1, PARENT_OF_AUTHORIZED_RECEIVER_IN_HSA_TREE);
        assertTrue(routingInfoList.isEmpty());
    }

    @Test
    public void testRoutingByOldStyleDefaultRouting() throws Exception {
        List<RoutingInfo> list = new ArrayList<>();
        list.add(new RoutingInfo(ADDRESS_2, RIV21));

        Mockito.when(takCache.getRoutingInfo(anyString(),eq(RECEIVER_2) )).thenReturn(list);
        Mockito.when(takCache.getRoutingInfo(anyString(), AdditionalMatchers.not(eq(RECEIVER_2)) )).thenReturn(Collections.<RoutingInfo>emptyList());

        vagvalHandler = new VagvalHandler(hsaCache, takCache, defaultRoutingDelimiter );

        List<RoutingInfo> routingInfoList = vagvalHandler.getRoutingInfo(NAMNRYMD_1, RECEIVER_1_DEFAULT_RECEIVER_2);
        assertEquals(1, routingInfoList.size());
        assertEquals(ADDRESS_2, routingInfoList.get(0).getAddress());
        assertEquals(RIV21, routingInfoList.get(0).getRivProfile());

        routingInfoList = vagvalHandler.getRoutingInfo(NAMNRYMD_1, RECEIVER_2_DEFAULT_RECEIVER_3);
        assertEquals(1, routingInfoList.size());
        assertEquals(ADDRESS_2, routingInfoList.get(0).getAddress());
        assertEquals(RIV21, routingInfoList.get(0).getRivProfile());

        routingInfoList = vagvalHandler.getRoutingInfo(NAMNRYMD_1, RECEIVER_3_DEFAULT_RECEIVER_4);
        assertTrue(routingInfoList.isEmpty());
    }

 @Test
    public void testTraceLoggingWhenRoutingByOldStyleDefaultRouting() throws Exception {
        List<RoutingInfo> list = new ArrayList<>();
        list.add(new RoutingInfo(ADDRESS_2, RIV21));

        Mockito.when(takCache.getRoutingInfo(anyString(),eq(RECEIVER_2) )).thenReturn(list);
        Mockito.when(takCache.getRoutingInfo(anyString(), AdditionalMatchers.not(eq(RECEIVER_2)) )).thenReturn(Collections.<RoutingInfo>emptyList());

        vagvalHandler = new VagvalHandler(hsaCache, takCache, defaultRoutingDelimiter );

        List<RoutingInfo> routingInfoList = vagvalHandler.getRoutingInfo(NAMNRYMD_1, RECEIVER_1_DEFAULT_RECEIVER_2);
        assertEquals(1, routingInfoList.size());
        assertEquals("(leaf)receiver-2", ThreadContextLogTrace.get(ThreadContextLogTrace.ROUTER_RESOLVE_VAGVAL_TRACE));

        routingInfoList = vagvalHandler.getRoutingInfo(NAMNRYMD_1, RECEIVER_2_DEFAULT_RECEIVER_3);
        assertEquals(1, routingInfoList.size());
        assertEquals("(leaf)receiver-3,receiver-2", ThreadContextLogTrace.get(ThreadContextLogTrace.ROUTER_RESOLVE_VAGVAL_TRACE));


        routingInfoList = vagvalHandler.getRoutingInfo(NAMNRYMD_1, RECEIVER_3_DEFAULT_RECEIVER_4);
        assertTrue(routingInfoList.isEmpty());
        assertEquals("(leaf)receiver-4,receiver-3", ThreadContextLogTrace.get(ThreadContextLogTrace.ROUTER_RESOLVE_VAGVAL_TRACE));
 }

    @Test
    public void testIsNotAuthorizedByOldStyleDefaultRoutingWhenItsDisabled() throws Exception {
        List<RoutingInfo> list = new ArrayList<>();
        list.add(new RoutingInfo(ADDRESS_2, RIV21));

        Mockito.when(takCache.getRoutingInfo(anyString(),eq(RECEIVER_2) )).thenReturn(list);
        Mockito.when(takCache.getRoutingInfo(anyString(), AdditionalMatchers.not(eq(RECEIVER_2)) )).thenReturn(Collections.<RoutingInfo>emptyList());

        vagvalHandler = new VagvalHandler(hsaCache, takCache, "" );
        List<RoutingInfo> routingInfoList = vagvalHandler.getRoutingInfo(NAMNRYMD_1, RECEIVER_1_DEFAULT_RECEIVER_2);
        assertTrue(routingInfoList.isEmpty());
    }

    @Configuration
    @ComponentScan(basePackages = { "se.skl.tp.hsa.cache"})
    static class InnerConfiguration {

    }

}