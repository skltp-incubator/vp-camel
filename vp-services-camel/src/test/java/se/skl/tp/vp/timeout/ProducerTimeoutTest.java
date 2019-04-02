package se.skl.tp.vp.timeout;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
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
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.httpheader.SenderIpExtractor;
import se.skl.tp.vp.TestBeanConfiguration;
import se.skl.tp.vp.util.soaprequests.TestSoapRequests;
import se.skltp.takcache.RoutingInfo;
import se.skltp.takcache.TakCache;

import java.util.ArrayList;
import java.util.List;

import static se.skl.tp.vp.util.soaprequests.RoutingInfoUtil.createRoutingInfo;
import static se.skl.tp.vp.util.takcache.TestTakDataDefines.RIV20;

@RunWith( SpringRunner.class )
@SpringBootTest(classes = TestBeanConfiguration.class)
@TestPropertySource("classpath:application.properties")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class ProducerTimeoutTest extends CamelTestSupport {

    @EndpointInject(uri = "mock:result")
    protected MockEndpoint resultEndpoint;

    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

    @Autowired
    SenderIpExtractor senderIpExtractor;

    @Autowired
    CamelContext camelContext;

    @MockBean
    TakCache takCache;

    @MockBean
    TimeoutConfiguration timeoutConfiguration;

    @Before
    public void setUp() throws Exception {
        createRoute(camelContext);
        camelContext.start();
        resultEndpoint.reset();
    }

    @Test
    public void timeoutInResponseTest() throws Exception {
        List<RoutingInfo> list = new ArrayList<>();
        list.add(createRoutingInfo("http://localhost:12123/vp",RIV20));
        Mockito.when(takCache.getRoutingInfo("urn:riv:insuranceprocess:healthreporting:GetCertificateResponder:1", "UnitTest")).thenReturn(list);
        Mockito.when(takCache.isAuthorized("UnitTest", "urn:riv:insuranceprocess:healthreporting:GetCertificateResponder:1", "UnitTest")).thenReturn(true);
        TimeoutConfig timeoutConfig = new TimeoutConfig();
        timeoutConfig.setProducertimeout(500);
        Mockito.when(timeoutConfiguration.getOnTjanstekontrakt("urn:riv:insuranceprocess:healthreporting:GetCertificateResponder:1")).thenReturn(timeoutConfig);

        template.sendBody(TestSoapRequests.GET_CERTIFICATE_TO_UNIT_TEST_SOAP_REQUEST);

        resultEndpoint.expectedHeaderReceived("http.status", 500);
        String resultBody = resultEndpoint.getExchanges().get(0).getIn().getBody(String.class);
        assertStringContains(resultBody , "Timeout");
        resultEndpoint.assertIsSatisfied();
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
                    .to("mock:result");;

                from("netty4-http:http://localhost:12123/vp")
                    .process((Exchange exchange)-> {
                        Thread.sleep(1000);
                    });
            }
        });
    }


}
