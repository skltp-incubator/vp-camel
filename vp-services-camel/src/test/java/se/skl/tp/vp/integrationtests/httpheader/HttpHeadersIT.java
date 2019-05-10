package se.skl.tp.vp.integrationtests.httpheader;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import se.skl.tp.vp.TestBeanConfiguration;
import se.skl.tp.vp.constants.HttpHeaders;
import se.skl.tp.vp.constants.PropertyConstants;
import se.skl.tp.vp.integrationtests.utils.TakMockWebService;
import se.skl.tp.vp.util.soaprequests.TestSoapRequests;
import static se.skl.tp.vp.integrationtests.httpheader.HeadersUtil.TEST_CONSUMER;
import static se.skl.tp.vp.integrationtests.httpheader.HeadersUtil.TEST_CORRELATION_ID;
import static se.skl.tp.vp.integrationtests.httpheader.HeadersUtil.TEST_SENDER;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = TestBeanConfiguration.class)
@TestPropertySource("classpath:application.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class HttpHeadersIT extends CamelTestSupport {

    public static TakMockWebService takMockWebService;

    @Value("${" + PropertyConstants.VP_HEADER_USER_AGENT + "}")
    private String vpHeaderUserAgent;

    @Value("${" + PropertyConstants.VP_HEADER_CONTENT_TYPE + "}")
    private String headerContentType;

    @Value("${" + PropertyConstants.VP_INSTANCE_ID + "}")
    private String vpInstanceId;

    @Value("${" + PropertyConstants.VP_HTTP_ROUTE_URL + "}")
    private String httpRoute;

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

    @AfterClass
    public static void afterClass() {
        takMockWebService.stop();
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
    public void checkSoapActionTest() {
        //This param is mandatory for the request to pass.
        template.sendBodyAndHeaders(TestSoapRequests.GET_NO_CERT_HTTP_SOAP_REQUEST, HeadersUtil.getHttpHeadersWithoutMembers());
        String s = (String) resultEndpoint.getExchanges().get(0).getIn().getHeaders().get(HttpHeaders.SOAP_ACTION);
        assertNotNull(s);
        assert(!s.isEmpty());
    }

    @Test
    public void setConfiguredHeadersTest() {
        //These params are partly set by configuration in HeaderConfigurationProcessorImpl.java
        template.sendBodyAndHeaders(TestSoapRequests.GET_NO_CERT_HTTP_SOAP_REQUEST, HeadersUtil.getHttpHeadersWithoutMembers());
        assert(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get(HttpHeaders.X_VP_INSTANCE_ID).equals(vpInstanceId));
        assert(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get(HttpHeaders.HEADER_CONTENT_TYPE).equals(headerContentType));
        assert(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get(HttpHeaders.HEADER_USER_AGENT).equals(vpHeaderUserAgent));
        assert(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get(HttpHeaders.X_VP_SENDER_ID).equals(TEST_SENDER));
    }

    @Test
    public void setCorrelationAndConsumerIdTest() {
        template.sendBodyAndHeaders(TestSoapRequests.GET_NO_CERT_HTTP_SOAP_REQUEST, HeadersUtil.getHttpHeadersWithMembers());
        assert(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get(HttpHeaders.X_SKLTP_CORRELATION_ID).equals(TEST_CORRELATION_ID));
        assert(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID).equals(TEST_CONSUMER));
    }

    @Test
    public void setCorrelationAndConsumerIdTestWithoutMembers() throws Exception {
        template.sendBodyAndHeaders(TestSoapRequests.GET_NO_CERT_HTTP_SOAP_REQUEST, HeadersUtil.getHttpHeadersWithoutMembers());
        String s = (String) resultEndpoint.getExchanges().get(0).getIn().getHeaders().get(HttpHeaders.X_SKLTP_CORRELATION_ID);
        assertNotNull(s);
        assert(!s.equals(TEST_CORRELATION_ID));
        assert(s.length() > 20);
        assert(resultEndpoint.getExchanges().get(0).getIn().getHeaders().get(HttpHeaders.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID).equals(TEST_SENDER));
    }

    private void addConsumerRoute(CamelContext camelContext) throws Exception {
        camelContext.addRoutes(
                new RouteBuilder() {
                    @Override
                    public void configure() {
                        from("direct:start").routeId("start").routeDescription("consumer")
                                .to("netty4-http:" + httpRoute);

                        from("netty4-http:http://localhost:19000/vardgivare-b/tjanst2").routeDescription("producer")
                                .to("mock:result");
                    }
                });
    }
}
