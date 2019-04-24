package se.skl.tp.vp.integrationtests.httpheader;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import se.skl.tp.vp.TestBeanConfiguration;
import se.skl.tp.vp.integrationtests.utils.TakMockWebService;
import se.skl.tp.vp.util.soaprequests.TestSoapRequests;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = TestBeanConfiguration.class)
@TestPropertySource("classpath:application.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class HttpHeadersIT extends CamelTestSupport {

    public static TakMockWebService takMockWebService;


    @EndpointInject(uri = "mock:result")
    protected MockEndpoint resultEndpoint;

    @Produce(uri = "direct:start")
    protected ProducerTemplate template;

    @BeforeClass
    public static void beforeClass() {
        //TODO Use dynamic ports and also set TAK address used by takcache (Override "takcache.endpoint.address" property)
        takMockWebService = new TakMockWebService("http://localhost:8086/tak-services/SokVagvalsInfo/v2");
        takMockWebService.start();
    }

    @Autowired
    private CamelContext camelContext;

    private static boolean isContextStarted = false;

    @Before
    public void setUp() throws Exception {
        if(!isContextStarted){
            addConsumerRoute(camelContext);
            camelContext.start();
            isContextStarted=true;
        }
        resultEndpoint.reset();
    }

    @Test
    public void setCorrelationAndConsumerIdTest() throws Exception {
        template.sendBodyAndHeaders(TestSoapRequests.GET_NO_CERT_HTTP_SOAP_REQUEST, HeadersUtil.getHttpHeadersWithMembers());
        assert(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get("x-skltp-correlation-id").equals("aTestCorrelationId"));
        assert(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get("x-rivta-original-serviceconsumer-hsaid").equals("aTestConsumer"));
        assert(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get("x-vp-instance-id").equals("dev_env"));
        assert(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get("x-vp-sender-id").equals("tp"));
        assert(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get("X-Forwarded-For").equals("1.2.3.4"));
        takMockWebService.stop();
    }

    @Test
    public void setCorrelationAndConsumerIdTestWithoutMembers() throws Exception {
        template.sendBodyAndHeaders(TestSoapRequests.GET_NO_CERT_HTTP_SOAP_REQUEST, HeadersUtil.getHttpHeadersWithoutMembers());
        String s = (String) resultEndpoint.getExchanges().get(0).getIn().getHeaders().get("x-skltp-correlation-id");
        assertNotNull(s);
        assert(!s.equals("aTestCorrelationId"));
        assert(s.length() > 20);
        assert(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get("x-rivta-original-serviceconsumer-hsaid").equals("tp"));
        assert(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get("x-vp-instance-id").equals("dev_env"));
        assert(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get("x-vp-sender-id").equals("tp"));
        assert(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get("X-Forwarded-For").equals("1.2.3.4"));
        takMockWebService.stop();
    }

    private void addConsumerRoute(CamelContext camelContext) throws Exception {
        camelContext.addRoutes(
                new RouteBuilder() {
                    @Override
                    public void configure() {
                        from("direct:start").routeId("start")
                                .to("netty4-http:http://localhost:12312/vp");
                        from("netty4-http:http://localhost:19000/vardgivare-b/tjanst2")
                                .to("mock:result");
                    }
                });
    }
}
