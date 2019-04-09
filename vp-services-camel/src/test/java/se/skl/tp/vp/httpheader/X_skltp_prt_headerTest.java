package se.skl.tp.vp.httpheader;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import se.skl.tp.vp.TestBeanConfiguration;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.service.TakCacheService;
import se.skl.tp.vp.util.soaprequests.TestSoapRequests;
import se.skltp.takcache.RoutingInfo;
import se.skltp.takcache.TakCache;

import java.util.ArrayList;
import java.util.List;

import static se.skl.tp.vp.util.soaprequests.RoutingInfoUtil.createRoutingInfo;
import static se.skl.tp.vp.util.takcache.TakCacheMockUtil.createTakCacheLogOk;
import static se.skl.tp.vp.util.takcache.TestTakDataDefines.RIV20;

@RunWith( SpringRunner.class )
@SpringBootTest(classes = TestBeanConfiguration.class)
@TestPropertySource("classpath:application.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class X_skltp_prt_headerTest extends CamelTestSupport {

    @MockBean
    TakCache takCache;

    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint resultEndpoint;

    @Autowired
    CamelContext camelContext;

    @Autowired
    TakCacheService takCacheService;

    @Before
    public void setUp() throws Exception {
        createRoute(camelContext);
        camelContext.start();
        resultEndpoint.reset();
        Mockito.when(takCache.refresh()).thenReturn(createTakCacheLogOk());
        takCacheService.refresh();
    }

    @Test
    public void headerTest() {
        List<RoutingInfo> list = new ArrayList<>();
        list.add(createRoutingInfo("http://localhost:12123/vp",RIV20));
        Mockito.when(takCache.getRoutingInfo("urn:riv:insuranceprocess:healthreporting:GetCertificateResponder:1", "UnitTest")).thenReturn(list);
        Mockito.when(takCache.isAuthorized("UnitTest", "urn:riv:insuranceprocess:healthreporting:GetCertificateResponder:1", "UnitTest")).thenReturn(true);

        template.sendBody(TestSoapRequests.GET_CERTIFICATE_TO_UNIT_TEST_SOAP_REQUEST);

        String header = (String) resultEndpoint.getExchanges().get(0).getIn().getHeader(HttpHeaders.X_SKLTP_PRODUCER_RESPONSETIME);
        assertNotNull(header);
    }


    private void createRoute(CamelContext camelContext) throws Exception {
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                        .setHeader(HttpHeaders.X_VP_SENDER_ID, constant("UnitTest"))
                        .setHeader(HttpHeaders.X_VP_INSTANCE_ID, constant("dev_env"))
                        .setHeader("X-Forwarded-For", constant("1.2.3.4"))
                        .to("netty4-http:http://localhost:12312/vp")
                        .to("mock:result");

                from("netty4-http:http://localhost:12123/vp")
                        .process((Exchange exchange)-> {
                            Thread.sleep(0);
                        });
            }
        });
    }
}
